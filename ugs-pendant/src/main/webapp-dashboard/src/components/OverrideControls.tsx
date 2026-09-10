import { Button, ButtonGroup } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { sendOverride } from "../services/machine";
import "./OverrideControls.scss";

const activeClass = (isActive: boolean) => (isActive ? "overrideActive" : "");

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

      <div className="overrideRow">
        <span className="overrideLabel">Feed</span>
        <ButtonGroup>
          <Button
            variant="outline-secondary"
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_FEED_OVR_COARSE_MINUS")}
          >
            -10%
          </Button>
          <Button
            variant="outline-secondary"
            className="overrideCurrent"
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_FEED_OVR_RESET")}
            title="Tap to reset to 100%"
          >
            {feed}%
          </Button>
          <Button
            variant="outline-secondary"
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_FEED_OVR_COARSE_PLUS")}
          >
            +10%
          </Button>
        </ButtonGroup>
      </div>

      <div className="overrideRow">
        <span className="overrideLabel">Spindle</span>
        <ButtonGroup>
          <Button
            variant="outline-secondary"
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_SPINDLE_OVR_COARSE_MINUS")}
          >
            -10%
          </Button>
          <Button
            variant="outline-secondary"
            className="overrideCurrent"
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_SPINDLE_OVR_RESET")}
            title="Tap to reset to 100%"
          >
            {spindle}%
          </Button>
          <Button
            variant="outline-secondary"
            disabled={!canAdjust}
            onClick={() => sendOverride("CMD_SPINDLE_OVR_COARSE_PLUS")}
          >
            +10%
          </Button>
        </ButtonGroup>
      </div>
    </div>
  );
};

export default OverrideControls;
