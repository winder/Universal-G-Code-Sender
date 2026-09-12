import { Form } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { sendGcode } from "../services/machine";
import "./ModalStatusRow.scss";

// Fixed set (not fetched from the backend) - the 9 work coordinate systems
// are a permanent part of the gcode standard (LinuxCNC/GRBL/FluidNC all
// agree on exactly these), not something that varies by firmware or config.
const WCS_OPTIONS = ["G54", "G55", "G56", "G57", "G58", "G59", "G59.1", "G59.2", "G59.3"];

// Mirrors the modal-state row FluidNC's own WebUI shows (e.g. "G0 G54 G21
// G90 G94 G17 M5 T0"), restyled to this dashboard's own theme instead of
// copying that one - the G54 badge doubles as this dashboard's WCS selector,
// the rest are read-only chips reflecting whatever the controller last
// reported (see StatusResource.getStatus/GcodeState on the Java side).
const ModalStatusRow = () => {
  const status = useAppSelector((state) => state.status);
  const isDisconnected = status.state === "DISCONNECTED";
  // Same gate as AxisRow's zero button - changing the active work
  // coordinate system (or any other modal state) mid-job or mid-motion
  // isn't safe, only while genuinely idle.
  const canAdjust = status.state === "IDLE";

  if (isDisconnected) {
    return <></>;
  }

  return (
    <div className="modalStatusRow">
      <Form.Select
        size="sm"
        className="modalStatusWcs"
        value={status.coordinateSystem || "G54"}
        disabled={!canAdjust}
        onChange={(e) => sendGcode(e.currentTarget.value)}
        title="Active work coordinate system"
      >
        {WCS_OPTIONS.map((wcs) => (
          <option key={wcs} value={wcs}>
            {wcs}
          </option>
        ))}
      </Form.Select>

      {[status.motionMode, status.units, status.distanceMode, status.feedMode, status.plane, status.spindleMode]
        .filter(Boolean)
        .map((code) => (
          <span className="modalStatusChip" key={code}>
            {code}
          </span>
        ))}
      <span className="modalStatusChip">T{status.toolNumber ?? 0}</span>
    </div>
  );
};

export default ModalStatusRow;
