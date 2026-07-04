# Build stage
FROM node:20-alpine AS build-stage
WORKDIR /app
COPY ./app/package*.json ./
RUN npm ci
COPY ./app ./
RUN npm run build

# Production stage
FROM nginx:1.25.1-alpine AS production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]