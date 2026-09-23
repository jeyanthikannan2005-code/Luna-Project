# Railway deployment

## Deploy

Create a Railway project, add a PostgreSQL service, then deploy this backend folder as a service. Railway will use the included `Dockerfile` and `railway.toml`.

Set these variables in the backend service:

```text
DATABASE_JDBC_URL=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}
DATABASE_USERNAME=${PGUSER}
DATABASE_PASSWORD=${PGPASSWORD}
JWT_SECRET=<long-random-secret-at-least-32-characters>
FRONTEND_ALLOWED_ORIGINS=https://YOUR-NETLIFY-SITE.netlify.app
```

If Railway does not expand the PostgreSQL reference syntax in the dashboard, copy the actual values from the PostgreSQL service into these variables. The application listens on Railway's `PORT` automatically and falls back to `8080` locally.

## Verify

After deploy, open:

```text
https://YOUR-RAILWAY-SERVICE.up.railway.app/actuator/health
https://YOUR-RAILWAY-SERVICE.up.railway.app/api/store/products
```

The first endpoint should report `UP`; the second should return the seeded products.

## Local run

For the original local setup, PostgreSQL should contain a database named `lunette_gifts`. With PostgreSQL using the default local `postgres` password, the backend can be started from VS Code with:

```powershell
.\mvnw.cmd spring-boot:run
```

For a different local password, set `DATABASE_PASSWORD` before starting. The local JWT fallback is only for development; always set `JWT_SECRET` on Railway.

The backend remains authoritative for prices, variants, customization charges, coupons, delivery, stock, payment amounts, and order totals.
