# PostgreSQL Connection Troubleshooting Guide

## Problem

Spring Boot application fails to connect to PostgreSQL with two possible errors:

### Error 1: No encryption (sin cifrado)
```
FATAL: no hay una línea en pg_hba.conf para «172.24.144.1»,
usuario «adminbckvue», base de datos «appdbvue», sin cifrado
```
**Translation:** There is no entry in `pg_hba.conf` for host `172.24.144.1`, user `adminbckvue`, database `appdbvue`, without encryption.

**Cause:** The JDBC driver connects without SSL, but `pg_hba.conf` doesn't have a rule that allows unencrypted connections from that IP.

### Error 2: Server does not support SSL
```
Este servidor no soporta SSL.
```
**Translation:** This server does not support SSL.

**Cause:** Adding `?sslmode=require` to the JDBC URL forces SSL, but PostgreSQL was not configured with SSL enabled.

---

## Root Cause

The PostgreSQL server:
- Does **NOT** have SSL enabled
- Does **NOT** have a `pg_hba.conf` entry allowing unencrypted connections from the app's IP (`172.24.144.1`)

Both conditions together create a deadlock where neither encrypted nor unencrypted connections work.

---

## Solution (2 Steps Required)

### Step 1: Fix the JDBC URL in `application.properties`

Use `sslmode=disable` to tell the JDBC driver not to attempt SSL:

```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://172.24.144.1:5433/appdbvue?sslmode=disable
spring.datasource.username=adminbckvue
spring.datasource.password=StrongPassw0rd-2028!
spring.datasource.driver-class-name=org.postgresql.Driver
```

> **Note:** This matches how we connect via psql:
> `psql "host=localhost port=5433 dbname=appdbvue user=adminbckvue password=... sslmode=disable"`

### Step 2: Edit `pg_hba.conf` on the PostgreSQL Server

Add a line to allow unencrypted (md5 password) connections from the app's IP.

#### Find `pg_hba.conf`

| Environment | Typical Path |
|-------------|-------------|
| **WSL/Ubuntu** | `/etc/postgresql/<version>/main/pg_hba.conf` |
| **Windows** | `C:\Program Files\PostgreSQL\<version>\data\pg_hba.conf` |

To search for it in WSL:
```bash
sudo find / -name pg_hba.conf 2>/dev/null
```

#### Add This Line

For specific access (recommended):
```
host    appdbvue    adminbckvue    172.24.144.1/32    md5
```

Or for broader access from all WSL IPs:
```
host    all    all    172.24.0.0/16    md5
```

> **Important:** Add this line **before** any `host all all ... reject` rules at the bottom of the file.

#### Restart PostgreSQL

After editing `pg_hba.conf`, restart PostgreSQL for changes to take effect:

- **WSL/Linux:**
  ```bash
  sudo systemctl restart postgresql
  ```

- **Windows (PowerShell as Admin):**
  ```powershell
  Restart-Service postgresql-x64-<version>
  ```

- **Windows (Services GUI):**
  Open `services.msc` → Find `postgresql` → Right-click → Restart

---

## Verification

After both steps, run the Spring Boot application. You should see:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

Instead of the previous SQL errors.

---

## Quick Reference: sslmode Options

| Value | Behavior |
|-------|----------|
| `disable` | Never use SSL |
| `allow` | Try without SSL first, then with SSL |
| `prefer` | Try SSL first, then without (default) |
| `require` | Always require SSL |

---

## Related Files

- `application.properties` → JDBC connection settings
- `pg_hba.conf` → PostgreSQL client authentication config
- `comandosdb.txt` → Database creation commands (project root)
