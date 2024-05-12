import { Button, Col, Container, Nav, Navbar, Row } from "react-bootstrap";
import AxisPanel, { AxisType } from "../components/AxisPanel";
import { useAppSelector } from "../hooks/useAppSelector";
import { homeMachine, resetToZero, returnToZero } from "../services/machine";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFile, faRobot, faRunning } from "@fortawesome/free-solid-svg-icons";
import { Link, Outlet, useLocation } from "react-router-dom";
import "./MainPage.scss";

const Menu = ({ className }: { className?: string }) => {
  const location = useLocation();

  return (
    <Nav
      fill
      variant="pills"
      style={{ width: "100%" }}
      className={className}
    >
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
    </Nav>
  );
};

const MainPage = () => {
  const state = useAppSelector((state) => state.status.state);

  return (
    <Row style={{ margin: 0, height: "100%" }}>
      <Col xs={'auto'} className="d-none d-sm-block sideMenu">
        <Navbar bg="dark" data-bs-theme="dark">
          <Menu className="flex-column" />
        </Navbar>
      </Col>
      <Col style={{ padding: 0 }}>
        <Container style={{ maxWidth: "800px", marginTop: "10px" }}>
          <Row>
            <Col sm="6" style={{ marginBottom: "20px" }}>
              <AxisPanel axisType={AxisType.X} />
              <AxisPanel axisType={AxisType.Y} />
              <AxisPanel axisType={AxisType.Z} />
              <AxisPanel axisType={AxisType.A} />
              <AxisPanel axisType={AxisType.B} />
              <AxisPanel axisType={AxisType.C} />

              <Row className="droActions">
                <Col>
                  <Button
                    variant="secondary"
                    onClick={() => returnToZero()}
                    disabled={state !== "IDLE"}
                  >
                    Return to zero
                  </Button>
                </Col>
                <Col>
                  <Button
                    variant="secondary"
                    onClick={() => homeMachine()}
                    disabled={state !== "IDLE"}
                  >
                    Home machine
                  </Button>
                </Col>
                <Col>
                  <Button
                    variant="secondary"
                    onClick={() => resetToZero()}
                    disabled={state !== "IDLE"}
                  >
                    Zero all axes
                  </Button>
                </Col>
              </Row>
            </Col>
            <Col sm="6">
              <Outlet />
            </Col>
          </Row>
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
