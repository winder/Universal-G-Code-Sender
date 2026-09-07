import {
  faFile,
  faPause,
  faPlay,
  faStop,
  faXmark,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, ProgressBar } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { useEffect, useState } from "react";
import { fetchFileStatus } from "../store/fileStatusSlice";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { closeFile, pause, send, stop } from "../services/files";
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
  // Jogging is also something you need to be able to abort - the backend already
  // handles this correctly (sends GRBL's real-time jog-cancel byte instead of a
  // full reset when it sees state JOG), this button just wasn't enabled for it.
  const isStoppable = isRunning || status.state === "JOG";

  return (
    <div className="jobBar">
      {showOpenFile && <OpenFileModal handleClose={() => setShowOpenFile(false)} />}

      <div className="jobFile">{getFileName(fileStatus.fileName)}</div>

      {fileStatus.fileName !== "" && (
        <div className="jobProgress">
          <ProgressBar
            now={fileStatus.completedRowCount}
            min={0}
            max={fileStatus.rowCount || 1}
            variant={getProgressVariant(status.state)}
            animated={status.state === "RUN"}
            label={`${fileStatus.completedRowCount} / ${fileStatus.rowCount}`}
          />
          <div className="jobSendStatus">
            <span className="jobSendStatusLabel">Send status:</span>
            <span>{status.state}</span>
            <span>{fileStatus.completedRowCount}/{fileStatus.rowCount} lines</span>
            <span>{fileStatus.remainingRowCount} remaining</span>
            <span>elapsed {formatTime(fileStatus.sendDuration)}</span>
            <span>{formatTime(fileStatus.sendRemainingDuration)} left</span>
          </div>
        </div>
      )}

      {/* All four always render (only `disabled` changes) so the bar's width/height
          never jumps around as the machine moves between states - e.g. a jog
          briefly puts the controller in "JOG", which used to hide every button here
          at once and made the whole bar visibly resize. */}
      <div className="jobActions">
        <Button variant="secondary" disabled={status.state !== "IDLE"} onClick={() => setShowOpenFile(true)}>
          <FontAwesomeIcon icon={faFile} /> Open
        </Button>
        <Button
          variant="secondary"
          disabled={fileStatus.fileName === "" || status.state !== "IDLE"}
          onClick={() => closeFile()}
        >
          <FontAwesomeIcon icon={faXmark} /> Close
        </Button>
        <Button
          variant="success"
          disabled={fileStatus.fileName === "" || (status.state !== "IDLE" && status.state !== "HOLD")}
          onClick={() => send()}
        >
          <FontAwesomeIcon icon={faPlay} /> Start
        </Button>
        <Button
          variant="warning"
          disabled={status.state !== "RUN" && status.state !== "CHECK"}
          onClick={() => pause()}
        >
          <FontAwesomeIcon icon={faPause} /> Pause
        </Button>
        <Button variant="danger" disabled={!isStoppable} onClick={() => stop()}>
          <FontAwesomeIcon icon={faStop} /> Stop
        </Button>
      </div>
    </div>
  );
};

export default JobBar;
