import { useCallback, useEffect, useRef, useState } from "react";
import { Badge, Button, ButtonGroup, Card, Form } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCompressArrowsAlt,
  faCrosshairs,
  faEraser,
  faSearchMinus,
  faSearchPlus,
  faSync,
} from "@fortawesome/free-solid-svg-icons";
import { useAppSelector } from "../hooks/useAppSelector";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { fetchFileStatus } from "../store/fileStatusSlice";
import { getToolpath, ToolpathResponse } from "../services/visualizer";

interface Point2D {
  x: number;
  y: number;
}

interface VisualizerPanelProps {
  compact?: boolean;
}

const VisualizerPanel = ({ compact = false }: VisualizerPanelProps) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const containerRef = useRef<HTMLDivElement | null>(null);

  const status = useAppSelector((state) => state.status);
  const fileStatus = useAppSelector((state) => state.fileStatus);
  const dispatch = useAppDispatch();

  const [toolpath, setToolpath] = useState<ToolpathResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [followTool, setFollowTool] = useState(false);
  const [showRapids, setShowRapids] = useState(true);
  const [trail, setTrail] = useState<Point2D[]>([]);

  // Viewport transform: screenX = offsetX + worldX * scale, screenY = offsetY - worldY * scale
  const viewRef = useRef({
    scale: 4,
    offsetX: 200,
    offsetY: 200,
  });
  const dragRef = useRef<{
    dragging: boolean;
    startX: number;
    startY: number;
    origOffsetX: number;
    origOffsetY: number;
  }>({
    dragging: false,
    startX: 0,
    startY: 0,
    origOffsetX: 0,
    origOffsetY: 0,
  });

  const fitToView = useCallback((tp: ToolpathResponse | null) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const w = canvas.width;
    const h = canvas.height;
    if (!tp || !tp.hasFile || tp.segments.length === 0) {
      viewRef.current = { scale: 4, offsetX: w / 2, offsetY: h / 2 };
      return;
    }
    const spanX = Math.max(10, tp.maxX - tp.minX);
    const spanY = Math.max(10, tp.maxY - tp.minY);
    const padding = 0.82;
    const scale = Math.min((w * padding) / spanX, (h * padding) / spanY);
    const centerX = (tp.minX + tp.maxX) / 2;
    const centerY = (tp.minY + tp.maxY) / 2;
    viewRef.current = {
      scale,
      offsetX: w / 2 - centerX * scale,
      offsetY: h / 2 + centerY * scale,
    };
  }, []);

  const loadToolpath = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getToolpath();
      setToolpath(data);
      fitToView(data);
    } catch (e) {
      console.error("Failed to fetch toolpath", e);
    } finally {
      setLoading(false);
    }
  }, [fitToView]);

  useEffect(() => {
    dispatch(fetchFileStatus());
  }, [dispatch]);

  useEffect(() => {
    loadToolpath();
  }, [fileStatus.fileName, loadToolpath]);

  // Record live pen trail when workCoord changes
  useEffect(() => {
    const x = status?.workCoord?.x;
    const y = status?.workCoord?.y;
    if (typeof x !== "number" || typeof y !== "number" || Number.isNaN(x) || Number.isNaN(y)) {
      return;
    }
    setTrail((prev) => {
      const last = prev[prev.length - 1];
      if (last && Math.hypot(last.x - x, last.y - y) < 0.05) {
        return prev;
      }
      const next = [...prev, { x, y }];
      return next.length > 5000 ? next.slice(next.length - 5000) : next;
    });
  }, [status?.workCoord?.x, status?.workCoord?.y]);

  // Resize canvas to container
  useEffect(() => {
    const resize = () => {
      const canvas = canvasRef.current;
      const container = containerRef.current;
      if (!canvas || !container) return;
      canvas.width = container.clientWidth;
      canvas.height = compact ? 320 : Math.max(380, window.innerHeight - 230);
      if (toolpath) {
        fitToView(toolpath);
      }
    };
    resize();
    window.addEventListener("resize", resize);
    return () => window.removeEventListener("resize", resize);
  }, [compact, fitToView, toolpath]);

  // Draw canvas
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const w = canvas.width;
    const h = canvas.height;
    const toolX = status?.workCoord?.x ?? 0;
    const toolY = status?.workCoord?.y ?? 0;

    if (followTool && !Number.isNaN(toolX) && !Number.isNaN(toolY)) {
      viewRef.current.offsetX = w / 2 - toolX * viewRef.current.scale;
      viewRef.current.offsetY = h / 2 + toolY * viewRef.current.scale;
    }

    const { scale, offsetX, offsetY } = viewRef.current;
    const toScreenX = (wx: number) => offsetX + wx * scale;
    const toScreenY = (wy: number) => offsetY - wy * scale;

    // Background
    ctx.fillStyle = "#161b22";
    ctx.fillRect(0, 0, w, h);

    // Adaptive grid
    const rawStep = 50 / scale;
    const mag = Math.pow(10, Math.floor(Math.log10(Math.max(0.01, rawStep))));
    const norm = rawStep / mag;
    const gridStep = norm < 2 ? mag : norm < 5 ? 2 * mag : 5 * mag;

    const minWorldX = (0 - offsetX) / scale;
    const maxWorldX = (w - offsetX) / scale;
    const minWorldY = (offsetY - h) / scale;
    const maxWorldY = offsetY / scale;

    ctx.lineWidth = 1;
    ctx.strokeStyle = "rgba(255,255,255,0.06)";
    ctx.beginPath();
    const startGX = Math.floor(minWorldX / gridStep) * gridStep;
    for (let gx = startGX; gx <= maxWorldX; gx += gridStep) {
      const sx = toScreenX(gx);
      ctx.moveTo(sx, 0);
      ctx.lineTo(sx, h);
    }
    const startGY = Math.floor(minWorldY / gridStep) * gridStep;
    for (let gy = startGY; gy <= maxWorldY; gy += gridStep) {
      const sy = toScreenY(gy);
      ctx.moveTo(0, sy);
      ctx.lineTo(w, sy);
    }
    ctx.stroke();

    // Origin axes (X red, Y green)
    const ox = toScreenX(0);
    const oy = toScreenY(0);
    ctx.lineWidth = 1.5;
    ctx.strokeStyle = "rgba(230, 60, 60, 0.7)";
    ctx.beginPath();
    ctx.moveTo(0, oy);
    ctx.lineTo(w, oy);
    ctx.stroke();

    ctx.strokeStyle = "rgba(60, 210, 90, 0.7)";
    ctx.beginPath();
    ctx.moveTo(ox, 0);
    ctx.lineTo(ox, h);
    ctx.stroke();

    // G-code toolpath segments
    if (toolpath?.hasFile && toolpath.segments.length > 0) {
      const completed = fileStatus.completedRowCount || 0;
      for (const seg of toolpath.segments) {
        const [x1, y1, , x2, y2, , type, lineNumber] = seg;
        if (type === 0 && !showRapids) continue;

        const isDone = completed > 0 && lineNumber <= completed;
        ctx.beginPath();
        if (type === 0) {
          ctx.setLineDash([4, 4]);
          ctx.strokeStyle = isDone ? "rgba(140,140,140,0.35)" : "rgba(220, 190, 40, 0.55)";
          ctx.lineWidth = 1;
        } else {
          ctx.setLineDash([]);
          ctx.lineWidth = 1.5;
          if (isDone) {
            ctx.strokeStyle = "rgba(120, 220, 140, 0.55)";
          } else if (type === 2) {
            ctx.strokeStyle = "#f85149";
          } else if (type === 3) {
            ctx.strokeStyle = "#2ea043";
          } else {
            ctx.strokeStyle = "#58a6ff";
          }
        }
        ctx.moveTo(toScreenX(x1), toScreenY(y1));
        ctx.lineTo(toScreenX(x2), toScreenY(y2));
        ctx.stroke();
      }
      ctx.setLineDash([]);
    }

    // Live pen trail
    if (trail.length > 1) {
      ctx.beginPath();
      ctx.strokeStyle = "#ffd33d";
      ctx.lineWidth = 2;
      ctx.moveTo(toScreenX(trail[0].x), toScreenY(trail[0].y));
      for (let i = 1; i < trail.length; i++) {
        ctx.lineTo(toScreenX(trail[i].x), toScreenY(trail[i].y));
      }
      ctx.stroke();
    }

    // Live tool / pen cursor (small crisp yellow dot + crosshair)
    if (!Number.isNaN(toolX) && !Number.isNaN(toolY)) {
      const tx = toScreenX(toolX);
      const ty = toScreenY(toolY);

      ctx.strokeStyle = "rgba(255, 235, 59, 0.55)";
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(tx - 10, ty);
      ctx.lineTo(tx + 10, ty);
      ctx.moveTo(tx, ty - 10);
      ctx.lineTo(tx, ty + 10);
      ctx.stroke();

      ctx.fillStyle = "#edff00";
      ctx.beginPath();
      ctx.arc(tx, ty, 3.5, 0, Math.PI * 2);
      ctx.fill();
      ctx.strokeStyle = "#000000";
      ctx.lineWidth = 1;
      ctx.stroke();
    }
  }, [toolpath, status, fileStatus.completedRowCount, trail, followTool, showRapids]);

  const zoomBy = (factor: number) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const cx = canvas.width / 2;
    const cy = canvas.height / 2;
    const v = viewRef.current;
    v.offsetX = cx - (cx - v.offsetX) * factor;
    v.offsetY = cy - (cy - v.offsetY) * factor;
    v.scale *= factor;
    setTrail((t) => [...t]); // trigger redraw
  };

  return (
    <Card bg="dark" text="light" className="mb-3">
      <Card.Header className="d-flex flex-wrap justify-content-between align-items-center gap-2">
        <div>
          <strong>Visualizer</strong>{" "}
          {toolpath?.hasFile ? (
            <Badge bg="secondary">{toolpath.fileName}</Badge>
          ) : (
            <Badge bg="secondary">No file</Badge>
          )}
          {toolpath?.truncated && (
            <Badge bg="warning" text="dark" className="ms-2">
              Downsampled
            </Badge>
          )}
        </div>
        <div className="d-flex flex-wrap align-items-center gap-2">
          <Form.Check
            type="switch"
            id="vis-rapids"
            label="Rapids"
            checked={showRapids}
            onChange={(e) => setShowRapids(e.target.checked)}
          />
          <ButtonGroup size="sm">
            <Button
              variant={followTool ? "warning" : "outline-light"}
              title="Follow Tool"
              onClick={() => setFollowTool((f) => !f)}
            >
              <FontAwesomeIcon icon={faCrosshairs} />
            </Button>
            <Button variant="outline-light" title="Zoom In" onClick={() => zoomBy(1.3)}>
              <FontAwesomeIcon icon={faSearchPlus} />
            </Button>
            <Button variant="outline-light" title="Zoom Out" onClick={() => zoomBy(1 / 1.3)}>
              <FontAwesomeIcon icon={faSearchMinus} />
            </Button>
            <Button
              variant="outline-light"
              title="Fit"
              onClick={() => {
                setFollowTool(false);
                fitToView(toolpath);
                setTrail((t) => [...t]);
              }}
            >
              <FontAwesomeIcon icon={faCompressArrowsAlt} />
            </Button>
            <Button variant="outline-light" title="Clear Trail" onClick={() => setTrail([])}>
              <FontAwesomeIcon icon={faEraser} />
            </Button>
            <Button variant="outline-light" title="Reload" onClick={loadToolpath} disabled={loading}>
              <FontAwesomeIcon icon={faSync} spin={loading} />
            </Button>
          </ButtonGroup>
        </div>
      </Card.Header>
      <Card.Body className="p-0" ref={containerRef} style={{ position: "relative" }}>
        <canvas
          ref={canvasRef}
          style={{ display: "block", width: "100%", cursor: "grab", touchAction: "none" }}
          onPointerDown={(e) => {
            setFollowTool(false);
            dragRef.current = {
              dragging: true,
              startX: e.clientX,
              startY: e.clientY,
              origOffsetX: viewRef.current.offsetX,
              origOffsetY: viewRef.current.offsetY,
            };
          }}
          onPointerMove={(e) => {
            if (!dragRef.current.dragging) return;
            viewRef.current.offsetX = dragRef.current.origOffsetX + (e.clientX - dragRef.current.startX);
            viewRef.current.offsetY = dragRef.current.origOffsetY + (e.clientY - dragRef.current.startY);
            setTrail((t) => [...t]);
          }}
          onPointerUp={() => {
            dragRef.current.dragging = false;
          }}
          onPointerLeave={() => {
            dragRef.current.dragging = false;
          }}
          onWheel={(e) => {
            e.preventDefault();
            const factor = e.deltaY < 0 ? 1.15 : 1 / 1.15;
            zoomBy(factor);
          }}
        />
        <div
          style={{
            position: "absolute",
            bottom: 8,
            left: 10,
            background: "rgba(0,0,0,0.65)",
            padding: "4px 8px",
            borderRadius: 4,
            fontSize: "0.8rem",
            fontFamily: "monospace",
          }}
        >
          X: {(status?.workCoord?.x ?? 0).toFixed(3)} | Y: {(status?.workCoord?.y ?? 0).toFixed(3)} | Z:{" "}
          {(status?.workCoord?.z ?? 0).toFixed(3)}
        </div>
      </Card.Body>
    </Card>
  );
};

export default VisualizerPanel;
