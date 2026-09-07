import { useState } from "react";
import { Button, ButtonGroup } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { sendGcode } from "../services/machine";
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
  const [coolantOn, setCoolantOn] = useState(false);

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
            disabled={!isIdleOrRunning}
            onClick={() => {
              setCoolantOn(true);
              sendGcode("M8");
            }}
          >
            On
          </Button>
          <Button
            variant="outline-secondary"
            className={activeClass(!coolantOn)}
            disabled={!isIdleOrRunning}
            onClick={() => {
              setCoolantOn(false);
              sendGcode("M9");
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
