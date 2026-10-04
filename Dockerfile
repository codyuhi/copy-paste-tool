# Stage 1: Build React Vite static bundle
FROM node:22-alpine AS frontend-builder
WORKDIR /build/app
COPY app/package.json app/package-lock.json ./
RUN npm ci || npm install
COPY app/ ./
RUN npm run build

# Stage 2: Build static Go server binary (Zero-CGO SQLite modernc.org/sqlite)
FROM golang:alpine AS backend-builder
WORKDIR /build
RUN apk add --no-cache git ca-certificates tzdata
COPY backend/go.mod backend/go.sum ./backend/
RUN cd backend && go mod download
COPY backend/ ./backend/
RUN cd backend && CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o /app/server ./cmd/server

# Stage 3: Minimal, secure production runtime
FROM alpine:3.20 AS runner
WORKDIR /app
RUN apk add --no-cache ca-certificates tzdata curl

# Create directory for persistent SQLite database & static assets
RUN mkdir -p /app/data /app/dist

# Copy binary & static assets
COPY --from=backend-builder /app/server /app/server
COPY --from=frontend-builder /build/app/dist /app/dist

ENV PORT=80
ENV DB_PATH=/app/data/copypaste.db
ENV STATIC_DIR=/app/dist

EXPOSE 80

CMD ["/app/server"]