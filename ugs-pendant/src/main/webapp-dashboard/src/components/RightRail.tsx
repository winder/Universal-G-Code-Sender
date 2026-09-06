import { useState } from "react";
import { Nav } from "react-bootstrap";
import ConsolePanel from "./ConsolePanel";
import MacrosPanel from "./MacrosPanel";
import "./RightRail.scss";

type Tab = "console" | "macros";

const RightRail = () => {
  const [tab, setTab] = useState<Tab>("console");

  return (
    <div className="rightRail">
      <Nav variant="pills" activeKey={tab} onSelect={(key) => setTab((key as Tab) ?? "console")}>
        <Nav.Item>
          <Nav.Link eventKey="console">Console</Nav.Link>
        </Nav.Item>
        <Nav.Item>
          <Nav.Link eventKey="macros">Macros</Nav.Link>
        </Nav.Item>
      </Nav>

      <div className="rightRailContent">
        {tab === "console" ? <ConsolePanel /> : <MacrosPanel />}
      </div>
    </div>
  );
};

export default RightRail;
