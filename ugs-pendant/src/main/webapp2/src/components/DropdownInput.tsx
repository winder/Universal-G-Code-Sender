import React, { useEffect, useState } from "react";
import { Dropdown, DropdownButton, Form, InputGroup } from "react-bootstrap";

type Props = {
  value: string;
  options: string[];
  label: string;
  editable?: boolean;
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
};

const DropdownInput = ({ value, options, label, onChange, editable}: Props) => {
  const [currentValue, setCurrentValue] = useState("");
  useEffect(() => setCurrentValue(value), [value]);

  return (
    <InputGroup className="mb-3">
      <Form.Floating>
        <Form.Control id="port" value={currentValue} onChange={onChange} readOnly={!editable}/>
        <label htmlFor="port">{label}</label>
      </Form.Floating>
      <DropdownButton title={""}>
        {options.map((option, index) => {
          return (
            <Dropdown.Item key={index} href="#" onClick={() => setCurrentValue(option)}>
              {option}
            </Dropdown.Item>
          );
        })}
      </DropdownButton>
    </InputGroup>
  );
};

export default DropdownInput;
