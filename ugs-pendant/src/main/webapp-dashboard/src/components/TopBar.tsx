import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlugCircleXmark, faRefresh } from "@fortawesome/free-solid-svg-icons";
import { Button } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { disconnect, softReset } from "../services/machine";
import AccessoryState from "./AccessoryState";
import ConnectionWidget from "./ConnectionWidget";
import "./TopBar.scss";

const TopBar = () => {
  const status = useAppSelector((state) => state.status);
  const isDisconnected = status.state === "DISCONNECTED";

  return (
    <div className="topBar">
      <div className="topBarSection">
        <ConnectionWidget />
        {status?.pins?.cycleStart && <AccessoryState title="Cycle start">C</AccessoryState>}
        {status?.pins?.hold && <AccessoryState title="HOLD">H</AccessoryState>}
        {status?.pins?.door && <AccessoryState title="Door">D</AccessoryState>}
      </div>

      <div className="topBarSection">
        <Button variant="warning" disabled={isDisconnected} onClick={() => softReset()} title="Soft reset">
          <FontAwesomeIcon icon={faRefresh} />
        </Button>
        <Button variant="danger" disabled={isDisconnected} onClick={() => disconnect()} title="Disconnect">
          <FontAwesomeIcon icon={faPlugCircleXmark} />
        </Button>
      </div>
    </div>
  );
};

export default TopBar;
