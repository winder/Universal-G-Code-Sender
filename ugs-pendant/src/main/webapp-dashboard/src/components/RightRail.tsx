import { Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPen } from "@fortawesome/free-solid-svg-icons";
import Toolbox from "./Toolbox";
import MacrosPanel from "./MacrosPanel";
import SpindleCoolantControls from "./SpindleCoolantControls";
import OverrideControls from "./OverrideControls";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { uiActions } from "../store/uiSlice";
import "./RightRail.scss";

const RightRail = () => {
  const dispatch = useAppDispatch();

  return (
    <div className="rightRail">
      <div className="rightRailSection">
        <h6 className="rightRailHeading">Toolbox</h6>
        <Toolbox />
      </div>

      <div className="rightRailSection rightRailMacros">
        <div className="rightRailSectionHeader">
          <h6 className="rightRailHeading">Macros</h6>
          <Button
            size="sm"
            variant="outline-secondary"
            className="rightRailEditButton"
            title="Edit macros"
            onClick={() => dispatch(uiActions.setCenterView("macros"))}
          >
            <FontAwesomeIcon icon={faPen} />
          </Button>
        </div>
        <MacrosPanel />
      </div>

      <div className="rightRailSection rightRailSpindle">
        <h6 className="rightRailHeading">Overrides</h6>
        <OverrideControls />
      </div>

      <div className="rightRailSection rightRailSpindle">
        <h6 className="rightRailHeading">Spindle / Coolant</h6>
        <SpindleCoolantControls />
      </div>
    </div>
  );
};

export default RightRail;
