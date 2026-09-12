import { Button, ButtonGroup } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faRotateLeft } from "@fortawesome/free-solid-svg-icons";
import { useAppSelector } from "../hooks/useAppSelector";
import { sendOverride, OverrideCommand } from "../services/machine";
import "./OverrideControls.scss";

const activeClass = (isActive: boolean) => (isActive ? "overrideActive" : "");

// Feed/spindle override range per the GRBL/FluidNC real-time protocol is
// 10%-200%, not 0-100 - the bar reflects that actual range so a value like
// 150% still reads as "over halfway", not as pinned to the end of the bar.
const OVERRIDE_BAR_MAX = 200;

type OverrideBarRowProps = {
  label: string;
  value: number;
  canAdjust: boolean;
  minusCommand: OverrideCommand;
  resetCommand: OverrideCommand;
  plusCommand: OverrideCommand;
};

const OverrideBarRow = ({ label, value, canAdjust, minusCommand, resetCommand, plusCommand }: OverrideBarRowProps) => (
  <div className="overrideRow">
    <span className="overrideLabel">{label}</span>
    <ButtonGroup>
      <Button variant="outline-secondary" disabled={!canAdjust} onClick={() => sendOverride(minusCommand)}>
        -10%
      </Button>
      <Button
        variant="outline-secondary"
        className="overrideCurrent"
        disabled={!canAdjust}
        onClick={() => sendOverride(resetCommand)}
        title="Tap to reset to 100%"
      >
        <FontAwesomeIcon icon={faRotateLeft} />
      </Button>
      <Button variant="outline-secondary" disabled={!canAdjust} onClick={() => sendOverride(plusCommand)}>
        +10%
      </Button>
    </ButtonGroup>
    <div className="overrideBar">
      <div className="overrideBarTrack">
        <div className="overrideBarFill" style={{ width: `${Math.min(100, (value / OVERRIDE_BAR_MAX) * 100)}%` }} />
      </div>
      <span className="overrideBarValue">{value}%</span>
    </div>
  </div>
);

const OverrideControls = () => {
  // Unlike M-codes (M3/M8/...), override commands are GRBL real-time bytes -
  // they bypass the normal command queue entirely, so they're safe to send
  // during a HOLD (paused) too, not just IDLE/RUN. Only truly unavailable
  // when there's no live connection to adjust.
  const canAdjust = useAppSelector(
    (state) => !["DISCONNECTED", "CONNECTING", "ALARM"].includes(state.status.state)
  );
  const { feed, rapid, spindle } = useAppSelector((state) => state.status.overrides);

  return (
    <div className="overrideControls">
      <div className="overrideRow">
        <span className="overrideLabel">Rapid</span>
        <ButtonGroup>
          <Button
            variant="outline-secondary"
            className={activeClass(rapid <= 25)}
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_RAPID_OVR_LOW")}
          >
            25%
          </Button>
          <Button
            variant="outline-secondary"
            className={activeClass(rapid === 50)}
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_RAPID_OVR_MEDIUM")}
          >
            50%
          </Button>
          <Button
            variant="outline-secondary"
            className={activeClass(rapid >= 100)}
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_RAPID_OVR_RESET")}
          >
            100%
          </Button>
        </ButtonGroup>
      </div>

      <OverrideBarRow
        label="Feed"
        value={feed}
        canAdjust={canAdjust}
        minusCommand="CMD_FEED_OVR_COARSE_MINUS"
        resetCommand="CMD_FEED_OVR_RESET"
        plusCommand="CMD_FEED_OVR_COARSE_PLUS"
      />

      <OverrideBarRow
        label="Spindle"
        value={spindle}
        canAdjust={canAdjust}
        minusCommand="CMD_SPINDLE_OVR_COARSE_MINUS"
        resetCommand="CMD_SPINDLE_OVR_RESET"
        plusCommand="CMD_SPINDLE_OVR_COARSE_PLUS"
      />
    </div>
  );
};

export default OverrideControls;
