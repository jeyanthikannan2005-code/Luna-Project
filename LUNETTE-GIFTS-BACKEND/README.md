# 🌸 LUNETTE GIFTS
### Complete Personalized Gift E-Commerce & Business Management System

A production-style, monolithic Maven Spring Boot enterprise system uniting an artisanal customer e-commerce boutique, an intuitive owner business management dashboard, and a controlled staff order-entry terminal.

---

## 💎 Brand & Aesthetic
**Lunette Gifts** is a boutique personalized gifting brand located in Madurai, India.
- **Visual Identity**: Warm ivory backgrounds (`#FAF7F2`), blush rose accents (`#C46D7E`), muted sage green (`#7C9D86`), warm beige (`#F4EDE4`), and charcoal typography (`#2C2523`).
- **Typography**: *Playfair Display* for headings and *Poppins* for user interface elements.
- **Boutique Opening Experience**: Animated 2–4s boutique entrance screen with closed wooden doors, glowing backlight, opening animation, floral petal burst, "Made for your memories", Skip Intro button, and `introShown` persistence in `localStorage`.

---

## 🛠️ Technology Stack
- **Backend**: Java 25 LTS, Spring Boot 3.4.3
- **Security**: Spring Security 6, JJWT (0.12.6), BCrypt password hashing
- **Persistence**: Spring Data JPA, Hibernate, PostgreSQL 18 Driver
- **Validation**: Jakarta Bean Validation
- **Frontend**: Vanilla HTML5, CSS3 Grid/Flexbox, JavaScript (ES6+), Responsive Mobile + Desktop UI
- **Build Tool**: Apache Maven 3.9+
- **Database**: PostgreSQL (`lunette_gifts` / `luna_ddb` on `localhost:5432`)

---

## 📦 Core Business Modules & Implementation Highlights

### 1. Customer E-Commerce Boutique
- **Interactive Product Customization**:
  - **Photo Frames**: 5×5 (₹140), 4×6 (₹150), 5×7 (₹170), A4 (₹250). Customization: +₹30.
    *Required photo count is stored in the database and fully admin-configurable (never hardcoded in frontend JS).*
  - **Photo Cards**: ₹8 each, minimum 10 cards (₹80 for 10, ₹160 for 20). 1 photo required per card. Customization: +₹10.
  - **Ring Albums**: Ring ₹20 + (quantity × ₹8), minimum 10 cards. 1 photo per card.
  - **LED Polaroids**: Fairy lights ₹150 + (quantity × ₹7), minimum 10 cards. 1 photo per card.
- **Dynamic Photo Slots System**:
  - Dynamically calculates exact photo requirements based on variant or quantity.
  - Upload slots with real-time thumbnail previews, Replace, and Remove controls.
  - Progress indicator: `X / N photos uploaded`.
  - Separate storage for `PRINT_PHOTO` vs `CUSTOMIZATION_REFERENCE`.
  - Customization instructions text box.
- **Single Delivery Fee Rule**:
  - Madurai delivery: **Flat ₹70**
  - Outside Madurai: **Flat ₹100**
  - **Charged only ONCE per order**, regardless of whether the customer purchases 1, 3, or 10 items.
  - Automatic PIN code verification (Madurai 625xxx detection).
- **UPI Scan & Pay**:
  - Displays the verified boutique QR code image: `upi-qr.png`.
  - Official UPI ID: `haring478-1@okicici`.
  - "Copy UPI ID" button with instant feedback.
  - Payment transaction reference ID and screenshot proof upload.
- **Live Order Tracking**:
  - Visual 6-step lifecycle timeline: `ORDER_PLACED` ➔ `PAYMENT_CONFIRMED` ➔ `DESIGN_APPROVED` ➔ `IN_PRODUCTION` ➔ `READY` ➔ `DELIVERED`.
- **Instagram Showcase**:
  - Direct call-to-action linking to `https://www.instagram.com/lunette_gifts/`.

---

### 2. Owner / Admin Management Dashboard
- **Beginner-Friendly UX**: Plain-language labels ("Product Code (SKU)", "Average Customer Order Value") designed specifically for owners new to business software.
- **"WHAT NEEDS YOUR ATTENTION?" Banner**: Urgent alerts for pending payments, pending designs, low-stock items, and orders ready for delivery.
- **Live Performance Metrics**: Toggle between Today, This Month, and This Year snapshots for Revenue, Orders, Expenses, and Estimated Net Profit.
- **Order Management & ZIP Download**:
  - View customer orders, items, and photos.
  - Verify payments (`CONFIRMED` or `REJECTED`).
  - **"DOWNLOAD ALL (ZIP)"**: Generates `ORDER_XXXX_FILES.zip` containing `customer_photos/`, `customization_reference/`, and `instructions.txt`.
- **Frame Variant Photo Configuration**:
  - Update required photo counts and pricing for frame variants directly in the database.
- **Inventory Management**:
  - Stock levels with Low-Stock alerts.
  - Stock adjustments with mandatory audit reason logging.
- **Worker Activity Monitoring**:
  - Append-only, tamper-proof activity log.
  - Filter by: Today, Yesterday, This Week, This Month, or All.
  - Factual performance metrics: orders created, orders updated, files downloaded, online status.
- **Expense & Profit Management**:
  - Categories: Material, Packaging, Delivery, Electricity, Advertising, Other.
  - Automated Net Profit calculation (`Revenue - Expenses`).
- **Smart Business Insights & Predictions**:
  - Actionable recommendations with "Why?", "Source Data", and "Suggested Action".
  - Demand projections with automatic fallback when historical data is insufficient.
- **Report Exports**: Download CSV sheets for Orders, Inventory, Expenses, and Worker Activity.
- **Business Settings**: Configurable delivery rates, UPI ID, contact information.

---

### 3. Staff / Worker Order-Entry System
- **Role**: `ROLE_WORKER` with granular admin-controlled permissions:
  `CREATE_ORDER`, `VIEW_ORDERS`, `UPDATE_ORDER_STATUS`, `VIEW_CUSTOMER`, `UPLOAD_FILES`, `DOWNLOAD_ORDER_FILES`, `VIEW_INVENTORY`, `CREATE_CUSTOMER`.
- **Walk-in / Phone Order Intake**:
  - Step 1: Customer lookup by phone/email or auto-registration.
  - Step 2: Product and variant selection with dynamic photo requirements.
  - Step 3: Photo upload and customization instructions.
  - Step 4: Address, PIN code, and delivery calculation.
  - Step 5: Payment status and reference intake.
- **Audit Logging**: Every staff order creation, status change, and photo download is recorded in the worker activity log.

---

## 🔑 Default Accounts (Seeded Automatically)

| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Owner / Admin** | `admin` | `admin123` | Full administrative control & dashboard access |
| **Staff / Worker** | `staff` | `staff123` | Workshop order entry, file downloads & status updates |
| **Customer** | `demo_user` | `user123` | Sample customer account with seeded orders |

---

## 🚀 Getting Started

### 1. Database Configuration
Ensure PostgreSQL is running on `localhost:5432`. Create the database:
```sql
CREATE DATABASE lunette_gifts;
```
*(The system will also connect to existing `luna_ddb` if configured in `application.properties`)*.

### 2. Build and Run
From the project root directory:
```bash
mvn clean compile
mvn spring-boot:run
```

### 3. Accessing the Application
- **Boutique Customer Store**: `http://localhost:8080/`
- **Owner Dashboard**: `http://localhost:8080/admin/index.html` (or log in as `admin`)
- **Staff Terminal**: `http://localhost:8080/staff/index.html` (or log in as `staff`)
- **Customer Portal**: `http://localhost:8080/customer/account.html` (or log in as `demo_user`)
- **Track Order**: `http://localhost:8080/track.html`

---

## 📁 File Structure
```
lunette-gifts/
├── pom.xml
├── README.md
├── .gitignore
├── src/
│   ├── main/
│   │   ├── java/com/lunette/gifts/
│   │   │   ├── LunetteGiftsApplication.java
│   │   │   ├── config/ (SecurityConfig, WebConfig, DataInitializer)
│   │   │   ├── controller/ (AuthController, StoreController, AdminController, WorkerController, DeliveryController, CustomerController)
│   │   │   ├── dto/ (AuthDto, OrderDto, AdminDto)
│   │   │   ├── entity/ (UserAccount, Product, ProductVariant, Order, OrderItem, CustomerPhoto, OrderStatusHistory, WorkerActivity, InventoryTransaction, Expense, Review, Coupon, BusinessSetting)
│   │   │   ├── repository/ (All 13 JPA repositories)
│   │   │   ├── security/ (JwtTokenProvider, JwtAuthenticationFilter)
│   │   │   └── service/ (OrderService, ProductService, FileStorageService, WorkerService, AnalyticsService, ExpenseService, InventoryService, ReportService)
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── register.html
│   │           ├── cart.html
│   │           ├── track.html
│   │           ├── customer/account.html
│   │           ├── staff/index.html
│   │           ├── admin/index.html
│   │           ├── css/ (boutique-theme.css, opening-door.css, dashboard.css)
│   │           ├── js/ (boutique.js, store.js, cart.js, staff.js, admin.js)
│   │           └── images/ (upi-qr.png, SVGs)
```
