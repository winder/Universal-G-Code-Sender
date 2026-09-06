import { Card } from "react-bootstrap";
import "./AccessoryState.scss";

type Props = {
   children: React.ReactNode;
   title?: string;
};
const AccessoryState = ({ children, title }: Props) => {
  return (
    <Card bg="dark" className="accessoryState" title={title}>
      <Card.Body>{children}</Card.Body>
    </Card>
  );
};

export default AccessoryState;
