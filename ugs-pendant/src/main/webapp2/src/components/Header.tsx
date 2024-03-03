import {
  faPause,
  faPlugCircleXmark,
  faRefresh,
  faStop,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useState } from "react";
import { Navbar, Nav, NavLink, Button } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { disconnect, softReset } from "../services/machine";
import ControllerState from "./ControllerState";

const Header = () => {
  const [activeKey, setActiveKey] = useState("/home");
  const isConnected = useAppSelector((state) => state.socket.isConnected);
  const state = useAppSelector((state) => state.status.state);

  return (
    <>
      <Navbar bg="dark" variant="dark" style={{ marginBottom: "0px" }}>
        <Nav
          activeKey={activeKey}
          onSelect={(key) => setActiveKey(key ?? "/home")}
          navbar
          className="container-fluid"
        >
          <Nav.Item>
            <ControllerState />
          </Nav.Item>


          {isConnected && state !== "DISCONNECTED" && (
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
    </>
  );
};

export default Header;
