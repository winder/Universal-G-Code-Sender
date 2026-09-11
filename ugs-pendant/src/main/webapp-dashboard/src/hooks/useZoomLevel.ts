import { useCallback, useEffect, useState } from "react";

const STORAGE_KEY = "ugs-dashboard-zoom";
const MIN_ZOOM = 70;
const MAX_ZOOM = 150;
const STEP = 10;
const DEFAULT_ZOOM = 100;

function readStoredZoom(): number {
  const stored = Number(localStorage.getItem(STORAGE_KEY));
  return Number.isFinite(stored) && stored >= MIN_ZOOM && stored <= MAX_ZOOM ? stored : DEFAULT_ZOOM;
}

// Whole-page zoom for touchscreens of different sizes/distances, persisted
// so it survives a reload. Uses the CSS `zoom` property rather than a
// transform: scale - transform would need extra width/height math to avoid
// clipping or stray scrollbars, `zoom` just reflows the page at the new
// size like the browser's own ctrl+/- would. Well supported in Chromium
// (what this dashboard actually runs in) and, since 2024, Firefox.
export function useZoomLevel() {
  const [zoom, setZoom] = useState(readStoredZoom);

  useEffect(() => {
    document.body.style.zoom = `${zoom}%`;
    localStorage.setItem(STORAGE_KEY, String(zoom));
  }, [zoom]);

  const zoomIn = useCallback(() => setZoom((z) => Math.min(MAX_ZOOM, z + STEP)), []);
  const zoomOut = useCallback(() => setZoom((z) => Math.max(MIN_ZOOM, z - STEP)), []);

  return { zoom, zoomIn, zoomOut, canZoomIn: zoom < MAX_ZOOM, canZoomOut: zoom > MIN_ZOOM };
}
