import { useEffect, useState } from "react";
import { Dropdown, DropdownButton, Form, InputGroup } from "react-bootstrap";

type Props = {
  value: string;
  options: string[];
  label: string;
  editable?: boolean;
  onChange?: (value: string) => void;
};

const DropdownInput = ({
  value,
  options,
  label,
  onChange,
  editable,
}: Props) => {
  const [currentValue, setCurrentValue] = useState("");
  useEffect(() => {
    setCurrentValue(value);
  }, [value]);

  return (
    <InputGroup className="mb-3">
      <Form.Floating>
        <Form.Control
          id="port"
          value={currentValue}
          onChange={(e) => setCurrentValue(e.currentTarget.value)}
          onBlur={(e) => onChange && onChange(e.currentTarget.value)}
          readOnly={!editable}
        />
        <label htmlFor="port">{label}</label>
      </Form.Floating>
      <DropdownButton title={""}>
        {options.map(option => {
          return (
            <Dropdown.Item
              key={option}
              href="#"
              onClick={() => {
                setCurrentValue(option);
                onChange && onChange(option);
              }}
            >
              {option}
            </Dropdown.Item>
          );
        })}
      </DropdownButton>
    </InputGroup>
  );
};

export default DropdownInput;
