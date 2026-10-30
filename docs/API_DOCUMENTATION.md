# PharmaTrack – RESTful API Reference Manual

This document provides complete documentation for the REST endpoints exposed by the PharmaTrack Spring Boot backend.

- **Base URL**: `http://localhost:8080/api`
- **Request/Response Format**: `application/json`
- **Security Scheme**: HTTP Authorization Header with Bearer Token (`Authorization: Bearer <jwt_token>`)

---

## 1. Authentication Endpoints (`/api/auth`)

### 1.1 Register New Account
- **Endpoint**: `POST /api/auth/register`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "Password@123",
  "fullName": "John Doe",
  "phone": "+1-555-0199",
  "address": "123 Maple Street, Springfield",
  "role": "ROLE_CUSTOMER"
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "type": "Bearer",
    "username": "johndoe",
    "fullName": "John Doe",
    "role": "ROLE_CUSTOMER"
  }
}
```

### 1.2 User Login
- **Endpoint**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "admin",
  "password": "adminpassword"
}
```
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "type": "Bearer",
    "username": "admin",
    "fullName": "Chief Pharmacist Administrator",
    "role": "ROLE_ADMIN"
  }
}
```

### 1.3 Get Current User Profile
- **Endpoint**: `GET /api/auth/profile`
- **Access**: Authenticated (`ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_CUSTOMER`)
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": 1,
    "username": "admin",
    "email": "admin@pharmatrack.com",
    "fullName": "Chief Pharmacist Administrator",
    "role": "ROLE_ADMIN",
    "phone": "+1-800-555-0100"
  }
}
```

---

## 2. Medicine & Catalog Endpoints (`/api/medicines`)

### 2.1 Search & Filter Medicines
- **Endpoint**: `GET /api/medicines`
- **Access**: Public
- **Query Parameters**:
  - `keyword` *(optional)*: Partial case-insensitive drug name search (e.g. `amox`).
  - `categoryId` *(optional)*: Filter by therapeutic category ID.
  - `inStockOnly` *(optional)*: Boolean (`true`/`false`) to show only drugs with unexpired stock $> 0$.
- **Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Amoxicillin 500mg Capsules",
      "genericName": "Amoxicillin Trihydrate",
      "brandName": "Amoxil",
      "manufacturer": "GlaxoSmithKline",
      "categoryId": 1,
      "categoryName": "Antibiotics",
      "price": 12.50,
      "dosageForm": "Capsule",
      "strength": "500mg",
      "prescriptionRequired": true,
      "totalAvailableStock": 420,
      "availabilityStatus": "IN_STOCK",
      "storageConditions": "Store below 25°C in a dry place"
    }
  ]
}
```

### 2.2 Get Medicine By ID
- **Endpoint**: `GET /api/medicines/{id}`
- **Access**: Public
- **Response (200 OK)**: Medicine details including category and stock availability.

### 2.3 Create Medicine
- **Endpoint**: `POST /api/medicines`
- **Access**: Restricted to `ROLE_ADMIN`
- **Request Body**:
```json
{
  "name": "Paracetamol 650mg Tablets",
  "genericName": "Acetaminophen",
  "brandName": "Dolo-650",
  "manufacturer": "Micro Labs Ltd",
  "categoryId": 2,
  "price": 3.20,
  "dosageForm": "Tablet",
  "strength": "650mg",
  "minimumStockLevel": 50,
  "prescriptionRequired": false,
  "storageConditions": "Room Temperature (15-25°C)"
}
```

### 2.4 Categories Management
- `GET /api/medicines/categories`: List all drug categories.
- `POST /api/medicines/categories`: Create a new category (`ROLE_ADMIN` only).

---

## 3. Inventory & Expiry Endpoints (`/api/inventory`)

### 3.1 List All Batches
- **Endpoint**: `GET /api/inventory/batches`
- **Access**: `ROLE_ADMIN`, `ROLE_STAFF`
- **Query Parameters**: `medicineId` *(optional)*, `status` *(optional)*.
- **Response (200 OK)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "batchNumber": "AMX-2026-001",
      "medicineId": 1,
      "medicineName": "Amoxicillin 500mg Capsules",
      "supplierName": "Apex Healthcare Logistics",
      "quantityReceived": 250,
      "availableQuantity": 170,
      "purchaseCost": 6.80,
      "manufacturingDate": "2026-01-10",
      "expiryDate": "2027-01-10",
      "status": "ACTIVE",
      "daysToExpiry": 75
    }
  ]
}
```

### 3.2 Record Batch Stock Adjustment
- **Endpoint**: `POST /api/inventory/batches/{id}/adjust`
- **Access**: `ROLE_ADMIN`, `ROLE_STAFF`
- **Description**: Adjusts batch quantity with mandatory audit reasoning (e.g., damaged items or count correction).
- **Request Body**:
```json
{
  "newQuantity": 165,
  "movementType": "DAMAGE_LOSS",
  "reason": "Water damage to carton packaging during warehouse transfer"
}
```
- **Response (200 OK)**:
```json
{
  "success": true,
  "message": "Stock adjusted successfully and audit record logged",
  "data": {
    "batchNumber": "AMX-2026-001",
    "previousQuantity": 170,
    "newQuantity": 165,
    "delta": -5
  }
}
```

### 3.3 Expiry Alerts
- `GET /api/inventory/alerts/near-expiry`: Batches with `daysToExpiry <= 30` and unexpired.
- `GET /api/inventory/alerts/expired`: Batches where `expiryDate < CURRENT_DATE()`.
- `GET /api/inventory/alerts/low-stock`: Medicines where eligible stock $\le$ `minimumStockLevel`.

---

## 4. Customer Order & Checkout Endpoints (`/api/orders`)

### 4.1 Place Order (Checkout)
- **Endpoint**: `POST /api/orders`
- **Access**: `ROLE_CUSTOMER`
- **Description**: Validates stock, executes **FEFO batch allocation**, locks units, and persists order.
- **Request Body**:
```json
{
  "deliveryAddress": "42 Baker Street, Apt 3B, New York, NY",
  "contactPhone": "+1-555-0144",
  "paymentMethod": "CASH_ON_DELIVERY",
  "prescriptionNotes": "Dr. House, NY Memorial, Rx ID: #RX-8841",
  "items": [
    {
      "medicineId": 1,
      "quantity": 2
    },
    {
      "medicineId": 4,
      "quantity": 1
    }
  ]
}
```
- **Response (201 Created)**:
```json
{
  "success": true,
  "message": "Order placed successfully",
  "data": {
    "id": 10,
    "orderNumber": "ORD-20261027-8921",
    "orderDate": "2026-10-27T23:45:00",
    "status": "PENDING",
    "subtotal": 33.70,
    "shippingFee": 5.00,
    "totalAmount": 38.70,
    "items": [
      {
        "medicineName": "Amoxicillin 500mg Capsules",
        "batchNumber": "AMX-2026-001",
        "quantity": 2,
        "unitPrice": 12.50,
        "subtotal": 25.00
      }
    ]
  }
}
```

### 4.2 Customer Cancel Order
- **Endpoint**: `POST /api/orders/{id}/cancel`
- **Access**: Customer who placed the order.
- **Description**: Only permitted if status is `PENDING` or `CONFIRMED`. Automatically restores deducted batch stock and records `RELEASED` audit logs.

### 4.3 Admin Order Management & Prescription Verification
- **Endpoint**: `PUT /api/orders/{id}/status`
- **Access**: `ROLE_ADMIN`, `ROLE_STAFF`
- **Request Body**:
```json
{
  "newStatus": "CONFIRMED",
  "prescriptionVerifiedBy": "Sarah Jenkins, R.Ph. (License #PH-9921)",
  "memo": "Prescription verified with NY Memorial Hospital database"
}
```

---

## 5. Restocking & Supplier Endpoints (`/api/suppliers`)

### 5.1 Create Restocking Purchase Order
- **Endpoint**: `POST /api/suppliers/purchase-orders`
- **Access**: `ROLE_ADMIN`
- **Request Body**:
```json
{
  "supplierId": 1,
  "notes": "Emergency stock replenishment",
  "items": [
    {
      "medicineId": 2,
      "expectedBatchNumber": "AZM-2026-R4",
      "quantityOrdered": 200,
      "unitCost": 12.00,
      "manufacturingDate": "2026-05-01",
      "expiryDate": "2028-05-01"
    }
  ]
}
```

### 5.2 Receive Purchase Order (Stock Inflow)
- **Endpoint**: `PUT /api/suppliers/purchase-orders/{id}/receive`
- **Access**: `ROLE_ADMIN`, `ROLE_STAFF`
- **Description**: Marks the PO as `RECEIVED`, creates new batch records, updates warehouse stock, and logs `PURCHASE_IN` movements.

---

## 6. Reports & Analytics Endpoints (`/api/reports`)

- `GET /api/reports/summary`: Financial valuation (Total Retail Value, Procurement Inventory Cost, Potential Expiry Loss).
- `GET /api/reports/low-stock`: Comprehensive low stock diagnostic table.
- `GET /api/reports/near-expiry`: Batches expiring in $\le 30$ days.
- `GET /api/reports/expired-loss`: Expired batches with calculated procurement loss.
- `GET /api/reports/stock-movements`: Full chronologically sorted audit trail.
- `GET /api/reports/export/csv?type={inventory|low-stock|near-expiry|expired|movements}`: Generates downloadable RFC 4180-compliant CSV files.
