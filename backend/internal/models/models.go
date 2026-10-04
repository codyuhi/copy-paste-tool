package models

import (
	"encoding/json"
	"time"
)

// User represents an account holder
type User struct {
	ID           string    `json:"id"`
	Username     string    `json:"username"`
	PasswordHash string    `json:"-"`
	CreatedAt    time.Time `json:"createdAt"`
	UpdatedAt    time.Time `json:"updatedAt"`
}

// CopyButton represents an individual copy/paste item
type CopyButton struct {
	ButtonName string `json:"buttonName"`
	PasteValue string `json:"pasteValue"`
}

// CopySection represents a category of buttons
type CopySection struct {
	SectionName    string       `json:"sectionName"`
	SectionButtons []CopyButton `json:"sectionButtons"`
}

// FavoriteButton represents a favorited button with references
type FavoriteButton struct {
	ButtonName string `json:"buttonName"`
	PasteValue string `json:"pasteValue"`
	SectionID  int    `json:"sectionId"`
	ButtonID   int    `json:"buttonId"`
}

// UserDataPayload represents the complete payload sent/received for user snippets
type UserDataPayload struct {
	Sections  []CopySection    `json:"sections"`
	Favorites []FavoriteButton `json:"favorites"`
	UpdatedAt time.Time        `json:"updatedAt,omitempty"`
}

// RegisterRequest payload for creating an account
type RegisterRequest struct {
	Username         string            `json:"username"`
	Password         string            `json:"password"`
	InitialSections  []CopySection    `json:"initialSections,omitempty"`
	InitialFavorites []FavoriteButton `json:"initialFavorites,omitempty"`
}

// LoginRequest payload for logging in
type LoginRequest struct {
	Username string `json:"username"`
	Password string `json:"password"`
}

// AuthResponse payload returned on login/register
type AuthResponse struct {
	Token string `json:"token"`
	User  User   `json:"user"`
}

// RawUserData stores the database representations
type RawUserData struct {
	UserID        string          `json:"userId"`
	SectionsJSON  json.RawMessage `json:"sections"`
	FavoritesJSON json.RawMessage `json:"favorites"`
	UpdatedAt     time.Time       `json:"updatedAt"`
}
