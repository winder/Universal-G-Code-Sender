import { useRef, useState } from "react";
import { Nav } from "react-bootstrap";
import Visualizer3D from "./Visualizer3D";
import GcodeEditor from "./GcodeEditor";
import ConsolePanel from "./ConsolePanel";
import "./CenterPanel.scss";

type View = "visualize" | "edit" | "split";

const CONSOLE_MIN_HEIGHT = 100;
const CONSOLE_MAX_HEIGHT = 640;

const CenterPanel = () => {
  const [view, setView] = useState<View>("visualize");
  const [consoleHeight, setConsoleHeight] = useState(220);
  const dragStartRef = useRef({ y: 0, height: 0 });

  const onResizeStart = (e: React.PointerEvent<HTMLDivElement>) => {
    dragStartRef.current = { y: e.clientY, height: consoleHeight };
    e.currentTarget.setPointerCapture(e.pointerId);
  };

  const onResizeMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!e.currentTarget.hasPointerCapture(e.pointerId)) return;
    // Dragging the handle up should grow the console (it sits below the
    // visualizer/editor), so height moves opposite to the pointer's Y delta.
    const delta = dragStartRef.current.y - e.clientY;
    const next = Math.min(CONSOLE_MAX_HEIGHT, Math.max(CONSOLE_MIN_HEIGHT, dragStartRef.current.height + delta));
    setConsoleHeight(next);
  };

  return (
    <div className="centerPanel">
      <div className="centerPanelTop">
        <Nav variant="pills" activeKey={view} onSelect={(key) => setView((key as View) ?? "visualize")}>
          <Nav.Item>
            <Nav.Link eventKey="visualize">Visualize</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey="edit">Edit</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey="split">Split</Nav.Link>
          </Nav.Item>
        </Nav>

        {/* Both stay mounted always (including in split view) so switching modes
            never resets the 3D camera or reloads/re-fetches the editor's content -
            only visibility/layout toggles. */}
        <div className={"centerPanelContentRow " + (view === "split" ? "split" : "")}>
          <div className="centerPanelContent" hidden={view === "edit"}>
            <Visualizer3D />
          </div>
          <div className="centerPanelContent" hidden={view === "visualize"}>
            <GcodeEditor />
          </div>
        </div>
      </div>

      <div
        className="centerPanelResizer"
        onPointerDown={onResizeStart}
        onPointerMove={onResizeMove}
        title="Drag to resize the console"
      />
      <div className="centerPanelConsole" style={{ flexBasis: consoleHeight }}>
        <h6 className="centerPanelHeading">Console</h6>
        <ConsolePanel />
      </div>
    </div>
  );
};

export default CenterPanel;
