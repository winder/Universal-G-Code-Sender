import { Button } from "react-bootstrap";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import {
  homeMachine,
  killAlarm,
  resetToZero,
  returnToZero,
  sendGcode,
  softReset,
} from "../services/machine";
import { fetchStatus } from "../store/statusSlice";
import "./Toolbox.scss";

// Mirrors UGS's own "Toolbox" plugin (ugs-platform-plugin-toolbox) default action set -
// ResetCoordinatesToZeroAction, ReturnToZeroAction, SoftResetAction, HomeAction,
// UnlockAction, GetStateAction, CheckModeAction. That selection is stored via NetBeans
// Preferences in the desktop app's userdir, which isn't reachable from this lightweight
// pendant server, so this list is fixed rather than read live from your Toolbox settings.
const Toolbox = () => {
  const dispatch = useAppDispatch();
  const isIdle = useAppSelector((state) => state.status.state === "IDLE");

  return (
    <div className="toolbox">
      <Button variant="secondary" disabled={!isIdle} onClick={() => resetToZero()}>
        Reset zero
      </Button>
      <Button variant="secondary" disabled={!isIdle} onClick={() => returnToZero()}>
        Return to zero
      </Button>
      <Button variant="secondary" onClick={() => softReset()}>
        Soft reset
      </Button>
      <Button variant="secondary" disabled={!isIdle} onClick={() => homeMachine()}>
        Home machine
      </Button>
      <Button variant="secondary" onClick={() => killAlarm()}>
        Unlock
      </Button>
      <Button variant="secondary" onClick={() => dispatch(fetchStatus())}>
        Get state
      </Button>
      <Button variant="secondary" disabled={!isIdle} onClick={() => sendGcode("$C")}>
        Check mode
      </Button>
    </div>
  );
};

export default Toolbox;
