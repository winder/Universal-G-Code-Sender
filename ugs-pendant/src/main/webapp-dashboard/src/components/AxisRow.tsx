import { useEffect, useState } from "react";
import { Button } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faDeleteLeft } from "@fortawesome/free-solid-svg-icons";
import { resetToZero } from "../services/machine";
import { useAppSelector } from "../hooks/useAppSelector";
import "./AxisRow.scss";

export enum AxisType {
  X = "X",
  Y = "Y",
  Z = "Z",
  A = "A",
  B = "B",
  C = "C",
}

type Props = {
  axisType: AxisType;
};

const round = (value: number) => Math.round((value + Number.EPSILON) * 1000) / 1000;

const AxisRow = ({ axisType }: Props) => {
  const [active, setActive] = useState(false);
  const status = useAppSelector((state) => state.status);

  const key = axisType.toLocaleLowerCase() as "x" | "y" | "z" | "a" | "b" | "c";
  const machineCoord = status.machineCoord[key];
  const workCoord = status.workCoord[key];
  const limitPin = status.pins?.[key];

  useEffect(() => {
    setActive(true);
    const timer = setTimeout(() => setActive(false), 300);
    return () => clearTimeout(timer);
  }, [machineCoord, workCoord]);

  if (workCoord === null || workCoord === undefined) {
    return <></>;
  }

  return (
    <div className={"axisRow " + (active ? "axisChanging " : "") + (limitPin ? "limitPin" : "")}>
      <div className="axisLabel">{axisType}</div>
      <div className="axisValue">
        {round(workCoord).toFixed(3)}
        <span className="axisUnits">{status.workCoord.units.toLocaleLowerCase()}</span>
      </div>
      <Button
        className="axisZeroButton"
        variant="secondary"
        onClick={() => resetToZero(axisType)}
        disabled={status.state !== "IDLE"}
      >
        <FontAwesomeIcon icon={faDeleteLeft} />
      </Button>
    </div>
  );
};

export default AxisRow;
