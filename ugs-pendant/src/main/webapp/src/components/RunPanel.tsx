import {
  faFile,
  faPause,
  faPlay,
  faStop,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Button,
  Col,
  Collapse,
  Container,
  ProgressBar,
  Row,
} from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { useEffect, useState } from "react";
import { fetchFileStatus } from "../store/fileStatusSlice";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { pause, send, stop } from "../services/files";
import "./RunPanel.scss";
import OpenFileModal from "./OpenFileModal";

const getProgressVariant = (state: string) => {
  if (state === "HOLD") {
    return "warning";
  } else if (state === "RUN" || state === "CHECK") {
    return "success";
  }
  return "secondary";
};

const formatTime = (milliseconds: number) => {
  return (
    String(Math.floor((milliseconds / 3600) % 3600)).padStart(2, "0") +
    ":" +
    String(Math.floor((milliseconds / 60) % 60)).padStart(2, "0") +
    ":" +
    String(milliseconds % 60).padStart(2, "0")
  );
};

const getFileName = (filePath: string) => {
  if (filePath === "") {
    return "No file loaded";
  }
  return filePath.replace(/^.*[\\/]/, "");
};

const RunPanel = () => {
  const dispatch = useAppDispatch();
  const fileStatus = useAppSelector((state) => state.fileStatus);
  const status = useAppSelector((state) => state.status);
  const [showOpenFile, setShowOpenFile] = useState(false);

  useEffect(() => {
    dispatch(fetchFileStatus());
  }, [dispatch]);

  useEffect(() => {
    let timer = setInterval(() => {
      if (status.state === "RUN") {
        dispatch(fetchFileStatus());
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [status.state]);

  const secondsRemaining = Math.round(fileStatus.sendRemainingDuration / 1000);

  return (
    <Container style={{ paddingLeft: 0, paddingRight: 0 }}>
      {showOpenFile && (
        <OpenFileModal handleClose={() => setShowOpenFile(false)} />
      )}
      <Row>
        <Col>
          <h3>{getFileName(fileStatus.fileName)}</h3>
          <Collapse
            in={
              status.state === "RUN" ||
              status.state === "CHECK" ||
              status.state === "HOLD"
            }
          >
            <div>
              <p>Time left: {formatTime(secondsRemaining)}</p>
              <ProgressBar
                style={{ height: "3em" }}
                now={fileStatus.completedRowCount}
                min={0}
                max={fileStatus.rowCount}
                variant={getProgressVariant(status.state)}
                animated={status.state === "RUN"}
                label={
                  fileStatus.completedRowCount + " / " + fileStatus.rowCount
                }
              />
            </div>
          </Collapse>
        </Col>
      </Row>
      <Row className="actions">
        {fileStatus.fileName !== "" && (
          <Col xs={4}>
            <Button
              variant="success"
              onClick={() => send()}
              disabled={
                (status.state !== "IDLE" && status.state != "HOLD") ||
                fileStatus.fileName === ""
              }
            >
              <FontAwesomeIcon icon={faPlay} /> Start
            </Button>
          </Col>
        )}

        {(status.state === "RUN" ||
          status.state === "HOLD" ||
          status.state === "CHECK") && (
          <Col xs={4}>
            <Button
              variant="warning"
              disabled={status.state !== "RUN" && status.state !== "CHECK"}
              onClick={() => pause()}
            >
              <FontAwesomeIcon icon={faPause} /> Pause
            </Button>
          </Col>
        )}

        {(status.state === "RUN" ||
          status.state === "HOLD" ||
          status.state === "CHECK") && (
          <Col xs={4}>
            <Button
              variant="danger"
              disabled={
                status.state !== "RUN" &&
                status.state !== "HOLD" &&
                status.state !== "CHECK" &&
                status.state !== "JOG"
              }
              onClick={() => stop()}
            >
              <FontAwesomeIcon icon={faStop} /> Stop
            </Button>
          </Col>
        )}
        {status.state === "IDLE" && (
          <Col xs={4}>
            <Button onClick={() => setShowOpenFile(true)}>
              <FontAwesomeIcon icon={faFile} /> Open
            </Button>
          </Col>
        )}
      </Row>
    </Container>
  );
};

export default RunPanel;
