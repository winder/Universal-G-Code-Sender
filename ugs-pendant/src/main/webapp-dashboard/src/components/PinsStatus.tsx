import { useAppSelector } from "../hooks/useAppSelector";
import "./PinsStatus.scss";

const PinsStatus = () => {
  const status = useAppSelector((state) => state.status);
  const workCoord = status.workCoord as unknown as Record<string, number | null>;

  // A/B/C only show up if the machine actually reports that axis (workCoord is
  // null for axes it doesn't have) - same rule the DRO uses to hide them.
  const axes: Array<"x" | "y" | "z" | "a" | "b" | "c"> = ["x", "y", "z", "a", "b", "c"].filter(
    (axis) => workCoord[axis] !== null && workCoord[axis] !== undefined
  ) as Array<"x" | "y" | "z" | "a" | "b" | "c">;

  return (
    <div className="pinsStatus">
      {axes.map((axis) => (
        <div key={axis} className={"pinBadge " + (status.pins?.[axis] ? "active" : "")}>
          {axis.toUpperCase()}
        </div>
      ))}
      <div className={"pinBadge " + (status.pins?.probe ? "active" : "")}>Probe</div>
    </div>
  );
};

export default PinsStatus;
