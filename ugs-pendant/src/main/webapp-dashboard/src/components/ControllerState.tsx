import { Card } from "react-bootstrap";
import { useAppSelector } from "../hooks/useAppSelector";
import "./ControllerState.scss";

const ControllerState = () => {
  const state = useAppSelector((state) => state.status.state);

  return (
    <Card bg="dark" className="controllerState">
     <Card.Body className={state.toLocaleLowerCase()}>
      {state}
      </Card.Body>
    </Card>
  );
};

export default ControllerState;
