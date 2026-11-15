package handlers_test

import (
	"bytes"
	"database/sql"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"path/filepath"
	"testing"

	"github.com/codyuhi/copy-paste-tool/backend/internal/database"
	"github.com/codyuhi/copy-paste-tool/backend/internal/handlers"
	"github.com/codyuhi/copy-paste-tool/backend/internal/lihim"
	"github.com/codyuhi/copy-paste-tool/backend/internal/models"
	"github.com/go-chi/chi/v5"
)

func setupLihimApp(t *testing.T, withKey bool) (*chi.Mux, *sql.DB) {
	t.Helper()
	db, err := database.InitDB(filepath.Join(t.TempDir(), "test.db"))
	if err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() { _ = db.Close() })

	opts := []handlers.Option{handlers.WithAdmins("Boss", " other ")}
	if withKey {
		k, err := lihim.NewKeyring(bytes.Repeat([]byte{3}, 32))
		if err != nil {
			t.Fatal(err)
		}
		opts = append(opts, handlers.WithLihim(k))
	}
	r := chi.NewRouter()
	r.Mount("/api", handlers.NewHandler(db, opts...).Routes())
	return r, db
}

func doJSON(t *testing.T, r http.Handler, method, path, token string, body interface{}) *httptest.ResponseRecorder {
	t.Helper()
	var buf bytes.Buffer
	if body != nil {
		_ = json.NewEncoder(&buf).Encode(body)
	}
	req := httptest.NewRequest(method, path, &buf)
	req.Header.Set("Content-Type", "application/json")
	if token != "" {
		req.Header.Set("Authorization", "Bearer "+token)
	}
	rec := httptest.NewRecorder()
	r.ServeHTTP(rec, req)
	return rec
}

func register(t *testing.T, r http.Handler, username string) models.AuthResponse {
	t.Helper()
	rec := doJSON(t, r, "POST", "/api/auth/register", "", map[string]string{"username": username, "password": "secret123"})
	if rec.Code != http.StatusCreated {
		t.Fatalf("register %s: %d %s", username, rec.Code, rec.Body.String())
	}
	var res models.AuthResponse
	_ = json.Unmarshal(rec.Body.Bytes(), &res)
	return res
}

func TestLihimAdminRoundTripEncryptedAtRest(t *testing.T) {
	r, db := setupLihimApp(t, true)
	admin := register(t, r, "boss")
	if !admin.User.IsAdmin {
		t.Fatal("expected admin flag on register response")
	}

	payload := models.UserDataPayload{
		Sections:  []models.CopySection{{SectionName: "Hidden", SectionButtons: []models.CopyButton{{ButtonName: "pin", PasteValue: "hunter2-unique-value"}}}},
		Favorites: []models.FavoriteButton{{ButtonName: "pin", PasteValue: "hunter2-unique-value"}},
	}
	if rec := doJSON(t, r, "PUT", "/api/lihim/data", admin.Token, payload); rec.Code != http.StatusOK {
		t.Fatalf("put: %d %s", rec.Code, rec.Body.String())
	}

	rec := doJSON(t, r, "GET", "/api/lihim/data", admin.Token, nil)
	if rec.Code != http.StatusOK {
		t.Fatalf("get: %d", rec.Code)
	}
	var got models.UserDataPayload
	_ = json.Unmarshal(rec.Body.Bytes(), &got)
	if len(got.Sections) != 1 || got.Sections[0].SectionButtons[0].PasteValue != "hunter2-unique-value" || len(got.Favorites) != 1 {
		t.Fatalf("unexpected lihim data: %+v", got)
	}

	// The stored row must not contain plaintext.
	var sealed []byte
	if err := db.QueryRow("SELECT sealed FROM lihim_data WHERE user_id = ?", admin.User.ID).Scan(&sealed); err != nil {
		t.Fatal(err)
	}
	if bytes.Contains(sealed, []byte("hunter2")) || bytes.Contains(sealed, []byte("Hidden")) {
		t.Fatal("lihim data stored in plaintext")
	}

	// Lihim content never leaks into the regular data endpoint.
	rec = doJSON(t, r, "GET", "/api/data", admin.Token, nil)
	if bytes.Contains(rec.Body.Bytes(), []byte("hunter2")) {
		t.Fatal("lihim content leaked into /api/data")
	}
}

func TestLihimHiddenFromNonAdmins(t *testing.T) {
	r, _ := setupLihimApp(t, true)
	user := register(t, r, "regular")
	if user.User.IsAdmin {
		t.Fatal("regular user flagged as admin")
	}
	for _, tok := range []string{"", "garbage", user.Token} {
		for _, m := range []string{"GET", "PUT"} {
			if rec := doJSON(t, r, m, "/api/lihim/data", tok, models.UserDataPayload{}); rec.Code != http.StatusNotFound {
				t.Fatalf("%s with token %q: expected 404, got %d", m, tok, rec.Code)
			}
		}
	}
}

func TestLihimDisabledWithoutKey(t *testing.T) {
	r, _ := setupLihimApp(t, false)
	admin := register(t, r, "BOSS")
	if !admin.User.IsAdmin {
		t.Fatal("admin match should be case-insensitive")
	}
	if rec := doJSON(t, r, "GET", "/api/lihim/data", admin.Token, nil); rec.Code != http.StatusNotFound {
		t.Fatalf("expected 404 without key, got %d", rec.Code)
	}
}

func TestMeReportsAdmin(t *testing.T) {
	r, _ := setupLihimApp(t, true)
	admin := register(t, r, "other")
	rec := doJSON(t, r, "GET", "/api/auth/me", admin.Token, nil)
	var res struct{ User models.User }
	_ = json.Unmarshal(rec.Body.Bytes(), &res)
	if !res.User.IsAdmin {
		t.Fatalf("expected isAdmin in /me: %s", rec.Body.String())
	}
}
