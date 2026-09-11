import { useEffect, useState } from "react";
import { useAppSelector } from "../hooks/useAppSelector";
import "./ConnectionHealth.scss";

// A WebSocket can stay technically "open" for a while after the underlying
// connection has actually gone stale (network drop, pendant server hung,
// etc.) - these thresholds are about the age of the last message actually
// received, not the socket's own readyState.
const STALE_AFTER_MS = 3000;
const LOST_AFTER_MS = 10000;

const ConnectionHealth = () => {
  const isConnected = useAppSelector((state) => state.socket.isConnected);
  const lastMessageAt = useAppSelector((state) => state.socket.lastMessageAt);

  // Without this, nothing would re-render the dot as time passes with no new
  // message arriving - it'd stay "live" forever even after the connection
  // actually stalled.
  const [now, setNow] = useState(Date.now());
  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(timer);
  }, []);

  const elapsedMs = lastMessageAt ? now - lastMessageAt : Infinity;
  const health = !isConnected
    ? "lost"
    : elapsedMs > LOST_AFTER_MS
      ? "lost"
      : elapsedMs > STALE_AFTER_MS
        ? "stale"
        : "live";

  const label = !isConnected
    ? "Not connected"
    : health === "lost"
      ? "Connection stalled - no updates received recently"
      : health === "stale"
        ? `No update in ${Math.round(elapsedMs / 1000)}s`
        : "Connection healthy";

  return <div className={`connectionHealth ${health}`} title={label} />;
};

export default ConnectionHealth;
