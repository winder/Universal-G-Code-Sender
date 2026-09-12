import { useCallback, useEffect, useState } from "react";

// Tracks the browser's actual fullscreen state (rather than assuming the
// toggle always succeeds) since a request can be silently refused - e.g. no
// direct user gesture, or a kiosk/embedded browser that disallows it.
export function useFullscreen() {
  const [isFullscreen, setIsFullscreen] = useState(() => Boolean(document.fullscreenElement));

  useEffect(() => {
    const onChange = () => setIsFullscreen(Boolean(document.fullscreenElement));
    document.addEventListener("fullscreenchange", onChange);
    return () => document.removeEventListener("fullscreenchange", onChange);
  }, []);

  const toggle = useCallback(() => {
    if (document.fullscreenElement) {
      document.exitFullscreen();
    } else {
      document.documentElement.requestFullscreen().catch(() => {
        // Nothing useful to do here - the browser already shows its own
        // permission UI/error for a refused request.
      });
    }
  }, []);

  return { isFullscreen, toggle };
}
