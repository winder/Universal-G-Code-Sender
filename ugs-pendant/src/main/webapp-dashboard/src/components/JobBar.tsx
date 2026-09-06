import {
  faFile,
  faPause,
  faPlay,
  faStop,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, ProgressBar } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { useEffect, useState } from "react";
import { fetchFileStatus } from "../store/fileStatusSlice";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { pause, send, stop } from "../services/files";
import OpenFileModal from "./OpenFileModal";
import "./JobBar.scss";

const getProgressVariant = (state: string) => {
  if (state === "HOLD") return "warning";
  if (state === "RUN" || state === "CHECK") return "success";
  return "secondary";
};

const formatTime = (milliseconds: number) => {
  const totalSeconds = Math.max(0, Math.round(milliseconds / 1000));
  return (
    String(Math.floor(totalSeconds / 3600)).padStart(2, "0") +
    ":" +
    String(Math.floor((totalSeconds / 60) % 60)).padStart(2, "0") +
    ":" +
    String(totalSeconds % 60).padStart(2, "0")
  );
};

const getFileName = (filePath: string) => {
  if (filePath === "") return "No file loaded";
  return filePath.replace(/^.*[\\/]/, "");
};

const JobBar = () => {
  const dispatch = useAppDispatch();
  const fileStatus = useAppSelector((state) => state.fileStatus);
  const status = useAppSelector((state) => state.status);
  const [showOpenFile, setShowOpenFile] = useState(false);

  useEffect(() => {
    dispatch(fetchFileStatus());
  }, [dispatch]);

  useEffect(() => {
    const timer = setInterval(() => {
      if (status.state === "RUN") {
        dispatch(fetchFileStatus());
      }
    }, 1000);
    return () => clearInterval(timer);
  }, [status.state, dispatch]);

  const isRunning =
    status.state === "RUN" || status.state === "HOLD" || status.state === "CHECK";

  return (
    <div className="jobBar">
      {showOpenFile && <OpenFileModal handleClose={() => setShowOpenFile(false)} />}

      <div className="jobFile">{getFileName(fileStatus.fileName)}</div>

      {isRunning && (
        <div className="jobProgress">
          <ProgressBar
            now={fileStatus.completedRowCount}
            min={0}
            max={fileStatus.rowCount || 1}
            variant={getProgressVariant(status.state)}
            animated={status.state === "RUN"}
            label={`${fileStatus.completedRowCount} / ${fileStatus.rowCount}`}
          />
          <span className="jobTimeLeft">{formatTime(fileStatus.sendRemainingDuration)} left</span>
        </div>
      )}

      <div className="jobActions">
        {status.state === "IDLE" && (
          <Button variant="secondary" onClick={() => setShowOpenFile(true)}>
            <FontAwesomeIcon icon={faFile} /> Open
          </Button>
        )}
        {fileStatus.fileName !== "" && (status.state === "IDLE" || status.state === "HOLD") && (
          <Button variant="success" onClick={() => send()}>
            <FontAwesomeIcon icon={faPlay} /> Start
          </Button>
        )}
        {isRunning && (
          <Button variant="warning" disabled={status.state !== "RUN" && status.state !== "CHECK"} onClick={() => pause()}>
            <FontAwesomeIcon icon={faPause} /> Pause
          </Button>
        )}
        {isRunning && (
          <Button variant="danger" onClick={() => stop()}>
            <FontAwesomeIcon icon={faStop} /> Stop
          </Button>
        )}
      </div>
    </div>
  );
};

export default JobBar;
