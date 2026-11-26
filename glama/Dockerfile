# syntax=docker/dockerfile:1
# ClipFlow MCP server over stdio, for Glama's introspection checks.
#
# ClipFlow is a desktop clipboard manager; its MCP server is built into the
# app and speaks SSE on 127.0.0.1:13130. This image runs the app's headless
# daemon (no display needed) and bridges its SSE endpoint to stdio with
# supergateway, so the container behaves like a plain stdio MCP server.
# Build from the repository root: docker build -f glama/Dockerfile .

FROM node:22-bookworm-slim

ARG CLIPFLOW_VERSION=2.2.0
ARG CLIPFLOW_BUILD=2544
ARG TARGETARCH

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl \
      libfreetype6 fontconfig \
 && rm -rf /var/lib/apt/lists/*

RUN set -eux; \
    case "${TARGETARCH}" in \
      amd64) arch=amd64 ;; \
      arm64) arch=aarch64 ;; \
      *) echo "unsupported arch ${TARGETARCH}" >&2; exit 1 ;; \
    esac; \
    curl -fsSL -o /tmp/clipflow.tar.gz \
      "https://github.com/ClipFlow/clipflow-desktop/releases/download/${CLIPFLOW_VERSION}.${CLIPFLOW_BUILD}/clipflow-${CLIPFLOW_VERSION}-${CLIPFLOW_BUILD}-linux-${arch}.tar.gz"; \
    mkdir -p /opt/clipflow; \
    tar -xzf /tmp/clipflow.tar.gz -C /opt/clipflow --strip-components=1; \
    rm /tmp/clipflow.tar.gz

RUN npm install -g supergateway@3.4.3

RUN useradd -m -u 1987 mcp

COPY glama/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod 755 /usr/local/bin/entrypoint.sh

USER mcp
WORKDIR /home/mcp
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
