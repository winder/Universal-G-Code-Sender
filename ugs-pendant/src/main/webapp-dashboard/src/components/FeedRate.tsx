import { ButtonGroup, ToggleButton } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import "./FeedRate.scss";

type Props = {
  value: number;
  onChange: (value: number) => void;
};

const options = [10, 100, 500, 1000, 2000, 5000];

const FeedRate = ({ value, onChange }: Props) => {
  const units = useAppSelector((state) => state.settings.preferredUnits);

  return (
    <div className="feedRate">
      <div className="feedRateHeading">
        Feed rate: {units.toLocaleLowerCase()}/min
      </div>
      <div className="feedRateRow">
        <ButtonGroup>
          {options.map((option) => (
            <ToggleButton
              key={option}
              id={`feed-rate-${option}`}
              type="radio"
              variant="outline-secondary"
              className={value === option ? "feedRateActive" : ""}
              name="feed-rate"
              value={option}
              checked={value === option}
              onChange={() => onChange(option)}
            >
              {option}
            </ToggleButton>
          ))}
        </ButtonGroup>
        {/* Always shows the real current value, even if it isn't one of the
            presets above (e.g. a value set from the real desktop app) - never
            silently snap it to a preset just to make a button light up. */}
        <div className="feedRateCurrent">{value}</div>
      </div>
    </div>
  );
};

export default FeedRate;
