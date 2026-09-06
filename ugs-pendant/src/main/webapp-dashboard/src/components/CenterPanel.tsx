import { useState } from "react";
import { Nav } from "react-bootstrap";
import Visualizer3D from "./Visualizer3D";
import GcodeEditor from "./GcodeEditor";
import "./CenterPanel.scss";

type View = "visualize" | "edit";

const CenterPanel = () => {
  const [view, setView] = useState<View>("visualize");

  return (
    <div className="centerPanel">
      <Nav variant="pills" activeKey={view} onSelect={(key) => setView((key as View) ?? "visualize")}>
        <Nav.Item>
          <Nav.Link eventKey="visualize">Visualize</Nav.Link>
        </Nav.Item>
        <Nav.Item>
          <Nav.Link eventKey="edit">Edit</Nav.Link>
        </Nav.Item>
      </Nav>

      <div className="centerPanelContent">
        {view === "visualize" ? <Visualizer3D /> : <GcodeEditor />}
      </div>
    </div>
  );
};

export default CenterPanel;
