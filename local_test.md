# Local Testing Guide

## Run Locally

- Ensure PostgreSQL is up with database `mrr`, user `mrr_user`, and sample data in `person`; adjust `src/main/resources/application-local.properties` if credentials differ.
- **IMPORTANT:** Set a secure JWT secret (minimum 512 bits for HS512 algorithm):
  ```bash
  export JWT_SECRET="this-is-a-very-secure-jwt-secret-key-that-is-at-least-512-bits-long-for-hs512-algorithm-security-requirement"
  ```
  Optional: `export DB_PASSWORD=respond!qu1cklY` if not using default
- The helper script `./local_test.sh` runs the app with profile `local`, sets required secrets, and defaults to a random ephemeral port with the web tier disabled (avoids port conflicts in CI). To expose the web server:
  ```bash
  JWT_SECRET="this-is-a-very-secure-jwt-secret-key-that-is-at-least-512-bits-long-for-hs512-algorithm-security-requirement" \
  LOCAL_TEST_WEB=1 SERVER_PORT=8080 ./local_test.sh
  ```
- Start the app manually without tests: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`; the service listens on `http://localhost:8080/api`.
- To run from a jar instead: `./mvnw clean package -DskipTests` then `java -jar target/mrr-sb-0.0.1-SNAPSHOT.jar --spring.profiles.active=local`.

## Testing with Postman

### 1. Enable the Web Server

The test script `./local_test.sh` runs with `LOCAL_TEST_WEB=0` by default, which **disables the web server** (for headless testing). To test with Postman, you need to enable it with a secure JWT secret:

```bash
# Stop the current running script (Ctrl+C if running)
# Then run with web server enabled and proper JWT secret:
JWT_SECRET="this-is-a-very-secure-jwt-secret-key-that-is-at-least-512-bits-long-for-hs512-algorithm-security-requirement" \
LOCAL_TEST_WEB=1 SERVER_PORT=8080 ./local_test.sh
```

**Why the long JWT secret?** The HS512 algorithm requires a signing key of at least 512 bits (64 bytes) for security. A shorter secret will cause authentication to fail with a `WeakKeyException`.

### 2. API Endpoints

Base URL: `http://localhost:8080/api`

#### Authentication Endpoints (No JWT Required)

**POST** `/api/auth/login`
```json
{
  "username": "your_username",
  "password": "your_password"
}
```
Response:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "your_username",
  "name": "Full Name",
  "userId": 1,
  "expiresAt": 1234567890123
}
```

**POST** `/api/auth/refresh`
- Header: `Authorization: Bearer <token>`

**POST** `/api/auth/logout`
- Optional (JWT is stateless)

#### Message Endpoints (Require JWT)

**GET** `/api/messages?page=0&size=20`
- Query params: `page`, `size`, `after` (optional LocalDateTime)

**GET** `/api/messages/{id}`
- Returns specific message

**GET** `/api/messages/unread-count`
- Returns count of unread messages

**PUT** `/api/messages/{id}/mark-read`
- Mark message as read

#### User Endpoints (Require JWT)

**GET** `/api/users/profile`
- Get current user profile

**PUT** `/api/users/profile`
- Update user profile

#### Health/Monitoring (No JWT Required)

**GET** `/api/actuator/health`
**GET** `/api/actuator/info`
**GET** `/api/actuator/metrics`

### 3. Postman Collection Setup

#### Environment Variables
Create a Postman environment with:
- `baseUrl`: `http://localhost:8080/api`
- `token`: (will be set after login)

#### Login Request Setup
1. Create POST request to `{{baseUrl}}/auth/login`
2. Body (raw JSON):
   ```json
   {
     "username": "your_username",
     "password": "your_password"
   }
   ```
3. Add to Tests tab to auto-save token:
   ```javascript
   pm.environment.set("token", pm.response.json().token);
   ```

#### Authenticated Requests
For all protected endpoints, add to Headers:
- Key: `Authorization`
- Value: `Bearer {{token}}`
- Key: `Content-Type`
- Value: `application/json`

### 4. Swagger UI Alternative

Once the server is running with web enabled, access interactive API docs:
```
http://localhost:8080/api/swagger-ui.html
```

This provides:
- All endpoint documentation
- Try-it-out functionality
- Request/response schemas
- Authentication setup (click "Authorize" button to set JWT)

### 5. WebSocket Testing

For WebSocket connections (STOMP over SockJS):
- **Endpoint**: `ws://localhost:8080/ws`
- **Protocol**: STOMP
- **Authentication**: Pass JWT in `Authorization` header during CONNECT
- **Subscribe**: `/user/queue/messages` for personal messages
- **Subscribe**: `/topic/alerts` for broadcast alerts
- **Heartbeat**: `/topic/heartbeat` every 30 seconds

Tools for WebSocket testing:
- Postman (supports WebSocket)
- Browser console with SockJS + STOMP.js
- Command-line tools like `wscat`

## Testing with curl

### Quick Test Commands

**1. Login and get JWT token:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"team1.member1","password":"12345"}'
```

**2. Available test users:**
All users have password: `12345`
- `team1.member1` - Team 1 Member 1 (userId: 1)
- `team1.member2` - Team 1 Member 2 (userId: 2)
- `team1.member3` - Team 1 Member 3 (userId: 3)
- `team2.member1` - Team 2 Member 1 (userId: 4)
- `team2.member2` - Team 2 Member 2 (userId: 5)

**3. Test with authentication:**
```bash
# Get token and save to variable
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"team1.member1","password":"12345"}' | jq -r '.token')

# Use token for authenticated request
curl -X GET http://localhost:8080/api/messages \
  -H "Authorization: Bearer $TOKEN"
```

**4. Health check (no auth required):**
```bash
curl -s http://localhost:8080/api/actuator/health | jq .
```

## Tips

- If Postman runs from a different origin, update `cors.allowed-origins` in `application-local.properties` before restarting.
- Use `expiresAt` (epoch millis) from the login response to decide when to re-authenticate during manual testing.
- To inspect users quickly, run `select id, username, password, active from person where username is not null limit 5;` in your `mrr` database.
- JWT tokens expire after 24 hours by default (configured in `jwt.expiration=86400000`)
- The database is read-only, so no POST/PUT/DELETE operations will modify data (except for planned future features)
- Timestamps in responses are adjusted from database UTC+8 to actual UTC time
- **JWT Secret Requirement:** The application requires a JWT secret key of at least 512 bits (64 bytes) for the HS512 algorithm. Set via `JWT_SECRET` environment variable.
