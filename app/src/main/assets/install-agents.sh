#!/bin/bash
# install-agents.sh
apt update && apt upgrade -y
apt install nodejs npm python3 python3-pip git curl wget -y

# OpenCode
npm install -g opencode

# Codex CLI
npm install -g @openai/codex

# OpenClaw
npm install -g openclaw

# Cursor CLI
npm install -g cursor-agent

# Claude Code (требует Python 3.11+)
pip install claude-code

# Gemini CLI
npm install -g @google/gemini-cli

# Aider
pip install aider-chat

echo "Все агенты установлены!"
