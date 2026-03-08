const PLACEHOLDER_URL = "/images/goods-placeholder.svg";

const hasWindow = typeof window !== "undefined";

const resolveApiBase = () => {
    const configured = (import.meta.env.VITE_API_BASE_URL || "").trim();
    if (configured) {
        return configured.replace(/\/+$/, "");
    }

    if (hasWindow) {
        const host = window.location.hostname;
        if (host === "localhost" || host === "127.0.0.1") {
            return `${window.location.protocol}//${host}:23333`;
        }
    }

    return "http://localhost:23333";
};

const toAbsolute = (path) => {
    const normalized = path.startsWith("/") ? path : `/${path}`;
    return `${resolveApiBase()}${normalized}`;
};

export const normalizeGoodsImageUrl = (rawUrl) => {
    const value = String(rawUrl || "").trim();
    if (!value) {
        return PLACEHOLDER_URL;
    }

    if (value.startsWith("data:") || value.startsWith("blob:")) {
        return value;
    }

    if (/^https?:\/\//i.test(value)) {
        // Normalize historical fixed-host goods paths to current backend host.
        if (/^https?:\/\/[^/]+\/goods-img\//i.test(value)) {
            return toAbsolute(value.replace(/^https?:\/\/[^/]+/i, ""));
        }
        return value;
    }

    if (/^\/\//.test(value)) {
        return hasWindow ? `${window.location.protocol}${value}` : `https:${value}`;
    }

    return toAbsolute(value);
};

export const handleGoodsImageError = (event) => {
    const img = event?.target;
    if (!img || typeof img !== "object") {
        return;
    }
    if (img.dataset && img.dataset.fallbackApplied === "1") {
        return;
    }
    if (img.dataset) {
        img.dataset.fallbackApplied = "1";
    }
    img.src = PLACEHOLDER_URL;
};

