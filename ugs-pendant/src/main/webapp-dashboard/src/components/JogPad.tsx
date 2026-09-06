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

  return (
    <div className="jogPad">
      <div className="jogXY">
        <div />
        <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(0, 1, 0)}>
          <FontAwesomeIcon icon={faCaretUp} size="xl" />
        </Button>
        <div />

        <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(-1, 0, 0)}>
          <FontAwesomeIcon icon={faCaretLeft} size="xl" />
        </Button>
        <div className="jogCenter">XY</div>
        <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(1, 0, 0)}>
          <FontAwesomeIcon icon={faCaretRight} size="xl" />
        </Button>

        <div />
        <Button variant="secondary" className="jogBtn" disabled={!isEnabled} onClick={() => jog(0, -1, 0)}>
          <FontAwesomeIcon icon={faCaretDown} size="xl" />
        </Button>
        <div />
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

      <div className="jogSettings">
        <div>
          <label>X/Y step</label>
          <StepSize
            value={settings.jogStepSizeXY}
            onChange={(value) => dispatch(setSettings({ ...settings, jogStepSizeXY: value }))}
          />
        </div>
        {settings.useZStepSize && (
          <div>
            <label>Z step</label>
            <StepSize
              value={settings.jogStepSizeZ}
              onChange={(value) => dispatch(setSettings({ ...settings, jogStepSizeZ: value }))}
            />
          </div>
        )}
        <div>
          <label>Feed rate</label>
          <FeedRate
            value={settings.jogFeedRate}
            onChange={(value) => dispatch(setSettings({ ...settings, jogFeedRate: value }))}
          />
        </div>
      </div>
    </div>
  );
};

export default JogPad;
