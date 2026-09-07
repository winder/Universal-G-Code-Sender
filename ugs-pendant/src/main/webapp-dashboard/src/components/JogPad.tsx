import {
  faCaretDown,
  faCaretLeft,
  faCaretRight,
  faCaretUp,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useMemo } from "react";
import { Button } from "react-bootstrap";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { jog } from "../services/machine";
import { stop } from "../services/files";
import { setSettings } from "../store/settingsSlice";
import StepSize from "./StepSize";
import FeedRate from "./FeedRate";
import "./JogPad.scss";

const JogPad = () => {
  const dispatch = useAppDispatch();
  const currentState = useAppSelector((state) => state.status.state);
  const settings = useAppSelector((state) => state.settings);
  const isEnabled = useMemo(
    () => currentState === "IDLE" || currentState === "JOG",
    [currentState]
  );
  const isStoppable = useMemo(
    () => ["RUN", "HOLD", "CHECK", "JOG"].includes(currentState),
    [currentState]
  );

  return (
    <div className="jogPad">
      <div className="jogArrows">
        <div className="jogXY">
          <Button variant="secondary" className="jogBtn jogDiag" disabled={!isEnabled} onClick={() => jog(-1, 1, 0)} title="Jog X- Y+">
            <FontAwesomeIcon icon={faCaretUp} size="xl" style={{ transform: "rotate(-45deg)" }} />
          </Button>
          <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(0, 1, 0)}>
            <FontAwesomeIcon icon={faCaretUp} size="xl" />
          </Button>
          <Button variant="secondary" className="jogBtn jogDiag" disabled={!isEnabled} onClick={() => jog(1, 1, 0)} title="Jog X+ Y+">
            <FontAwesomeIcon icon={faCaretUp} size="xl" style={{ transform: "rotate(45deg)" }} />
          </Button>

          <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(-1, 0, 0)}>
            <FontAwesomeIcon icon={faCaretLeft} size="xl" />
          </Button>
          <Button
            variant="danger"
            className="jogCenter jogStop"
            disabled={!isStoppable}
            onClick={() => stop()}
            title="Stop - cancels an in-progress jog or job"
          >
            STOP
          </Button>
          <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(1, 0, 0)}>
            <FontAwesomeIcon icon={faCaretRight} size="xl" />
          </Button>

          <Button variant="secondary" className="jogBtn jogDiag" disabled={!isEnabled} onClick={() => jog(-1, -1, 0)} title="Jog X- Y-">
            <FontAwesomeIcon icon={faCaretUp} size="xl" style={{ transform: "rotate(225deg)" }} />
          </Button>
          <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(0, -1, 0)}>
            <FontAwesomeIcon icon={faCaretDown} size="xl" />
          </Button>
          <Button variant="secondary" className="jogBtn jogDiag" disabled={!isEnabled} onClick={() => jog(1, -1, 0)} title="Jog X+ Y-">
            <FontAwesomeIcon icon={faCaretUp} size="xl" style={{ transform: "rotate(135deg)" }} />
          </Button>
        </div>

        <div className="jogZ">
          <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(0, 0, 1)}>
            <FontAwesomeIcon icon={faCaretUp} size="xl" />
            <br />
            Z+
          </Button>
          <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(0, 0, -1)}>
            <FontAwesomeIcon icon={faCaretDown} size="xl" />
            <br />
            Z-
          </Button>
        </div>
      </div>

      <div className="jogSettings">
        <StepSize
          name="xy"
          label="X/Y step"
          value={settings.jogStepSizeXY}
          onChange={(value) => dispatch(setSettings({ ...settings, jogStepSizeXY: value }))}
        />
        {settings.useZStepSize && (
          <StepSize
            name="z"
            label="Z step"
            value={settings.jogStepSizeZ}
            onChange={(value) => dispatch(setSettings({ ...settings, jogStepSizeZ: value }))}
          />
        )}
        <FeedRate
          value={settings.jogFeedRate}
          onChange={(value) => dispatch(setSettings({ ...settings, jogFeedRate: value }))}
        />
      </div>
    </div>
  );
};

export default JogPad;
