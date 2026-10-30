package com.pharmatrack.config;

import com.pharmatrack.model.*;
import com.pharmatrack.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerOrderRepository orderRepository;
    private final StockMovementRepository movementRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           MedicineRepository medicineRepository,
                           MedicineBatchRepository batchRepository,
                           SupplierRepository supplierRepository,
                           CustomerOrderRepository orderRepository,
                           StockMovementRepository movementRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.supplierRepository = supplierRepository;
        this.orderRepository = orderRepository;
        this.movementRepository = movementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Seed Users if empty
        if (userRepository.count() == 0) {
            seedUsers();
        }

        // Seed Categories if empty
        if (categoryRepository.count() == 0) {
            seedCategoriesAndCatalog();
        }
    }

    private void seedUsers() {
        System.out.println(">> Seeding Default Users...");

        User admin = new User("admin", "admin@pharmatrack.com", passwordEncoder.encode("Admin@123"),
                "Dr. John Administrator", "+1 (555) 019-2831", Role.ROLE_ADMIN);

        User staff = new User("staff", "staff@pharmatrack.com", passwordEncoder.encode("Staff@123"),
                "Sarah Jenkins (PharmTech)", "+1 (555) 019-4822", Role.ROLE_STAFF);

        User customer = new User("customer", "customer@pharmatrack.com", passwordEncoder.encode("Customer@123"),
                "Alex Rivera (Patient)", "+1 (555) 019-7711", Role.ROLE_CUSTOMER);

        userRepository.saveAll(Arrays.asList(admin, staff, customer));
        System.out.println(">> Users seeded: admin, staff, customer (Password: Admin@123 / Staff@123 / Customer@123)");
    }

    private void seedCategoriesAndCatalog() {
        System.out.println(">> Seeding Categories, Suppliers, Medicines, Batches, and Orders...");

        // 1. Categories
        Category catAntibiotics = new Category("Antibiotics & Anti-Infectives", "Medications used to treat and prevent bacterial infections");
        Category catAnalgesics = new Category("Analgesics & Pain Relief", "Medications for pain reduction, fever management, and inflammation");
        Category catCardio = new Category("Cardiovascular & Hypertension", "Heart medications, blood pressure regulators, and cholesterol agents");
        Category catRespiratory = new Category("Respiratory & Anti-Allergic", "Inhalers, allergy relief antihistamines, and cough preparations");
        Category catGastro = new Category("Gastrointestinal & Antacids", "Acid blockers, proton pump inhibitors, and digestive aids");
        Category catVitamins = new Category("Vitamins & Dietary Supplements", "Essential multivitamins, calcium, minerals, and wellness supplements");
        Category catDiabetes = new Category("Diabetes Care & Endocrine", "Blood glucose regulators, insulin sensitizers, and metabolic agents");

        categoryRepository.saveAll(Arrays.asList(
                catAntibiotics, catAnalgesics, catCardio, catRespiratory, catGastro, catVitamins, catDiabetes
        ));

        // 2. Suppliers
        Supplier s1 = new Supplier();
        s1.setName("Apex Pharma Distributing Co.");
        s1.setContactPerson("Robert Mitchell");
        s1.setEmail("orders@apexpharma.com");
        s1.setPhone("+1-800-555-0101");
        s1.setAddress("4500 Industrial Parkway, Chicago, IL");

        Supplier s2 = new Supplier();
        s2.setName("MedLife Pharmaceuticals Ltd.");
        s2.setContactPerson("Elena Rostova");
        s2.setEmail("sales@medlifepharma.com");
        s2.setPhone("+1-800-555-0102");
        s2.setAddress("120 Health Sciences Blvd, Boston, MA");

        Supplier s3 = new Supplier();
        s3.setName("Global Health Logistics");
        s3.setContactPerson("Marcus Sterling");
        s3.setEmail("contact@globalhealthlogistics.com");
        s3.setPhone("+1-800-555-0103");
        s3.setAddress("88 Logistics Center Way, Atlanta, GA");

        supplierRepository.saveAll(Arrays.asList(s1, s2, s3));

        // 3. 16 Realistic Medicines
        LocalDate today = LocalDate.now();

        // Med 1: Paracetamol 500mg (OTC, Analgesic)
        Medicine m1 = createMed("Paracetamol 500mg", "Acetaminophen", "MED-PCM-500", catAnalgesics,
                "GSK Healthcare", new BigDecimal("4.50"), 25, false, "Tablet",
                "Fast-acting pain reliever and antipyretic for headaches, body aches, and fever.");

        // Med 2: Amoxicillin 500mg (Prescription, Antibiotic)
        Medicine m2 = createMed("Amoxicillin 500mg", "Amoxicillin Trihydrate", "MED-AMX-500", catAntibiotics,
                "Pfizer Inc.", new BigDecimal("14.20"), 15, true, "Capsule",
                "Broad-spectrum penicillin antibiotic for ear, throat, urinary tract, and chest infections.");

        // Med 3: Atorvastatin 20mg (Prescription, Cardio)
        Medicine m3 = createMed("Atorvastatin 20mg", "Atorvastatin Calcium", "MED-ATV-020", catCardio,
                "Viatris Specialty", new BigDecimal("18.50"), 20, true, "Tablet",
                "Lipid-lowering statin medication used to prevent cardiovascular disease and lower cholesterol.");

        // Med 4: Cetirizine 10mg (OTC, Respiratory)
        Medicine m4 = createMed("Cetirizine 10mg", "Cetirizine Hydrochloride", "MED-CTZ-010", catRespiratory,
                "Bayer Pharma", new BigDecimal("6.75"), 20, false, "Tablet",
                "Second-generation antihistamine for seasonal allergic rhinitis, sneezing, and hives.");

        // Med 5: Omeprazole 20mg (OTC/Prescription, Gastro)
        Medicine m5 = createMed("Omeprazole 20mg", "Omeprazole Delayed Release", "MED-OMP-020", catGastro,
                "AstraZeneca", new BigDecimal("11.00"), 15, false, "Capsule",
                "Proton pump inhibitor (PPI) for treating acid reflux, heartburn, and gastroesophageal disease.");

        // Med 6: Metformin 850mg (Prescription, Diabetes)
        Medicine m6 = createMed("Metformin 850mg", "Metformin Hydrochloride", "MED-MET-850", catDiabetes,
                "Merck Group", new BigDecimal("9.50"), 30, true, "Tablet",
                "First-line biguanide medication for the treatment of type 2 diabetes mellitus.");

        // Med 7: Ibuprofen 400mg (OTC, Analgesic)
        Medicine m7 = createMed("Ibuprofen 400mg", "Ibuprofen USP", "MED-IBU-400", catAnalgesics,
                "Abbott Labs", new BigDecimal("5.25"), 25, false, "Tablet",
                "Nonsteroidal anti-inflammatory drug (NSAID) for arthritis, dental pain, and muscular pain.");

        // Med 8: Azithromycin 500mg (Prescription, Antibiotic)
        Medicine m8 = createMed("Azithromycin 500mg", "Azithromycin Dihydrate", "MED-AZM-500", catAntibiotics,
                "Teva Pharmaceuticals", new BigDecimal("22.00"), 10, true, "Tablet",
                "Macrolide antibiotic commonly used for respiratory tract infections and sinusitis.");

        // Med 9: Amlodipine 5mg (Prescription, Cardio)
        Medicine m9 = createMed("Amlodipine 5mg", "Amlodipine Besylate", "MED-AML-005", catCardio,
                "Novartis AG", new BigDecimal("8.80"), 20, true, "Tablet",
                "Calcium channel blocker used to treat high blood pressure and coronary artery disease.");

        // Med 10: Vitamin C 1000mg + Zinc (OTC, Vitamins)
        Medicine m10 = createMed("Vitamin C 1000mg + Zinc", "Ascorbic Acid + Zinc Citrate", "MED-VTC-100", catVitamins,
                "NatureMade Wellness", new BigDecimal("7.99"), 30, false, "Effervescent Tablet",
                "Immune-support nutritional supplement with high-potency antioxidant Vitamin C and Zinc.");

        // Med 11: Salbutamol Inhaler 100mcg (Prescription, Respiratory)
        Medicine m11 = createMed("Salbutamol Inhaler 100mcg", "Albuterol Sulfate", "MED-SLB-100", catRespiratory,
                "GlaxoSmithKline", new BigDecimal("16.50"), 12, true, "Inhaler",
                "Fast-acting bronchodilator for prompt relief of asthma bronchospasm and COPD symptoms.");

        // Med 12: Pantoprazole 40mg (Prescription, Gastro)
        Medicine m12 = createMed("Pantoprazole 40mg", "Pantoprazole Sodium", "MED-PAN-040", catGastro,
                "Takeda Pharma", new BigDecimal("13.40"), 15, true, "Tablet",
                "Gastric acid pump inhibitor for erosive esophagitis and Zollinger-Ellison syndrome.");

        // Med 13: Ciprofloxacin 500mg (Prescription, Antibiotic)
        Medicine m13 = createMed("Ciprofloxacin 500mg", "Ciprofloxacin Hydrochloride", "MED-CPR-500", catAntibiotics,
                "Bayer Healthcare", new BigDecimal("15.00"), 15, true, "Tablet",
                "Fluoroquinolone antibiotic for serious urinary, abdominal, and skin bacterial infections.");

        // Med 14: Vitamin D3 60,000 IU (OTC/Rx, Vitamins)
        Medicine m14 = createMed("Vitamin D3 60000 IU", "Cholecalciferol", "MED-VTD-060", catVitamins,
                "Cadila Healthcare", new BigDecimal("12.50"), 20, false, "Softgel Capsule",
                "High-potency weekly Vitamin D3 supplement for bone density support and hypocalcemia.");

        // Med 15: Glimepiride 2mg (Prescription, Diabetes)
        Medicine m15 = createMed("Glimepiride 2mg", "Glimepiride USP", "MED-GLM-002", catDiabetes,
                "Sanofi Aventis", new BigDecimal("10.20"), 15, true, "Tablet",
                "Second-generation sulfonylurea antidiabetic for lowering blood sugar in type 2 diabetes.");

        // Med 16: Losartan Potassium 50mg (Prescription, Cardio) - Low Stock Demo Item!
        Medicine m16 = createMed("Losartan Potassium 50mg", "Losartan Potassium", "MED-LOS-050", catCardio,
                "Merck Sharp & Dohme", new BigDecimal("11.50"), 25, true, "Tablet",
                "Angiotensin II receptor blocker (ARB) for hypertension and kidney protection in diabetics.");

        List<Medicine> allMeds = Arrays.asList(
                m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11, m12, m13, m14, m15, m16
        );
        medicineRepository.saveAll(allMeds);

        // 4. Create Batches (Active, Near-Expiry, Expired, and Low Stock scenarios)
        List<MedicineBatch> batches = new ArrayList<>();

        // Paracetamol: Batch 1 (Active, 100 units, exp +18 months)
        batches.add(createBatch(m1, "PCM-2024-B1", today.minusMonths(6), today.plusMonths(18), 150, 120, new BigDecimal("2.20"), s1));
        // Paracetamol: Batch 2 (Near-expiry, 18 units, exp in 14 days)
        batches.add(createBatch(m1, "PCM-2023-B2", today.minusMonths(23), today.plusDays(14), 100, 18, new BigDecimal("2.00"), s1));
        // Paracetamol: Batch 3 (Expired, 25 units, expired 45 days ago)
        batches.add(createBatch(m1, "PCM-2022-EXP", today.minusMonths(26), today.minusDays(45), 100, 25, new BigDecimal("1.90"), s1));

        // Amoxicillin: Batch 1 (Active, 60 units, exp +12 months)
        batches.add(createBatch(m2, "AMX-2024-01", today.minusMonths(4), today.plusMonths(12), 100, 60, new BigDecimal("7.50"), s2));
        // Amoxicillin: Batch 2 (Near-expiry, 12 units, exp in 22 days)
        batches.add(createBatch(m2, "AMX-2023-NE", today.minusMonths(23), today.plusDays(22), 80, 12, new BigDecimal("7.00"), s2));

        // Atorvastatin: Batch 1 (Active, 80 units, exp +24 months)
        batches.add(createBatch(m3, "ATV-24-901", today.minusMonths(3), today.plusMonths(24), 100, 80, new BigDecimal("9.20"), s2));

        // Cetirizine: Batch 1 (Active, 90 units, exp +15 months)
        batches.add(createBatch(m4, "CTZ-24-11", today.minusMonths(5), today.plusMonths(15), 120, 90, new BigDecimal("3.10"), s3));
        // Cetirizine: Batch 2 (Expired, 15 units, expired 20 days ago)
        batches.add(createBatch(m4, "CTZ-23-EXP", today.minusMonths(25), today.minusDays(20), 50, 15, new BigDecimal("2.90"), s3));

        // Omeprazole: Batch 1 (Active, 75 units, exp +10 months)
        batches.add(createBatch(m5, "OMP-24-44", today.minusMonths(2), today.plusMonths(10), 100, 75, new BigDecimal("5.50"), s1));

        // Metformin: Batch 1 (Active, 140 units, exp +20 months)
        batches.add(createBatch(m6, "MET-24-500", today.minusMonths(4), today.plusMonths(20), 150, 140, new BigDecimal("4.80"), s2));
        // Metformin: Batch 2 (Near-expiry, 20 units, exp in 28 days)
        batches.add(createBatch(m6, "MET-23-NEAR", today.minusMonths(23), today.plusDays(28), 100, 20, new BigDecimal("4.50"), s2));

        // Ibuprofen: Batch 1 (Active, 110 units, exp +14 months)
        batches.add(createBatch(m7, "IBU-24-88", today.minusMonths(6), today.plusMonths(14), 150, 110, new BigDecimal("2.60"), s3));

        // Azithromycin: Batch 1 (Active, 45 units, exp +16 months)
        batches.add(createBatch(m8, "AZM-24-02", today.minusMonths(2), today.plusMonths(16), 50, 45, new BigDecimal("11.50"), s1));

        // Amlodipine: Batch 1 (Active, 65 units, exp +18 months)
        batches.add(createBatch(m9, "AML-24-07", today.minusMonths(5), today.plusMonths(18), 80, 65, new BigDecimal("4.20"), s2));

        // Vitamin C: Batch 1 (Active, 150 units, exp +22 months)
        batches.add(createBatch(m10, "VTC-24-30", today.minusMonths(3), today.plusMonths(22), 200, 150, new BigDecimal("3.80"), s3));

        // Salbutamol: Batch 1 (Active, 40 units, exp +11 months)
        batches.add(createBatch(m11, "SLB-24-101", today.minusMonths(4), today.plusMonths(11), 50, 40, new BigDecimal("8.50"), s1));

        // Pantoprazole: Batch 1 (Active, 55 units, exp +13 months)
        batches.add(createBatch(m12, "PAN-24-91", today.minusMonths(3), today.plusMonths(13), 70, 55, new BigDecimal("6.70"), s2));

        // Ciprofloxacin: Batch 1 (Active, 50 units, exp +15 months)
        batches.add(createBatch(m13, "CPR-24-60", today.minusMonths(4), today.plusMonths(15), 60, 50, new BigDecimal("7.90"), s1));

        // Vitamin D3: Batch 1 (Active, 85 units, exp +19 months)
        batches.add(createBatch(m14, "VTD-24-12", today.minusMonths(2), today.plusMonths(19), 100, 85, new BigDecimal("6.00"), s3));

        // Glimepiride: Batch 1 (Active, 45 units, exp +17 months)
        batches.add(createBatch(m15, "GLM-24-33", today.minusMonths(3), today.plusMonths(17), 50, 45, new BigDecimal("5.10"), s2));

        // Losartan: LOW STOCK DEMO ITEM! (Total eligible stock = 6 units, threshold = 25)
        batches.add(createBatch(m16, "LOS-24-LOW", today.minusMonths(5), today.plusMonths(8), 50, 6, new BigDecimal("5.80"), s2));

        batchRepository.saveAll(batches);

        // Record Initial Stock Movements for these batches
        for (MedicineBatch b : batches) {
            StockMovement movement = new StockMovement(
                    b.getMedicine(),
                    b,
                    MovementType.PURCHASE_RECEIPT,
                    b.getQuantityReceived(),
                    b.getAvailableQuantity(),
                    "INIT-" + b.getBatchNumber(),
                    "Initial stock warehouse inventory seed",
                    "SYSTEM"
            );
            movementRepository.save(movement);
        }

        // 5. Seed Customer Orders
        User customerUser = userRepository.findByUsername("customer").orElse(null);
        if (customerUser != null) {
            seedSampleOrders(customerUser, m1, batches.get(0), m2, batches.get(3), m4, batches.get(6), m10, batches.get(11));
        }

        System.out.println(">> Catalog and sample data successfully seeded!");
    }

    private void seedSampleOrders(User customer, Medicine m1, MedicineBatch b1,
                                 Medicine m2, MedicineBatch b2,
                                 Medicine m4, MedicineBatch b4,
                                 Medicine m10, MedicineBatch b10) {

        // Order 1: Completed OTC Order
        CustomerOrder o1 = new CustomerOrder();
        o1.setCustomer(customer);
        o1.setOrderNumber("ORD-20261020-1011");
        o1.setCustomerName("Alex Rivera");
        o1.setCustomerPhone("+1 (555) 019-7711");
        o1.setCustomerEmail(customer.getEmail());
        o1.setDeliveryAddress("742 Evergreen Terrace, Springfield, OR");
        o1.setPaymentMethod(PaymentMethod.SIMULATED_ONLINE);
        o1.setPaymentStatus(CustomerOrder.PaymentStatus.PAID);
        o1.setStatus(OrderStatus.COMPLETED);
        o1.setRequiresPrescription(false);
        o1.setCreatedAt(LocalDateTime.now().minusDays(5));
        o1.setUpdatedAt(LocalDateTime.now().minusDays(4));

        OrderItem i1 = new OrderItem(o1, m1, b1, 2, m1.getUnitPrice());
        OrderItem i2 = new OrderItem(o1, m4, b4, 1, m4.getUnitPrice());
        o1.setItems(Arrays.asList(i1, i2));
        o1.setTotalAmount(i1.getSubtotal().add(i2.getSubtotal()));
        orderRepository.save(o1);

        // Order 2: Prescription Order in PENDING status (Requires Staff Verification)
        CustomerOrder o2 = new CustomerOrder();
        o2.setCustomer(customer);
        o2.setOrderNumber("ORD-20261026-4421");
        o2.setCustomerName("Alex Rivera");
        o2.setCustomerPhone("+1 (555) 019-7711");
        o2.setCustomerEmail(customer.getEmail());
        o2.setDeliveryAddress("742 Evergreen Terrace, Springfield, OR");
        o2.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        o2.setPaymentStatus(CustomerOrder.PaymentStatus.PENDING);
        o2.setStatus(OrderStatus.PENDING);
        o2.setRequiresPrescription(true);
        o2.setPrescriptionNote("Dr. Emily Watson, MD - Rx License #NY-883921. Patient diagnosed with severe strep bacterial infection. Take 1 cap every 8 hrs for 7 days.");
        o2.setCreatedAt(LocalDateTime.now().minusHours(4));
        o2.setUpdatedAt(LocalDateTime.now().minusHours(4));

        OrderItem i3 = new OrderItem(o2, m2, b2, 1, m2.getUnitPrice()); // Amoxicillin (Rx)
        OrderItem i4 = new OrderItem(o2, m10, b10, 1, m10.getUnitPrice()); // Vitamin C
        o2.setItems(Arrays.asList(i3, i4));
        o2.setTotalAmount(i3.getSubtotal().add(i4.getSubtotal()));
        orderRepository.save(o2);
    }

    private Medicine createMed(String name, String generic, String sku, Category cat,
                               String mfr, BigDecimal price, int threshold, boolean rx,
                               String form, String desc) {
        Medicine m = new Medicine();
        m.setName(name);
        m.setGenericName(generic);
        m.setSkuCode(sku);
        m.setCategory(cat);
        m.setManufacturer(mfr);
        m.setUnitPrice(price);
        m.setMinStockThreshold(threshold);
        m.setPrescriptionRequired(rx);
        m.setDosageForm(form);
        m.setDescription(desc);
        m.setActive(true);
        return m;
    }

    private MedicineBatch createBatch(Medicine med, String batchNo, LocalDate mfg,
                                      LocalDate exp, int qtyRcv, int avail,
                                      BigDecimal cost, Supplier supplier) {
        MedicineBatch b = new MedicineBatch();
        b.setMedicine(med);
        b.setBatchNumber(batchNo);
        b.setManufacturingDate(mfg);
        b.setExpiryDate(exp);
        b.setQuantityReceived(qtyRcv);
        b.setAvailableQuantity(avail);
        b.setPurchaseCost(cost);
        b.setSupplier(supplier);
        b.setReceiptDate(mfg.plusDays(5));

        LocalDate today = LocalDate.now();
        if (avail <= 0) {
            b.setStatus(BatchStatus.DEPLETED);
        } else if (exp.isBefore(today)) {
            b.setStatus(BatchStatus.EXPIRED);
        } else if (exp.isBefore(today.plusDays(30))) {
            b.setStatus(BatchStatus.NEAR_EXPIRY);
        } else {
            b.setStatus(BatchStatus.ACTIVE);
        }
        return b;
    }
}
