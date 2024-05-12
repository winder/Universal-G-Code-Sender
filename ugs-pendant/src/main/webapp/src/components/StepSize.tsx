import { Dropdown, DropdownButton } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import "./StepSize.scss";

type Props = {
  value: number;
  onChange: (value: number) => void;
};

const options = [0.001, 0.01, 0.1, 1, 10, 100];

const StepSize = ({ value, onChange }: Props) => {
  const units = useAppSelector((state) => state.settings.preferredUnits);

  return (
    <DropdownButton variant="secondary" className="stepSize" title={value}>
      {options.map((option) => (
        <Dropdown.Item
          key={option}
          as="button"
          onClick={() => onChange(option)}
        >
          {option} {units.toLocaleLowerCase()}
        </Dropdown.Item>
      ))}
    </DropdownButton>
  );
};

export default StepSize;
