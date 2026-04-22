# User Preferences — Backend Documentation

## Overview

Car owners can save personal search preferences to their profile: a preferred city, a default search radius, and a preferred parts quality tier. These preferences are stored on the user record and returned whenever the profile is fetched. The frontend uses them to pre-fill search defaults so the user does not have to repeat themselves every search.

---

## Parts Quality Values

The `partsQualityPreference` field is constrained to the following values:

| Value | Meaning |
|-------|---------|
| `oem` | Original Equipment Manufacturer — exact parts the car was built with |
| `oem_equivalent` | Same quality as OEM but made by a third party (cheaper) |
| `budget` | Cheapest option, lower quality and shorter lifespan |
| `not_sure` | User has no preference, leaves it to the shop |

---

## API Endpoints

Base path: `/api/v1/users`

All endpoints require a valid JWT token: `Authorization: Bearer <token>`

---

### GET `/api/v1/users/{id}`

Returns the user profile including preferences.

**Response `200 OK`:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "role": "car_owner",
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+372 555 9999",
  "preferredCity": "Tallinn",
  "searchRadiusKm": 10,
  "partsQualityPreference": "oem_equivalent",
  "createdAt": "2026-04-01T10:00:00"
}
```

Preference fields are `null` if the user has not set them yet.

---

### PUT `/api/v1/users/{id}`

Update user profile and preferences. All fields are sent together.

**Request body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+372 555 9999",
  "preferredCity": "Tallinn",
  "searchRadiusKm": 10,
  "partsQualityPreference": "oem_equivalent"
}
```

**Field rules:**

| Field | Required | Constraints |
|-------|----------|-------------|
| `firstName` | Yes | Non-blank |
| `lastName` | Yes | Non-blank |
| `phone` | No | — |
| `preferredCity` | No | Max 100 characters |
| `searchRadiusKm` | No | Between 1 and 250 |
| `partsQualityPreference` | No | One of: `oem`, `oem_equivalent`, `budget`, `not_sure` |

**Response `200 OK`:** Same shape as GET above.

**Error responses:**

| Status | Reason |
|--------|--------|
| `400` | Validation failed (e.g. invalid `partsQualityPreference` value, `searchRadiusKm` out of range) |
| `404` | User not found |

---

## Database

Migration `V5__add_user_preferences.sql` adds three columns to the `users` table:

```sql
ALTER TABLE users
  ADD COLUMN preferred_city          VARCHAR(100),
  ADD COLUMN search_radius_km        INT DEFAULT 10,
  ADD COLUMN parts_quality_preference VARCHAR(20);
```

All three columns are nullable — existing users are unaffected.

---

## Running Tests

From the `backend/` directory:

```bash
# Run user service tests only
./mvnw test -Dtest="UserServiceTest"

# Run all tests
./mvnw test
```

---

## Postman — Update Preferences Flow

Requires a logged-in car owner token in `{{token}}`.

**1. Fetch current profile:**
```http
GET {{baseUrl}}/api/v1/users/1
Authorization: Bearer {{token}}
```

**2. Update preferences:**
```http
PUT {{baseUrl}}/api/v1/users/1
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+372 555 9999",
  "preferredCity": "Tallinn",
  "searchRadiusKm": 10,
  "partsQualityPreference": "oem_equivalent"
}
```

**3. Send an invalid value to verify validation:**
```http
PUT {{baseUrl}}/api/v1/users/1
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Smith",
  "partsQualityPreference": "oem-eq"
}
```
Expected: `400 Bad Request`
