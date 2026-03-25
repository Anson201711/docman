#!/bin/bash
set -e

echo "============================================"
echo "DocMan Frontend Build"
echo "============================================"

cd docman-frontend

# Install dependencies
npm install --legacy-peer-deps

# Build
npm run build

# Go back to root
cd ..

# Build Docker image
docker build -t docman-frontend:latest -f docker/Dockerfile-frontend .

echo "============================================"
echo "Frontend build completed!"
echo "Docker image: docman-frontend:latest"
echo "============================================"
