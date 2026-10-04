package main

import (
	"context"
	"errors"
	"io/fs"
	"log"
	"net/http"
	"os"
	"os/signal"
	"path/filepath"
	"strings"
	"syscall"
	"time"

	"github.com/codyuhi/copy-paste-tool/backend/internal/database"
	"github.com/codyuhi/copy-paste-tool/backend/internal/handlers"
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"github.com/go-chi/cors"
)

func main() {
	port := os.Getenv("PORT")
	if port == "" {
		port = "80"
	}

	dbPath := os.Getenv("DATABASE_URL")
	if dbPath == "" {
		dbPath = os.Getenv("DB_PATH")
	}
	if dbPath == "" {
		dbPath = "./data/copypaste.db"
	}
	dbPath = strings.TrimPrefix(dbPath, "file:")

	log.Printf("Initializing database at: %s", dbPath)
	db, err := database.InitDB(dbPath)
	if err != nil {
		log.Fatalf("Fatal: failed to initialize database: %v", err)
	}
	defer db.Close()

	r := chi.NewRouter()

	// Standard middleware
	r.Use(middleware.RequestID)
	r.Use(middleware.RealIP)
	r.Use(middleware.Logger)
	r.Use(middleware.Recoverer)
	r.Use(middleware.Timeout(60 * time.Second))

	// CORS configuration for local development & LAN access
	r.Use(cors.Handler(cors.Options{
		AllowedOrigins:   []string{"*"},
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"Accept", "Authorization", "Content-Type", "X-CSRF-Token"},
		ExposedHeaders:   []string{"Link"},
		AllowCredentials: false,
		MaxAge:           300,
	}))

	h := handlers.NewHandler(db)

	// Liveness and readiness probe endpoint
	r.Get("/healthz", h.HealthCheck)

	// API routes
	r.Mount("/api", h.Routes())

	// Static SPA Files
	staticDir := os.Getenv("STATIC_DIR")
	if staticDir == "" {
		candidates := []string{"./dist", "../app/dist", "./app/dist", "/app/dist"}
		for _, c := range candidates {
			if info, err := os.Stat(c); err == nil && info.IsDir() {
				staticDir = c
				break
			}
		}
	}

	if staticDir != "" {
		log.Printf("Serving static frontend files from: %s", staticDir)
		serveSPA(r, staticDir)
	} else {
		log.Println("No static files directory found, serving API only")
		r.Get("/", func(w http.ResponseWriter, r *http.Request) {
			w.Header().Set("Content-Type", "application/json")
			_, _ = w.Write([]byte(`{"app":"copy-paste-tool","status":"running","api":"/api"}`))
		})
	}

	srv := &http.Server{
		Addr:         ":" + port,
		Handler:      r,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	go func() {
		log.Printf("Copy-Paste-Tool server listening on http://0.0.0.0:%s", port)
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			log.Fatalf("Server listen error: %v", err)
		}
	}()

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)
	<-stop

	log.Println("Shutting down server gracefully...")
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		log.Printf("Server forced shutdown error: %v", err)
	}
	log.Println("Server stopped cleanly")
}

// serveSPA sets up static file serving with fallback to index.html for Single Page Applications
func serveSPA(r chi.Router, staticDir string) {
	fsys := os.DirFS(staticDir)
	fileServer := http.FileServer(http.FS(fsys))

	r.Get("/*", func(w http.ResponseWriter, req *http.Request) {
		path := strings.TrimPrefix(req.URL.Path, "/")
		if path == "" {
			path = "index.html"
		}

		// If file exists on disk, serve it
		f, err := fsys.Open(path)
		if err == nil {
			_ = f.Close()
			fileServer.ServeHTTP(w, req)
			return
		}

		// Check if it's a missing file or directory
		if errors.Is(err, fs.ErrNotExist) {
			// For non-existent files with extensions (e.g. missing images/fonts), return 404
			if filepath.Ext(path) != "" && !strings.HasSuffix(path, ".html") {
				http.NotFound(w, req)
				return
			}
			// Otherwise fallback to index.html for client-side routing
			req.URL.Path = "/index.html"
			fileServer.ServeHTTP(w, req)
			return
		}

		fileServer.ServeHTTP(w, req)
	})
}
