import { useRef, useState } from "react";
import { Nav, Form } from "react-bootstrap";
import Visualizer3D from "./Visualizer3D";
import GcodeEditor from "./GcodeEditor";
import ConsolePanel from "./ConsolePanel";
import MacroEditor from "./MacroEditor";
import ProbePanel from "./ProbePanel";
import { useAppSelector } from "../hooks/useAppSelector";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { consoleActions } from "../store/consoleSlice";
import { uiActions, CenterView, PaneContent } from "../store/uiSlice";
import "./CenterPanel.scss";

const CONSOLE_MIN_HEIGHT = 100;
const CONSOLE_MAX_HEIGHT = 640;
// Macros' own list panel alone is 160px wide in compact mode, so anything
// much smaller than this leaves almost nothing for the actual form/settings
// - this is the floor for BOTH panes (it also caps how far the other side
// can grow via onSplitResizeMove's maxWidth calculation).
const SPLIT_MIN_WIDTH = 400;

const PANE_LABELS: { content: PaneContent; label: string }[] = [
  { content: "visualize", label: "Visualize" },
  { content: "edit", label: "Edit" },
  { content: "macros", label: "Macros" },
  { content: "probe", label: "Probe" },
];

const CenterPanel = () => {
  const dispatch = useAppDispatch();
  const verboseEnabled = useAppSelector((state) => state.console.verboseEnabled);
  // Lifted to Redux (rather than local state) so the RightRail's macro edit
  // button can jump here to the Macros tab without CenterPanel and RightRail
  // needing to know about each other, and so the split pane assignments
  // survive toggling in and out of split mode.
  const view = useAppSelector((state) => state.ui.centerView);
  const splitLeft = useAppSelector((state) => state.ui.splitLeft);
  const splitRight = useAppSelector((state) => state.ui.splitRight);
  const isSplit = view === "split";
  const setView = (next: CenterView) => dispatch(uiActions.setCenterView(next));

  const [consoleHeight, setConsoleHeight] = useState(220);
  const dragStartRef = useRef({ y: 0, height: 0 });

  // null = not yet customized - the left pane stays a true, responsive 50%
  // of the row (via flex-basis: 50%) rather than a fixed pixel amount, so it
  // stays 50/50 across window resizes until the user actually drags the
  // resizer, at which point it becomes a fixed px width like before.
  const [splitLeftWidth, setSplitLeftWidth] = useState<number | null>(null);
  const splitDragRef = useRef({ x: 0, width: 0 });
  const splitRowRef = useRef<HTMLDivElement | null>(null);

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

  const onSplitResizeStart = (e: React.PointerEvent<HTMLDivElement>) => {
    // First drag ever (splitLeftWidth still null, meaning "true 50%") needs
    // a real starting px value to compute deltas from - read the row's
    // actual current width rather than assuming one.
    const rowWidth = splitRowRef.current?.clientWidth ?? SPLIT_MIN_WIDTH * 2;
    const startWidth = splitLeftWidth ?? rowWidth / 2;
    splitDragRef.current = { x: e.clientX, width: startWidth };
    e.currentTarget.setPointerCapture(e.pointerId);
  };

  const onSplitResizeMove = (e: React.PointerEvent<HTMLDivElement>) => {
    if (!e.currentTarget.hasPointerCapture(e.pointerId)) return;
    const delta = e.clientX - splitDragRef.current.x;
    const maxWidth = (splitRowRef.current?.clientWidth ?? SPLIT_MIN_WIDTH * 2) - SPLIT_MIN_WIDTH;
    const next = Math.min(maxWidth, Math.max(SPLIT_MIN_WIDTH, splitDragRef.current.width + delta));
    setSplitLeftWidth(next);
  };

  // The main nav doubles as the left-pane selector while split (it's "the
  // same buttons in the same place" the user already knows from single-view
  // mode), rather than adding a whole separate control for it.
  const onSelectMainNav = (key: string | null) => {
    if (!key) return;
    if (key === "split") {
      // Tapping Split again while already split collapses back to a single
      // full panel showing whatever's currently on the left, rather than
      // being a dead end with no way back to single-view mode.
      setView(isSplit ? splitLeft : "split");
      return;
    }
    if (isSplit) {
      dispatch(uiActions.setSplitLeft(key as PaneContent));
    } else {
      setView(key as CenterView);
    }
  };

  const leftBasis = splitLeftWidth === null ? "50%" : `${splitLeftWidth}px`;

  const contentStyle = (content: PaneContent): React.CSSProperties => {
    if (!isSplit) return {};
    if (content === splitLeft) return { order: 1, flex: `0 0 ${leftBasis}` };
    if (content === splitRight) return { order: 3, flex: "1 1 auto" };
    return {};
  };

  const isVisible = (content: PaneContent) => (isSplit ? content === splitLeft || content === splitRight : view === content);

  return (
    <div className="centerPanel">
      <div className="centerPanelTop">
        {/* Left-pane selector, right-pane selector (only while split), and
            the Split toggle all sit on one row - the right selector's
            leading spacer matches the left pane's current width so it
            lines up directly above the right pane, not on a row of its own. */}
        <div className="centerPanelNavRow">
          <Nav variant="pills" activeKey={isSplit ? splitLeft : view} onSelect={onSelectMainNav} className="centerPanelMainNav">
            {PANE_LABELS.map((p) => (
              <Nav.Item key={p.content}>
                <Nav.Link eventKey={p.content}>{p.label}</Nav.Link>
              </Nav.Item>
            ))}
          </Nav>

          {isSplit && (
            <>
              <div className="centerPanelRightNavSpacer" style={{ flexBasis: leftBasis }} />
              <Nav
                variant="pills"
                activeKey={splitRight}
                onSelect={(key) => key && dispatch(uiActions.setSplitRight(key as PaneContent))}
                className="centerPanelRightNav"
              >
                {PANE_LABELS.map((p) => (
                  <Nav.Item key={p.content}>
                    <Nav.Link eventKey={p.content}>{p.label}</Nav.Link>
                  </Nav.Item>
                ))}
              </Nav>
            </>
          )}

          {/* Never shows as "active" itself - isSplit ? splitLeft : view
              never equals "split", by construction (see onSelectMainNav). */}
          <Nav variant="pills" activeKey={isSplit ? splitLeft : view} onSelect={onSelectMainNav} className="centerPanelSplitNav">
            <Nav.Item>
              <Nav.Link eventKey="split">Split</Nav.Link>
            </Nav.Item>
          </Nav>
        </div>

        {/* All four stay mounted always so switching tabs or split assignment
            never resets the 3D camera, reloads/re-fetches the editor's
            content, or discards in-progress macro edits - only which pane
            (if any) a component is visually placed into changes, via the
            order/flex-basis in contentStyle(). */}
        <div className={"centerPanelContentRow " + (isSplit ? "split" : "")} ref={splitRowRef}>
          <div className="centerPanelContent" style={contentStyle("visualize")} hidden={!isVisible("visualize")}>
            <Visualizer3D />
          </div>
          <div className="centerPanelContent" style={contentStyle("edit")} hidden={!isVisible("edit")}>
            <GcodeEditor />
          </div>
          {isSplit && (
            <div
              className="centerPanelSplitResizer"
              style={{ order: 2 }}
              onPointerDown={onSplitResizeStart}
              onPointerMove={onSplitResizeMove}
              title="Drag to resize the split"
            />
          )}
          <div className="centerPanelContent" style={contentStyle("macros")} hidden={!isVisible("macros")}>
            <MacroEditor compact={isSplit} />
          </div>
          <div className="centerPanelContent" style={contentStyle("probe")} hidden={!isVisible("probe")}>
            <ProbePanel compact={isSplit} />
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
