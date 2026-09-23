# Lunette Gifts Frontend

Standalone, Netlify-ready static storefront extracted from the existing Lunette Gifts Spring Boot application. It preserves the original boutique floral identity, product customization flow, cart behavior, photo upload slots, delivery rules, and backend-driven product pricing.

## Run locally

Open this folder in VS Code and run `index.html` with Live Server (recommended port `5500`). The default backend URL is configured in `js/config.js` as `http://localhost:8080`.

To point the site at a deployed backend without editing every script, set `window.LUNETTE_API_BASE_URL` before `js/config.js`, or change the single `API_BASE_URL` line in `js/config.js`.

## Deploy to Netlify

Deploy this folder as the site root. The included `netlify.toml` publishes the current directory without a build step. Before production use, change `API_BASE_URL` to the deployed backend URL and configure the backend's `FRONTEND_ALLOWED_ORIGINS` value to the Netlify domain.

## Design upgrade

The redesign adds realistic product imagery for frames, photo cards, ring albums, and LED polaroids; depth-aware product cards; glassy surfaces; animated entrance and scroll-friendly reveals; hover light sweeps; floating hero motion; reduced-motion support; and resilient image fallbacks. Pricing, stock, coupon validity, delivery charges, payment amounts, and order totals remain backend-authoritative.
