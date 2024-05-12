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
          {status?.pins?.probe && <AccessoryState>P</AccessoryState>}
          {status?.pins?.cycleStart && <AccessoryState>C</AccessoryState>}
          {status?.pins?.hold && (
            <Nav.Item>
              <AccessoryState>H</AccessoryState>
            </Nav.Item>
          )}
          {status?.pins?.door && (
            <Nav.Item>
              <AccessoryState>D</AccessoryState>
            </Nav.Item>
          )}

          {status?.pins?.x && (
            <Nav.Item>
              <AccessoryState>X</AccessoryState>
            </Nav.Item>
          )}
          {status?.pins?.z && (
            <Nav.Item>
              <AccessoryState>Y</AccessoryState>
            </Nav.Item>
          )}
          {status?.pins?.a && (
            <Nav.Item>
              <AccessoryState>Z</AccessoryState>
            </Nav.Item>
          )}
          {status?.pins?.b && (
            <Nav.Item>
              <AccessoryState>A</AccessoryState>
            </Nav.Item>
          )}
          {status?.pins?.c && (
            <Nav.Item>
              <AccessoryState>B</AccessoryState>
            </Nav.Item>
          )}
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
