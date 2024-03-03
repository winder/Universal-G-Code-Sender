import { useEffect, useState } from "react";
import { Button, Col, Container, Form, FormGroup, Row } from "react-bootstrap";
import DropdownInput from "../components/DropdownInput";
import { useAppDispatch } from "../hooks/useAppDispatch";
import {
  connect,
  getBaudRateList,
  getFirmwareList,
  getPortList,
  getSelectedBaudRate,
  getSelectedFirmware,
  getSelectedPort,
} from "../services/machine";
import { fetchStatus } from "../store/statusSlice";

const ConnectPage = () => {
  const [ports, setPorts] = useState<string[]>([]);
  const [firmwares, setFirmwares] = useState<string[]>([]);
  const [baudRates, setBaudRates] = useState<string[]>([]);

  const [selectedFirmware, setSelectedFirmware] = useState<string>("");
  const [selectedPort, setSelectedPort] = useState<string>("");
  const [selectedBaudRate, setSelectedBaud] = useState<string>("");
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(fetchStatus())
      .unwrap()
      .catch((error: any) => {
        console.error(error);
      });

    getPortList().then((portList) => setPorts(portList));
    getFirmwareList().then((firmwareList) => setFirmwares(firmwareList));
    getBaudRateList().then((buadRateList) => setBaudRates(buadRateList));
    getSelectedPort().then((selectedPort) => setSelectedPort(selectedPort));
    getSelectedFirmware().then((selectedFirmware) =>
      setSelectedFirmware(selectedFirmware)
    );
    getSelectedBaudRate().then((selectedBaudRate) =>
      setSelectedBaud(selectedBaudRate)
    );
  }, []);

  return (
    <Container style={{maxWidth: "400px", marginTop: "24px"}}>
      <Row>
        <Col>
          <Form>
            <FormGroup>
              <DropdownInput
                value={selectedFirmware}
                options={firmwares}
                label="Firmware"
                onChange={(element) =>
                  setSelectedFirmware(element.currentTarget.value)
                }
              />
            </FormGroup>

            <FormGroup>
              <DropdownInput
                value={selectedPort}
                options={ports}
                label="Port"
                onChange={(element) =>
                  setSelectedPort(element.currentTarget.value)
                }
                editable={true}
              />
            </FormGroup>

            <FormGroup>
              <DropdownInput
                value={selectedBaudRate}
                options={baudRates}
                label="Baud"
                onChange={(element) =>
                  setSelectedBaud(element.currentTarget.value)
                }
                editable={true}
              />
            </FormGroup>

            <Button onClick={connect}>Connect</Button>
          </Form>
        </Col>
      </Row>
    </Container>
  );
};

export default ConnectPage;
