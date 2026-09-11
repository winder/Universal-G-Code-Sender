import { useEffect, useMemo } from "react";
import { runMacro } from "../services/macros";
import { Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useAppSelector } from "../hooks/useAppSelector";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { fetchMacros } from "../store/macrosSlice";
import { MACRO_ICONS } from "../utils/macroIcons";
import { macroColorStyle } from "../utils/macroColors";
import "./MacrosPanel.scss";

const MacrosPanel = () => {
  const dispatch = useAppDispatch();
  // Shared with the editor (CenterPanel's Macros tab) via macrosSlice, so a
  // save there is reflected here without a page reload.
  const macros = useAppSelector((state) => state.macros.macros);
  const loaded = useAppSelector((state) => state.macros.loaded);

  const currentState = useAppSelector((state) => state.status.state);
  const isEnabled = useMemo(
    () => currentState === "IDLE" || currentState === "JOG",
    [currentState]
  );

  useEffect(() => {
    if (!loaded) {
      dispatch(fetchMacros());
    }
  }, [dispatch, loaded]);

  if (loaded && macros.length === 0) {
    return <div className="macrosEmpty">No macros configured in UGS.</div>;
  }

  return (
    <div className="macrosPanel">
      {macros.map((macro) => (
        <Button
          key={macro.uuid}
          className="macroButton"
          variant="secondary"
          style={macroColorStyle(macro.color)}
          title={macro.description ? `${macro.name} — ${macro.description}` : macro.name}
          onClick={() => runMacro(macro)}
          disabled={!isEnabled}
        >
          {macro.icon && MACRO_ICONS[macro.icon] && (
            <FontAwesomeIcon icon={MACRO_ICONS[macro.icon]} />
          )}
          <span className="macroButtonLabel">{macro.name}</span>
        </Button>
      ))}
    </div>
  );
};

export default MacrosPanel;
