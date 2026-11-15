package lihim

import (
	"bytes"
	"encoding/base64"
	"testing"
)

func testKeyring(t *testing.T) *Keyring {
	t.Helper()
	k, err := NewKeyring(bytes.Repeat([]byte{7}, keySize))
	if err != nil {
		t.Fatal(err)
	}
	return k
}

func TestSealOpenRoundTrip(t *testing.T) {
	k := testKeyring(t)
	msg := []byte(`{"sections":[{"sectionName":"secret"}]}`)
	sealed, err := k.Seal("user-1", msg)
	if err != nil {
		t.Fatal(err)
	}
	if bytes.Contains(sealed, []byte("secret")) {
		t.Fatal("sealed blob contains plaintext")
	}
	got, err := k.Open("user-1", sealed)
	if err != nil || !bytes.Equal(got, msg) {
		t.Fatalf("round trip failed: %v %q", err, got)
	}
}

func TestOpenRejectsWrongIDAndTampering(t *testing.T) {
	k := testKeyring(t)
	sealed, _ := k.Seal("user-1", []byte("hello"))
	if _, err := k.Open("user-2", sealed); err != ErrCorrupt {
		t.Fatalf("expected ErrCorrupt for wrong id, got %v", err)
	}
	sealed[len(sealed)-1] ^= 1
	if _, err := k.Open("user-1", sealed); err != ErrCorrupt {
		t.Fatalf("expected ErrCorrupt for tampered blob, got %v", err)
	}
	if _, err := k.Open("user-1", []byte{1, 2}); err != ErrCorrupt {
		t.Fatalf("expected ErrCorrupt for short blob, got %v", err)
	}
}

func TestParseKey(t *testing.T) {
	raw := bytes.Repeat([]byte{9}, keySize)
	for _, enc := range []string{
		base64.StdEncoding.EncodeToString(raw),
		base64.RawURLEncoding.EncodeToString(raw),
		"0909090909090909090909090909090909090909090909090909090909090909",
	} {
		got, err := ParseKey(" " + enc + "\n")
		if err != nil || !bytes.Equal(got, raw) {
			t.Fatalf("ParseKey(%q) = %v, %v", enc, got, err)
		}
	}
	if _, err := ParseKey(""); err != ErrNoKey {
		t.Fatalf("expected ErrNoKey, got %v", err)
	}
	if _, err := ParseKey("c2hvcnQ="); err == nil {
		t.Fatal("expected error for short key")
	}
}
