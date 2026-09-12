import { Button, Modal } from "react-bootstrap";

type Props = {
  show: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  confirmVariant?: string;
  onConfirm: () => void;
  onCancel: () => void;
};

const ConfirmDialog = ({ show, title, message, confirmLabel, confirmVariant, onConfirm, onCancel }: Props) => {
  return (
    <Modal show={show} onHide={onCancel} centered>
      <Modal.Header closeButton>
        <Modal.Title>{title}</Modal.Title>
      </Modal.Header>
      {/* pre-line: some callers pass multi-line messages (e.g. quoting a line
          of gcode) - plain text collapses \n, this preserves it without
          affecting the single-line messages every other caller passes */}
      <Modal.Body style={{ whiteSpace: "pre-line" }}>{message}</Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
        <Button variant={confirmVariant ?? "primary"} onClick={onConfirm}>
          {confirmLabel ?? "Confirm"}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default ConfirmDialog;
