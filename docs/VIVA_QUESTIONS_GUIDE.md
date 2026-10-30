# PharmaTrack – Comprehensive Academic Viva & Technical Interview Guide

> **Prepared for 3rd-Year B.Tech Computer Science & Information Technology Students**  
> Covers software design, Spring Boot 3, Spring Security 6, Hibernate/JPA, MySQL normalization, and pharmaceutical algorithms.

---

## Table of Contents
1. [Part 1: Project Overview & Core Business Logic](#part-1-project-overview--core-business-logic)
2. [Part 2: Spring Boot Architecture & Backend Implementation](#part-2-spring-boot-architecture--backend-implementation)
3. [Part 3: Spring Security, JWT & Authentication](#part-3-spring-security-jwt--authentication)
4. [Part 4: Database Design, JPA & Hibernate](#part-4-database-design-jpa--hibernate)
5. [Part 5: Concurrency, Transactions & Advanced Viva Scenarios](#part-5-concurrency-transactions--advanced-viva-scenarios)

---

## Part 1: Project Overview & Core Business Logic

### Q1: What is PharmaTrack, and what specific problem does it solve?
**Answer:**  
PharmaTrack is an enterprise-grade pharmacy inventory, expiry management, and customer medicine ordering system. Traditional pharmacy systems often track inventory as a single bulk quantity per medicine without batch differentiation. This leads to dispensing expired drugs, undetected wastage, and stockouts. PharmaTrack solves this by implementing **batch-level inventory tracking**, **First-Expired, First-Out (FEFO)** automated stock allocation, **real-time expiry risk isolation**, and a **prescription verification workflow** before order fulfillment.

---

### Q2: Why did you choose FEFO (First-Expired, First-Out) instead of FIFO (First-In, First-Out)?
**Answer:**  
In standard retail, FIFO assumes the first items delivered should be sold first. However, in pharmaceuticals, supplier delivery dates do not always align with expiry dates (e.g., a batch delivered today might have an earlier expiry date than a batch delivered last month due to different production runs).  
**FEFO** sorts active, unexpired batches strictly by `expiry_date ASC`. It automatically allocates units from the batch that will expire soonest, minimizing drug spoilage and financial loss while guaranteeing that patients never receive expired medications.

---

### Q3: What happens when an order is cancelled or rejected?
**Answer:**  
The system executes a safe rollback workflow:
1. The allocated batch ID and quantity are retrieved from each `OrderItem`.
2. The batch's `available_quantity` is incremented by the cancelled count.
3. If the batch was previously marked `DEPLETED`, its status is updated back to `ACTIVE`.
4. An immutable audit record is saved in `stock_movements` with `movement_type = 'RELEASED'` and a mandatory justification note.
5. All operations run inside an atomic `@Transactional` boundary.

---

### Q4: How does the system handle prescription-required medicines?
**Answer:**  
Medicines flagged with `prescription_required = true` require doctor details during checkout. When placed, the order enters `PENDING` status. The system prohibits progression to `CONFIRMED` or `PROCESSING` until a licensed pharmacist reviews the prescription and enters their verification signature and license ID into the system.

---

## Part 2: Spring Boot Architecture & Backend Implementation

### Q5: Explain the layered architecture of your Spring Boot backend.
**Answer:**  
PharmaTrack implements a clean, decoupled 4-tier architecture:
1. **Presentation / Controller Layer (`@RestController`)**: Handles HTTP requests, extracts parameters, validates incoming DTOs using `@Valid`, and returns uniform `ApiResponse<T>` objects.
2. **Service / Business Logic Layer (`@Service`)**: Encapsulates all domain logic, transaction boundaries (`@Transactional`), FEFO calculations, and security authorization checks.
3. **Data Access Layer (`@Repository`)**: Extends Spring Data JPA `JpaRepository` to perform CRUD and custom JPQL/native queries.
4. **Entity / Domain Layer (`@Entity`)**: Plain Old Java Objects (POJOs) mapped to MySQL database tables using Hibernate annotations.

---

### Q6: What is the purpose of `@SpringBootApplication`?
**Answer:**  
`@SpringBootApplication` is a convenience meta-annotation that bundles three key annotations:
1. `@Configuration`: Tags the class as a source of bean definitions for the application context.
2. `@EnableAutoConfiguration`: Tells Spring Boot to configure beans based on classpath dependencies (e.g., configuring `DataSource` when MySQL Connector is detected).
3. `@ComponentScan`: Tells Spring to scan the current package and its sub-packages for `@Component`, `@Service`, `@Repository`, and `@RestController` classes.

---

### Q7: Why use DTOs (Data Transfer Objects) instead of returning JPA entities directly to the UI?
**Answer:**  
1. **Security & Data Hiding**: Prevents exposing internal fields like `passwordHash` or audit metadata.
2. **Decoupling**: The API contract remains stable even if internal database tables are refactored.
3. **Preventing Infinite Recursion**: Bidirectional JPA relationships (e.g., `@OneToMany` and `@ManyToOne`) can trigger infinite circular serialization loops with JSON serializers like Jackson.
4. **Performance**: Transmits only necessary fields over the network.

---

### Q8: What does the `@Transactional` annotation do in your checkout flow?
**Answer:**  
`@Transactional` manages the database transaction boundary. In `OrderService.checkout()`, multiple operations occur: validating stock, deducting quantities across batches, updating batch statuses, inserting the order, inserting order items, and creating stock movement logs.  
If any step fails (e.g., database connection drops or stock suddenly becomes insufficient), Spring automatically triggers a **rollback**, ensuring the database is not left in an inconsistent state (satisfying the **Atomicity** property of ACID).

---

## Part 3: Spring Security, JWT & Authentication

### Q9: Why did you use JWT (JSON Web Tokens) instead of traditional HTTP Sessions?
**Answer:**  
1. **Statelessness**: The backend server does not need to store session IDs in memory. The token carries the user's identity and claims.
2. **Scalability**: Stateless JWTs allow horizontal scaling across multiple servers behind a load balancer without needing sticky sessions or distributed session caches like Redis.
3. **Decoupled Architecture**: Frontends running on different ports or mobile clients can consume the REST API using a standard `Authorization: Bearer <token>` header.

---

### Q10: How does your `JwtAuthenticationFilter` work internally?
**Answer:**  
1. For every incoming HTTP request, the filter intercepts the request before it reaches the controller (`OncePerRequestFilter`).
2. It parses the `Authorization` header looking for the `Bearer ` prefix.
3. It passes the token string to `JwtTokenProvider.validateToken()`, which verifies the HMAC-SHA256 signature using the secret key.
4. If valid, it extracts the username and role claims, constructs a `UsernamePasswordAuthenticationToken`, and injects it into Spring's `SecurityContextHolder`.
5. Spring Security uses this context to enforce role-based access rules configured in `SecurityFilterChain`.

---

### Q11: How are passwords stored and validated securely?
**Answer:**  
Passwords are never stored in plaintext. We use `BCryptPasswordEncoder`.  
- **Salting**: BCrypt generates a random 16-byte salt for each password, defending against Rainbow Table attacks.
- **Adaptive Hashing**: We configure a work factor of 10 ($2^{10} = 1024$ rounds), making brute-force dictionary attacks computationally prohibitive.
- During login, `BCrypt.checkpw()` hashes the candidate password with the salt embedded in the stored hash and compares the results in constant time.

---

## Part 4: Database Design, JPA & Hibernate

### Q12: Explain the normalization of your database. Is it in 3NF?
**Answer:**  
Yes, the `pharmatrack` schema satisfies **Third Normal Form (3NF)**:
- **1NF**: Every table cell contains single, atomic values, and each table has a primary key.
- **2NF**: It is in 1NF and all non-key attributes are fully functionally dependent on the primary key (no partial dependencies on composite keys).
- **3NF**: It is in 2NF and there are no transitive functional dependencies. For instance, drug category details are factored into `categories`, supplier details into `suppliers`, and individual batch lifecycles into `medicine_batches`.

---

### Q13: What is the difference between `@ManyToOne` and `@OneToMany` in your entities?
**Answer:**  
They define the two sides of a bidirectional relationship:
- In `Medicine`: `@OneToMany(mappedBy = "medicine")` indicates that one medicine can have multiple inventory batches.
- In `MedicineBatch`: `@ManyToOne @JoinColumn(name = "medicine_id")` represents the owning side containing the actual foreign key column in the MySQL table.

---

### Q14: What is the Hibernate N+1 query problem, and how do you prevent it?
**Answer:**  
The N+1 problem occurs when querying parent entities (e.g., 10 orders), and Hibernate subsequently executes 1 additional query for each parent to lazily fetch its children (e.g., 10 additional queries for order items).  
In PharmaTrack, we prevent this by:
1. Using **DTO projections** for lightweight queries.
2. Using `JOIN FETCH` queries in custom repository methods when child collections are required.

---

### Q15: Why did you create an immutable `stock_movements` table?
**Answer:**  
In financial and pharmaceutical industries, knowing the current stock count is insufficient; compliance mandates knowing *how*, *when*, *why*, and *by whom* the stock count was changed.  
The `stock_movements` table acts as an append-only transaction ledger recording the before/after balance, quantity delta, movement type (`PURCHASE_IN`, `DISPATCH_OUT`, `DAMAGE_LOSS`, etc.), and user ID.

---

## Part 5: Concurrency, Transactions & Advanced Viva Scenarios

### Q16: How would you prevent overselling if two customers purchase the last available units simultaneously?
**Answer:**  
In our design:
1. **Transactional Boundary**: Both checkouts execute inside `@Transactional(isolation = Isolation.READ_COMMITTED)`.
2. **Atomic Verification**: In the checkout method, we re-query available stock immediately prior to batch deduction.
3. If Customer A's transaction commits first, Customer B's query will see the updated lower balance, triggering an `InsufficientStockException` and cleanly rolling back Customer B's order.
4. In high-concurrency production deployments, we can apply **Pessimistic Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on the `MedicineBatch` record during the FEFO loop.

---

### Q17: Why did you choose soft deletion (`is_active = false`) for medicines instead of `DELETE FROM medicines`?
**Answer:**  
Hard-deleting a medicine record would cause **referential integrity violations** with past `order_items`, `purchase_orders`, and `stock_movements`. Furthermore, deleting past transaction history violates medical regulatory compliance.  
By toggling `is_active = false`, the drug is hidden from the customer search catalog while preserving all historical order records and audit logs.

---

### Q18: What are the three roles in PharmaTrack and their authorization boundaries?
**Answer:**  
1. **`ROLE_ADMIN`**: Full system control. Can add/deactivate medicines, create categories, manage suppliers, issue purchase orders, configure stock thresholds, and view financial valuation reports.
2. **`ROLE_STAFF`**: Pharmacy operations desk. Can view stock alerts, adjust batch stock with mandatory reason codes, verify doctor prescriptions, and mark customer orders as ready for dispatch.
3. **`ROLE_CUSTOMER`**: Public facing. Can search medicines, manage cart, place orders, view order history, and cancel eligible pending orders. Cannot access administrative endpoints.

---

### Q19: How are near-expiry alerts determined?
**Answer:**  
The system uses a configurable threshold (default: 30 days). The repository method `findNearExpiryBatches()` runs:
```sql
SELECT b FROM MedicineBatch b 
WHERE b.status = 'ACTIVE' 
  AND b.availableQuantity > 0 
  AND b.expiryDate > CURRENT_DATE 
  AND b.expiryDate <= :nearExpiryDate 
ORDER BY b.expiryDate ASC
```
This isolates stock that must be prioritized for liquidation or supplier return before expiration.

---

### Q20: How does the CSV export functionality work without third-party heavy reporting libraries?
**Answer:**  
`ReportService.exportReportCsv(reportType)` retrieves the queried dataset from the repository and constructs an RFC 4180-compliant comma-separated string stream using Java `StringBuilder`. The controller returns this as a `byte[]` with HTTP headers:
- `Content-Type: text/csv`
- `Content-Disposition: attachment; filename="report-*.csv"`  
This enables zero-overhead client-side downloading directly in the browser.
