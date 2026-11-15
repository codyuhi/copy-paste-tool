// Package lihim provides at-rest encryption for the private (lihim) snippet set.
//
// The whole lihim payload for a user (sections + favorites JSON) is sealed with
// AES-256-GCM under a key derived from LIHIM_KEY. The user id is bound as
// additional data so a sealed blob cannot be moved to another user's row.
//
// Sealed layout: nonce (12) | ciphertext | GCM tag (16).
package lihim

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/hmac"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"errors"
	"fmt"
	"strings"
)

const (
	keySize   = 32
	nonceSize = 12
	tagSize   = 16
)

var (
	ErrNoKey   = errors.New("lihim: master key not configured")
	ErrCorrupt = errors.New("lihim: ciphertext is corrupt or was tampered with")
)

// Keyring seals and opens lihim blobs.
type Keyring struct {
	aead cipher.AEAD
}

// ParseKey accepts a 32-byte key encoded as base64 (std or URL, padded or not) or hex.
func ParseKey(raw string) ([]byte, error) {
	raw = strings.TrimSpace(raw)
	if raw == "" {
		return nil, ErrNoKey
	}
	decoders := []func(string) ([]byte, error){
		base64.StdEncoding.DecodeString,
		base64.RawStdEncoding.DecodeString,
		base64.URLEncoding.DecodeString,
		base64.RawURLEncoding.DecodeString,
		hex.DecodeString,
	}
	for _, dec := range decoders {
		if b, err := dec(raw); err == nil && len(b) == keySize {
			return b, nil
		}
	}
	return nil, fmt.Errorf("lihim: key must be %d bytes encoded as base64 or hex", keySize)
}

// NewKeyring builds a keyring from a 32-byte master key.
func NewKeyring(masterKey []byte) (*Keyring, error) {
	if len(masterKey) != keySize {
		return nil, fmt.Errorf("lihim: master key must be %d bytes", keySize)
	}
	m := hmac.New(sha256.New, masterKey)
	m.Write([]byte("copy-paste-tool/lihim/enc"))
	block, err := aes.NewCipher(m.Sum(nil))
	if err != nil {
		return nil, err
	}
	aead, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}
	return &Keyring{aead: aead}, nil
}

// Seal encrypts plaintext, binding id as additional data.
func (k *Keyring) Seal(id string, plaintext []byte) ([]byte, error) {
	nonce := make([]byte, nonceSize)
	if _, err := rand.Read(nonce); err != nil {
		return nil, err
	}
	return k.aead.Seal(nonce, nonce, plaintext, []byte(id)), nil
}

// Open decrypts a blob produced by Seal for the same id.
func (k *Keyring) Open(id string, sealed []byte) ([]byte, error) {
	if len(sealed) < nonceSize+tagSize {
		return nil, ErrCorrupt
	}
	pt, err := k.aead.Open(nil, sealed[:nonceSize], sealed[nonceSize:], []byte(id))
	if err != nil {
		return nil, ErrCorrupt
	}
	return pt, nil
}
