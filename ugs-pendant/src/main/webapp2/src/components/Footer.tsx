import { faPause, faPlay, faStop } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Nav, Navbar } from "react-bootstrap";
import { cancelSend, pause, send } from "../services/machine";
import { useAppSelector } from "../hooks/useAppSelector";
import "./Footer.scss";

const Footer = () => {
  const state = useAppSelector((state) => state.status.state);

  return (
    <Navbar
      fixed="bottom"
      bg="dark"
      variant="dark"
      style={{ marginBottom: "0px", paddingLeft: "10px" }}
    >
      <Nav variant="pills">
        {state === "IDLE" && (
          <Nav.Item style={{ marginRight: "10px" }}>
            <Button variant="success" onClick={send}>
              <FontAwesomeIcon icon={faPlay} /> Start
            </Button>
          </Nav.Item>
        )}
        {state === "HOLD" && (
          <Nav.Item style={{ marginRight: "10px" }}>
            <Button variant="warning" onClick={send}>
              <FontAwesomeIcon icon={faPlay} /> Resume
            </Button>
          </Nav.Item>
        )}
        {state === "RUN" && (
          <Nav.Item style={{ marginRight: "10px" }}>
            <Button variant="warning" onClick={pause}>
              <FontAwesomeIcon icon={faPause} /> Pause
            </Button>
          </Nav.Item>
        )}
        {(state === "RUN" || state === "HOLD" || state === "JOG") && (
          <Nav.Item>
            <Button variant="danger" onClick={cancelSend}>
              <FontAwesomeIcon icon={faStop} /> Stop
            </Button>
          </Nav.Item>
        )}
      </Nav>
    </Navbar>
  );
};

export default Footer;
