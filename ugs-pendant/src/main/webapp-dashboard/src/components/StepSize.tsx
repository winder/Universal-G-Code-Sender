import { ButtonGroup, ToggleButton } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import "./StepSize.scss";

type Props = {
  name: string;
  label: string;
  value: number;
  onChange: (value: number) => void;
};

const options = [0.01, 0.1, 1, 10, 100, 1000];

const StepSize = ({ name, label, value, onChange }: Props) => {
  const units = useAppSelector((state) => state.settings.preferredUnits);

  return (
    <div className="stepSize">
      <div className="stepSizeHeading">
        {label}: {units.toLocaleLowerCase()}
      </div>
      <div className="stepSizeRow">
        <ButtonGroup>
          {options.map((option) => (
            <ToggleButton
              key={option}
              id={`step-size-${name}-${option}`}
              type="radio"
              variant="outline-secondary"
              className={value === option ? "stepSizeActive" : ""}
              name={`step-size-${name}`}
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
        <div className="stepSizeCurrent">{value}</div>
      </div>
    </div>
  );
};

export default StepSize;
