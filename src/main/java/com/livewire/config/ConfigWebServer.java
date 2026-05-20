package com.livewire.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class ConfigWebServer implements AutoCloseable {
    private final HttpServer server;
    private final ConfigRegistry registry;
    private final ConfigStore store;
    private final ConfigWatcher watcher;
    private final ObjectMapper mapper;

    public ConfigWebServer(int port, ConfigRegistry registry, ConfigStore store, ConfigWatcher watcher) throws IOException {
        this.registry = registry;
        this.store = store;
        this.watcher = watcher;
        this.mapper = store.mapper();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleIndex);
        server.createContext("/api/config", this::handleApi);
        server.setExecutor(null);
    }

    public void start() { server.start(); }
    public int port() { return server.getAddress().getPort(); }

    private void handleIndex(HttpExchange ex) throws IOException {
        if (!"/".equals(ex.getRequestURI().getPath())) {
            send(ex, 404, "Not Found", "text/plain");
            return;
        }
        send(ex, 200, INDEX_HTML, "text/html; charset=utf-8");
    }

    private void handleApi(HttpExchange ex) throws IOException {
        try {
            switch (ex.getRequestMethod()) {
                case "GET" -> sendJson(ex, 200, snapshot());
                case "POST" -> {
                    JsonNode body = mapper.readTree(ex.getRequestBody());
                    String key = body.path("key").asText();
                    JsonNode value = body.path("value");
                    if (key.isEmpty()) { send(ex, 400, "missing key", "text/plain"); return; }
                    if (registry.get(key) == null) { send(ex, 404, "unknown key: " + key, "text/plain"); return; }
                    registry.setFromJson(key, value, mapper);
                    watcher.suppress(500);
                    store.write(registry);
                    sendJson(ex, 200, snapshot());
                }
                default -> send(ex, 405, "method not allowed", "text/plain");
            }
        } catch (Exception e) {
            send(ex, 500, "error: " + e.getMessage(), "text/plain");
        }
    }

    private JsonNode snapshot() {
        ArrayNode arr = mapper.createArrayNode();
        for (ConfigBinding b : registry.bindings()) {
            ObjectNode n = mapper.createObjectNode();
            n.put("key", b.key());
            n.put("type", b.type().getSimpleName());
            n.put("description", b.description());
            n.set("value", mapper.valueToTree(b.read()));
            arr.add(n);
        }
        return arr;
    }

    private void send(HttpExchange ex, int code, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", contentType);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private void sendJson(HttpExchange ex, int code, JsonNode node) throws IOException {
        send(ex, code, mapper.writeValueAsString(node), "application/json; charset=utf-8");
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private static final String INDEX_HTML = """
            <!doctype html>
            <html lang="en"><head><meta charset="utf-8"><title>LiveWire</title>
            <style>
              body { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; background:#111; color:#eee; padding:24px; }
              h1 { margin:0 0 16px; font-weight:500; }
              h1 small { color:#888; font-size:0.6em; margin-left:8px; }
              table { border-collapse:collapse; width:100%; }
              th, td { padding:8px 12px; border-bottom:1px solid #2a2a2a; text-align:left; vertical-align:top; }
              th { color:#9ca; font-weight:500; }
              input { background:#1a1a1a; color:#eee; border:1px solid #333; padding:6px 10px; font:inherit; min-width:200px; border-radius:3px; }
              button { background:#2a7a4a; color:#fff; border:0; padding:6px 14px; cursor:pointer; border-radius:3px; font:inherit; }
              button:hover { background:#358a5a; }
              .desc { color:#888; font-size:0.85em; margin-top:2px; }
              .type { color:#79a; font-size:0.85em; }
              #status { padding:8px 0; }
              .ok { color:#9c9; }
              .err { color:#f88; }
            </style></head><body>
            <h1>LiveWire <small>live config</small></h1>
            <div id="status">loading...</div>
            <table id="t"><thead><tr><th>Key</th><th>Type</th><th>Value (JSON)</th><th></th></tr></thead><tbody></tbody></table>
            <script>
            async function refresh() {
              try {
                const r = await fetch('/api/config');
                const data = await r.json();
                const tbody = document.querySelector('#t tbody');
                const active = document.activeElement;
                const activeKey = active && active.dataset ? active.dataset.key : null;
                tbody.innerHTML = '';
                for (const e of data) {
                  const tr = document.createElement('tr');
                  const k = document.createElement('td');
                  k.innerHTML = '<div>'+e.key+'</div>'+(e.description?'<div class="desc">'+e.description+'</div>':'');
                  const t = document.createElement('td'); t.className='type'; t.textContent = e.type;
                  const v = document.createElement('td');
                  const inp = document.createElement('input');
                  inp.value = JSON.stringify(e.value);
                  inp.dataset.key = e.key;
                  inp.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') apply(e.key, inp); });
                  v.appendChild(inp);
                  const a = document.createElement('td');
                  const btn = document.createElement('button'); btn.textContent='Apply';
                  btn.onclick = () => apply(e.key, inp);
                  a.appendChild(btn);
                  tr.append(k,t,v,a);
                  tbody.appendChild(tr);
                  if (activeKey === e.key) inp.focus();
                }
                if (!activeKey) setStatus('synced (' + data.length + ' fields)', false);
              } catch (e) { setStatus('refresh failed: '+e.message, true); }
            }
            function setStatus(msg, isErr) {
              const s = document.getElementById('status');
              s.textContent = msg;
              s.className = isErr ? 'err' : 'ok';
            }
            async function apply(key, inp) {
              try {
                const value = JSON.parse(inp.value);
                const r = await fetch('/api/config', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({key, value})});
                if (!r.ok) throw new Error(await r.text());
                setStatus('updated '+key, false);
              } catch (e) {
                setStatus('error on '+key+': '+e.message, true);
              }
            }
            refresh();
            setInterval(refresh, 2000);
            </script>
            </body></html>
            """;
}
