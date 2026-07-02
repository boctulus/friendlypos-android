---
name: docker-network-reliability-guard
description: Diagnose and prevent ECONNRESET, UND_ERR_SOCKET, and other networking errors in containerized environments (Docker / WSL) when applications interact with services like Supabase.
---

# Skill: Docker Networking Reliability Check

## Objective

Detect and prevent networking errors such as:

- ECONNRESET
- UND_ERR_SOCKET
- UND_ERR_CONNECT_TIMEOUT
- socket hang up
- fetch failed
- other side closed

These errors typically occur when applications communicate with services running in containers (for example **Supabase** running in **Docker**).

The verification must always occur at **three levels**:

1. Host infrastructure
2. Docker runtime environment
3. HTTP client resilience in application code

Never assume the issue is only in application code.

---

# 0. Quick Diagnosis (Always run first)

Many networking issues can be detected in seconds.

Check container status:

```

docker ps

```

Check resource usage:

```

docker stats

```

Verify endpoint availability:

```

curl [http://localhost](http://localhost):<PORT>

```

If the service returns HTTP 200, the problem is likely in the application layer.

---

# 1. Host Infrastructure Verification

## Case A — Windows with WSL

If the system uses **Windows Subsystem for Linux**, verify the WSL resource configuration.

File:

```

C:\Users<user>.wslconfig

````

Recommended configuration:

```ini
[wsl2]
memory=6GB
processors=2
swap=2GB
localhostForwarding=true
````

### Runtime verification

Inside WSL:

```
nproc
free -h
```

Minimum recommended resources:

```
CPU >= 2
RAM >= 4GB
```

If resources are lower, networking failures may occur due to container instability.

After modifying `.wslconfig`:

```
wsl --shutdown
```

Restart WSL afterwards.

---

## Case B — Native Linux

Verify host resources:

```
nproc
free -h
docker info
```

Check container limits:

```
docker inspect <container>
```

Look for:

```
Memory
NanoCpus
```

Containers with low limits may terminate sockets abruptly.

---

# 2. Docker Environment Verification

## 2.1 Container health

First mandatory check:

```
docker ps
```

Ensure no container is in:

```
Restarting
Exited
Unhealthy
```

If a container is restarting, inspect logs:

```
docker logs <container> --tail 30
```

Restarting containers frequently cause:

```
ECONNRESET
UND_ERR_SOCKET
```

---

## 2.2 Resource pressure

Monitor containers:

```
docker stats
```

Look for:

* high memory usage
* OOM kills
* CPU throttling

---

## 2.3 Stale connections after container restart

If the database container restarts, dependent services may keep stale connections.

Typical affected services in Supabase:

* rest
* kong
* auth

Fix:

```
docker restart supabase-<project>-rest supabase-<project>-kong
```

---

## 2.4 Docker DNS issues

Container IP addresses change when containers restart.

Dependent containers may keep outdated DNS resolutions.

Fix:

```
docker compose restart rest kong auth
```

---

# 3. Networking Verification

Verify that the service is reachable from the host.

Example Supabase query through Kong:

```
curl -s -o /dev/null -w "%{http_code}" \
"http://localhost:<KONG_PORT>/rest/v1/<table>?select=id&limit=1" \
-H "apikey: <SERVICE_ROLE_KEY>" \
-H "Authorization: Bearer <SERVICE_ROLE_KEY>"
```

Expected result:

```
200
```

If the request fails:

```
docker ps
docker network ls
docker logs <container>
```

---

# 4. Common Supabase Local Issues

## 4.1 Missing postgres role

Symptom:

```
role "postgres" does not exist
```

Cause:

Custom `POSTGRES_USER` in docker-compose.

Temporary fix:

```
docker exec <db-container> psql -U supabase -d postgres \
-c "CREATE ROLE postgres WITH SUPERUSER LOGIN PASSWORD 'postgres';"
```

Permanent fix: init script.

```
volumes/db/init/01-create-postgres-role.sql
```

```sql
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'postgres') THEN
    CREATE ROLE postgres WITH SUPERUSER LOGIN PASSWORD 'postgres';
  END IF;
END $$;
```

Mount in docker-compose:

```yaml
volumes:
  - ./volumes/db/init:/docker-entrypoint-initdb.d:ro
```

---

## 4.2 Realtime container failures

Required variables:

```yaml
environment:
  APP_NAME: realtime
  RLIMIT_NOFILE: "1048576"
```

Correct command:

```yaml
command: >
  bash -c "/app/bin/realtime eval Realtime.Release.migrate
  && /app/bin/realtime start"
```

Verify binary location:

```
docker run --rm --entrypoint bash supabase/realtime:<version> -c "ls /app/bin/"
```

---

# 5. Mandatory Code Rules

Application code must never rely on default `fetch` behavior.

Required protections:

* retry
* exponential backoff
* coverage of socket errors
* server-side cache disabled

Errors that must be handled:

```
ECONNRESET
UND_ERR_SOCKET
UND_ERR_CONNECT_TIMEOUT
fetch failed
other side closed
```

---

# 6. Reference Implementation (Supabase Client)

Example resilient client for Node.js.

```typescript
import { createClient } from '@supabase/supabase-js'

async function fetchWithRetry(
  url: RequestInfo | URL,
  options: RequestInit = {}
): Promise<Response> {

  const MAX_RETRIES = 3
  let lastError: unknown

  for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
    try {
      const res = await fetch(url, {
        ...options,
        cache: 'no-store'
      })

      if (res.status >= 500) {
        throw new Error(`Server error ${res.status}`)
      }

      return res

    } catch (err: any) {
      lastError = err

      const retryable =
        err instanceof TypeError &&
        (
          err.message.includes('fetch failed') ||
          err.message.includes('ECONNRESET') ||
          err.message.includes('UND_ERR_SOCKET') ||
          err.message.includes('UND_ERR_CONNECT_TIMEOUT') ||
          err.message.includes('other side closed')
        )

      if (!retryable || attempt === MAX_RETRIES) throw err

      const delay =
        200 * Math.pow(2, attempt) +
        Math.random() * 100

      await new Promise(r => setTimeout(r, delay))
    }
  }

  throw lastError
}

export const supabase = createClient(
  process.env.SUPABASE_URL!,
  process.env.SUPABASE_ANON_KEY!,
  {
    global: { fetch: fetchWithRetry }
  }
)
```

---

# 7. Mandatory Debug Checklist

When encountering:

```
ECONNRESET
UND_ERR_SOCKET
fetch failed
```

Always perform these steps in order:

1. `docker ps`
2. verify no containers are restarting
3. inspect logs of failing containers
4. verify service availability with `curl`
5. restart dependent services if DB restarted
6. confirm resilient fetch implementation
7. verify WSL / host resources
