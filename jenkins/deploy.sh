#!/bin/bash
# ============================================
# DocMan Deployment Script
# ============================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
DEPLOY_TYPE="backend"
ENVIRONMENT=""
PROFILE="standalone"
IMAGE=""
DEPLOY_PATH=""
HEALTH_CHECK_URL=""
CONFIG_FILE=""
SSH_USER="deploy"
SSH_HOST=""
SSH_KEY=""

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Show usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --frontend                     Deploy frontend application"
    echo "  --backend                      Deploy backend application (default)"
    echo "  --environment ENV              Environment: dev, test, prod"
    echo "  --profile PROFILE              Profile: standalone, distributed"
    echo "  --image IMAGE                  Docker image to deploy"
    echo "  --deploy-path PATH             Deployment path on target server"
    echo "  --health-check-url URL         Health check URL"
    echo "  --ssh-host HOST                SSH host for remote deployment"
    echo "  --ssh-user USER                SSH user (default: deploy)"
    echo "  --ssh-key KEY                  SSH private key path"
    echo "  --config FILE                  Configuration file"
    echo "  -h, --help                     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --backend --environment prod --image registry.example.com/docman-backend:latest --deploy-path /opt/docman"
    echo "  $0 --frontend --environment prod --image registry.example.com/docman-frontend:latest --deploy-path /var/www/docman"
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --frontend)
            DEPLOY_TYPE="frontend"
            shift
            ;;
        --backend)
            DEPLOY_TYPE="backend"
            shift
            ;;
        --environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --profile)
            PROFILE="$2"
            shift 2
            ;;
        --image)
            IMAGE="$2"
            shift 2
            ;;
        --deploy-path)
            DEPLOY_PATH="$2"
            shift 2
            ;;
        --health-check-url)
            HEALTH_CHECK_URL="$2"
            shift 2
            ;;
        --ssh-host)
            SSH_HOST="$2"
            shift 2
            ;;
        --ssh-user)
            SSH_USER="$2"
            shift 2
            ;;
        --ssh-key)
            SSH_KEY="$2"
            shift 2
            ;;
        --config)
            CONFIG_FILE="$2"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            log_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Validate required parameters
if [ -z "$ENVIRONMENT" ]; then
    log_error "--environment is required"
    usage
    exit 1
fi

if [ -z "$IMAGE" ]; then
    log_error "--image is required"
    usage
    exit 1
fi

if [ -z "$DEPLOY_PATH" ]; then
    DEPLOY_PATH="/opt/docman/${ENVIRONMENT}"
fi

# Load config file if provided
if [ -n "$CONFIG_FILE" ] && [ -f "$CONFIG_FILE" ]; then
    log_info "Loading configuration from $CONFIG_FILE"
    source <(grep -E '^[a-zA-Z_]+=' "$CONFIG_FILE" | sed 's/ *= */=/')
fi

# Determine if remote deployment
REMOTE_DEPLOY=false
if [ -n "$SSH_HOST" ]; then
    REMOTE_DEPLOY=true
fi

# SSH command helper
SSH_CMD="ssh"
if [ -n "$SSH_KEY" ]; then
    SSH_CMD="ssh -i $SSH_KEY"
fi

REMOTE_SSH="${SSH_CMD} ${SSH_USER}@${SSH_HOST}"

# ============================================
# Pre-deployment checks
# ============================================
log_info "============================================"
log_info "DocMan Deployment Script"
log_info "============================================"
log_info "Type: $DEPLOY_TYPE"
log_info "Environment: $ENVIRONMENT"
log_info "Profile: $PROFILE"
log_info "Image: $IMAGE"
log_info "Deploy Path: $DEPLOY_PATH"
log_info "Remote: $REMOTE_DEPLOY"
log_info "============================================"

# ============================================
# Remote deployment
# ============================================
if [ "$REMOTE_DEPLOY" = true ]; then
    log_info "Preparing remote deployment to ${SSH_USER}@${SSH_HOST}..."

    # Check SSH connection
    if ! $REMOTE_SSH "echo 'SSH connection OK'" > /dev/null 2>&1; then
        log_error "Cannot connect to ${SSH_USER}@${SSH_HOST}"
        exit 1
    fi

    # Create deployment directory
    log_info "Creating deployment directory..."
    $REMOTE_SSH "sudo mkdir -p ${DEPLOY_PATH} && sudo chown -R ${SSH_USER}:${SSH_USER} ${DEPLOY_PATH}"

    # Stop existing containers
    log_info "Stopping existing containers..."
    $REMOTE_SSH "cd ${DEPLOY_PATH} && docker compose down -v 2>/dev/null || true"

    # Copy deployment files
    log_info "Uploading deployment files..."
    if [ "$DEPLOY_TYPE" = "frontend" ]; then
        $REMOTE_SSH "cat > ${DEPLOY_PATH}/docker-compose.yml" < docker/docker-compose-frontend-standalone.yml
    else
        if [ "$PROFILE" = "distributed" ]; then
            $REMOTE_SSH "cat > ${DEPLOY_PATH}/docker-compose.yml" < docker/docker-compose-distributed.yml
        else
            $REMOTE_SSH "cat > ${DEPLOY_PATH}/docker-compose.yml" < docker/docker-compose-standalone.yml
        fi
    fi

    # Update image in docker-compose
    log_info "Updating Docker image..."
    $REMOTE_SSH "cd ${DEPLOY_PATH} && sed -i 's|image:.*|image: ${IMAGE}|' docker-compose.yml"

    # Pull and start containers
    log_info "Pulling Docker image..."
    $REMOTE_SSH "cd ${DEPLOY_PATH} && docker pull ${IMAGE}"

    log_info "Starting containers..."
    $REMOTE_SSH "cd ${DEPLOY_PATH} && docker compose up -d"

# ============================================
# Local deployment
# ============================================
else
    log_info "Preparing local deployment..."

    # Check if Docker is running
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running or not accessible"
        exit 1
    fi

    # Create deployment directory
    log_info "Creating deployment directory..."
    mkdir -p "$DEPLOY_PATH"

    # Copy docker-compose file
    log_info "Setting up docker-compose..."
    if [ "$DEPLOY_TYPE" = "frontend" ]; then
        cp docker/docker-compose-frontend.yml "$DEPLOY_PATH/docker-compose.yml" 2>/dev/null || \
        cp docker/docker-compose-standalone.yml "$DEPLOY_PATH/docker-compose.yml"
    else
        if [ "$PROFILE" = "distributed" ]; then
            cp docker/docker-compose-distributed.yml "$DEPLOY_PATH/docker-compose.yml"
        else
            cp docker/docker-compose-standalone.yml "$DEPLOY_PATH/docker-compose.yml"
        fi
    fi

    # Update image in docker-compose
    log_info "Updating Docker image..."
    sed -i "s|image:.*|image: ${IMAGE}|" "$DEPLOY_PATH/docker-compose.yml"

    # Stop existing containers
    log_info "Stopping existing containers..."
    cd "$DEPLOY_PATH" && docker compose down -v 2>/dev/null || true

    # Pull and start containers
    log_info "Pulling Docker image..."
    docker pull "$IMAGE"

    log_info "Starting containers..."
    cd "$DEPLOY_PATH" && docker compose up -d
fi

# ============================================
# Health check
# ============================================
if [ -n "$HEALTH_CHECK_URL" ]; then
    log_info "Performing health check..."
    MAX_RETRIES=30
    RETRY_COUNT=0

    while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
        if curl -sf "$HEALTH_CHECK_URL" > /dev/null 2>&1; then
            log_success "Health check passed!"
            break
        fi

        RETRY_COUNT=$((RETRY_COUNT + 1))
        log_info "Health check attempt ${RETRY_COUNT}/${MAX_RETRIES}..."
        sleep 10
    done

    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        log_error "Health check failed after ${MAX_RETRIES} attempts"
        exit 1
    fi
fi

# ============================================
# Deployment summary
# ============================================
log_success "============================================"
log_success "Deployment completed successfully!"
log_success "============================================"
log_info "Type: $DEPLOY_TYPE"
log_info "Environment: $ENVIRONMENT"
log_info "Image: $IMAGE"
log_info "Path: $DEPLOY_PATH"
if [ "$REMOTE_DEPLOY" = true ]; then
    log_info "Host: ${SSH_USER}@${SSH_HOST}"
fi
log_info "============================================"

# Show running containers
if [ "$REMOTE_DEPLOY" = true ]; then
    $REMOTE_SSH "cd ${DEPLOY_PATH} && docker compose ps"
else
    cd "$DEPLOY_PATH" && docker compose ps
fi

exit 0
