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
      <Modal.Body>{message}</Modal.Body>
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
