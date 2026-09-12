import { useEffect, useRef, useState } from "react";
import * as THREE from "three";
import { OrbitControls } from "three/examples/jsm/controls/OrbitControls.js";
import { Button, ButtonGroup } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { getToolpath, ToolpathSegment } from "../services/visualizer";
import { sendGcode } from "../services/machine";
import "./Visualizer3D.scss";

const RAPID_COLOR = new THREE.Color("#6b7280");
const CUT_COLOR = new THREE.Color("#4ade80");
const ARC_COLOR = new THREE.Color("#7bdcff");
// Matches desktop UGS's own selected-segment highlight (VisualizerUtils.Color.YELLOW).
const HIGHLIGHT_COLOR = new THREE.Color("rgb(237, 255, 0)");
// Matches desktop's GcodeLineColorizer "completed" color (VISUALIZER_OPTION_COMPLETE,
// default rgb(190,190,190) - its alpha isn't reproduced here since this material isn't
// transparent, but the gray-out itself is the part that matters).
const COMPLETED_COLOR = new THREE.Color("rgb(190, 190, 190)");

// The grid with nothing loaded: a fixed 200x200mm square, 10mm per cell.
const DEFAULT_GRID_SIZE = 200;
const GRID_CELL_SIZE = 10;
// How far past the toolpath's own bounds the grid should extend, per side.
const GRID_PADDING = 100;
const X_AXIS_COLOR = "#ff8a8a";
const Y_AXIS_COLOR = "#8affa0";

type Bounds = { minX: number; maxX: number; minY: number; maxY: number };
type ViewPreset = "top" | "bottom" | "left" | "right" | "3d";

const createAxisLabel = (text: string, color: string) => {
  const canvas = document.createElement("canvas");
  canvas.width = 128;
  canvas.height = 128;
  const ctx = canvas.getContext("2d");
  if (ctx) {
    ctx.font = "bold 96px sans-serif";
    ctx.fillStyle = color;
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText(text, 64, 70);
  }
  const texture = new THREE.CanvasTexture(canvas);
  return new THREE.Sprite(new THREE.SpriteMaterial({ map: texture, depthTest: false }));
};

// getToolpath() already reflects an armed "run from" line - the backend
// reads the processed file (see VisualizerResource.getToolpath), which
// applyCommandProcessor/RunFromProcessor itself rewrites, skipped commands
// dropped and the resume preamble included - so no client-side filtering
// belongs here, just coloring.
//
// highlightLine is either the dashboard's 1-based editor line number (the
// user's manual cursor position, 0 = none) or, while a job is running,
// completedRowCount standing in for it (see liveHighlightLine) - both compare
// directly against ToolpathSegment.lineNumber, the backend's 0-based
// GcodeParser command index, with no offset. NOT the same "- 2" conversion
// "run from" needs - traced desktop's two features separately and they use
// different arithmetic:
// RunFromHere.java computes root.getElementIndex(caret) - 1 (elementIndex
// is already 0-based, so that's editorLine - 2 net). EditorListener.java
// passes the raw elementIndex (no extra - 1) to Highlight.setHighlightedLines,
// whose filter (lineNumber > start && lineNumber - 1 <= end) - worked
// through for a single cursor position - reduces to lineNumber == editorLine
// exactly, no offset at all. Confirmed empirically too: applying the run-
// from -2 here highlighted a visibly different segment than desktop did for
// the identical selected line.
//
// The correlation itself keeps working even once a line's armed:
// GcodeStreamWriter embeds each command's original commandNumber as
// metadata in the processed file (see addLine's commandNumber param), and
// GcodeStreamReader reads that same number back rather than recounting
// from scratch - so ToolpathSegment.lineNumber still reflects the
// *original* file's command index even for a command that only survived
// because RunFromProcessor's preamble carried it through.
// completedThroughLine mirrors desktop's GcodeLineColorizer.getColor: any segment whose
// lineNumber is less than it is already-run and gets grayed out (0 = nothing completed
// yet / not currently running, matching GcodeEditor.tsx's own dimThroughLine gate on
// RUN/HOLD/CHECK). It uses the same lineNumber space as highlightLine - see the comment
// above - so no separate offset is needed here either.
const buildToolpathGeometry = (
  segments: ToolpathSegment[],
  highlightLine: number,
  completedThroughLine: number
) => {
  const highlightCommand = highlightLine;

  const positions = new Float32Array(segments.length * 6);
  const colors = new Float32Array(segments.length * 6);

  segments.forEach((segment, i) => {
    const offset = i * 6;
    positions[offset] = segment.start.x;
    positions[offset + 1] = segment.start.y;
    positions[offset + 2] = segment.start.z;
    positions[offset + 3] = segment.end.x;
    positions[offset + 4] = segment.end.y;
    positions[offset + 5] = segment.end.z;

    const color =
      segment.lineNumber === highlightCommand
        ? HIGHLIGHT_COLOR
        : segment.lineNumber < completedThroughLine
          ? COMPLETED_COLOR
          : segment.rapid
            ? RAPID_COLOR
            : segment.arc
              ? ARC_COLOR
              : CUT_COLOR;
    colors[offset] = color.r;
    colors[offset + 1] = color.g;
    colors[offset + 2] = color.b;
    colors[offset + 3] = color.r;
    colors[offset + 4] = color.g;
    colors[offset + 5] = color.b;
  });

  const geometry = new THREE.BufferGeometry();
  geometry.setAttribute("position", new THREE.BufferAttribute(positions, 3));
  geometry.setAttribute("color", new THREE.BufferAttribute(colors, 3));
  return geometry;
};

const Visualizer3D = () => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const toolMarkerRef = useRef<THREE.Mesh | null>(null);
  const sceneRef = useRef<THREE.Scene | null>(null);
  const cameraRef = useRef<THREE.OrthographicCamera | null>(null);
  // Orthographic "zoom level" - the visible height in world units. Set by setView
  // to fit the loaded job, then reapplied on container resize (which only changes
  // aspect ratio, not how much of the job should be visible).
  const frustumSizeRef = useRef(100);
  const applyFrustumRef = useRef(() => {});
  const controlsRef = useRef<OrbitControls | null>(null);
  const rendererRef = useRef<THREE.WebGLRenderer | null>(null);
  const toolpathLinesRef = useRef<THREE.LineSegments | null>(null);
  // The last-fetched segments (already reflecting any armed "run from" line,
  // per getToolpath()) - cached so the cursor-highlight effect below can
  // recolor without re-fetching the toolpath from the server on every cursor
  // move, which doesn't change what the server would return anyway.
  const segmentsRef = useRef<ToolpathSegment[]>([]);
  const boundsSphereRef = useRef<THREE.Sphere | null>(null);
  const gridRef = useRef<THREE.GridHelper | null>(null);
  const xLabelRef = useRef<THREE.Sprite | null>(null);
  const yLabelRef = useRef<THREE.Sprite | null>(null);
  // Set inside the one-time setup effect below (it closes over the scene/refs it
  // needs); called from the toolpath-loading effect to resize/recenter the grid
  // to the loaded file, or put it back to the default size once nothing is loaded.
  const applyGridExtentRef = useRef((_size: number, _centerX: number, _centerY: number) => {});
  const [isEmpty, setIsEmpty] = useState(false);
  const [bounds, setBounds] = useState<Bounds | null>(null);
  const workCoord = useAppSelector((state) => state.status.workCoord);
  const currentState = useAppSelector((state) => state.status.state);
  const isIdle = useAppSelector((state) => state.status.state === "IDLE");
  // Only used to notice "a different file is now loaded" and re-fetch the
  // toolpath - the fetch itself always reads whatever's currently open.
  const fileName = useAppSelector((state) => state.fileStatus.fileName);
  // Not completedRowCount: that's just a count of rows from zero for
  // whatever's currently streaming, which badly undercounts once "run from"
  // starts a stream partway through the file - it'd read 1, 2, 3... while
  // segments keep the original file's line numbers (81, 82, 83...), so
  // nothing would ever compare equal/less-than and neither gray-out nor the
  // live highlight below would show at all. lastCompletedLineNumber is the
  // original file's own line number instead (see FileStatus.ts), correct
  // either way - -1 (nothing completed yet) safely matches/grays nothing.
  const lastCompletedLineNumber = useAppSelector((state) => state.fileStatus.lastCompletedLineNumber);
  const armedRunFromLine = useAppSelector((state) => state.ui.runFromLine);
  const editorCursorLine = useAppSelector((state) => state.ui.editorCursorLine);
  // Same RUN/HOLD/CHECK gate as GcodeEditor.tsx's dimThroughLine - only gray
  // out "already sent" segments while a job's actually streaming, since
  // lastCompletedLineNumber is otherwise just left over from the last job.
  const completedThroughLine =
    currentState === "RUN" || currentState === "HOLD" || currentState === "CHECK" ? lastCompletedLineNumber : 0;
  // Desktop's "yellow = currently transmitted" isn't a separate color at all -
  // it's this same cursor highlight, auto-driven to the just-completed line on
  // every CommandEvent by its (default-on) Follow feature (FollowLineUpdater,
  // SourceMultiviewElement.java) instead of the user's own click. Reproduce
  // that here: while running, the highlight tracks lastCompletedLineNumber
  // live instead of the last manual click - the two never apply at once,
  // since lastCompletedLineNumber === completedThroughLine in that state, so
  // this line is exactly the boundary (equal, not less-than) between gray
  // and normal.
  const liveHighlightLine =
    currentState === "RUN" || currentState === "HOLD" || currentState === "CHECK"
      ? lastCompletedLineNumber
      : editorCursorLine;
  // Bumped specifically once the backend's processed file is actually ready
  // (see uiSlice.ts's comment) - fileName alone isn't enough to re-trigger a
  // fetch here, since it's already set well before that file exists on disk.
  const toolpathVersion = useAppSelector((state) => state.ui.toolpathVersion);

  // Disposes/replaces just the toolpath geometry in the scene, from whatever
  // segments are passed in - shared by the fetch effect (fresh segments) and
  // the recolor effect below (the cached, already-fetched ones), so neither
  // has to duplicate the swap-into-scene bookkeeping.
  const applyToolpathGeometry = (segments: ToolpathSegment[]) => {
    const scene = sceneRef.current;
    if (!scene) return;

    if (toolpathLinesRef.current) {
      scene.remove(toolpathLinesRef.current);
      toolpathLinesRef.current.geometry.dispose();
      toolpathLinesRef.current = null;
    }
    if (segments.length === 0) return;

    // Highlights the cursor's line regardless of whether anything's armed -
    // see buildToolpathGeometry's comment: original command numbers survive
    // into the processed file's segments too, not just the unfiltered one.
    const geometry = buildToolpathGeometry(segments, liveHighlightLine, completedThroughLine);
    const material = new THREE.LineBasicMaterial({ vertexColors: true });
    const toolpathLines = new THREE.LineSegments(geometry, material);
    scene.add(toolpathLines);
    toolpathLinesRef.current = toolpathLines;
  };

  // Re-applies the toolpath geometry (recolor only, no re-fetch) whenever the
  // editor's cursor line or the live "already run"/"currently transmitting"
  // progress changes - none of these change what the server would return,
  // only which segments get highlighted or grayed out.
  useEffect(() => {
    if (segmentsRef.current.length > 0) {
      applyToolpathGeometry(segmentsRef.current);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [liveHighlightLine, completedThroughLine]);

  useEffect(() => {
    if (toolMarkerRef.current) {
      toolMarkerRef.current.position.set(workCoord.x, workCoord.y, workCoord.z);
    }
  }, [workCoord]);

  const setView = (preset: ViewPreset) => {
    const camera = cameraRef.current;
    const renderer = rendererRef.current;
    const sphere = boundsSphereRef.current;
    if (!camera || !renderer || !sphere) return;

    const distance = sphere.radius * 2.2 || 100;
    const center = sphere.center;

    // Orthographic projection: how much of the scene is visible depends only on
    // this frustum size, not on the camera's distance from center - unlike a
    // perspective camera, distance no longer causes any parallax/foreshortening,
    // which is the whole point of switching to it for Top/Left/Right/Bottom.
    frustumSizeRef.current = sphere.radius * 2.4 || 100;
    // Reset any zoom left over from however the user last scrolled/pinched the
    // previous view, so every preset starts from the same predictable framing.
    camera.zoom = 1;
    applyFrustumRef.current();

    // Z is always "up" except for the top/bottom views, which need Y as their
    // screen-up axis (a camera looking straight down/up the up-axis is degenerate).
    if (preset === "top") {
      camera.up.set(0, 1, 0);
      camera.position.set(center.x, center.y, center.z + distance);
    } else if (preset === "bottom") {
      camera.up.set(0, 1, 0);
      camera.position.set(center.x, center.y, center.z - distance);
    } else if (preset === "left") {
      camera.up.set(0, 0, 1);
      camera.position.set(center.x - distance, center.y, center.z);
    } else if (preset === "right") {
      camera.up.set(0, 0, 1);
      camera.position.set(center.x + distance, center.y, center.z);
    } else {
      camera.up.set(0, 0, 1);
      camera.position.set(center.x + distance, center.y - distance, center.z + distance);
    }
    camera.lookAt(center);

    // OrbitControls bakes the camera's up-axis into its internal rotation math at
    // construction time, and keeps its own damped rotation state between drags - so
    // a stale instance can silently pull a freshly-set preset view off-axis (e.g. a
    // "Left" view creeping into a downward tilt after the user had been dragging).
    // Recreating it guarantees the preset view is applied exactly, with no leftover
    // rotation momentum from however the user last left the camera.
    controlsRef.current?.dispose();
    const controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controls.target.copy(center);
    controls.update();
    controlsRef.current = controls;
  };

  const runBoundary = () => {
    if (!bounds) return;
    const commands = [
      `G0 X${bounds.minX.toFixed(3)} Y${bounds.minY.toFixed(3)}`,
      `G0 X${bounds.maxX.toFixed(3)} Y${bounds.minY.toFixed(3)}`,
      `G0 X${bounds.maxX.toFixed(3)} Y${bounds.maxY.toFixed(3)}`,
      `G0 X${bounds.minX.toFixed(3)} Y${bounds.maxY.toFixed(3)}`,
      `G0 X${bounds.minX.toFixed(3)} Y${bounds.minY.toFixed(3)}`,
    ].join("\n");
    sendGcode(commands);
  };

  // One-time scene/camera/renderer setup. Stays mounted for the component's whole
  // lifetime (switching Visualize/Edit/Split no longer tears this down), so the
  // camera position and OrbitControls state survive tab switches.
  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const scene = new THREE.Scene();
    scene.background = new THREE.Color("#111213");
    sceneRef.current = scene;

    // Orthographic rather than perspective: parallel lines stay parallel and
    // sizes don't shrink with distance, so Top/Left/Right/Bottom views are true
    // flat projections (no parallax) instead of looking subtly skewed.
    const camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0.1, 10000);
    camera.up.set(0, 0, 1);
    camera.position.set(100, -100, 100);
    camera.lookAt(0, 0, 0);
    cameraRef.current = camera;

    const renderer = new THREE.WebGLRenderer({ antialias: true });
    container.appendChild(renderer.domElement);
    rendererRef.current = renderer;

    const controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    controlsRef.current = controls;

    const xLabel = createAxisLabel("X", X_AXIS_COLOR);
    const yLabel = createAxisLabel("Y", Y_AXIS_COLOR);
    scene.add(xLabel);
    scene.add(yLabel);
    xLabelRef.current = xLabel;
    yLabelRef.current = yLabel;

    // GridHelper only takes one color for both its center lines, so it can't give
    // the X and Y center lines different colors on its own - drawn as two plain
    // Line objects instead, on top of it (slightly raised in Z to avoid z-fighting).
    const xAxisLine = new THREE.Line(
      new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(), new THREE.Vector3()]),
      new THREE.LineBasicMaterial({ color: X_AXIS_COLOR })
    );
    const yAxisLine = new THREE.Line(
      new THREE.BufferGeometry().setFromPoints([new THREE.Vector3(), new THREE.Vector3()]),
      new THREE.LineBasicMaterial({ color: Y_AXIS_COLOR })
    );
    scene.add(xAxisLine);
    scene.add(yAxisLine);

    const applyGridExtent = (size: number, centerX: number, centerY: number) => {
      if (gridRef.current) {
        scene.remove(gridRef.current);
        gridRef.current.dispose();
      }
      const divisions = Math.max(1, Math.round(size / GRID_CELL_SIZE));
      const grid = new THREE.GridHelper(size, divisions, 0x2f3132, 0x2f3132);
      grid.rotation.x = Math.PI / 2;
      grid.position.set(centerX, centerY, 0);
      scene.add(grid);
      gridRef.current = grid;

      const half = size / 2;
      // These mark true work-coordinate zero (X0/Y0), not wherever the grid itself
      // is currently centered - the grid recenters on the loaded job, but zero
      // doesn't move just because the job isn't drawn around it. The X line runs
      // along Y=0 (spanning the grid's X extent); the Y line runs along X=0
      // (spanning the grid's Y extent) - each sized to the grid, but pinned to 0
      // on the other axis instead of following centerX/centerY.
      xAxisLine.geometry.setFromPoints([
        new THREE.Vector3(centerX - half, 0, 0.05),
        new THREE.Vector3(centerX + half, 0, 0.05),
      ]);
      yAxisLine.geometry.setFromPoints([
        new THREE.Vector3(0, centerY - half, 0.05),
        new THREE.Vector3(0, centerY + half, 0.05),
      ]);

      const labelScale = Math.max(size * 0.06, 8);
      xLabel.position.set(centerX + half + labelScale, 0, 1);
      xLabel.scale.set(labelScale, labelScale, 1);
      yLabel.position.set(0, centerY + half + labelScale, 1);
      yLabel.scale.set(labelScale, labelScale, 1);
    };
    applyGridExtentRef.current = applyGridExtent;
    applyGridExtent(DEFAULT_GRID_SIZE, 0, 0);

    // A cone pointing straight down at the tool position, tip-first - closer to
    // how an actual bit/torch looks than a plain ball. ConeGeometry's tip points
    // along +Y by default; rotate it onto -Z (down, since Z is up in this scene)
    // then shift it so the tip - not the geometric center - sits at the mesh's
    // position, so setting toolMarker.position to the current coordinate puts the
    // tip exactly there with the body rising above it.
    const toolMarkerHeight = 8;
    const toolMarkerGeometry = new THREE.ConeGeometry(2, toolMarkerHeight, 16);
    toolMarkerGeometry.rotateX(-Math.PI / 2);
    toolMarkerGeometry.translate(0, 0, toolMarkerHeight / 2);
    const toolMarker = new THREE.Mesh(toolMarkerGeometry, new THREE.MeshBasicMaterial({ color: "#ffd400" }));
    scene.add(toolMarker);
    toolMarkerRef.current = toolMarker;

    const applyFrustum = () => {
      const { clientWidth, clientHeight } = container;
      if (clientWidth === 0 || clientHeight === 0) return;
      const aspect = clientWidth / clientHeight;
      const size = frustumSizeRef.current;
      camera.left = (-size * aspect) / 2;
      camera.right = (size * aspect) / 2;
      camera.top = size / 2;
      camera.bottom = -size / 2;
      camera.updateProjectionMatrix();
    };
    applyFrustumRef.current = applyFrustum;

    const resize = () => {
      const { clientWidth, clientHeight } = container;
      if (clientWidth === 0 || clientHeight === 0) return;
      applyFrustum();
      renderer.setSize(clientWidth, clientHeight);
    };
    resize();
    const resizeObserver = new ResizeObserver(resize);
    resizeObserver.observe(container);

    let animationFrame: number;
    const animate = () => {
      controlsRef.current?.update();
      renderer.render(scene, camera);
      animationFrame = requestAnimationFrame(animate);
    };
    animate();

    return () => {
      cancelAnimationFrame(animationFrame);
      resizeObserver.disconnect();
      controlsRef.current?.dispose();
      renderer.dispose();
      toolpathLinesRef.current?.geometry.dispose();
      gridRef.current?.dispose();
      xLabel.material.map?.dispose();
      xLabel.material.dispose();
      yLabel.material.map?.dispose();
      yLabel.material.dispose();
      xAxisLine.geometry.dispose();
      xAxisLine.material.dispose();
      yAxisLine.geometry.dispose();
      yAxisLine.material.dispose();
      container.removeChild(renderer.domElement);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Re-fetches and rebuilds the toolpath geometry whenever a different file is
  // opened, the armed "run from" line changes, or the backend reports the
  // processed file is actually ready (toolpathVersion - see its comment in
  // uiSlice.ts). fileName alone used to be the only trigger, but it's set as
  // soon as the file starts opening, well before VisualizerResource's
  // processed file exists on disk - a fetch right then could come back
  // empty with nothing left to retry it once the file was actually ready,
  // which is exactly what made the very first open of a file (but not a
  // subsequent reload) fail to visualize. Arming/resetting a "run from" line
  // needs a fresh fetch too, since that rewrites the processed file the same
  // way (see VisualizerResource.getToolpath's doc comment) - and framing/
  // bounds intentionally follow whatever's actually visible (the now-
  // server-filtered set), so the camera reframes on arming too, confirmed
  // that's what desktop's own visualizer does as well.
  useEffect(() => {
    const scene = sceneRef.current;
    if (!scene) return;

    setIsEmpty(false);
    setBounds(null);
    boundsSphereRef.current = null;

    if (toolpathLinesRef.current) {
      scene.remove(toolpathLinesRef.current);
      toolpathLinesRef.current.geometry.dispose();
      toolpathLinesRef.current = null;
    }

    getToolpath().then((segments) => {
      segmentsRef.current = segments;
      if (segments.length === 0) {
        setIsEmpty(true);
        applyGridExtentRef.current(DEFAULT_GRID_SIZE, 0, 0);
        return;
      }

      applyToolpathGeometry(segments);

      // Frame/bound on the actual cutting moves only, not rapids: a big safe-Z
      // retract or a "return to X0 Y0" at the end of a file would otherwise drag
      // the framing center and the min/max readout out over empty space that was
      // never really part of the job, and make side views look tilted (the camera
      // ends up centered high above the Z=0 floor grid instead of near the part).
      const cutPoints = new THREE.Box3();
      const expandWith = (segment: ToolpathSegment) => {
        cutPoints.expandByPoint(new THREE.Vector3(segment.start.x, segment.start.y, segment.start.z));
        cutPoints.expandByPoint(new THREE.Vector3(segment.end.x, segment.end.y, segment.end.z));
      };
      segments.filter((segment) => !segment.rapid).forEach(expandWith);
      // An all-rapid file has no cutting moves to frame on - fall back to
      // every segment's own extent so the camera still frames something.
      if (cutPoints.isEmpty()) {
        segments.forEach(expandWith);
      }

      setBounds({
        minX: cutPoints.min.x,
        maxX: cutPoints.max.x,
        minY: cutPoints.min.y,
        maxY: cutPoints.max.y,
      });

      // Grid grows to fit the loaded job, padded out on every side, so the whole
      // part sits comfortably inside it rather than floating over a generic
      // 200x200 square (or spilling off the edge of one, for anything bigger).
      if (!cutPoints.isEmpty()) {
        const width = cutPoints.max.x - cutPoints.min.x;
        const height = cutPoints.max.y - cutPoints.min.y;
        const size = Math.max(width, height) + GRID_PADDING * 2;
        const centerX = (cutPoints.min.x + cutPoints.max.x) / 2;
        const centerY = (cutPoints.min.y + cutPoints.max.y) / 2;
        applyGridExtentRef.current(size, centerX, centerY);
      } else {
        applyGridExtentRef.current(DEFAULT_GRID_SIZE, 0, 0);
      }

      const sphere = new THREE.Sphere();
      cutPoints.getBoundingSphere(sphere);
      if (sphere.radius > 0) {
        boundsSphereRef.current = sphere;
        setView("3d");
      }
    }).catch(() => {
      setIsEmpty(true);
      applyGridExtentRef.current(DEFAULT_GRID_SIZE, 0, 0);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fileName, armedRunFromLine, toolpathVersion]);

  return (
    <div className="visualizer3D">
      <div className="visualizer3DToolbar">
        <ButtonGroup>
          <Button variant="outline-secondary" onClick={() => setView("top")}>
            Top
          </Button>
          <Button variant="outline-secondary" onClick={() => setView("left")}>
            Left
          </Button>
          <Button variant="outline-secondary" onClick={() => setView("right")}>
            Right
          </Button>
          <Button variant="outline-secondary" onClick={() => setView("bottom")}>
            Bottom
          </Button>
          <Button variant="outline-secondary" onClick={() => setView("3d")}>
            3D
          </Button>
        </ButtonGroup>

        {bounds && (
          <div className="visualizer3DBounds">
            X: {bounds.minX.toFixed(1)} &rarr; {bounds.maxX.toFixed(1)} &nbsp; Y:{" "}
            {bounds.minY.toFixed(1)} &rarr; {bounds.maxY.toFixed(1)}
          </div>
        )}

        <Button
          className="visualizer3DBoundary"
          variant="outline-primary"
          disabled={!bounds || !isIdle}
          onClick={runBoundary}
          title="Rapid the machine around the toolpath's bounding box at the current Z, to check stock/part alignment before running"
        >
          Run boundary
        </Button>
      </div>

      <div className="visualizer3DBody">
        {isEmpty && <div className="visualizer3DEmpty">No file loaded to visualize.</div>}
        <div className="visualizer3DCanvas" ref={containerRef} />
      </div>
    </div>
  );
};

export default Visualizer3D;
