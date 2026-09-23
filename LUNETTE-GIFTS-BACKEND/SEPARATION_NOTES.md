# Lunette Gifts Backend

This folder is the independent Spring Boot backend extracted from the original project. Java entities, repositories, services, controllers, pricing rules, order lifecycle, inventory, expenses, worker activity logging, and database relationships are preserved.

## Run

Use Java 21+ and Maven. Create the PostgreSQL database `lunette_gifts`, copy `.env.example` into your environment, set real values, and run `mvn spring-boot:run` (or the Maven Wrapper if available).

## Security changes

Database credentials and the JWT secret are now environment-backed. CORS is restricted to the comma-separated origins in `FRONTEND_ALLOWED_ORIGINS`, and unknown routes require authentication. Admin and worker endpoints remain role-protected.

## Business authority

The backend remains authoritative for product and variant prices, customization charges, coupon discounts, delivery fees, inventory, payment amounts, and final order totals. The frontend only displays and collects inputs.
