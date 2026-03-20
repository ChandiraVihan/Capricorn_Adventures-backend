# Quick Test Reference Guide
**For Testing the Booking Reference & History Management Features**

---

## Prerequisites

1. **Start the application:**
   ```bash
   cd /workspaces/Capricorn_Adventures-backend
   mvn spring-boot:run
   ```

2. **Database seeding (automatic on startup):**
   - User A: `userA@example.com` / password: `password`
   - User B: `userB@example.com` / password: `password`
   - Pre-loaded bookings ready for testing

---

## Test Scenarios

### Step 0: Get JWT Tokens

**User A Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "userA@example.com",
    "password": "password"
  }'
```

**User B Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "userB@example.com",
    "password": "password"
  }'
```

Save the `accessToken` values as:
- `TOKEN_A` for User A
- `TOKEN_B` for User B

---

### API-TS-01: Generate Reference ID on Booking Creation

**Test:** POST /api/bookings generates unique Reference ID

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "checkInDate": "2026-04-15",
    "checkOutDate": "2026-04-20"
  }'
```

**Expected Result:**
- Status: 201 Created
- Response includes `referenceId` field (format: BK-XXXXXXXX)
- Example: `"referenceId": "BK-A3F8C2D1"`

---

### API-TS-02: Retrieve Booking by Reference ID

**Test:** GET /api/bookings/ref/{referenceId} returns booking

```bash
# Use a referenceId from test data or from the previous test
BOOKING_REF="BK-A3F8C2D1"

curl -X GET "http://localhost:8080/api/bookings/ref/$BOOKING_REF" \
  -H "Authorization: Bearer $TOKEN_A"
```

**Expected Result:**
- Status: 200 OK
- Returns complete booking object with all details
- Includes: id, referenceId, userId, roomId, dates, status

---

### API-TS-03: Invalid Reference ID Returns 404

**Test:** GET with non-existent referenceId returns 404

```bash
curl -X GET "http://localhost:8080/api/bookings/ref/BK-INVALID999" \
  -H "Authorization: Bearer $TOKEN_A"
```

**Expected Result:**
- Status: 404 Not Found
- Response body:
  ```json
  {
    "timestamp": "2026-03-04T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Booking not found"
  }
  ```

---

### API-TS-04: Link Booking to Authenticated User

**Test:** Booking created via POST is linked to current user (JWT)

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 2,
    "checkInDate": "2026-05-10",
    "checkOutDate": "2026-05-15"
  }'
```

**Verification:**
- Response includes `userId` field
- `userId` matches the authenticated user's UUID from User A
- Can be verified in database: SELECT * FROM bookings WHERE reference_id='BK-...';

---

### API-TS-05: Booking History with Date Filtering

**Test:** GET /api/bookings/history returns only authenticated user's bookings

```bash
# User A's bookings
curl -X GET "http://localhost:8080/api/bookings/history?startDate=2026-01-01&endDate=2026-12-31" \
  -H "Authorization: Bearer $TOKEN_A"
```

**Expected Result:**
- Status: 200 OK
- Returns array of bookings for User A only
- User A should have 3 existing bookings:
  - January 15-20
  - February 10-15
  - March 1-5

```bash
# User B's bookings (different user)
curl -X GET "http://localhost:8080/api/bookings/history?startDate=2026-01-01&endDate=2026-12-31" \
  -H "Authorization: Bearer $TOKEN_B"
```

**Expected Result:**
- Status: 200 OK
- Returns array with only User B's bookings
- User B should have 1 existing booking:
  - March 10-12

### Date Range Filtering Test:

```bash
# Only March bookings for User A
curl -X GET "http://localhost:8080/api/bookings/history?startDate=2026-03-01&endDate=2026-03-31" \
  -H "Authorization: Bearer $TOKEN_A"
```

**Expected Result:**
- Status: 200 OK
- Returns only 1 booking (March 1-5)
- Excludes January and February bookings

---

### API-TS-06: Reference ID Security Check

**Test:** Reference IDs are cryptographically secure (not sequential)

```bash
# Create first booking
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{"roomId": 3, "checkInDate": "2026-06-01", "checkOutDate": "2026-06-05"}' \
  | jq '.referenceId'
# Result: BK-X1Y2Z3A4

# Create second booking immediately after
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{"roomId": 3, "checkInDate": "2026-06-10", "checkOutDate": "2026-06-15"}' \
  | jq '.referenceId'
# Result: BK-P9Q8R7S6 (DIFFERENT from first!)
```

**Expected Result:**
- Reference IDs are completely different
- NOT sequential (e.g., not 1001 and 1002)
- NOT predictable
- Based on UUID random generation

---

### API-TS-07: BOLA/IDOR Prevention

**Test A:** User cannot access other user's booking by reference ID

```bash
# Get User A's booking reference ID
REF_ID=$(curl -s -X GET "http://localhost:8080/api/bookings/history?startDate=2026-01-01&endDate=2026-12-31" \
  -H "Authorization: Bearer $TOKEN_A" \
  | jq -r '.[0].referenceId')

echo "User A's booking ID: $REF_ID"

# User B tries to access User A's booking
curl -X GET "http://localhost:8080/api/bookings/ref/$REF_ID" \
  -H "Authorization: Bearer $TOKEN_B"
```

**Expected Result:**
- Status: 403 Forbidden (User B not authorized)
- Response body shows forbidden/unauthorized message
- User B cannot see User A's booking details

**Test B:** User A can still access their own bookings

```bash
# User A accesses their own booking - should work
curl -X GET "http://localhost:8080/api/bookings/ref/$REF_ID" \
  -H "Authorization: Bearer $TOKEN_A"
```

**Expected Result:**
- Status: 200 OK
- Returns full booking details
- User A can access their own booking

---

## Manual Database Verification

Connect to the database and run:

```sql
-- Verify reference IDs are generated and unique
SELECT id, reference_id, user_id, check_in_date, status 
FROM bookings 
ORDER BY id DESC;

-- Verify user associations
SELECT b.reference_id, b.user_id, u.email, b.check_in_date, b.check_out_date
FROM bookings b
JOIN users u ON b.user_id = u.id
ORDER BY b.id DESC;

-- Verify no duplicate reference IDs
SELECT reference_id, COUNT(*) as count
FROM bookings
GROUP BY reference_id
HAVING count > 1;
-- Should return empty result (no duplicates)
```

---

## Quick Test Script (Bash)

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

# Get tokens
echo "Getting tokens..."
TOKEN_A=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"userA@example.com","password":"password"}' \
  | jq -r '.accessToken')

TOKEN_B=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"userB@example.com","password":"password"}' \
  | jq -r '.accessToken')

echo "Token A: $TOKEN_A"
echo "Token B: $TOKEN_B"

# Test 1: Create booking
echo -e "\n=== Test 1: Create Booking ==="
RESPONSE=$(curl -s -X POST $BASE_URL/api/bookings \
  -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{"roomId":1,"checkInDate":"2026-07-01","checkOutDate":"2026-07-05"}')

BOOKING_ID=$(echo $RESPONSE | jq -r '.referenceId')
echo "Created booking with reference ID: $BOOKING_ID"

# Test 2: Retrieve by reference ID
echo -e "\n=== Test 2: Get Booking by Reference ID ==="
curl -s -X GET "$BASE_URL/api/bookings/ref/$BOOKING_ID" \
  -H "Authorization: Bearer $TOKEN_A" | jq '.'

# Test 3: Invalid reference
echo -e "\n=== Test 3: Invalid Reference ID (should be 404) ==="
curl -s -X GET "$BASE_URL/api/bookings/ref/INVALID" \
  -H "Authorization: Bearer $TOKEN_A" | jq '.'

# Test 4: History
echo -e "\n=== Test 4: User A's Booking History ==="
curl -s -X GET "$BASE_URL/api/bookings/history?startDate=2026-01-01&endDate=2026-12-31" \
  -H "Authorization: Bearer $TOKEN_A" | jq '.[] | {referenceId, checkInDate, checkOutDate}'

# Test 5: BOLA test - User B accessing User A's booking
echo -e "\n=== Test 5: BOLA Prevention (User B accessing User A's booking) ==="
curl -s -X GET "$BASE_URL/api/bookings/ref/$BOOKING_ID" \
  -H "Authorization: Bearer $TOKEN_B" | jq '.'

echo -e "\nAll tests completed!"
```

---

## Expected Test Results Summary

| Test | Expected Status | Expected Behavior |
|------|---|---|
| API-TS-01 | 201 Created | referenceId in response |
| API-TS-02 | 200 OK | Full booking returned |
| API-TS-03 | 404 Not Found | "Booking not found" message |
| API-TS-04 | 201 Created | userId matches authenticated user |
| API-TS-05 | 200 OK | Array of filtered bookings |
| API-TS-06 | 201 Created | Different random reference IDs |
| API-TS-07 (Part A) | 403 Forbidden | User B cannot access User A's bookings |
| API-TS-07 (Part B) | 200 OK | User A can access own bookings |

---

## Troubleshooting

**Issue:** "Booking not found" when trying to retrieve by reference ID in Test 2
- **Solution:** Wait for the response from Test 1 to ensure the booking was created successfully

**Issue:** "User not found" when creating a booking
- **Solution:** Ensure the JWT token is valid and represents an authenticated user

**Issue:** 401 Unauthorized errors
- **Solution:** The JWT token might have expired. Get a new one using the login endpoint

**Issue:** "Room is not available" error
- **Solution:** Try a different room ID or date range that hasn't been booked

---

## Rebuilding the Project

If you make changes to the code:

```bash
mvn clean compile
```

To run the application:

```bash
mvn spring-boot:run
```

To run tests:

```bash
mvn test
```

---

Generated: March 4, 2026
Status: Ready for QA Testing
