import {
  faPlugCircleXmark,
  faRefresh,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import { Button, Nav, Navbar } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { disconnect, softReset } from "../services/machine";
import AccessoryState from "./AccessoryState";
import ControllerState from "./ControllerState";

const Header = () => {
  const [activeKey, setActiveKey] = useState("/home");
  const isConnected = useAppSelector((state) => state.socket.isConnected);
  const status = useAppSelector((state) => state.status);

  return (
    <Navbar
      fixed="top"
      bg="dark"
      variant="dark"
      style={{ marginBottom: "0px" }}
    >
      <Nav
        activeKey={activeKey}
        onSelect={(key) => setActiveKey(key ?? "/home")}
        navbar
        className="container-fluid"
      >
        <Nav.Item>
          <ControllerState />
        </Nav.Item>

        <Nav.Item>
          {status?.pins?.probe && <AccessoryState title="Probe">P</AccessoryState>}
          {status?.pins?.cycleStart && <AccessoryState title="Cycle start">C</AccessoryState>}
          {status?.pins?.hold && <AccessoryState title="HOLD">H</AccessoryState>}
          {status?.pins?.door && <AccessoryState title="Door">D</AccessoryState>}
          {status?.pins?.x && <AccessoryState>X</AccessoryState>}
          {status?.pins?.y && <AccessoryState>Y</AccessoryState>}
          {status?.pins?.z && <AccessoryState>Z</AccessoryState>}
          {status?.pins?.a && <AccessoryState>A</AccessoryState>}
          {status?.pins?.b && <AccessoryState>B</AccessoryState>}
          {status?.pins?.c && <AccessoryState>C</AccessoryState>}
        </Nav.Item>

        {isConnected && status?.state !== "DISCONNECTED" && (
          <Nav.Item className="ml-auto">
            <Button
              variant="warning"
              onClick={() => softReset()}
              style={{ marginRight: "12px" }}
            >
              <FontAwesomeIcon icon={faRefresh} />
            </Button>

            <Button variant="danger" onClick={() => disconnect()}>
              <FontAwesomeIcon icon={faPlugCircleXmark} />
            </Button>
          </Nav.Item>
        )}
      </Nav>
    </Navbar>
  );
};

export default Header;
