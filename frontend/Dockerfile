# syntax=docker/dockerfile:1.7
FROM node:20-alpine AS build
WORKDIR /workspace
COPY package.json ./
RUN npm install --no-audit --no-fund
COPY tsconfig.json angular.json ./
COPY src ./src
RUN npx ng build --configuration production

FROM nginx:1.27-alpine
RUN apk add --no-cache wget
COPY --from=build /workspace/dist/sifap-frontend/browser /usr/share/nginx/html
RUN printf 'server {\n  listen 4200;\n  root /usr/share/nginx/html;\n  index index.html;\n\n  # Proxy API calls to backend service in Docker network.\n  location /api/ {\n    proxy_pass http://backend:8080;\n    proxy_http_version 1.1;\n    proxy_set_header Host $host;\n    proxy_set_header X-Real-IP $remote_addr;\n    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n    proxy_set_header X-Forwarded-Proto $scheme;\n  }\n\n  location / {\n    try_files $uri $uri/ /index.html;\n  }\n}\n' > /etc/nginx/conf.d/default.conf
EXPOSE 4200
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD wget --spider -q http://localhost:4200/ || exit 1
