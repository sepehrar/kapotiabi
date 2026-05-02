# Kapotiabi — demo deployment notes

## Target

`https://www.kapotiabi.ee` on a self-managed VPS, docker-compose stack.
Audience: course staff + team only — keep the URL inside that group
until the demo banner ships in a follow-up cut.

Initial deploy: _<YYYY-MM-DD — fill in when first deployed>_

## What changed for the demo cut

Each item links the issue #39 number it closes.

### Security hardening

- Removed seeded admin via `V10__remove_seeded_admin.sql`. The original
  V3 seed bcrypted `Admin1234`, which is permanently public via git
  history. Real admin must be created post-deploy (see "Create the real
  admin" below). — closes #39 P0-1
- JWT secret is now mandatory. Backend refuses to start if `JWT_SECRET`
  is missing or blank: `JwtConfig.ensureSecret()` throws
  `IllegalStateException`, the dev fallback in `application.yaml` is
  gone, and the matching error message in `JwtTokenProvider` no longer
  references a fallback that doesn't exist. — closes #39 P0-3
- Postgres no longer trusts all connections.
  `POSTGRES_HOST_AUTH_METHOD: trust` removed; `POSTGRES_PASSWORD` is
  required from env (`${DB_PASSWORD:?...}`). The 5432 port is bound to
  `127.0.0.1` only — admin access is via SSH tunnel. — closes #39 P0-2
- Swagger paths require auth at the app layer AND basic auth at the
  proxy. `SecurityConfig` no longer `permitAll`s `/swagger-ui/**` or
  `/v3/api-docs/**`; they fall through to `.authenticated()`. Caddy
  adds basic auth in front (defense-in-depth). The
  `swaggerOpenApiPathIsPublic_noToken` integration test was inverted to
  match the new behavior. — closes #39 P1-9
- CORS already env-driven (commit d73a4d8). Prod env value is
  `APP_CORS_ALLOWED_ORIGINS=https://www.kapotiabi.ee` (default in the
  hardened `docker-compose.yml`). — closes #39 P0-4

### Infra

- Added Caddy reverse proxy (`Caddyfile` at repo root) with:
  - automatic Let's Encrypt TLS for `www.kapotiabi.ee`
  - HSTS, X-Content-Type-Options, Referrer-Policy, X-Robots-Tag
  - `handle /api/*` → `backend:8080`
  - `@swagger` matcher → backend, gated by HTTP basic auth
    (`SWAGGER_BASIC_HASH` env var)
  - apex `kapotiabi.ee` 301 → `https://www.kapotiabi.ee`
  — closes #39 P1-7
- Frontend `NEXT_PUBLIC_API_URL` baked at build time via compose
  `build.args` (the runtime env var was a no-op for `NEXT_PUBLIC_*`).
  Default value in compose: `https://www.kapotiabi.ee/api/v1`. —
  closes #39 P1-8
- Uploads mounted to a named Docker volume `uploads_data`
  (`/app/uploads` inside backend, `APP_UPLOAD_DIR` set accordingly).
  Rebuilds no longer wipe uploaded PDFs. — closes #39 P1-10
- Backend / frontend / db ports bound to `127.0.0.1` only — only Caddy
  (80/443) is reachable from outside the host.
- Container memory limits: backend 1g, db 512m, frontend 512m,
  caddy 256m. — closes #39 P1-12
- Docker `json-file` log rotation (max 10MB × 5 per service) on every
  service. — closes #39 P2-19
- Manual pre-launch DB snapshot in lieu of cron'd daily backups
  (single-shot demo window). — partial #39 P1-11

### SEO

- `frontend/src/app/robots.ts` returns `Disallow: /` for all user
  agents. — closes #39 P0-6

## Explicitly NOT done (and why)

- **Demo banner (#39 P0-5)** — pulled from this cut, slated for a
  follow-up frontend pass. Until it ships, the only "this is a demo"
  signal is the URL itself plus the `noindex` robots/header. **Do not
  share the URL outside course staff + team.**
- Full GDPR bundle (#13–15): privacy/ToS pages, signup ToS checkbox,
  cookie consent UI, in-app account deletion. Audience scope keeps this
  out of the demo cut.
- SMTP / email (#16): no forgot-password UI exists yet — nothing to
  wire to.
- CI-driven deploys (#17): manual `.env` on the VPS for the demo
  window.
- Daily DB backups, UptimeRobot, fail2ban, non-root containers, refresh
  tokens, Sentry, image scanning: all post-demo.

## How to deploy

### Prerequisites on the VPS

- Docker Engine + the docker-compose plugin (`docker compose ...`).
- Inbound firewall: ports 80 and 443 open. Ports 3000, 5432, 8080 are
  bound to `127.0.0.1` so they're not reachable externally regardless,
  but you should still firewall them off.
- DNS A records `www.kapotiabi.ee` and `kapotiabi.ee` pointing at the
  VPS IP. Caddy needs both reachable on 80/443 to issue certs.

### Generate secrets (one-off)

```bash
openssl rand -base64 24      # → DB_PASSWORD
openssl rand -base64 48      # → JWT_SECRET
docker run --rm caddy:2-alpine caddy hash-password
                              # → SWAGGER_BASIC_HASH (will prompt twice)
```

### Write `/opt/kapotiabi/.env` (chmod 600, owned by deploy user)

```dotenv
DB_PASSWORD=<paste>
JWT_SECRET=<paste>
SWAGGER_BASIC_HASH=<paste, single-quoted to escape any $ in bcrypt>
# Optional overrides — defaults are correct for prod
# APP_CORS_ALLOWED_ORIGINS=https://www.kapotiabi.ee
# NEXT_PUBLIC_API_URL=https://www.kapotiabi.ee/api/v1
```

### Bring up the stack

```bash
cd /opt/kapotiabi
git pull
docker compose --env-file .env up -d --build
```

Caddy fetches Let's Encrypt certs on first run; allow ~30 seconds.
Watch with `docker compose logs -f caddy`.

### Create the real admin (one-off, after first deploy)

1. Register a normal account through the UI with a strong password
   (this hits `/api/v1/auth/register`).
2. Promote it to admin:
   ```bash
   docker compose exec db psql -U kapotiabi -c \
     "UPDATE users SET role='admin' WHERE email='<your-email>';"
   ```
3. Log in with that account, confirm `/api/v1/admin/...` endpoints
   respond.

### Pre-launch DB snapshot

```bash
docker compose exec db pg_dump -U kapotiabi kapotiabi \
  > snapshot-$(date +%F).sql
scp snapshot-*.sql <off-host>
```

## Post-deploy checks

Tick each one before announcing the URL.

- [ ] `curl -I https://www.kapotiabi.ee` → 200, `Strict-Transport-Security`
      and `X-Robots-Tag: noindex, nofollow` headers present.
- [ ] `curl https://www.kapotiabi.ee/robots.txt` → contains `Disallow: /`.
- [ ] `curl -I https://www.kapotiabi.ee/swagger-ui/index.html` → 401.
      With `-u admin:<pw>` → 200.
- [ ] `curl -I http://kapotiabi.ee` → 301 to `https://www.kapotiabi.ee`.
- [ ] From a non-VPS host: `nc -vz <vps-ip> 5432`, `... 8080`, `... 3000`
      → all refused (only 80/443 open).
- [ ] Try logging in as `admin@kapotiabi.ee / Admin1234` → fails.
- [ ] Promote your real admin account, log in, hit
      `/api/v1/admin/shops` → 200.
- [ ] Upload a PDF as a shop owner → file persists across
      `docker compose restart backend`.

## Rollback

- **Bad app deploy:**
  ```bash
  git checkout <prev-sha>
  docker compose up -d --build
  ```
- **Bad migration:** restore from the latest `snapshot-*.sql` into a
  fresh DB volume:
  ```bash
  docker compose down
  docker volume rm kapotiabi_postgres_data
  docker compose --env-file .env up -d db
  docker compose exec -T db psql -U kapotiabi kapotiabi < snapshot-YYYY-MM-DD.sql
  docker compose --env-file .env up -d
  ```

## Change log

- _<YYYY-MM-DD>_ — initial demo deploy.
