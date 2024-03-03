import {
  faCaretDown,
  faCaretLeft,
  faCaretRight,
  faCaretUp,
  faChevronLeft,
  faChevronRight,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useMemo } from "react";
import { Button, Col, Container, Form, Row } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { jog } from "../services/machine";
import "./JogPanel.scss";
import RangeSlider from "./RangeSlider";
import { setSettings } from "../store/settingsSlice";
import { useDispatch } from "react-redux";
import { useAppDispatch } from "../hooks/useAppDispatch";

const JogPanel = () => {
  const dispatch = useAppDispatch();
  const currentState = useAppSelector((state) => state.status.state);
  const settings = useAppSelector((state) => state.settings);
  const isEnabled = useMemo(
    () => currentState === "IDLE" || currentState === "JOG",
    [currentState]
  );

  return (
    <Container>
      <Row className="jogRow">
        <Col className="jogColumn">
          <Button
            variant="secondary"
            className="jogButtonSmall"
            onClick={() => jog(-1, 1, 0)}
            disabled={!isEnabled}
          >
            <FontAwesomeIcon icon={faChevronLeft} transform={{ rotate: 45 }} />
          </Button>
        </Col>
        <Col className="jogColumn">
          <Button
            variant="secondary"
            className="jogButton"
            onClick={() => jog(0, +1, 0)}
            style={{
              borderBottomLeftRadius: 0,
              borderBottomRightRadius: 0,
              borderBottomStyle: "none",
            }}
            disabled={!isEnabled}
          >
            <FontAwesomeIcon icon={faCaretUp} size="xl" />
            <br />
            Y+
          </Button>
        </Col>
        <Col className="jogColumn">
          <Button
            variant="secondary"
            className="jogButtonSmall"
            onClick={() => jog(1, 1, 0)}
            disabled={!isEnabled}
          >
            <FontAwesomeIcon
              icon={faChevronRight}
              transform={{ rotate: -45 }}
            />
          </Button>
        </Col>
        <Col className="jogColumn" style={{ marginLeft: "16px" }}>
          <Button
            variant="secondary"
            className="jogButton"
            onClick={() => jog(0, 0, -1)}
            disabled={!isEnabled}
          >
            <FontAwesomeIcon icon={faCaretUp} size="xl" />
            <br />Z -
          </Button>
        </Col>
      </Row>

      <Row className="jogRow">
        <Col className="jogColumn">
          <Button
            variant="secondary"
            className="jogButton"
            onClick={() => jog(-1, 0, 0)}
            style={{
              borderBottomRightRadius: 0,
              borderTopRightRadius: 0,
              borderRightStyle: "none",
            }}
            disabled={!isEnabled}
          >
            <FontAwesomeIcon icon={faCaretLeft} size="xl" /> X-
          </Button>
        </Col>
        <Col
          className="jogColumn"
          style={{
            backgroundColor: isEnabled ? "white" : "#f7f7f7",
            marginTop: 0,
            marginBottom: 0,
          }}
        >
          &nbsp;
        </Col>
        <Col className="jogColumn">
          <Button
            variant="secondary"
            className="jogButton"
            onClick={() => jog(1, 0, 0)}
            style={{
              borderBottomLeftRadius: 0,
              borderTopLeftRadius: 0,
              borderLeftStyle: "none",
            }}
            disabled={!isEnabled}
          >
            X+ <FontAwesomeIcon icon={faCaretRight} size="xl" />
          </Button>
        </Col>
        <Col className="jogColumn" style={{ marginLeft: "16px" }}></Col>
      </Row>

      <Row className="jogRow">
        <Col className="jogColumn">
          <Button
            variant="secondary"
            className="jogButtonSmall"
            onClick={() => jog(-1, -1, 0)}
            disabled={!isEnabled}
          >
            <FontAwesomeIcon icon={faChevronLeft} transform={{ rotate: -45 }} />
          </Button>
        </Col>
        <Col className="jogColumn">
          <Button
            variant="secondary"
            className="jogButton"
            onClick={() => jog(0, -1, 0)}
            style={{
              borderTopLeftRadius: 0,
              borderTopRightRadius: 0,
              borderTopStyle: "none",
            }}
            disabled={!isEnabled}
          >
            Y-
            <br />
            <FontAwesomeIcon icon={faCaretDown} size="xl" />
          </Button>
        </Col>
        <Col className="jogColumn">
          <Button
            variant="secondary"
            className="jogButtonSmall"
            onClick={() => jog(1, -1, 0)}
            disabled={!isEnabled}
          >
            <FontAwesomeIcon icon={faChevronRight} transform={{ rotate: 45 }} />
          </Button>
        </Col>

        <Col className="jogColumn" style={{ marginLeft: "16px" }}>
          <Button
            variant="secondary"
            className="jogButton"
            onClick={() => jog(0, 0, -1)}
            disabled={!isEnabled}
          >
            Z -
            <br />
            <FontAwesomeIcon icon={faCaretDown} size="xl" />
          </Button>
        </Col>
      </Row>
      <Row>
        <Col sm="12">
          <RangeSlider
            disabled={!isEnabled}
            value={settings.jogStepSizeXY}
            min={1}
            max={1000}
            step={1}
            onChange={(value) =>
              dispatch(setSettings({ ...settings, jogStepSizeXY: value }))
            }
          />
        </Col>
        <Col sm="12">
          <Form.Range />
        </Col>
      </Row>
    </Container>
  );
};

export default JogPanel;
