import { Col, Form, Row } from "react-bootstrap";
import useDebounce from "../hooks/useDebounce";
import { useEffect, useState } from "react";

type Props = {
  max?: number;
  min?: number;
  step?: number;
  value?: number;
  onChange: (value: number) => void;
  disabled?: boolean;
};

const RangeSlider = ({ onChange, max, min, step, value, disabled }: Props) => {

  const debouncedValue = useDebounce(value);
  useEffect(() => {
    onChange(debouncedValue || 0);
  }, [debouncedValue, onChange]);

  return (
    <Row>
     <Col xs="4" sm="5">Step size {value}</Col>
      <Col xs="8" sm="7">
        <Form.Range
          value={value}
          max={max || 100}
          min={min || 0}
          step={step || 1}
          onChange={(event) => onChange(+event.target.value)}
          disabled={disabled}
        />
      </Col>
    </Row>
  );
};

export default RangeSlider;
