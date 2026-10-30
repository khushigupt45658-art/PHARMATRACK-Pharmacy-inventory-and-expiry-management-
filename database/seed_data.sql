-- ====================================================================
-- PharmaTrack: Sample Seed Data for MySQL
-- Passwords: BCrypt hashed for 'Admin@123', 'Staff@123', 'Customer@123'
-- ====================================================================

USE `pharmatrack`;

-- Disable Foreign Key checks temporarily for clean loading
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `order_items`;
TRUNCATE TABLE `customer_orders`;
TRUNCATE TABLE `stock_movements`;
TRUNCATE TABLE `medicine_batches`;
TRUNCATE TABLE `medicines`;
TRUNCATE TABLE `categories`;
TRUNCATE TABLE `suppliers`;
TRUNCATE TABLE `users`;
TRUNCATE TABLE `notifications`;
SET FOREIGN_KEY_CHECKS = 1;

-- Users (BCrypt $2a$10$wN9aE9G...)
INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`, `phone_number`, `role`, `active`, `created_at`) VALUES
(1, 'admin', 'admin@pharmatrack.com', '$2a$10$j80z4g.Ece5wE8jCkgG9KusH5w.f0tL/iXoU9vBqS3zV0rYyF/6Xy', 'Dr. John Administrator', '+1 (555) 019-2831', 'ROLE_ADMIN', 1, NOW()),
(2, 'staff', 'staff@pharmatrack.com', '$2a$10$5jT1h11aGfZi3eKk.q6fIe0o1a1G2k3L4m5N6o7P8q9R0s1T2u3V4', 'Sarah Jenkins (PharmTech)', '+1 (555) 019-4822', 'ROLE_STAFF', 1, NOW()),
(3, 'customer', 'customer@pharmatrack.com', '$2a$10$7kU2i22bHgAj4fLl.r7gJf1p2b2H3l4M5n6O7p8Q9r0S1t2U3v4W5', 'Alex Rivera (Patient)', '+1 (555) 019-7711', 'ROLE_CUSTOMER', 1, NOW());

-- Categories
INSERT INTO `categories` (`id`, `name`, `description`, `active`) VALUES
(1, 'Antibiotics & Anti-Infectives', 'Medications used to treat and prevent bacterial infections', 1),
(2, 'Analgesics & Pain Relief', 'Medications for pain reduction, fever management, and inflammation', 1),
(3, 'Cardiovascular & Hypertension', 'Heart medications, blood pressure regulators, and cholesterol agents', 1),
(4, 'Respiratory & Anti-Allergic', 'Inhalers, allergy relief antihistamines, and cough preparations', 1),
(5, 'Gastrointestinal & Antacids', 'Acid blockers, proton pump inhibitors, and digestive aids', 1),
(6, 'Vitamins & Dietary Supplements', 'Essential multivitamins, calcium, minerals, and wellness supplements', 1),
(7, 'Diabetes Care & Endocrine', 'Blood glucose regulators, insulin sensitizers, and metabolic agents', 1);

-- Suppliers
INSERT INTO `suppliers` (`id`, `name`, `contact_person`, `email`, `phone`, `address`, `active`, `created_at`) VALUES
(1, 'Apex Pharma Distributing Co.', 'Robert Mitchell', 'orders@apexpharma.com', '+1-800-555-0101', '4500 Industrial Parkway, Chicago, IL', 1, NOW()),
(2, 'MedLife Pharmaceuticals Ltd.', 'Elena Rostova', 'sales@medlifepharma.com', '+1-800-555-0102', '120 Health Sciences Blvd, Boston, MA', 1, NOW()),
(3, 'Global Health Logistics', 'Marcus Sterling', 'contact@globalhealthlogistics.com', '+1-800-555-0103', '88 Logistics Center Way, Atlanta, GA', 1, NOW());

-- 16 Sample Medicines
INSERT INTO `medicines` (`id`, `name`, `generic_name`, `sku_code`, `category_id`, `manufacturer`, `unit_price`, `min_stock_threshold`, `prescription_required`, `description`, `dosage_form`, `active`, `created_at`) VALUES
(1, 'Paracetamol 500mg', 'Acetaminophen', 'MED-PCM-500', 2, 'GSK Healthcare', 4.50, 25, 0, 'Fast-acting pain reliever and antipyretic for headaches and fever.', 'Tablet', 1, NOW()),
(2, 'Amoxicillin 500mg', 'Amoxicillin Trihydrate', 'MED-AMX-500', 1, 'Pfizer Inc.', 14.20, 15, 1, 'Broad-spectrum penicillin antibiotic for bacterial infections.', 'Capsule', 1, NOW()),
(3, 'Atorvastatin 20mg', 'Atorvastatin Calcium', 'MED-ATV-020', 3, 'Viatris Specialty', 18.50, 20, 1, 'Lipid-lowering statin medication used to lower cholesterol.', 'Tablet', 1, NOW()),
(4, 'Cetirizine 10mg', 'Cetirizine Hydrochloride', 'MED-CTZ-010', 4, 'Bayer Pharma', 6.75, 20, 0, 'Antihistamine for seasonal allergic rhinitis, sneezing, and hives.', 'Tablet', 1, NOW()),
(5, 'Omeprazole 20mg', 'Omeprazole Delayed Release', 'MED-OMP-020', 5, 'AstraZeneca', 11.00, 15, 0, 'Proton pump inhibitor for treating acid reflux and heartburn.', 'Capsule', 1, NOW()),
(6, 'Metformin 850mg', 'Metformin Hydrochloride', 'MED-MET-850', 7, 'Merck Group', 9.50, 30, 1, 'First-line medication for the treatment of type 2 diabetes.', 'Tablet', 1, NOW()),
(7, 'Ibuprofen 400mg', 'Ibuprofen USP', 'MED-IBU-400', 2, 'Abbott Labs', 5.25, 25, 0, 'Nonsteroidal anti-inflammatory drug (NSAID) for muscular pain.', 'Tablet', 1, NOW()),
(8, 'Azithromycin 500mg', 'Azithromycin Dihydrate', 'MED-AZM-500', 1, 'Teva Pharmaceuticals', 22.00, 10, 1, 'Macrolide antibiotic commonly used for respiratory tract infections.', 'Tablet', 1, NOW()),
(9, 'Amlodipine 5mg', 'Amlodipine Besylate', 'MED-AML-005', 3, 'Novartis AG', 8.80, 20, 1, 'Calcium channel blocker used to treat high blood pressure.', 'Tablet', 1, NOW()),
(10, 'Vitamin C 1000mg + Zinc', 'Ascorbic Acid + Zinc Citrate', 'MED-VTC-100', 6, 'NatureMade Wellness', 7.99, 30, 0, 'Immune-support nutritional supplement with Vitamin C and Zinc.', 'Effervescent Tablet', 1, NOW()),
(11, 'Salbutamol Inhaler 100mcg', 'Albuterol Sulfate', 'MED-SLB-100', 4, 'GlaxoSmithKline', 16.50, 12, 1, 'Fast-acting bronchodilator for relief of asthma symptoms.', 'Inhaler', 1, NOW()),
(12, 'Pantoprazole 40mg', 'Pantoprazole Sodium', 'MED-PAN-040', 5, 'Takeda Pharma', 13.40, 15, 1, 'Gastric acid pump inhibitor for erosive esophagitis.', 'Tablet', 1, NOW()),
(13, 'Ciprofloxacin 500mg', 'Ciprofloxacin Hydrochloride', 'MED-CPR-500', 1, 'Bayer Healthcare', 15.00, 15, 1, 'Fluoroquinolone antibiotic for serious bacterial infections.', 'Tablet', 1, NOW()),
(14, 'Vitamin D3 60000 IU', 'Cholecalciferol', 'MED-VTD-060', 6, 'Cadila Healthcare', 12.50, 20, 0, 'Weekly Vitamin D3 supplement for bone density support.', 'Softgel Capsule', 1, NOW()),
(15, 'Glimepiride 2mg', 'Glimepiride USP', 'MED-GLM-002', 7, 'Sanofi Aventis', 10.20, 15, 1, 'Sulfonylurea antidiabetic for lowering blood sugar.', 'Tablet', 1, NOW()),
(16, 'Losartan Potassium 50mg', 'Losartan Potassium', 'MED-LOS-050', 3, 'Merck Sharp & Dohme', 11.50, 25, 1, 'ARB for hypertension and kidney protection in diabetics.', 'Tablet', 1, NOW());

-- Batches with FEFO, Near-Expiry, and Expired Scenarios
INSERT INTO `medicine_batches` (`id`, `medicine_id`, `batch_number`, `manufacturing_date`, `expiry_date`, `quantity_received`, `available_quantity`, `purchase_cost`, `supplier_id`, `receipt_date`, `status`, `created_at`) VALUES
-- Paracetamol: Active batch (exp 2027)
(1, 1, 'PCM-2024-B1', '2024-04-10', '2027-04-10', 150, 120, 2.20, 1, '2024-04-15', 'ACTIVE', NOW()),
-- Paracetamol: Near-expiry batch (exp in 14 days)
(2, 1, 'PCM-2023-B2', '2023-11-10', DATE_ADD(CURRENT_DATE(), INTERVAL 14 DAY), 100, 18, 2.00, 1, '2023-11-15', 'NEAR_EXPIRY', NOW()),
-- Paracetamol: Expired batch (expired 45 days ago)
(3, 1, 'PCM-2022-EXP', '2022-09-01', DATE_SUB(CURRENT_DATE(), INTERVAL 45 DAY), 100, 25, 1.90, 1, '2022-09-05', 'EXPIRED', NOW()),
-- Amoxicillin: Active batch
(4, 2, 'AMX-2024-01', '2024-06-01', DATE_ADD(CURRENT_DATE(), INTERVAL 365 DAY), 100, 60, 7.50, 2, '2024-06-05', 'ACTIVE', NOW()),
-- Amoxicillin: Near-expiry batch (exp in 22 days)
(5, 2, 'AMX-2023-NE', '2023-11-01', DATE_ADD(CURRENT_DATE(), INTERVAL 22 DAY), 80, 12, 7.00, 2, '2023-11-05', 'NEAR_EXPIRY', NOW()),
-- Atorvastatin: Active batch
(6, 3, 'ATV-24-901', '2024-07-01', DATE_ADD(CURRENT_DATE(), INTERVAL 700 DAY), 100, 80, 9.20, 2, '2024-07-05', 'ACTIVE', NOW()),
-- Cetirizine: Active batch
(7, 4, 'CTZ-24-11', '2024-05-01', DATE_ADD(CURRENT_DATE(), INTERVAL 450 DAY), 120, 90, 3.10, 3, '2024-05-05', 'ACTIVE', NOW()),
-- Cetirizine: Expired batch (expired 20 days ago)
(8, 4, 'CTZ-23-EXP', '2022-10-01', DATE_SUB(CURRENT_DATE(), INTERVAL 20 DAY), 50, 15, 2.90, 3, '2022-10-05', 'EXPIRED', NOW()),
-- Omeprazole: Active batch
(9, 5, 'OMP-24-44', '2024-08-01', DATE_ADD(CURRENT_DATE(), INTERVAL 300 DAY), 100, 75, 5.50, 1, '2024-08-05', 'ACTIVE', NOW()),
-- Metformin: Active batch
(10, 6, 'MET-24-500', '2024-06-01', DATE_ADD(CURRENT_DATE(), INTERVAL 600 DAY), 150, 140, 4.80, 2, '2024-06-05', 'ACTIVE', NOW()),
-- Metformin: Near-expiry batch (exp in 28 days)
(11, 6, 'MET-23-NEAR', '2023-11-01', DATE_ADD(CURRENT_DATE(), INTERVAL 28 DAY), 100, 20, 4.50, 2, '2023-11-05', 'NEAR_EXPIRY', NOW()),
-- Ibuprofen: Active batch
(12, 7, 'IBU-24-88', '2024-04-01', DATE_ADD(CURRENT_DATE(), INTERVAL 400 DAY), 150, 110, 2.60, 3, '2024-04-05', 'ACTIVE', NOW()),
-- Azithromycin: Active batch
(13, 8, 'AZM-24-02', '2024-08-01', DATE_ADD(CURRENT_DATE(), INTERVAL 480 DAY), 50, 45, 11.50, 1, '2024-08-05', 'ACTIVE', NOW()),
-- Amlodipine: Active batch
(14, 9, 'AML-24-07', '2024-05-01', DATE_ADD(CURRENT_DATE(), INTERVAL 540 DAY), 80, 65, 4.20, 2, '2024-05-05', 'ACTIVE', NOW()),
-- Vitamin C: Active batch
(15, 10, 'VTC-24-30', '2024-07-01', DATE_ADD(CURRENT_DATE(), INTERVAL 660 DAY), 200, 150, 3.80, 3, '2024-07-05', 'ACTIVE', NOW()),
-- Salbutamol: Active batch
(16, 11, 'SLB-24-101', '2024-06-01', DATE_ADD(CURRENT_DATE(), INTERVAL 330 DAY), 50, 40, 8.50, 1, '2024-06-05', 'ACTIVE', NOW()),
-- Pantoprazole: Active batch
(17, 12, 'PAN-24-91', '2024-07-01', DATE_ADD(CURRENT_DATE(), INTERVAL 390 DAY), 70, 55, 6.70, 2, '2024-07-05', 'ACTIVE', NOW()),
-- Ciprofloxacin: Active batch
(18, 13, 'CPR-24-60', '2024-06-01', DATE_ADD(CURRENT_DATE(), INTERVAL 450 DAY), 60, 50, 7.90, 1, '2024-06-05', 'ACTIVE', NOW()),
-- Vitamin D3: Active batch
(19, 14, 'VTD-24-12', '2024-08-01', DATE_ADD(CURRENT_DATE(), INTERVAL 570 DAY), 100, 85, 6.00, 3, '2024-08-05', 'ACTIVE', NOW()),
-- Glimepiride: Active batch
(20, 15, 'GLM-24-33', '2024-07-01', DATE_ADD(CURRENT_DATE(), INTERVAL 510 DAY), 50, 45, 5.10, 2, '2024-07-05', 'ACTIVE', NOW()),
-- Losartan: LOW STOCK DEMO (Available = 6, threshold = 25)
(21, 16, 'LOS-24-LOW', '2024-05-01', DATE_ADD(CURRENT_DATE(), INTERVAL 240 DAY), 50, 6, 5.80, 2, '2024-05-05', 'ACTIVE', NOW());

-- Stock Movements
INSERT INTO `stock_movements` (`id`, `medicine_id`, `batch_id`, `movement_type`, `quantity`, `quantity_after`, `reference_number`, `reason`, `created_by`, `created_at`) VALUES
(1, 1, 1, 'PURCHASE_RECEIPT', 150, 120, 'INIT-PCM-2024-B1', 'Initial stock intake from Apex Pharma', 'SYSTEM', NOW()),
(2, 1, 2, 'PURCHASE_RECEIPT', 100, 18, 'INIT-PCM-2023-B2', 'Restock receipt', 'SYSTEM', NOW()),
(3, 1, 3, 'PURCHASE_RECEIPT', 100, 25, 'INIT-PCM-2022-EXP', 'Archived batch seed', 'SYSTEM', NOW()),
(4, 16, 21, 'PURCHASE_RECEIPT', 50, 6, 'INIT-LOS-24-LOW', 'Low stock threshold demo', 'SYSTEM', NOW());

-- Sample Customer Orders
INSERT INTO `customer_orders` (`id`, `order_number`, `user_id`, `customer_name`, `customer_phone`, `customer_email`, `delivery_address`, `total_amount`, `status`, `payment_method`, `payment_status`, `requires_prescription`, `created_at`) VALUES
(1, 'ORD-20261020-1011', 3, 'Alex Rivera', '+1 (555) 019-7711', 'customer@pharmatrack.com', '742 Evergreen Terrace, Springfield, OR', 15.75, 'COMPLETED', 'SIMULATED_ONLINE', 'PAID', 0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'ORD-20261026-4421', 3, 'Alex Rivera', '+1 (555) 019-7711', 'customer@pharmatrack.com', '742 Evergreen Terrace, Springfield, OR', 22.19, 'PENDING', 'CASH_ON_DELIVERY', 'PENDING', 1, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- Order Items
INSERT INTO `order_items` (`id`, `customer_order_id`, `medicine_id`, `batch_id`, `quantity`, `unit_price`, `subtotal`) VALUES
(1, 1, 1, 1, 2, 4.50, 9.00),
(2, 1, 4, 7, 1, 6.75, 6.75),
(3, 2, 2, 4, 1, 14.20, 14.20),
(4, 2, 10, 15, 1, 7.99, 7.99);

-- Notifications
INSERT INTO `notifications` (`id`, `type`, `title`, `message`, `target_role`, `target_user_id`, `reference_id`, `is_read`, `created_at`) VALUES
(1, 'LOW_STOCK', 'Low Stock Alert: Losartan Potassium 50mg', 'Eligible stock is 6 units (threshold: 25). Restocking required.', 'ROLE_STAFF', NULL, 16, 0, NOW()),
(2, 'NEAR_EXPIRY', 'Near-Expiry Stock Alert', 'Batch PCM-2023-B2 of Paracetamol 500mg expires in 14 days.', 'ROLE_STAFF', NULL, 2, 0, NOW()),
(3, 'PRESCRIPTION_REVIEW', 'New Order Received: ORD-20261026-4421', 'New order ORD-20261026-4421 requires Prescription Verification.', 'ROLE_STAFF', NULL, 2, 0, NOW());
