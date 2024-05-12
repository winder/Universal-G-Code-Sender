import { useEffect, useState } from "react";
import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import {
  getWorkspaceFileList,
  openWorkspaceFile,
  uploadAndOpen,
} from "../services/files";
import { Container, ListGroup, ListGroupItem, Spinner } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFile, faUpload } from "@fortawesome/free-solid-svg-icons";

type Props = {
  handleClose: () => void;
};

const OpenFileModal = ({ handleClose }: Props) => {
  const [workspaceFileList, setWorkspaceFileList] = useState<string[]>();
  const [isLoading, setIsLoading] = useState<boolean>(false);

  useEffect(() => {
    getWorkspaceFileList().then((result) =>
      setWorkspaceFileList(
        result?.fileList.sort((a, b) =>
          a.toLocaleLowerCase().localeCompare(b.toLocaleLowerCase())
        )
      )
    );
  }, [setWorkspaceFileList]);

  const alertClicked = (file: string) => {
    setIsLoading(true);
    openWorkspaceFile(file)
      .then(() => handleClose())
      .finally(() => setIsLoading(false));
  };

  const onUploadFile = () => {
    return new Promise((resolve) => {
      const input = document.createElement("input");
      input.accept = ".cnc,.nc,.ngc,.tap,.txt,.gcode";
      input.type = "file";
      input.multiple = false;

      // eslint-disable-next-line
      input.onchange = async (e: any) => {
        setIsLoading(true);
        const files = e?.target?.files ?? [];
        if (files.length === 0) {
          resolve(1);
        }

        for (const file of files) {
          await uploadAndOpen(file);
        }
        resolve(1);
      };
      input.click();
    })
      .then(() => {
        handleClose();
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  return (
    <Modal show={true} fullscreen={true} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>Open file</Modal.Title>
      </Modal.Header>

      <Modal.Body style={{ padding: 0 }}>
        {!workspaceFileList?.length && (
          <Container style={{ paddingTop: "24px" }}>
            <p>
              There are no files in the workspace directory. Please check the
              UGS configuration to get started with your workspace.
            </p>
            <p>Press open to load a gcode file from this device.</p>
          </Container>
        )}
        <ListGroup variant="flush">
          {workspaceFileList?.map((file) => (
            <ListGroupItem
              key={file}
              action
              onClick={() => alertClicked(file)}
              style={{ minHeight: "60px" }}
              disabled={isLoading}
            >
              <FontAwesomeIcon
                icon={faFile}
                size="xl"
                style={{ marginRight: "10px" }}
              />{" "}
              {file}
            </ListGroupItem>
          ))}
        </ListGroup>
      </Modal.Body>

      <Modal.Footer>
        <Button variant="secondary" onClick={handleClose} disabled={isLoading}>
          Close
        </Button>
        <Button
          variant="primary"
          disabled={isLoading}
          onClick={() => onUploadFile()}
        >
          <FontAwesomeIcon icon={faUpload} />
          Open... {isLoading && <Spinner size="sm" />}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default OpenFileModal;
