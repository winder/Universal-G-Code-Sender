import { Dropdown, DropdownButton } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import "./FeedRate.scss";

type Props = {
  value: number;
  onChange: (value: number) => void;
};

const options = [10, 20, 50, 100, 200, 500, 1000, 2000, 5000];

const FeedRate = ({ value, onChange }: Props) => {
  const units = useAppSelector((state) => state.settings.preferredUnits);

  return (
    <DropdownButton
      variant="secondary"
      className="stepSize"
      title={value}
    >
      {options.map(option => (
        <Dropdown.Item key={option} as="button" onClick={() => onChange(option)}>
          {option} {units.toLocaleLowerCase() + "/min"}
        </Dropdown.Item>
      ))}
    </DropdownButton>
  );
};

export default FeedRate;
