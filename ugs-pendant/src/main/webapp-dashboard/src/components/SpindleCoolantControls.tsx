import { useState } from "react";
import { Button, ButtonGroup } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { sendGcode, sendOverride } from "../services/machine";
import "./SpindleCoolantControls.scss";

const activeClass = (isActive: boolean) => (isActive ? "spindleCoolantActive" : "");

const SpindleCoolantControls = () => {
  const isIdleOrRunning = useAppSelector(
    (state) => state.status.state === "IDLE" || state.status.state === "RUN"
  );
  // Driven from the controller's accessory-state flag (the "A:" field's "S"),
  // not spindleSpeed > 0 - confirmed via real hardware that some FluidNC setups
  // (no RPM feedback wired) always report spindle speed as 0 even while actually
  // spinning, so that number can't be trusted for on/off state. The "A:" flag is
  // the real signal GRBL/FluidNC use for this regardless of speed feedback, and
  // reacts to M3/M5 however they were sent (this UI, a manually typed command,
  // another client), not just button presses here.
  const spindleOn = useAppSelector((state) => state.status.accessoryStates.spindleCW);

  // Confirmed via real hardware that this FluidNC setup never reports coolant
  // state at all - the "A:" field never carries an "F", even immediately after
  // M8, because there's no coolant output configured on the controller to report
  // on. With no real signal to read, showing a value here that LOOKS confirmed
  // but is actually always wrong is worse than being honest that this is just
  // "the last button you pressed," not something the controller has verified.
  //
  // This local guess is also why coolant uses CMD_TOGGLE_FLOOD_COOLANT (a
  // real-time byte, like the override commands) rather than a blind toggle:
  // the On/Off buttons only send it when it would actually change this
  // tracked state, so a stray extra click can't flip flood the wrong way.
  // If the real state ever drifts from this guess (toggled by another
  // client, a controller reset, etc.) these buttons drift with it - same
  // known limitation as the guess itself.
  const [coolantOn, setCoolantOn] = useState(false);
  // Real-time byte, not gcode - unlike M8/M9 it isn't queued behind motion,
  // so it keeps working to toggle coolant during a paused (HOLD) job too.
  const canAdjustCoolant = useAppSelector(
    (state) => !["DISCONNECTED", "CONNECTING", "ALARM"].includes(state.status.state)
  );

  return (
    <div className="spindleCoolantControls">
      <div className="spindleCoolantRow">
        <span className="spindleCoolantLabel">Spindle</span>
        {/* Plain buttons, not a radio ToggleButton pair: a radio only fires its
            change handler when a click actually flips its checked state, so if the
            highlighted button is ever wrong (e.g. no live spindle feedback from the
            controller, or state changed some other way - see the code comment
            below), clicking the button that's stuck showing "checked" silently
            sends nothing. A plain button's onClick always fires the command,
            regardless of what the highlight currently shows. */}
        <ButtonGroup>
          <Button
            variant="outline-secondary"
            className={activeClass(spindleOn)}
            disabled={!isIdleOrRunning}
            onClick={() => sendGcode("M3 S1000")}
          >
            On
          </Button>
          <Button
            variant="outline-secondary"
            className={activeClass(!spindleOn)}
            disabled={!isIdleOrRunning}
            onClick={() => sendGcode("M5")}
          >
            Off
          </Button>
        </ButtonGroup>
      </div>

      <div className="spindleCoolantRow">
        <span className="spindleCoolantLabel">Coolant</span>
        <ButtonGroup>
          <Button
            variant="outline-secondary"
            className={activeClass(coolantOn)}
            disabled={!canAdjustCoolant}
            onClick={() => {
              if (!coolantOn) {
                setCoolantOn(true);
                sendOverride("CMD_TOGGLE_FLOOD_COOLANT");
              }
            }}
          >
            On
          </Button>
          <Button
            variant="outline-secondary"
            className={activeClass(!coolantOn)}
            disabled={!canAdjustCoolant}
            onClick={() => {
              if (coolantOn) {
                setCoolantOn(false);
                sendOverride("CMD_TOGGLE_FLOOD_COOLANT");
              }
            }}
          >
            Off
          </Button>
        </ButtonGroup>
      </div>
    </div>
  );
};

export default SpindleCoolantControls;
