#!/usr/bin/env bash
#
# scripts/setup.sh — Bootstrap a workshop team repository
#
# Usage:
#   ./11-scripts/setup.sh
#
# What it does:
#   1. Verifies prerequisites (Java 21, Node 20, Docker, gh CLI)
#   2. Clones reference materials (legacy SIFAP code + reference prototype)
#      into ./reference/ (read-only, gitignored)
#   3. Initializes empty specs/ directory for Spec-Kit artifacts
#   4. Verifies devcontainer is reachable

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

# ANSI colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

ok()    { printf "${GREEN}✓${NC} %s\n" "$*"; }
warn()  { printf "${YELLOW}!${NC} %s\n" "$*"; }
fail()  { printf "${RED}✗${NC} %s\n" "$*" >&2; }

echo "== SIFAP 2.0 team repo bootstrap =="
echo

# 1. Prerequisites
echo "Checking prerequisites..."
need_tool() {
  if command -v "$1" >/dev/null 2>&1; then
    ok "$1 found"
  else
    fail "$1 NOT found — install before continuing"
    exit 1
  fi
}
need_tool java
need_tool node
need_tool docker
need_tool git

if docker info >/dev/null 2>&1; then
  ok "Docker daemon acessível para o usuário atual"
else
  warn "Docker instalado, mas sem acesso ao daemon para o usuário atual"
  warn "Se aparecer 'permission denied' no /var/run/docker.sock, execute:"
  warn "  sudo usermod -aG docker \$USER"
  warn "  newgrp docker   (ou faça logout/login)"
fi

if command -v gh >/dev/null 2>&1; then
  ok "gh CLI found"
else
  warn "gh CLI not found — you'll need it for Issue/PR automation"
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | grep -oE '"[0-9]+' | tr -d '"' || echo "0")
if [ "$JAVA_VERSION" -ge 21 ]; then
  ok "Java $JAVA_VERSION (>= 21 required)"
else
  fail "Java version $JAVA_VERSION too old — Java 21 required"
  exit 1
fi

NODE_VERSION=$(node -v | grep -oE '[0-9]+' | head -1 || echo "0")
if [ "$NODE_VERSION" -ge 20 ]; then
  ok "Node $NODE_VERSION (>= 20 required)"
else
  fail "Node version $NODE_VERSION too old — Node 20 required"
  exit 1
fi

# 2. Reference materials
echo
echo "Cloning reference materials (read-only)..."
mkdir -p reference

# Repositório de referência (sobrescreva via env WORKSHOP_REPO se necessário)
WORKSHOP_REPO="${WORKSHOP_REPO:-https://github.com/serpro-curitiba/grupo-1-vermelho-old.git}"

clone_or_pull() {
  local url="$1" dest="$2"
  if [ -d "$dest/.git" ]; then
    warn "$dest exists — skipping clone"
  else
    git clone --depth 1 "$url" "$dest"
    ok "Cloned $url"
  fi
}

if [ ! -d "reference/workshop-datacorp" ]; then
  clone_or_pull "$WORKSHOP_REPO" "reference/workshop-datacorp"
fi

# Symlink prototype/ e infra/ se existirem na referência
PROTOTYPE_SOURCE="${PROTOTYPE_SOURCE:-reference/workshop-datacorp/04-prototipo-sifap-moderno}"
INFRA_SOURCE="${INFRA_SOURCE:-reference/workshop-datacorp/05-terraform-azure}"

if [ -d "$PROTOTYPE_SOURCE" ]; then
  ln -sfn "$PROTOTYPE_SOURCE" prototype
  ok "Linked prototype/ -> $PROTOTYPE_SOURCE"
else
  warn "Prototype não encontrado em '$PROTOTYPE_SOURCE'"
  warn "Defina PROTOTYPE_SOURCE com o caminho correto antes de subir o docker compose"
fi

if [ -d "$INFRA_SOURCE" ]; then
  ln -sfn "$INFRA_SOURCE" infra
  ok "Linked infra/ -> $INFRA_SOURCE"
else
  warn "Infra não encontrada em '$INFRA_SOURCE'"
  warn "Defina INFRA_SOURCE com o caminho correto se precisar dos módulos Terraform"
fi

if [ ! -d "prototype/backend" ] || [ ! -d "prototype/frontend" ]; then
  warn "Estrutura esperada para Docker Compose ausente: prototype/backend e/ou prototype/frontend"
  warn "Sem isso, 'docker compose up -d --wait' não conseguirá buildar backend/frontend"
fi

# 3. Initialize specs/ for Spec-Kit
echo
echo "Initializing Spec-Kit workspace..."
mkdir -p specs
if [ ! -f specs/.gitkeep ]; then
  touch specs/.gitkeep
fi
ok "specs/ ready"

# 4. Devcontainer hint
echo
if [ -f .devcontainer/devcontainer.json ]; then
  ok "Dev container ready — open VS Code and 'Reopen in Container'"
else
  warn ".devcontainer/devcontainer.json not found"
fi

echo
echo "Done. Next steps:"
echo "  1. Read 00-TEAM-FLOW.md"
echo "  2. Read your two persona cards in 05-personas/"
echo "  3. Open Stage 1 guide: 01-arqueologia/GUIDE.md"
