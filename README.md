# PharmaTrack – Smart Pharmacy Inventory & Expiry Management System

> **A Complete, College-Ready Full-Stack Web Application for Academic Demonstration & Viva Evaluation**  
> Built with **Java 17+**, **Spring Boot 3.2.5**, **Spring Data JPA**, **Spring Security 6**, **MySQL 8.0**, and **Responsive Vanilla Web Frontend**.

---

## 📋 Table of Contents
1. [Project Overview](#-project-overview)
2. [Key Architectural Features](#-key-architectural-features)
3. [Technology Stack](#-technology-stack)
4. [Project Directory Structure](#-project-directory-structure)
5. [Prerequisites & System Requirements](#-prerequisites--system-requirements)
6. [Step-by-Step Local Setup & Execution](#-step-by-step-local-setup--execution)
7. [Pre-Seeded Demo User Credentials](#-pre-seeded-demo-user-credentials)
8. [Guided Viva Evaluation & Demonstration Walkthrough](#-guided-viva-evaluation--demonstration-walkthrough)
9. [Academic Documentation Directory](#-academic-documentation-directory)

---

## 🏥 Project Overview

**PharmaTrack** is an enterprise-grade pharmacy management application designed to eliminate drug expiration wastage, prevent stockouts, enforce prescription medication safety, and automate pharmaceutical order fulfillment.

Unlike basic store demos that track stock as a single aggregate number, PharmaTrack implements **true batch-level inventory tracking**. Every physical delivery is tracked with its unique batch number, manufacturing date, and expiry date.

### Core Business Highlights:
- **FEFO (First-Expired, First-Out) Engine**: Customer orders automatically draw stock from the unexpired batch expiring soonest.
- **Expiry Risk Quarantine**: Expired batches are automatically blocked from catalog searches and order allocation.
- **Prescription Gatekeeper**: Prescription drugs require pharmacist license verification before order fulfillment.
- **Immutable Audit Trail**: Every stock change (order, purchase, disposal, damage, audit correction) is logged with before/after balances.
- **Dual-Mode Serving**: Can run as a unified single-port Spring Boot application (`http://localhost:8080/`) or with decoupled frontend servers.

---

## ⚡ Key Architectural Features

| Feature | Description |
|:---|:---|
| **Batch-Level Expiry Control** | Monitors individual batches with distinct expiry dates and procurement costs. |
| **FEFO Allocation Engine** | Prioritizes stock allocation by `expiry_date ASC` to minimize shelf spoilage. |
| **Near-Expiry Warning Desk** | Proactive alerts for batches expiring within 30 days. |
| **Low-Stock Detection** | Flags medicines whose unexpired count falls below minimum threshold. |
| **Prescription Safety Review** | Prevents order processing until a pharmacist enters their verification signature. |
| **Supplier PO Restocking** | Stock increments into inventory only when purchase orders are physically marked as `RECEIVED`. |
| **Audit Ledger (`stock_movements`)** | Mandatory reason codes for all stock adjustments (damage, count corrections, expired disposal). |
| **Financial Valuation Reports** | Real-time calculation of retail inventory value, wholesale acquisition cost, and expiry loss with CSV download. |

---

## 🛠 Technology Stack

### Backend
- **Language**: Java 17 or higher
- **Framework**: Spring Boot 3.2.5
- **Modules**:
  - `spring-boot-starter-web` (REST APIs, MVC static serving)
  - `spring-boot-starter-data-jpa` (Hibernate ORM, Repository abstraction)
  - `spring-boot-starter-security` (Role-based access control)
  - `spring-boot-starter-validation` (Bean validation with Hibernate Validator)
- **Authentication**: Stateless JSON Web Tokens (**JJWT** 0.11.5) & **BCrypt** password hashing
- **Build Tool**: Apache Maven 3.8+

### Database
- **Engine**: MySQL 8.0+
- **Driver**: `com.mysql:mysql-connector-j`
- **Fallback / Testing**: H2 In-Memory Database for automated unit/integration tests

### Frontend
- **Languages**: HTML5, CSS3, Vanilla JavaScript (ES6+)
- **Design System**: Responsive modern healthcare layout, Clean Inter typography, zero external bloated UI dependencies.

---

## 📁 Project Directory Structure

```text
pharmatrack/
│
├── backend/                                  # Spring Boot Application Root
│   ├── pom.xml                               # Maven project definition & dependencies
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/pharmatrack/
│   │   │   │   ├── PharmaTrackApplication.java # Spring Boot Main Class (@EnableScheduling)
│   │   │   │   ├── config/                   # SecurityConfig, JwtAuthenticationFilter, DataInitializer
│   │   │   │   ├── controller/               # 8 REST Controllers
│   │   │   │   ├── dto/                      # Request & Response Data Transfer Objects
│   │   │   │   ├── exception/                # GlobalExceptionHandler & Custom Exceptions
│   │   │   │   ├── model/                    # 11 JPA Entities & Enums
│   │   │   │   ├── repository/               # 10 Spring Data JPA Repositories
│   │   │   │   └── service/                  # Business Logic & FEFO Allocation Engine
│   │   │   └── resources/
│   │   │       ├── application.properties    # MySQL connection, JWT secrets, thresholds
│   │   │       └── static/                   # Mirrored UI files (served on http://localhost:8080)
│   │   └── test/                             # Unit tests (OrderServiceTest, InventoryServiceTest)
│   │
├── frontend/                                 # Standalone Frontend Source Files
│   ├── index.html                            # Customer Medicine Storefront
│   ├── login.html                            # Multi-Role Login Portal with 1-Click Evaluation Buttons
│   ├── cart.html                             # Customer Shopping Cart & Prescription Upload
│   ├── orders.html                           # Customer Order History & Tracking
│   ├── admin-dashboard.html                  # Admin Operational KPIs & Expiry Alerts
│   ├── admin-medicines.html                  # Medicine Master Catalog & Category Management
│   ├── admin-batches.html                    # Batch-Level Expiry Control & Stock Adjustments
│   ├── admin-suppliers.html                  # Wholesaler Directory & Restocking POs
│   ├── admin-orders.html                     # Order Fulfillment & Pharmacist Rx Verification Desk
│   ├── admin-reports.html                    # Valuation Reports & CSV Data Downloader
│   ├── staff-dashboard.html                  # Pharmacy Staff Desk
│   ├── css/style.css                         # Unified Healthcare Design System
│   └── js/                                   # 11 Modular JavaScript Controllers
│
├── database/
│   ├── schema.sql                            # Production MySQL DDL script with constraints & indexes
│   └── seed_data.sql                         # Standalone SQL insert script for demo data
│
├── docs/                                     # Comprehensive Academic Project Documentation
│   ├── ARCHITECTURE.md                       # Layered design, FEFO algorithm, Security pipeline
│   ├── DATABASE_ERD.md                       # Mermaid ER diagram, 3NF normalization, table dictionary
│   ├── API_DOCUMENTATION.md                  # Complete REST API reference with schemas
│   └── VIVA_QUESTIONS_GUIDE.md               # 20+ Curated viva questions & detailed answers
│
├── .gitignore                                # Git ignore file for Java/Maven/IDE artifacts
└── README.md                                 # Master project guide
```

---

## 💻 Prerequisites & System Requirements

1. **Java Development Kit (JDK)**: Version 17, 21, or newer.
2. **Apache Maven**: Version 3.8+ (or use standard IDE Maven integration).
3. **MySQL Server**: Version 8.0+ running on port 3306.
4. **Modern Web Browser**: Google Chrome, Mozilla Firefox, or Microsoft Edge.

---

## 🚀 Step-by-Step Local Setup & Execution

### Step 1: Create the MySQL Database
Open your MySQL client (MySQL Workbench, Command Line, or phpMyAdmin) and create the database:
```sql
CREATE DATABASE pharmatrack CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Configure Database Credentials
Open `backend/src/main/resources/application.properties` and verify your MySQL username and password:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pharmatrack?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```
*(Replace `your_mysql_password` with your local MySQL root password).*

### Step 3: Build & Launch the Spring Boot Server
Navigate to the `backend` folder in your terminal and execute:
```powershell
cd C:\Users\khush\.gemini\antigravity\scratch\pharmatrack\backend
mvn clean spring-boot:run
```

> **Automated Database Seeding**:  
> Upon the first startup, `DataInitializer.java` detects the empty database and automatically populates:
> - 3 System Users (`admin`, `staff`, `customer`) with encrypted BCrypt passwords.
> - 7 Clinical Therapeutic Categories.
> - 3 Pharmaceutical Wholesalers / Suppliers.
> - 16 Realistic Medicines with active stock, near-expiry batches, expired batches, and low-stock items.
> - Real audit logs in `stock_movements`.

### Step 4: Open the Application in Your Browser
Once you see `Started PharmaTrackApplication in X.XXX seconds`, open:
👉 **`http://localhost:8080/`**

---

## 🔑 Pre-Seeded Demo User Credentials

The login screen (`/login.html`) includes **1-Click Quick Demo Evaluation Buttons** that automatically populate credentials for immediate viva presentation:

| Role | Username | Password | Purpose / Capabilities |
|:---|:---|:---|:---|
| **Administrator** | `admin` | `adminpassword` | Complete access: Medicine catalog, batches, purchase orders, financial reports, user management. |
| **Pharmacy Staff** | `staff` | `staffpassword` | Operations desk: Real-time alerts, stock adjustments with audit logs, prescription review, order dispatch. |
| **Customer** | `customer` | `customerpassword` | Public storefront: Search & filter medicines, cart management, checkout with FEFO allocation, order cancellation. |

---

## 🎓 Guided Viva Evaluation & Demonstration Walkthrough

Follow this 5-minute walkthrough during your project demonstration:

### Demo 1: Customer Storefront & FEFO Checkout
1. Navigate to `http://localhost:8080/`.
2. Notice the live search bar, category pills (Antibiotics, Analgesics, etc.), and the "In-Stock Only" toggle.
3. Observe that expired batches are hidden and medicines with zero unexpired units show `Out of Stock`.
4. Click **Add to Cart** on "Amoxicillin 500mg" and "Paracetamol 650mg".
5. Go to the **Cart** (`/cart.html`), review line item totals, input delivery address, and click **Place Order**.
6. The system generates a tracking code (`ORD-YYYYMMDD-XXXX`) and allocates units strictly from the batch expiring soonest (FEFO).

### Demo 2: Admin Dashboard & Real-Time Alerts
1. Log in as **Administrator** (`admin` / `adminpassword`).
2. View the KPI cards: Total Active Medicines, Eligible Stock, Near-Expiry Batches, Expired Batches, and Orders to Fulfill.
3. Look at the alert banners warning about near-expiry drugs (within 30 days) and low-stock items.

### Demo 3: Batch Management & Stock Adjustment with Audit Trail
1. Click **Batches & Expiry (FEFO)** in the navigation.
2. View the batch breakdown with color-coded expiry countdowns.
3. Click **Adjust Stock** on any batch:
   - Enter a new count and select reason `DAMAGE_LOSS` or `AUDIT_CORRECTION`.
   - Enter mandatory justification: *"Crushed blister pack discovered during physical inventory"*.
   - Click Save. Notice how the stock updates immediately and an immutable record is posted to `stock_movements`.

### Demo 4: Order Fulfillment & Pharmacist Prescription Verification
1. Click **Order Fulfillment & Rx** in the navigation.
2. Click **Review & Process** on a pending customer order containing a prescription drug.
3. Notice the highlighted prescription memo.
4. Enter the Pharmacist Verification signature (e.g., `Sarah Jenkins, R.Ph. #PH-9921`).
5. Progress status to `CONFIRMED` $\rightarrow$ `PROCESSING` $\rightarrow$ `COMPLETED`.

### Demo 5: Supplier Restocking Workflow
1. Click **Suppliers & Restocking**.
2. Click **Create Purchase Order**: Select supplier, drug, quantity, and manufacturing/expiry dates.
3. Show that warehouse inventory **does not increase** when the PO is simply placed.
4. Click **Mark as Received**: The system automatically registers the incoming batch, adds stock, and logs a `PURCHASE_IN` movement.

### Demo 6: Financial Valuation & CSV Export
1. Click **Reports & CSV Export**.
2. Switch between **Inventory Valuation**, **Low-Stock Alert**, **Near-Expiry Risk**, **Expired Loss**, and **Stock Movement Audit**.
3. Click **Download CSV Report** to demonstrate instant client-side CSV generation.

---

## 📚 Academic Documentation Directory

Detailed technical reports for examiner review are available in the `docs/` folder:
- 🏛️ **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)**: Deep dive into the N-Tier architecture, Spring Security JWT filter chain, FEFO mathematical logic, and concurrency handling.
- 🗄️ **[docs/DATABASE_ERD.md](docs/DATABASE_ERD.md)**: Complete database dictionary, Mermaid ER diagram, 3NF normalization proofs, foreign key cascades, and indexing strategies.
- 🔌 **[docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)**: Full REST API specification with request/response schemas and HTTP status codes.
- 💡 **[docs/VIVA_QUESTIONS_GUIDE.md](docs/VIVA_QUESTIONS_GUIDE.md)**: 20+ essential academic viva questions and answers covering Spring Boot, JPA, BCrypt, transactions, and system design.
