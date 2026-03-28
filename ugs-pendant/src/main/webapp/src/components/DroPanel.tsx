import { Button, Col, Container, Row } from "react-bootstrap";
import AxisPanel, { AxisType } from "./AxisPanel";
import { homeMachine, resetToZero, returnToZero } from "../services/machine";
import { useAppSelector } from "../hooks/useAppSelector";

const DroPanel = () => {
  const state = useAppSelector((state) => state.status.state);

  return (
    <Container style={{padding: 0}}>
      <Row className="droRow">
        <Col className="droColumn">
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
      </Row>
    </Container>
  );
};

export default DroPanel;