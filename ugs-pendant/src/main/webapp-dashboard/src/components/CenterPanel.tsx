import { useRef, useState } from "react";
import { Nav, Form } from "react-bootstrap";
import Visualizer3D from "./Visualizer3D";
import GcodeEditor from "./GcodeEditor";
import ConsolePanel from "./ConsolePanel";
import MacroEditor from "./MacroEditor";
import { useAppSelector } from "../hooks/useAppSelector";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { consoleActions } from "../store/consoleSlice";
import { uiActions, CenterView } from "../store/uiSlice";
import "./CenterPanel.scss";

const CONSOLE_MIN_HEIGHT = 100;
const CONSOLE_MAX_HEIGHT = 640;

const CenterPanel = () => {
  const dispatch = useAppDispatch();
  const verboseEnabled = useAppSelector((state) => state.console.verboseEnabled);
  // Lifted to Redux (rather than local state) so the RightRail's macro edit
  // button can jump here to the Macros tab without CenterPanel and RightRail
  // needing to know about each other.
  const view = useAppSelector((state) => state.ui.centerView);
  const setView = (next: CenterView) => dispatch(uiActions.setCenterView(next));
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
        <Nav variant="pills" activeKey={view} onSelect={(key) => setView((key as CenterView) ?? "visualize")}>
          <Nav.Item>
            <Nav.Link eventKey="visualize">Visualize</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey="edit">Edit</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey="split">Split</Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey="macros">Macros</Nav.Link>
          </Nav.Item>
        </Nav>

        {/* All stay mounted always (including in split view) so switching modes
            never resets the 3D camera, reloads/re-fetches the editor's content,
            or discards in-progress macro edits - only visibility/layout toggles. */}
        <div className={"centerPanelContentRow " + (view === "split" ? "split" : "")} hidden={view === "macros"}>
          <div className="centerPanelContent" hidden={view === "edit"}>
            <Visualizer3D />
          </div>
          <div className="centerPanelContent" hidden={view === "visualize"}>
            <GcodeEditor />
          </div>
        </div>
        <div className="centerPanelContent" hidden={view !== "macros"}>
          <MacroEditor />
        </div>
      </div>

      <div
        className="centerPanelResizer"
        onPointerDown={onResizeStart}
        onPointerMove={onResizeMove}
        title="Drag to resize the console"
      />
      <div className="centerPanelConsole" style={{ flexBasis: consoleHeight }}>
        <div className="centerPanelConsoleHeader">
          <h6 className="centerPanelHeading">Console</h6>
          <Form.Check
            type="switch"
            id="verbose-toggle"
            label="Verbose"
            checked={verboseEnabled}
            // Gated server-side too (EventsSocket.java only forwards
            // MessageType.VERBOSE traffic to sessions that asked for it) -
            // toggling this off actually stops the extra traffic at the
            // source, not just hides it here.
            onChange={(e) => dispatch(consoleActions.setVerboseEnabled(e.target.checked))}
          />
        </div>
        <ConsolePanel />
      </div>
    </div>
  );
};

export default CenterPanel;
