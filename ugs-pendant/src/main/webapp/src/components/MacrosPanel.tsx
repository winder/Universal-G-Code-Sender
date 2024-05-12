import { useEffect, useMemo, useState } from "react";
import { getMacroList, runMacro } from "../services/macros";
import { Macro } from "../model/Macro";
import { Button, Col, Container, Row } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";

const MacrosPanel = () => {
  const [macros, setMacros] = useState<Macro[]>([]);

  const currentState = useAppSelector((state) => state.status.state);
  const isEnabled = useMemo(
    () => currentState === "IDLE" || currentState === "JOG",
    [currentState]
  );

  useEffect(() => {
    getMacroList().then((m) => setMacros(m));
  }, [setMacros]);

  return (
    <Container style={{ paddingLeft: 0, paddingRight: 0 }}>
      <Row>
        {macros.map((macro) => (
          <Col key={macro.name} xs={4} style={{ marginBottom: "20px" }}>
            <Button
              style={{
                height: "100%",
                width: "100%",
                minHeight: "62px",
                padding: "0",
              }}
              variant="secondary"
              onClick={() => {
                runMacro(macro);
              }}
              disabled={!isEnabled}
            >
              {macro.name}
            </Button>
          </Col>
        ))}
      </Row>
    </Container>
  );
};

export default MacrosPanel;
