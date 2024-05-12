import { useEffect, useState } from "react";
import "./AxisPanel.scss";
import { Button, Col, Row } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faDeleteLeft } from "@fortawesome/free-solid-svg-icons";
import { resetToZero } from "../services/machine";
import { useAppSelector } from "../hooks/useAppSelector";

type Props = {
  axisType: AxisType;
};

const round = (value: number) => {
  return Math.round((value + Number.EPSILON) * 1000) / 1000;
};

export enum AxisType {
  X = "X",
  Y = "Y",
  Z = "Z",
  A = "A",
  B = "B",
  C = "C",
}

const AxisPanel = ({ axisType }: Props) => {
  const [active, setActive] = useState(false);
  const status = useAppSelector((state) => state.status);

  const machineCoord = (status.machineCoord as any)[
    axisType.toLocaleLowerCase()
  ];
  const workCoord = (status.workCoord as any)[axisType.toLocaleLowerCase()];
  const limitPin = (status.pins as any)?.[axisType.toLocaleLowerCase()];

  useEffect(() => {
    setActive(true);
    const timer = setTimeout(() => setActive(false), 300);
    return () => clearTimeout(timer);
  }, [machineCoord, workCoord]);

  // Hide axis if there is no work coordinate
  if(workCoord === null) {
    return <></>;
  }

  return (
    <Row className="axisRow">
      <Col
        xs={8}
        sm={8}
        md={9}
        className={
          "axisPanel " +
          (active ? "axisChanging " : "") +
          (limitPin ? "limitPin" : "")
        }
      >
        <Row>
          <Col className="text-start" xs={2}>
            {axisType}
          </Col>
          <Col style={{paddingLeft: 0}}>
            {round(workCoord).toFixed(3)}
            <span className="axisUnits">{" "}{status.workCoord.units.toLocaleLowerCase()}</span>
          </Col>
        </Row>
      </Col>
      <Col xs={4} sm={4} md={3}>
        <Button className="axisZeroButton"
          variant="secondary"
          onClick={() => resetToZero(axisType)}
          disabled={status.state !== "IDLE"}
        >
          <span>
            Zero {axisType}
            <br />
          </span>
          <FontAwesomeIcon icon={faDeleteLeft} />
        </Button>
      </Col>
    </Row>
  );
};

export default AxisPanel;
