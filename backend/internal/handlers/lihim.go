package handlers

import (
	"database/sql"
	"encoding/json"
	"errors"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/codyuhi/copy-paste-tool/backend/internal/auth"
	"github.com/codyuhi/copy-paste-tool/backend/internal/models"
	"github.com/go-chi/chi/v5"
)

// maxLihimPayload bounds a single lihim save.
const maxLihimPayload = 5 << 20

// sealedPayload is the plaintext that is encrypted into lihim_data.sealed.
type sealedPayload struct {
	Sections  []models.CopySection    `json:"sections"`
	Favorites []models.FavoriteButton `json:"favorites"`
}

// isAdmin reports whether username is on the configured admin allow-list.
func (h *Handler) isAdmin(username string) bool {
	_, ok := h.admins[strings.ToLower(strings.TrimSpace(username))]
	return ok
}

func (h *Handler) lihimRoutes(r chi.Router) {
	// Every failure (feature disabled, no session, not an admin) is a plain 404,
	// so the endpoint does not reveal that it exists.
	r.Use(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
			if h.keyring == nil {
				http.NotFound(w, req)
				return
			}
			parts := strings.SplitN(req.Header.Get("Authorization"), " ", 2)
			if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
				http.NotFound(w, req)
				return
			}
			claims, err := auth.ValidateToken(strings.TrimSpace(parts[1]))
			if err != nil {
				http.NotFound(w, req)
				return
			}
			// Re-read the username so a deleted account's token cannot get in.
			var username string
			if err := h.db.QueryRow("SELECT username FROM users WHERE id = ?", claims.UserID).Scan(&username); err != nil || !h.isAdmin(username) {
				http.NotFound(w, req)
				return
			}
			w.Header().Set("Cache-Control", "no-store")
			ctx := auth.WithUser(req.Context(), &models.User{ID: claims.UserID, Username: username})
			next.ServeHTTP(w, req.WithContext(ctx))
		})
	})
	r.Get("/data", h.GetLihimData)
	r.Put("/data", h.PutLihimData)
}

// GetLihimData decrypts and returns the admin's lihim sections and favorites.
func (h *Handler) GetLihimData(w http.ResponseWriter, r *http.Request) {
	user, _ := auth.GetUserFromContext(r.Context())

	var sealed []byte
	var updatedAt time.Time
	err := h.db.QueryRow("SELECT sealed, updated_at FROM lihim_data WHERE user_id = ?", user.ID).Scan(&sealed, &updatedAt)
	if errors.Is(err, sql.ErrNoRows) {
		respondJSON(w, http.StatusOK, models.UserDataPayload{
			Sections:  []models.CopySection{},
			Favorites: []models.FavoriteButton{},
			UpdatedAt: time.Now().UTC(),
		})
		return
	} else if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to retrieve data")
		return
	}

	plain, err := h.keyring.Open(user.ID, sealed)
	if err != nil {
		log.Printf("lihim: failed to decrypt data for user %s: %v", user.ID, err)
		respondError(w, http.StatusInternalServerError, "Failed to decrypt data")
		return
	}
	var p sealedPayload
	if err := json.Unmarshal(plain, &p); err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to decode data")
		return
	}
	if p.Sections == nil {
		p.Sections = []models.CopySection{}
	}
	if p.Favorites == nil {
		p.Favorites = []models.FavoriteButton{}
	}
	respondJSON(w, http.StatusOK, models.UserDataPayload{
		Sections:  p.Sections,
		Favorites: p.Favorites,
		UpdatedAt: updatedAt,
	})
}

// PutLihimData encrypts and stores the admin's lihim sections and favorites.
func (h *Handler) PutLihimData(w http.ResponseWriter, r *http.Request) {
	user, _ := auth.GetUserFromContext(r.Context())

	var req models.UserDataPayload
	if err := json.NewDecoder(http.MaxBytesReader(w, r.Body, maxLihimPayload)).Decode(&req); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}
	p := sealedPayload{Sections: req.Sections, Favorites: req.Favorites}
	if p.Sections == nil {
		p.Sections = []models.CopySection{}
	}
	if p.Favorites == nil {
		p.Favorites = []models.FavoriteButton{}
	}

	plain, err := json.Marshal(p)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to encode data")
		return
	}
	sealed, err := h.keyring.Seal(user.ID, plain)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to encrypt data")
		return
	}

	now := time.Now().UTC()
	_, err = h.db.Exec(`
	INSERT INTO lihim_data (user_id, sealed, updated_at) VALUES (?, ?, ?)
	ON CONFLICT(user_id) DO UPDATE SET sealed = excluded.sealed, updated_at = excluded.updated_at;
	`, user.ID, sealed, now)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to save data")
		return
	}

	respondJSON(w, http.StatusOK, map[string]interface{}{
		"success":   true,
		"updatedAt": now.Format(time.RFC3339),
	})
}
