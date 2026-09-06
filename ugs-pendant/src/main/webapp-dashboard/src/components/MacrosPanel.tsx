import { useEffect, useMemo, useState } from "react";
import { getMacroList, runMacro } from "../services/macros";
import { Macro } from "../model/Macro";
import { Button } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import "./MacrosPanel.scss";

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

  if (macros.length === 0) {
    return <div className="macrosEmpty">No macros configured in UGS.</div>;
  }

  return (
    <div className="macrosPanel">
      {macros.map((macro) => (
        <Button
          key={macro.name}
          className="macroButton"
          variant="secondary"
          title={macro.description}
          onClick={() => runMacro(macro)}
          disabled={!isEnabled}
        >
          {macro.name}
        </Button>
      ))}
    </div>
  );
};

export default MacrosPanel;
