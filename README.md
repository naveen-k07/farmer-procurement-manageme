# Farmer Procurement Management System — Java Backend

## Requirements
- JDK 17 or later
- Maven 3.6.3+ (or use the Maven wrapper if you add one)
- Internet on first build so Maven can download dependencies

## Run in VS Code
1. Extract this ZIP.
2. Open the `farmer-procurement-backend` folder in VS Code.
3. In the terminal, run: `mvn spring-boot:run`
4. Test: `http://localhost:8080/api/health`

If `mvn` is not recognised, install Maven or generate/download a Maven wrapper. You can also import this `pom.xml` project in an IDE that supports Maven.

## API endpoints
- `GET /api/health`
- `POST /api/farmers`
- `GET /api/farmers`
- `GET /api/farmers/{id}`
- `GET /api/slots`
- `POST /api/slots` (staff creates a slot)
- `POST /api/bookings`
- `GET /api/bookings`
- `GET /api/bookings/{bookingId}`
- `POST /api/bookings/{bookingId}/arrive`
- `GET /api/queue`
- `POST /api/queue/next`
- `PATCH /api/bookings/{bookingId}/procurement`

## Example requests

### Register farmer
POST `http://localhost:8080/api/farmers`
```json
{
  "name": "Kumar",
  "mobile": "9876543210",
  "village": "Thanjavur"
}
```

### Book a slot
First call `GET /api/slots` and note a slot `id`. Use the farmer `id` returned by registration.
POST `http://localhost:8080/api/bookings`
```json
{
  "farmerId": 1,
  "slotId": 1
}
```

### Mark arrival
POST `/api/bookings/FPM-XXXXXXXX/arrive` using the returned booking ID.

### Serve next farmer
POST `/api/queue/next`

### Record procurement and payment status
PATCH `/api/bookings/FPM-XXXXXXXX/procurement`
```json
{
  "quantityKg": 725.5,
  "procurementStatus": "COMPLETED",
  "paymentStatus": "PENDING"
}
```

## Notes
- Three demo paddy slots are seeded for tomorrow if no slots exist.
- H2 stores data in `./data/procurementdb`.
- CORS is open for hackathon integration. Restrict origins before production.
- This is a hackathon prototype, not production-ready. Capacity checking is suitable for a single-instance demo but needs database locking/transactional safeguards for simultaneous production bookings. There is no authentication or real SMS/payment integration.
