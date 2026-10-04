package handlers

import (
	"database/sql"
	"encoding/json"
	"errors"
	"net/http"
	"regexp"
	"strings"
	"time"

	"github.com/codyuhi/copy-paste-tool/backend/internal/auth"
	"github.com/codyuhi/copy-paste-tool/backend/internal/models"
	"github.com/go-chi/chi/v5"
	"github.com/google/uuid"
)

var usernameRegex = regexp.MustCompile(`^[a-zA-Z0-9_\-\.]{3,32}$`)

type Handler struct {
	db *sql.DB
}

func NewHandler(db *sql.DB) *Handler {
	return &Handler{db: db}
}

func (h *Handler) Routes() chi.Router {
	r := chi.NewRouter()

	// Public routes
	r.Post("/auth/register", h.Register)
	r.Post("/auth/login", h.Login)
	r.Post("/auth/logout", h.Logout)

	// Authenticated routes
	r.Group(func(protected chi.Router) {
		protected.Use(auth.RequireAuth)
		protected.Get("/auth/me", h.Me)
		protected.Get("/data", h.GetData)
		protected.Put("/data", h.PutData)
	})

	return r
}

func (h *Handler) HealthCheck(w http.ResponseWriter, r *http.Request) {
	respondJSON(w, http.StatusOK, map[string]interface{}{
		"status":    "ok",
		"service":   "copy-paste-tool",
		"timestamp": time.Now().UTC().Format(time.RFC3339),
	})
}

// Register creates a new user and returns a token
func (h *Handler) Register(w http.ResponseWriter, r *http.Request) {
	var req models.RegisterRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	req.Username = strings.TrimSpace(req.Username)
	if !usernameRegex.MatchString(req.Username) {
		respondError(w, http.StatusBadRequest, "Username must be 3-32 characters and contain only letters, numbers, dots, dashes, or underscores")
		return
	}

	if len(req.Password) < 6 {
		respondError(w, http.StatusBadRequest, "Password must be at least 6 characters")
		return
	}

	// Check if username already exists
	var existingID string
	err := h.db.QueryRow("SELECT id FROM users WHERE username = ? COLLATE NOCASE", req.Username).Scan(&existingID)
	if err == nil {
		respondError(w, http.StatusConflict, "Username is already taken")
		return
	} else if !errors.Is(err, sql.ErrNoRows) {
		respondError(w, http.StatusInternalServerError, "Database check failed")
		return
	}

	hashedPassword, err := auth.HashPassword(req.Password)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to hash password")
		return
	}

	userID := uuid.New().String()
	now := time.Now().UTC()

	initialSections := req.InitialSections
	if initialSections == nil {
		initialSections = []models.CopySection{}
	}
	initialFavorites := req.InitialFavorites
	if initialFavorites == nil {
		initialFavorites = []models.FavoriteButton{}
	}

	sectionsJSON, err := json.Marshal(initialSections)
	if err != nil {
		sectionsJSON = []byte("[]")
	}
	favoritesJSON, err := json.Marshal(initialFavorites)
	if err != nil {
		favoritesJSON = []byte("[]")
	}

	tx, err := h.db.Begin()
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Transaction begin failed")
		return
	}
	defer tx.Rollback()

	_, err = tx.Exec(
		"INSERT INTO users (id, username, password_hash, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
		userID, req.Username, hashedPassword, now, now,
	)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to create user record")
		return
	}

	_, err = tx.Exec(
		"INSERT INTO user_data (user_id, sections_json, favorites_json, updated_at) VALUES (?, ?, ?, ?)",
		userID, string(sectionsJSON), string(favoritesJSON), now,
	)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to initialize user data")
		return
	}

	if err := tx.Commit(); err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to commit user creation")
		return
	}

	user := &models.User{
		ID:        userID,
		Username:  req.Username,
		CreatedAt: now,
		UpdatedAt: now,
	}

	token, err := auth.GenerateToken(user)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to generate authorization token")
		return
	}

	respondJSON(w, http.StatusCreated, models.AuthResponse{
		Token: token,
		User:  *user,
	})
}

// Login authenticates credentials and returns a token
func (h *Handler) Login(w http.ResponseWriter, r *http.Request) {
	var req models.LoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	req.Username = strings.TrimSpace(req.Username)
	if req.Username == "" || req.Password == "" {
		respondError(w, http.StatusBadRequest, "Username and password are required")
		return
	}

	var user models.User
	var passwordHash string
	err := h.db.QueryRow(
		"SELECT id, username, password_hash, created_at, updated_at FROM users WHERE username = ? COLLATE NOCASE",
		req.Username,
	).Scan(&user.ID, &user.Username, &passwordHash, &user.CreatedAt, &user.UpdatedAt)

	if errors.Is(err, sql.ErrNoRows) || !auth.CheckPasswordHash(req.Password, passwordHash) {
		respondError(w, http.StatusUnauthorized, "Invalid username or password")
		return
	} else if err != nil {
		respondError(w, http.StatusInternalServerError, "Database query failed")
		return
	}

	token, err := auth.GenerateToken(&user)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to generate authorization token")
		return
	}

	respondJSON(w, http.StatusOK, models.AuthResponse{
		Token: token,
		User:  user,
	})
}

// Logout responds successfully
func (h *Handler) Logout(w http.ResponseWriter, r *http.Request) {
	respondJSON(w, http.StatusOK, map[string]interface{}{
		"success": true,
		"message": "Logged out successfully",
	})
}

// Me returns the current authenticated user
func (h *Handler) Me(w http.ResponseWriter, r *http.Request) {
	user, err := auth.GetUserFromContext(r.Context())
	if err != nil {
		respondError(w, http.StatusUnauthorized, "Unauthorized")
		return
	}

	var fullUser models.User
	err = h.db.QueryRow(
		"SELECT id, username, created_at, updated_at FROM users WHERE id = ?",
		user.ID,
	).Scan(&fullUser.ID, &fullUser.Username, &fullUser.CreatedAt, &fullUser.UpdatedAt)
	if err != nil {
		respondError(w, http.StatusNotFound, "User not found")
		return
	}

	respondJSON(w, http.StatusOK, map[string]interface{}{
		"user": fullUser,
	})
}

// GetData fetches copy-paste data for the authenticated user
func (h *Handler) GetData(w http.ResponseWriter, r *http.Request) {
	user, err := auth.GetUserFromContext(r.Context())
	if err != nil {
		respondError(w, http.StatusUnauthorized, "Unauthorized")
		return
	}

	var sectionsRaw, favoritesRaw string
	var updatedAt time.Time
	err = h.db.QueryRow(
		"SELECT sections_json, favorites_json, updated_at FROM user_data WHERE user_id = ?",
		user.ID,
	).Scan(&sectionsRaw, &favoritesRaw, &updatedAt)

	if errors.Is(err, sql.ErrNoRows) {
		respondJSON(w, http.StatusOK, models.UserDataPayload{
			Sections:  []models.CopySection{},
			Favorites: []models.FavoriteButton{},
			UpdatedAt: time.Now().UTC(),
		})
		return
	} else if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to retrieve user data")
		return
	}

	var sections []models.CopySection
	if err := json.Unmarshal([]byte(sectionsRaw), &sections); err != nil {
		sections = []models.CopySection{}
	}

	var favorites []models.FavoriteButton
	if err := json.Unmarshal([]byte(favoritesRaw), &favorites); err != nil {
		favorites = []models.FavoriteButton{}
	}

	respondJSON(w, http.StatusOK, models.UserDataPayload{
		Sections:  sections,
		Favorites: favorites,
		UpdatedAt: updatedAt,
	})
}

// PutData saves copy-paste data for the authenticated user
func (h *Handler) PutData(w http.ResponseWriter, r *http.Request) {
	user, err := auth.GetUserFromContext(r.Context())
	if err != nil {
		respondError(w, http.StatusUnauthorized, "Unauthorized")
		return
	}

	var req models.UserDataPayload
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid request payload")
		return
	}

	if req.Sections == nil {
		req.Sections = []models.CopySection{}
	}
	if req.Favorites == nil {
		req.Favorites = []models.FavoriteButton{}
	}

	sectionsJSON, err := json.Marshal(req.Sections)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to encode sections")
		return
	}

	favoritesJSON, err := json.Marshal(req.Favorites)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to encode favorites")
		return
	}

	now := time.Now().UTC()
	query := `
	INSERT INTO user_data (user_id, sections_json, favorites_json, updated_at)
	VALUES (?, ?, ?, ?)
	ON CONFLICT(user_id) DO UPDATE SET
		sections_json = excluded.sections_json,
		favorites_json = excluded.favorites_json,
		updated_at = excluded.updated_at;
	`

	_, err = h.db.Exec(query, user.ID, string(sectionsJSON), string(favoritesJSON), now)
	if err != nil {
		respondError(w, http.StatusInternalServerError, "Failed to save user data")
		return
	}

	respondJSON(w, http.StatusOK, map[string]interface{}{
		"success":   true,
		"updatedAt": now.Format(time.RFC3339),
	})
}

func respondJSON(w http.ResponseWriter, status int, data interface{}) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(data)
}

func respondError(w http.ResponseWriter, status int, message string) {
	respondJSON(w, status, map[string]string{"error": message})
}
