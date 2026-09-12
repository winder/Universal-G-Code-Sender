import { Button } from "react-bootstrap";
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
  // state via the "A:" accessory-state field (unlike spindle), so unlike
  // spindleOn above this can't be read from status reports. It's sourced from
  // the gcode parser's M7/M8/M9 modal state instead (Status.floodCoolantOn on
  // the Java side) - kept fresh by socketMiddleware.ts re-fetching status
  // whenever ANY M7/M8/M9 command completes, from any source (this UI, the
  // native UGS console, another client), not just this component's own
  // clicks.
  //
  // On/Off deliberately send plain M8/M9 gcode, not the real-time
  // CMD_TOGGLE_FLOOD_COOLANT byte used earlier: a toggle is only as safe as
  // the tracked state it's toggling from, and that state can be stale (e.g.
  // coolant changed via the native console) - a stale toggle sends the WRONG
  // direction. M8/M9 are idempotent: pressing "On" always means on, however
  // stale the highlight was, so it can never make things worse.
  const coolantOn = useAppSelector((state) => state.status.floodCoolantOn ?? false);

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
        <div className="spindleCoolantButtons">
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
        </div>
      </div>

      <div className="spindleCoolantRow">
        <span className="spindleCoolantLabel">Coolant</span>
        <div className="spindleCoolantButtons">
          <Button
            variant="outline-secondary"
            className={activeClass(coolantOn)}
            disabled={!isIdleOrRunning}
            onClick={() => sendGcode("M8")}
          >
            On
          </Button>
          <Button
            variant="outline-secondary"
            className={activeClass(!coolantOn)}
            disabled={!isIdleOrRunning}
            onClick={() => sendGcode("M9")}
          >
            Off
          </Button>
        </div>
      </div>
    </div>
  );
};

export default SpindleCoolantControls;
