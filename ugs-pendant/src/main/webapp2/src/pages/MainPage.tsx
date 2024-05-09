import { Button, Col, Container, Nav, Navbar, Row } from "react-bootstrap";
import AxisPanel, { AxisType } from "../components/AxisPanel";
import { useAppSelector } from "../hooks/useAppSelector";
import { homeMachine, resetToZero, returnToZero } from "../services/machine";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFile, faRobot, faRunning } from "@fortawesome/free-solid-svg-icons";
import { Link, Outlet, useLocation } from "react-router-dom";

const MainPage = () => {
  const state = useAppSelector((state) => state.status.state);
  const location = useLocation();

  return (
    <Container>
      <Container style={{ maxWidth: "800px", marginTop: "10px" }}>
        <Row style={{ marginBottom: "5em" }}>
          <Col sm="6" style={{ marginBottom: "20px" }}>
            <AxisPanel axisType={AxisType.X} />
            <AxisPanel axisType={AxisType.Y} />
            <AxisPanel axisType={AxisType.Z} />
            <AxisPanel axisType={AxisType.A} />
            <AxisPanel axisType={AxisType.B} />
            <AxisPanel axisType={AxisType.C} />

            <Row className="actions">
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
        <Nav fill variant="pills" style={{ width: "100%" }}>
          <Nav.Item>
            <Nav.Link as={Link} eventKey="/jog" to="/jog" active={location.pathname.startsWith("/jog")}>
              <FontAwesomeIcon icon={faRunning} size="lg" />
              <br />
              Jog
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link as={Link} eventKey="/macros" to="/macros" active={location.pathname.startsWith("/macros")}>
              <FontAwesomeIcon icon={faRobot} size="lg" />
              <br />
              Macros
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link as={Link} eventKey="/run" to="/run" active={location.pathname.startsWith("/run")}>
              <FontAwesomeIcon icon={faFile} size="lg" />
              <br />
              Run file
            </Nav.Link>
          </Nav.Item>
        </Nav>
      </Navbar>
    </Container>
  );
};

export default MainPage;
