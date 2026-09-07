import { useAppSelector } from "../hooks/useAppSelector";
import "./FeedSpindleReadout.scss";

const FeedSpindleReadout = () => {
  const status = useAppSelector((state) => state.status);

  return (
    <div className="feedSpindleReadout">
      <div className="feedSpindleRow">
        <div className="feedSpindleLabel">Feed</div>
        <div className="feedSpindleValue">
          {status.feedSpeed}
          <span className="feedSpindleUnits">{status.workCoord.units.toLocaleLowerCase()}/min</span>
        </div>
      </div>
      <div className="feedSpindleRow">
        <div className="feedSpindleLabel">Spindle</div>
        <div className="feedSpindleValue">
          {status.spindleSpeed}
          <span className="feedSpindleUnits">rpm</span>
        </div>
      </div>
    </div>
  );
};

export default FeedSpindleReadout;
