import { Card } from "react-bootstrap";
import "./AccessoryState.scss";

type Props = {
   children: React.ReactNode;
};
const AccessoryState = ({ children }: Props) => {
  return (
    <Card bg="dark" className="accessoryState">
      <Card.Body>{children}</Card.Body>
    </Card>
  );
};

export default AccessoryState;
