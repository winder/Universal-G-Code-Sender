import { useEffect, useMemo, useRef, useState } from "react";
import { Button, Form, InputGroup } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import { sendGcode } from "../services/machine";
import "./ConsolePanel.scss";

const ConsolePanel = () => {
  const currentState = useAppSelector((state) => state.status.state);
  const [gcodeCommand, setGcodeCommand] = useState("");
  const messages = useAppSelector((state) => state.console.messages);
  const consoleRef = useRef<HTMLDivElement | null>(null);

  const isEnabled = useMemo(
    () => currentState === "IDLE" || currentState === "JOG",
    [currentState],
  );

  useEffect(() => {
    const el = consoleRef.current;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }, [messages]);

  const handleSendGcode = () => {
    if (gcodeCommand.trim()) {
      sendGcode(gcodeCommand.trim()).then(() => {
        setGcodeCommand("");
      });
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      handleSendGcode();
    }
  };

  return (
    <div className="consolePanel">
      <div className="console" ref={consoleRef}>
        {messages.length === 0 ? (
          <div style={{ color: "#888" }}>
            Controller console output will appear here.
          </div>
        ) : (
          messages.map((line, index) => (
            <div
              key={index}
              style={{
                color:
                  line.type === "error"
                    ? "#ff6b6b"
                    : line.type === "ok"
                      ? "#7bdcff"
                      : "#ddd",
              }}
            >
              {line.text}
            </div>
          ))
        )}
      </div>

      <div className="consoleInput">
        <InputGroup>
          <Form.Control
            id="gcode-command-input"
            type="text"
            placeholder="e.g., G0 X0 Y0 Z-100"
            value={gcodeCommand}
            onChange={(e) => setGcodeCommand(e.target.value)}
            onKeyDown={handleKeyDown}
            disabled={!isEnabled}
          />
          <Button
            variant="primary"
            onClick={handleSendGcode}
            disabled={!isEnabled || !gcodeCommand.trim()}
          >
            Send
          </Button>
        </InputGroup>
      </div>
    </div>
  );
};

export default ConsolePanel;
