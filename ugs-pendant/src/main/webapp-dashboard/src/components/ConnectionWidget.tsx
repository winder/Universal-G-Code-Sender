import { useEffect, useState } from "react";
import { Button, Form } from "react-bootstrap";
import { useAppDispatch } from "../hooks/useAppDispatch";
import { useAppSelector } from "../hooks/useAppSelector";
import { setSettings } from "../store/settingsSlice";
import { connect, getBaudRateList, getFirmwareList, getPortList } from "../services/machine";
import ControllerState from "./ControllerState";
import "./ConnectionWidget.scss";

const ConnectionWidget = () => {
  const dispatch = useAppDispatch();
  const settings = useAppSelector((state) => state.settings);
  const isDisconnected = useAppSelector((state) => state.status.state === "DISCONNECTED");

  const [ports, setPorts] = useState<string[]>([]);
  const [firmwares, setFirmwares] = useState<string[]>([]);
  const [baudRates, setBaudRates] = useState<string[]>([]);
  const [port, setPort] = useState(settings.port);
  const [portRate, setPortRate] = useState(settings.portRate);

  useEffect(() => {
    setPort(settings.port);
    setPortRate(settings.portRate);
  }, [settings.port, settings.portRate]);

  useEffect(() => {
    if (!isDisconnected) return;

    getFirmwareList().then(setFirmwares);
    getBaudRateList().then(setBaudRates);
    getPortList().then(setPorts);
    const refreshPorts = setInterval(() => getPortList().then(setPorts), 5000);
    return () => clearInterval(refreshPorts);
  }, [isDisconnected]);

  if (!isDisconnected) {
    return <ControllerState />;
  }

  return (
    <div className="connectionWidget">
      <ControllerState />

      <Form.Select
        size="sm"
        value={settings.firmwareVersion}
        onChange={(e) => dispatch(setSettings({ ...settings, firmwareVersion: e.currentTarget.value }))}
      >
        {(firmwares.length ? firmwares : [settings.firmwareVersion]).map((firmware) => (
          <option key={firmware} value={firmware}>
            {firmware}
          </option>
        ))}
      </Form.Select>

      <Form.Control
        size="sm"
        className="connectionWidgetPort"
        list="connectionWidgetPortList"
        placeholder="Port / IP"
        value={port}
        onChange={(e) => setPort(e.currentTarget.value)}
        onBlur={() => dispatch(setSettings({ ...settings, port }))}
      />
      <datalist id="connectionWidgetPortList">
        {ports.map((p) => (
          <option key={p} value={p} />
        ))}
      </datalist>

      <Form.Control
        size="sm"
        className="connectionWidgetBaud"
        list="connectionWidgetBaudList"
        placeholder="Baud"
        value={portRate}
        onChange={(e) => setPortRate(e.currentTarget.value)}
        onBlur={() => dispatch(setSettings({ ...settings, portRate }))}
      />
      <datalist id="connectionWidgetBaudList">
        {baudRates.map((b) => (
          <option key={b} value={b} />
        ))}
      </datalist>

      <Button size="sm" variant="primary" onClick={() => connect()}>
        Connect
      </Button>
    </div>
  );
};

export default ConnectionWidget;
