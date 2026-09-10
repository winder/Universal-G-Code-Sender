import Toolbox from "./Toolbox";
import MacrosPanel from "./MacrosPanel";
import SpindleCoolantControls from "./SpindleCoolantControls";
import OverrideControls from "./OverrideControls";
import "./RightRail.scss";

const RightRail = () => {
  return (
    <div className="rightRail">
      <div className="rightRailSection">
        <h6 className="rightRailHeading">Toolbox</h6>
        <Toolbox />
      </div>

      <div className="rightRailSection rightRailMacros">
        <h6 className="rightRailHeading">Macros</h6>
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
