# Saved Shops — Backend Documentation

## Overview

Car owners can bookmark repair shops to their account. Saved shops appear on the `/account/saved` page in the frontend. This feature uses a `user_saved_shops` join table that links a user to a shop with a timestamp.

---

## API Endpoints

Base path: `/api/v1/users/{userId}/saved-shops`

All endpoints require a valid JWT token: `Authorization: Bearer <token>`

---

### GET `/api/v1/users/{userId}/saved-shops`

Returns saved shops for a user, paginated. Sorted by `savedAt` descending (newest first).

**Query params:**

| Param | Default | Description |
|-------|---------|-------------|
| `page` | `0` | Page index (zero-based) |
| `size` | `10` | Number of items per page |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "shopId": 10,
      "name": "AutoExpert Tallinn",
      "city": "Tallinn",
      "county": "Harju",
      "phone": "+372 555 1234",
      "status": "active",
      "savedAt": "2026-04-19T14:22:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 25,
  "totalPages": 3
}
```

Returns an empty `content` array if the user has no saved shops.

---

### POST `/api/v1/users/{userId}/saved-shops/{shopId}`

Save a shop to the user's bookmarks.

**Response `201 Created`:**
```json
{
  "shopId": 10,
  "name": "AutoExpert Tallinn",
  "city": "Tallinn",
  "county": "Harju",
  "phone": "+372 555 1234",
  "status": "active",
  "savedAt": "2026-04-19T14:22:00"
}
```

**Error responses:**
| Status | Reason |
|--------|--------|
| 404 | Shop not found |
| 409 | Shop already saved by this user |

---

### DELETE `/api/v1/users/{userId}/saved-shops/{shopId}`

Remove a shop from the user's bookmarks.

**Response `204 No Content`**

**Error responses:**
| Status | Reason |
|--------|--------|
| 404 | This shop was not in the user's saved list |

---

## Database

Migration `V4__add_user_saved_shops.sql` adds:

```sql
CREATE TABLE user_saved_shops (
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT NOT NULL REFERENCES users(id),
    shop_id  BIGINT NOT NULL REFERENCES repair_shops(id),
    saved_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, shop_id)
);
```

The `UNIQUE(user_id, shop_id)` constraint prevents duplicate saves at the database level.

---

## Error Response Format

Same structure as the rest of the API:

```json
{
  "timestamp": "2026-04-19T14:22:00Z",
  "status": 409,
  "message": "Shop already saved",
  "path": "/api/v1/users/5/saved-shops/10"
}
```

---

## Running Tests

From the `backend/` directory:

```bash
# Run saved shops tests only
./mvnw test -Dtest="UserSavedShopServiceTest,UserSavedShopControllerTest"

# Run all tests
./mvnw test
```

**16 tests total:**
- `UserSavedShopServiceTest` (8) — getSaved with pagination, save, unsave including error cases
- `UserSavedShopControllerTest` (8) — HTTP status codes, response bodies, custom page/size params

---

## Postman — Saved Shops Flow

Requires a logged-in car owner token in `{{token}}`.

**1. Save a shop:**
```http
POST {{baseUrl}}/api/v1/users/1/saved-shops/10
Authorization: Bearer {{token}}
```

**2. List saved shops (first page):**
```http
GET {{baseUrl}}/api/v1/users/1/saved-shops
Authorization: Bearer {{token}}
```

**2a. List saved shops (custom page/size):**
```http
GET {{baseUrl}}/api/v1/users/1/saved-shops?page=1&size=5
Authorization: Bearer {{token}}
```

**3. Remove a saved shop:**
```http
DELETE {{baseUrl}}/api/v1/users/1/saved-shops/10
Authorization: Bearer {{token}}
```
