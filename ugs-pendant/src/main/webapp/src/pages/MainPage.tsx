import { Col, Container, Nav, Navbar, Row } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faFile,
  faRobot,
  faRunning,
  faTerminal,
} from "@fortawesome/free-solid-svg-icons";
import { Link, Outlet, useLocation } from "react-router-dom";
import "./MainPage.scss";

const Menu = ({ className }: { className?: string }) => {
  const location = useLocation();

  return (
    <Nav fill variant="pills" style={{ width: "100%" }} className={className}>
      <Nav.Item>
        <Nav.Link
          as={Link}
          eventKey="/jog"
          to="/jog"
          active={location.pathname.startsWith("/jog")}
          className="navLink"
        >
          <FontAwesomeIcon icon={faRunning} size="lg" />
          <br />
          Jog
        </Nav.Link>
      </Nav.Item>
      <Nav.Item>
        <Nav.Link
          as={Link}
          eventKey="/macros"
          to="/macros"
          active={location.pathname.startsWith("/macros")}
          className="navLink"
        >
          <FontAwesomeIcon icon={faRobot} size="lg" />
          <br />
          Macros
        </Nav.Link>
      </Nav.Item>
      <Nav.Item>
        <Nav.Link
          as={Link}
          eventKey="/run"
          to="/run"
          active={location.pathname.startsWith("/run")}
          className="navLink"
        >
          <FontAwesomeIcon icon={faFile} size="lg" />
          <br />
          Run file
        </Nav.Link>
      </Nav.Item>
      <Nav.Item>
        <Nav.Link
          as={Link}
          eventKey="/console"
          to="/console"
          active={location.pathname.startsWith("/console")}
          className="navLink"
        >
          <FontAwesomeIcon icon={faTerminal} size="lg" />
          <br />
          Console
        </Nav.Link>
      </Nav.Item>
    </Nav>
  );
};

const MainPage = () => {
  return (
    <Row className="mainPageRow">
      <Col xs={"auto"} className="d-none d-sm-block sideMenu">
        <Navbar bg="dark" data-bs-theme="dark">
          <Menu className="flex-column" />
        </Navbar>
      </Col>
      <Col className="mainContentCol">
        <Container fluid className="mainContentContainer">
          <Outlet />
        </Container>

        <Navbar
          fixed="bottom"
          bg="dark"
          data-bs-theme="dark"
          style={{ paddingLeft: "10px", paddingRight: "10px" }}
          className="d-block d-sm-none"
        >
          <Menu className="" />
        </Navbar>
      </Col>
    </Row>
  );
};

export default MainPage;
