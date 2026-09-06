import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faHome,
  faLocationCrosshairs,
  faPlugCircleXmark,
  faRefresh,
  faRotateLeft,
} from "@fortawesome/free-solid-svg-icons";
import { Button } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import {
  disconnect,
  homeMachine,
  resetToZero,
  returnToZero,
  softReset,
} from "../services/machine";
import AccessoryState from "./AccessoryState";
import ControllerState from "./ControllerState";
import "./TopBar.scss";

const TopBar = () => {
  const status = useAppSelector((state) => state.status);
  const isIdle = status.state === "IDLE";

  return (
    <div className="topBar">
      <div className="topBarSection">
        <ControllerState />
        {status?.pins?.probe && <AccessoryState title="Probe">P</AccessoryState>}
        {status?.pins?.cycleStart && <AccessoryState title="Cycle start">C</AccessoryState>}
        {status?.pins?.hold && <AccessoryState title="HOLD">H</AccessoryState>}
        {status?.pins?.door && <AccessoryState title="Door">D</AccessoryState>}
      </div>

      <div className="topBarSection topBarActions">
        <Button variant="secondary" disabled={!isIdle} onClick={() => homeMachine()}>
          <FontAwesomeIcon icon={faHome} /> Home
        </Button>
        <Button variant="secondary" disabled={!isIdle} onClick={() => resetToZero()}>
          <FontAwesomeIcon icon={faLocationCrosshairs} /> Zero all
        </Button>
        <Button variant="secondary" disabled={!isIdle} onClick={() => returnToZero()}>
          <FontAwesomeIcon icon={faRotateLeft} /> Return to zero
        </Button>
      </div>

      <div className="topBarSection">
        <Button variant="warning" onClick={() => softReset()} title="Soft reset">
          <FontAwesomeIcon icon={faRefresh} />
        </Button>
        <Button variant="danger" onClick={() => disconnect()} title="Disconnect">
          <FontAwesomeIcon icon={faPlugCircleXmark} />
        </Button>
      </div>
    </div>
  );
};

export default TopBar;
