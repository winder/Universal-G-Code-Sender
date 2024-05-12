import { Form } from "react-bootstrap";
import useDebounce from "../hooks/useDebounce";
import { useEffect, useState } from "react";
import "./RangeSlider.scss";

type Props = {
  max?: number;
  min?: number;
  step?: number;
  value?: number;
  onChange: (value: number) => void;
  disabled?: boolean;
};

const RangeSlider = ({ onChange, max, min, step, value, disabled }: Props) => {
  const [currentValue, setCurrentValue] = useState(value);
  const debouncedValue = useDebounce(currentValue, 300);

  useEffect(() => {
    onChange(debouncedValue ?? 0);
  }, [debouncedValue, onChange]);

  return (
    <div className="range-slider">
      <Form.Range
        value={currentValue}
        max={max ?? 100}
        min={min ?? 0}
        step={step ?? 1}
        onChange={(event) => setCurrentValue(+event.currentTarget.value)}
        disabled={disabled}
      />
    </div>
  );
};

export default RangeSlider;
