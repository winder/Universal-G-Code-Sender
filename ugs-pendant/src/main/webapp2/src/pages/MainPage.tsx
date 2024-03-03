import { Button, Col, Container, Nav, NavDropdown, Row } from "react-bootstrap";
import AxisPanel, { AxisType } from "../components/AxisPanel";
import JogPanel from "../components/JogPanel";
import { useAppSelector } from "../hooks/useAppSelector";
import { homeMachine, resetToZero, returnToZero } from "../services/machine";

const isAvailable = (value: number | undefined) => {
  return value !== null && !Number.isNaN(value);
};

const MainPage = () => {
  const state = useAppSelector((state) => state.status.state);

  return (
    <Container style={{ maxWidth: "800px" }}>
      <Row>
        <Col sm="6" style={{ marginBottom: "32px" }}>
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
          <JogPanel />
        </Col>
      </Row>
    </Container>
  );
};

export default MainPage;
