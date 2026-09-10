import { Button } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { sendGcode, sendOverride } from "../services/machine";
import { fetchStatus } from "../store/statusSlice";
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
  // spindleOn above this can't be read from status reports. Instead it comes
  // from the gcode parser's M7/M8/M9 modal state, which sendOverride() below
  // refreshes with a "$G" query after every coolant toggle - see
  // MachineResource.sendOverride() and StatusResource.getStatus() on the
  // Java side. The dispatch(fetchStatus()) after each click pulls that
  // refreshed value in; until it lands, the highlight still shows whatever
  // was last confirmed rather than flipping optimistically, since a real-time
  // toggle that got ignored (e.g. no coolant relay wired) should NOT look
  // like it worked.
  const coolantOn = useAppSelector((state) => state.status.floodCoolantOn ?? false);
  const dispatch = useAppDispatch();
  // Real-time byte, not gcode - unlike M8/M9 it isn't queued behind motion,
  // so it keeps working to toggle coolant during a paused (HOLD) job too.
  const canAdjustCoolant = useAppSelector(
    (state) => !["DISCONNECTED", "CONNECTING", "ALARM"].includes(state.status.state)
  );

  const toggleCoolant = () => {
    sendOverride("CMD_TOGGLE_FLOOD_COOLANT").then(() => {
      // Give the "$G" query MachineResource sends alongside the toggle time
      // to round-trip before reading the refreshed state back.
      window.setTimeout(() => dispatch(fetchStatus()), 400);
    });
  };

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
            disabled={!canAdjustCoolant}
            onClick={() => !coolantOn && toggleCoolant()}
          >
            On
          </Button>
          <Button
            variant="outline-secondary"
            className={activeClass(!coolantOn)}
            disabled={!canAdjustCoolant}
            onClick={() => coolantOn && toggleCoolant()}
          >
            Off
          </Button>
        </div>
      </div>
    </div>
  );
};

export default SpindleCoolantControls;
