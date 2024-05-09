import { faPause, faPlay, faStop } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Col, Container, Row } from "react-bootstrap";

const RunPanel = () => {
  return (
    <Container style={{ paddingLeft: 0, paddingRight: 0 }}>
      <Row className="actions">
        <Col xs={4} style={{ marginBottom: "20px" }}>
          <Button
            variant="success"
            style={{ height: "100%", width: "100%", minHeight: "62px", padding: "0"}}
            onClick={() => {}}
          >
            <FontAwesomeIcon icon={faPlay} /> Start
          </Button>
        </Col>

        <Col xs={4} style={{ marginBottom: "20px" }}>
          <Button
            variant="warning"
            style={{ height: "100%", width: "100%", minHeight: "62px", padding: "0"}}
            onClick={() => {}}
          >
            <FontAwesomeIcon icon={faPause} /> Pause
          </Button>
        </Col>

        <Col xs={4} style={{ marginBottom: "20px" }}>
          <Button
            variant="danger"
            style={{ height: "100%", width: "100%", minHeight: "62px", padding: "0"}}
            onClick={() => {}}
          >
            <FontAwesomeIcon icon={faStop} /> Pause
          </Button>
        </Col>
      </Row>
    </Container>
  );
};

export default RunPanel;
