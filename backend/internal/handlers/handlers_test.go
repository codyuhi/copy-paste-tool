package handlers_test

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"testing"

	"github.com/codyuhi/copy-paste-tool/backend/internal/database"
	"github.com/codyuhi/copy-paste-tool/backend/internal/handlers"
	"github.com/codyuhi/copy-paste-tool/backend/internal/models"
	"github.com/go-chi/chi/v5"
)

func setupTestApp(t *testing.T) (*chi.Mux, func()) {
	tmpDir, err := os.MkdirTemp("", "cpt-test-*")
	if err != nil {
		t.Fatalf("failed to create temp dir: %v", err)
	}

	dbPath := filepath.Join(tmpDir, "test.db")
	db, err := database.InitDB(dbPath)
	if err != nil {
		t.Fatalf("failed to init test db: %v", err)
	}

	h := handlers.NewHandler(db)
	r := chi.NewRouter()
	r.Get("/healthz", h.HealthCheck)
	r.Mount("/api", h.Routes())

	cleanup := func() {
		_ = db.Close()
		_ = os.RemoveAll(tmpDir)
	}

	return r, cleanup
}

func TestHealthCheck(t *testing.T) {
	router, cleanup := setupTestApp(t)
	defer cleanup()

	req := httptest.NewRequest("GET", "/healthz", nil)
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("expected status 200, got %d", rec.Code)
	}

	var res map[string]interface{}
	if err := json.Unmarshal(rec.Body.Bytes(), &res); err != nil {
		t.Fatalf("failed to parse response: %v", err)
	}
	if res["status"] != "ok" {
		t.Fatalf("expected status ok, got %v", res["status"])
	}
}

func TestRegisterAndLogin(t *testing.T) {
	router, cleanup := setupTestApp(t)
	defer cleanup()

	// 1. Register new user
	regPayload := models.RegisterRequest{
		Username: "cody",
		Password: "password123",
		InitialSections: []models.CopySection{
			{
				SectionName: "General",
				SectionButtons: []models.CopyButton{
					{ButtonName: "Hello", PasteValue: "World"},
				},
			},
		},
		InitialFavorites: []models.FavoriteButton{
			{
				ButtonName: "Hello",
				PasteValue: "World",
				SectionID:  0,
				ButtonID:   0,
			},
		},
	}
	body, _ := json.Marshal(regPayload)
	req := httptest.NewRequest("POST", "/api/auth/register", bytes.NewReader(body))
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected 201 Created, got %d: %s", rec.Code, rec.Body.String())
	}

	var authRes models.AuthResponse
	if err := json.Unmarshal(rec.Body.Bytes(), &authRes); err != nil {
		t.Fatalf("failed to parse auth response: %v", err)
	}
	if authRes.Token == "" {
		t.Fatal("expected non-empty token")
	}
	if authRes.User.Username != "cody" {
		t.Fatalf("expected username cody, got %s", authRes.User.Username)
	}

	// 2. Register duplicate user should fail with 409 Conflict
	dupReq := httptest.NewRequest("POST", "/api/auth/register", bytes.NewReader(body))
	dupReq.Header.Set("Content-Type", "application/json")
	dupRec := httptest.NewRecorder()
	router.ServeHTTP(dupRec, dupReq)

	if dupRec.Code != http.StatusConflict {
		t.Fatalf("expected 409 Conflict for duplicate user, got %d", dupRec.Code)
	}

	// 3. Login with correct credentials
	loginPayload := models.LoginRequest{
		Username: "cody",
		Password: "password123",
	}
	loginBody, _ := json.Marshal(loginPayload)
	loginReq := httptest.NewRequest("POST", "/api/auth/login", bytes.NewReader(loginBody))
	loginReq.Header.Set("Content-Type", "application/json")
	loginRec := httptest.NewRecorder()
	router.ServeHTTP(loginRec, loginReq)

	if loginRec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK for login, got %d", loginRec.Code)
	}

	var loginRes models.AuthResponse
	_ = json.Unmarshal(loginRec.Body.Bytes(), &loginRes)
	if loginRes.Token == "" {
		t.Fatal("expected token on login")
	}

	// 4. Login with bad password
	badLoginPayload := models.LoginRequest{
		Username: "cody",
		Password: "wrongpassword",
	}
	badBody, _ := json.Marshal(badLoginPayload)
	badReq := httptest.NewRequest("POST", "/api/auth/login", bytes.NewReader(badBody))
	badReq.Header.Set("Content-Type", "application/json")
	badRec := httptest.NewRecorder()
	router.ServeHTTP(badRec, badReq)

	if badRec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 Unauthorized for bad password, got %d", badRec.Code)
	}
}

func TestDataPersistenceAndCrossDeviceSync(t *testing.T) {
	router, cleanup := setupTestApp(t)
	defer cleanup()

	// Register user
	regPayload := models.RegisterRequest{
		Username: "device_user",
		Password: "password123",
	}
	body, _ := json.Marshal(regPayload)
	req := httptest.NewRequest("POST", "/api/auth/register", bytes.NewReader(body))
	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	var authRes models.AuthResponse
	_ = json.Unmarshal(rec.Body.Bytes(), &authRes)
	token := authRes.Token

	// Device 1: PUT updated data
	updatePayload := models.UserDataPayload{
		Sections: []models.CopySection{
			{
				SectionName: "Links",
				SectionButtons: []models.CopyButton{
					{ButtonName: "Cluster", PasteValue: "https://home.minipc.local"},
					{ButtonName: "Tasks", PasteValue: "https://tasks.lan.codyuhi.online"},
				},
			},
		},
		Favorites: []models.FavoriteButton{
			{
				ButtonName: "Tasks",
				PasteValue: "https://tasks.lan.codyuhi.online",
				SectionID:  0,
				ButtonID:   1,
			},
		},
	}
	putBody, _ := json.Marshal(updatePayload)
	putReq := httptest.NewRequest("PUT", "/api/data", bytes.NewReader(putBody))
	putReq.Header.Set("Content-Type", "application/json")
	putReq.Header.Set("Authorization", fmt.Sprintf("Bearer %s", token))
	putRec := httptest.NewRecorder()
	router.ServeHTTP(putRec, putReq)

	if putRec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK from PUT /api/data, got %d: %s", putRec.Code, putRec.Body.String())
	}

	// Device 2: GET data using same token
	getReq := httptest.NewRequest("GET", "/api/data", nil)
	getReq.Header.Set("Authorization", fmt.Sprintf("Bearer %s", token))
	getRec := httptest.NewRecorder()
	router.ServeHTTP(getRec, getReq)

	if getRec.Code != http.StatusOK {
		t.Fatalf("expected 200 OK from GET /api/data, got %d: %s", getRec.Code, getRec.Body.String())
	}

	var getData models.UserDataPayload
	if err := json.Unmarshal(getRec.Body.Bytes(), &getData); err != nil {
		t.Fatalf("failed to decode get response: %v", err)
	}

	if len(getData.Sections) != 1 || getData.Sections[0].SectionName != "Links" {
		t.Fatalf("unexpected sections data: %+v", getData.Sections)
	}
	if len(getData.Sections[0].SectionButtons) != 2 {
		t.Fatalf("expected 2 buttons, got %d", len(getData.Sections[0].SectionButtons))
	}
	if len(getData.Favorites) != 1 || getData.Favorites[0].ButtonName != "Tasks" {
		t.Fatalf("unexpected favorites data: %+v", getData.Favorites)
	}

	// Unauthenticated request should fail with 401
	unauthReq := httptest.NewRequest("GET", "/api/data", nil)
	unauthRec := httptest.NewRecorder()
	router.ServeHTTP(unauthRec, unauthReq)
	if unauthRec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401 Unauthorized, got %d", unauthRec.Code)
	}
}
