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
import { Button, Col, Container, Row } from "react-bootstrap";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { jog } from "../services/machine";
import { setSettings } from "../store/settingsSlice";
import "./JogPanel.scss";
import StepSize from "./StepSize";
import FeedRate from "./FeedRate";

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
            onClick={() => jog(0, 0, 1)}
            disabled={!isEnabled}
          >
            <FontAwesomeIcon icon={faCaretUp} size="xl" />
            <br />Z +
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
      <Row style={{ marginTop: "20px" }}>
        <Col style={{ paddingLeft: "0px" }}>
          X/Y step:
          <StepSize
            value={settings.jogStepSizeXY}
            onChange={(value) =>
              dispatch(setSettings({ ...settings, jogStepSizeXY: value }))
            }
          />
        </Col>
        {settings.useZStepSize && (
          <Col style={{ paddingLeft: "0px" }}>
            Z step:
            <br />
            <StepSize
              value={settings.jogStepSizeZ}
              onChange={(value) =>
                dispatch(setSettings({ ...settings, jogStepSizeZ: value }))
              }
            />
          </Col>
        )}

        <Col style={{ paddingLeft: "0px", paddingRight: "0px" }}>
          Feed rate:
          <FeedRate
            value={settings.jogFeedRate}
            onChange={(value) =>
              dispatch(setSettings({ ...settings, jogFeedRate: value }))
            }
          />
        </Col>
      </Row>
    </Container>
  );
};

export default JogPanel;
