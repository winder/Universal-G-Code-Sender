import { useEffect, useState } from "react";
import { Button, Col, Container, FormGroup, Row } from "react-bootstrap";
import DropdownInput from "../components/DropdownInput";
import { useAppDispatch } from "../hooks/useAppDispatch";
import {
  connect,
  getBaudRateList,
  getFirmwareList,
  getPortList,
} from "../services/machine";
import { fetchStatus } from "../store/statusSlice";
import { useAppSelector } from "../hooks/useAppSelector";
import { setSettings } from "../store/settingsSlice";

const ConnectPage = () => {
  const settings = useAppSelector((state) => state.settings);
  const [ports, setPorts] = useState<string[]>([]);
  const [firmwares, setFirmwares] = useState<string[]>([]);
  const [baudRates, setBaudRates] = useState<string[]>([]);
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

    // Refresh the port list at regular intervals
    const refreshInterval = setInterval(
      () => getPortList().then((portList) => setPorts(portList)),
      5000
    );
    return () => clearInterval(refreshInterval);
  }, []);

  return (
    <Container style={{ maxWidth: "400px", marginTop: "24px" }}>
      <Row>
        <Col>
          <FormGroup>
            <DropdownInput
              value={settings.firmwareVersion}
              options={firmwares}
              label="Firmware"
              onChange={(value) => {
                console.log(value);
                dispatch(setSettings({ ...settings, firmwareVersion: value }));
              }}
            />
          </FormGroup>

          <FormGroup>
            <DropdownInput
              value={settings.port}
              options={ports}
              label="Port"
              onChange={(value) =>
                dispatch(setSettings({ ...settings, port: value }))
              }
              editable={true}
            />
          </FormGroup>

          <FormGroup>
            <DropdownInput
              value={settings.portRate}
              options={baudRates}
              label="Baud"
              onChange={(value) =>
                dispatch(setSettings({ ...settings, portRate: value }))
              }
              editable={true}
            />
          </FormGroup>

          <Button onClick={connect}>Connect</Button>
        </Col>
      </Row>
    </Container>
  );
};

export default ConnectPage;
