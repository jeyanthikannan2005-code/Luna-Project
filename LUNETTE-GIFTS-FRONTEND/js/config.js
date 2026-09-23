/*
 * API configuration for both local development and Netlify.
 * After Railway deploys the backend, replace the Railway URL below once.
 */
const RAILWAY_BACKEND_URL = "https://YOUR-RAILWAY-SERVICE.up.railway.app";
const isLocalFrontend = ["localhost", "127.0.0.1"].includes(window.location.hostname);
const API_BASE_URL = window.LUNETTE_API_BASE_URL ||
    (isLocalFrontend ? "http://localhost:8080" : RAILWAY_BACKEND_URL);

function apiUrl(path) {
    return `${API_BASE_URL.replace(/\/$/, '')}${path.startsWith('/') ? path : `/${path}`}`;
}
