-- ====================================================================
-- PharmaTrack: Smart Pharmacy Inventory & Expiry Management System
-- Database Schema Definition (MySQL 8.0+)
-- Database: pharmatrack
-- ====================================================================

CREATE DATABASE IF NOT EXISTS `pharmatrack` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `pharmatrack`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `phone_number` VARCHAR(25) NULL,
    `role` VARCHAR(20) NOT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_users_role` (`role`),
    INDEX `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Medicine Categories
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(500) NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Suppliers
CREATE TABLE IF NOT EXISTS `suppliers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `contact_person` VARCHAR(100) NULL,
    `email` VARCHAR(100) NULL,
    `phone` VARCHAR(25) NULL,
    `address` VARCHAR(255) NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Medicines Table
CREATE TABLE IF NOT EXISTS `medicines` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL,
    `generic_name` VARCHAR(150) NULL,
    `sku_code` VARCHAR(50) NOT NULL UNIQUE,
    `category_id` BIGINT NOT NULL,
    `manufacturer` VARCHAR(150) NOT NULL,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `min_stock_threshold` INT NOT NULL DEFAULT 20,
    `prescription_required` BOOLEAN NOT NULL DEFAULT FALSE,
    `description` VARCHAR(1000) NULL,
    `dosage_form` VARCHAR(50) DEFAULT 'Tablet',
    `image_url` VARCHAR(255) NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_medicines_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT,
    INDEX `idx_medicines_name` (`name`),
    INDEX `idx_medicines_generic` (`generic_name`),
    INDEX `idx_medicines_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Medicine Batches Table (Supports FEFO & Expiry Management)
CREATE TABLE IF NOT EXISTS `medicine_batches` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `medicine_id` BIGINT NOT NULL,
    `batch_number` VARCHAR(50) NOT NULL,
    `manufacturing_date` DATE NOT NULL,
    `expiry_date` DATE NOT NULL,
    `quantity_received` INT NOT NULL,
    `available_quantity` INT NOT NULL,
    `purchase_cost` DECIMAL(10,2) NOT NULL,
    `supplier_id` BIGINT NULL,
    `receipt_date` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_batches_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_batches_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`) ON DELETE SET NULL,
    CONSTRAINT `uk_batch_per_med` UNIQUE (`medicine_id`, `batch_number`),
    INDEX `idx_batches_expiry` (`expiry_date`),
    INDEX `idx_batches_fefo` (`medicine_id`, `expiry_date`, `available_quantity`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Purchase Orders (Restocking)
CREATE TABLE IF NOT EXISTS `purchase_orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `po_number` VARCHAR(50) NOT NULL UNIQUE,
    `supplier_id` BIGINT NOT NULL,
    `order_date` DATE NOT NULL,
    `expected_delivery_date` DATE NULL,
    `received_date` DATE NULL,
    `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `notes` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_po_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Purchase Order Items
CREATE TABLE IF NOT EXISTS `purchase_order_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `purchase_order_id` BIGINT NOT NULL,
    `medicine_id` BIGINT NOT NULL,
    `batch_number` VARCHAR(50) NULL,
    `manufacturing_date` DATE NULL,
    `expiry_date` DATE NULL,
    `quantity` INT NOT NULL,
    `unit_cost` DECIMAL(10,2) NOT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL,
    CONSTRAINT `fk_poi_po` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_poi_med` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Customer Orders
CREATE TABLE IF NOT EXISTS `customer_orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_number` VARCHAR(50) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `customer_name` VARCHAR(100) NOT NULL,
    `customer_phone` VARCHAR(25) NOT NULL,
    `customer_email` VARCHAR(100) NULL,
    `delivery_address` VARCHAR(500) NOT NULL,
    `total_amount` DECIMAL(12,2) NOT NULL,
    `status` VARCHAR(25) NOT NULL DEFAULT 'PENDING',
    `payment_method` VARCHAR(25) NOT NULL DEFAULT 'CASH_ON_DELIVERY',
    `payment_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `requires_prescription` BOOLEAN NOT NULL DEFAULT FALSE,
    `prescription_note` VARCHAR(500) NULL,
    `prescription_verified_by` VARCHAR(100) NULL,
    `prescription_verified_at` DATETIME NULL,
    `cancellation_reason` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL,
    CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    INDEX `idx_orders_status` (`status`),
    INDEX `idx_orders_customer` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Customer Order Items (Tied to Specific Batches via FEFO)
CREATE TABLE IF NOT EXISTS `order_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `customer_order_id` BIGINT NOT NULL,
    `medicine_id` BIGINT NOT NULL,
    `batch_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL,
    CONSTRAINT `fk_oi_order` FOREIGN KEY (`customer_order_id`) REFERENCES `customer_orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_oi_med` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_oi_batch` FOREIGN KEY (`batch_id`) REFERENCES `medicine_batches` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Stock Movement Audit Trail
CREATE TABLE IF NOT EXISTS `stock_movements` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `medicine_id` BIGINT NOT NULL,
    `batch_id` BIGINT NOT NULL,
    `movement_type` VARCHAR(30) NOT NULL,
    `quantity` INT NOT NULL,
    `quantity_after` INT NULL,
    `reference_number` VARCHAR(50) NULL,
    `reason` VARCHAR(255) NULL,
    `created_by` VARCHAR(50) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_sm_med` FOREIGN KEY (`medicine_id`) REFERENCES `medicines` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_sm_batch` FOREIGN KEY (`batch_id`) REFERENCES `medicine_batches` (`id`) ON DELETE RESTRICT,
    INDEX `idx_sm_type` (`movement_type`),
    INDEX `idx_sm_date` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. System Notifications and Alerts
CREATE TABLE IF NOT EXISTS `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `type` VARCHAR(30) NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `message` VARCHAR(500) NOT NULL,
    `target_role` VARCHAR(30) NULL,
    `target_user_id` BIGINT NULL,
    `reference_id` BIGINT NULL,
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_notif_target` (`target_role`, `target_user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
