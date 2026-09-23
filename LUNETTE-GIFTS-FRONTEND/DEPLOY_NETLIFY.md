# Netlify deployment

1. Deploy this folder as a static Netlify site. The folder containing `index.html` is the publish directory.
2. Create the backend on Railway using the `LUNETTE-GIFTS-BACKEND` folder.
3. Copy the public Railway backend URL, for example `https://lunette-gifts-production.up.railway.app`.
4. Edit one line in `js/config.js`:

```js
const RAILWAY_BACKEND_URL = "https://YOUR-RAILWAY-SERVICE.up.railway.app";
```

Replace it with the real Railway URL and redeploy Netlify.

The frontend automatically uses `http://localhost:8080` when opened with Live Server on `localhost` or `127.0.0.1`. On the deployed Netlify domain it uses the Railway URL.

Do not put database passwords or JWT secrets in this folder.
