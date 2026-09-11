import { useEffect, useState } from "react";
import { Button, Form, Spinner } from "react-bootstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBullseye } from "@fortawesome/free-solid-svg-icons";
import { useAppSelector } from "../hooks/useAppSelector";
import { ProbeOperation, ProbeResult, ProbeSettings } from "../model/Probe";
import { getProbeSettings, runProbe, saveProbeSettings } from "../services/probe";
import ProbeDiagram from "./ProbeDiagram";
import ConfirmDialog from "./ConfirmDialog";
import "./ProbePanel.scss";

const SINGLE_FACE: { operation: ProbeOperation; label: string }[] = [
  { operation: "Z", label: "Z" },
  { operation: "X_NEG", label: "X−" },
  { operation: "X_POS", label: "X+" },
  { operation: "Y_NEG", label: "Y−" },
  { operation: "Y_POS", label: "Y+" },
];

const CENTER_FINDING: { operation: ProbeOperation; label: string }[] = [
  { operation: "X_CENTER", label: "X center" },
  { operation: "Y_CENTER", label: "Y center" },
  { operation: "CENTER", label: "Bore / rectangle center" },
];

const INSTRUCTIONS: Record<ProbeOperation, string> = {
  Z: "Position the tool above the material (or touch plate) with a clear path straight down.",
  X_NEG: "Position the tool to the right of the target face, clear to travel left into it.",
  X_POS: "Position the tool to the left of the target face, clear to travel right into it.",
  Y_NEG: "Position the tool above the target face, clear to travel down into it.",
  Y_POS: "Position the tool below the target face, clear to travel up into it.",
  X_CENTER: "Position the tool roughly centered between the left and right walls.",
  Y_CENTER: "Position the tool roughly centered between the top and bottom walls.",
  CENTER: "Position the tool roughly centered inside the bore or pocket.",
};

const emptySettings: ProbeSettings = {
  feedRateFast: 100,
  feedRateSlow: 10,
  retractDistance: 3,
  delayAfterRetract: 1,
  probeDiameter: 3.175,
  plateThickness: 0,
  maxTravel: 25,
  compensateSoftLimits: true,
};

type Props = {
  // True when rendered inside a split pane (half-width) - stacks the
  // settings fields into a single column instead of the responsive grid,
  // which would otherwise land on an unpredictable number of columns.
  compact?: boolean;
};

const ProbePanel = ({ compact = false }: Props) => {
  const currentState = useAppSelector((state) => state.status.state);
  const isIdle = currentState === "IDLE";

  const [operation, setOperation] = useState<ProbeOperation>("Z");
  const [settings, setSettings] = useState<ProbeSettings>(emptySettings);
  const [loaded, setLoaded] = useState(false);
  const [isRunning, setIsRunning] = useState(false);
  const [result, setResult] = useState<ProbeResult | null>(null);
  const [showConfirm, setShowConfirm] = useState(false);

  useEffect(() => {
    getProbeSettings()
      .then(setSettings)
      .finally(() => setLoaded(true));
  }, []);

  const updateSetting = <K extends keyof ProbeSettings>(key: K, value: ProbeSettings[K]) => {
    setSettings((prev) => ({ ...prev, [key]: value }));
  };

  const commitSetting = () => {
    saveProbeSettings(settings).catch(() => undefined);
  };

  const selectOperation = (next: ProbeOperation) => {
    setOperation(next);
    setResult(null);
  };

  const startRun = () => {
    setShowConfirm(true);
  };

  const confirmRun = () => {
    setShowConfirm(false);
    setIsRunning(true);
    setResult(null);
    runProbe(operation)
      .then(setResult)
      .catch((err) => setResult({ success: false, message: err instanceof Error ? err.message : "Probe failed" }))
      .finally(() => setIsRunning(false));
  };

  const travelLabel = SINGLE_FACE.find((o) => o.operation === operation)?.label ?? CENTER_FINDING.find((o) => o.operation === operation)?.label ?? operation;

  return (
    <div className="probePanel">
      <div className="probePanelOperations">
        <div className="probePanelGroup">
          <h6 className="probePanelGroupHeading">Single surface</h6>
          <div className="probePanelButtons">
            {SINGLE_FACE.map((o) => (
              <Button
                key={o.operation}
                variant={operation === o.operation ? "primary" : "outline-secondary"}
                onClick={() => selectOperation(o.operation)}
              >
                {o.label}
              </Button>
            ))}
          </div>
        </div>

        <div className="probePanelGroup">
          <h6 className="probePanelGroupHeading">Center finding</h6>
          <div className="probePanelButtons">
            {CENTER_FINDING.map((o) => (
              <Button
                key={o.operation}
                variant={operation === o.operation ? "primary" : "outline-secondary"}
                onClick={() => selectOperation(o.operation)}
              >
                {o.label}
              </Button>
            ))}
          </div>
        </div>
      </div>

      <div className={"probePanelBody" + (compact ? " compact" : "")}>
        <div className="probePanelDiagramColumn">
          <ProbeDiagram operation={operation} />
          <p className="probePanelInstructions">{INSTRUCTIONS[operation]}</p>
        </div>

        <div className={"probePanelSettings" + (compact ? " compact" : "")}>
          <Form.Group className="probePanelField">
            <Form.Label>Probe feed (fast)</Form.Label>
            <div className="probePanelFieldInput">
              <Form.Control
                type="number"
                value={settings.feedRateFast}
                onChange={(e) => updateSetting("feedRateFast", Number(e.target.value))}
                onBlur={commitSetting}
              />
              <span>mm/min</span>
            </div>
          </Form.Group>

          <Form.Group className="probePanelField">
            <Form.Label>Probe feed (slow)</Form.Label>
            <div className="probePanelFieldInput">
              <Form.Control
                type="number"
                value={settings.feedRateSlow}
                onChange={(e) => updateSetting("feedRateSlow", Number(e.target.value))}
                onBlur={commitSetting}
              />
              <span>mm/min</span>
            </div>
          </Form.Group>

          <Form.Group className="probePanelField">
            <Form.Label>Max travel</Form.Label>
            <div className="probePanelFieldInput">
              <Form.Control
                type="number"
                value={settings.maxTravel}
                onChange={(e) => updateSetting("maxTravel", Number(e.target.value))}
                onBlur={commitSetting}
              />
              <span>mm</span>
            </div>
          </Form.Group>

          <Form.Group className="probePanelField">
            <Form.Label>Retract</Form.Label>
            <div className="probePanelFieldInput">
              <Form.Control
                type="number"
                value={settings.retractDistance}
                onChange={(e) => updateSetting("retractDistance", Number(e.target.value))}
                onBlur={commitSetting}
              />
              <span>mm</span>
            </div>
          </Form.Group>

          <Form.Group className="probePanelField">
            <Form.Label>Probe diameter</Form.Label>
            <div className="probePanelFieldInput">
              <Form.Control
                type="number"
                value={settings.probeDiameter}
                onChange={(e) => updateSetting("probeDiameter", Number(e.target.value))}
                onBlur={commitSetting}
              />
              <span>mm</span>
            </div>
          </Form.Group>

          {operation === "Z" && (
            <Form.Group className="probePanelField">
              <Form.Label>Touch plate thickness</Form.Label>
              <div className="probePanelFieldInput">
                <Form.Control
                  type="number"
                  value={settings.plateThickness}
                  onChange={(e) => updateSetting("plateThickness", Number(e.target.value))}
                  onBlur={commitSetting}
                />
                <span>mm</span>
              </div>
            </Form.Group>
          )}

          <Form.Check
            className="probePanelSoftLimits"
            type="switch"
            id="probe-compensate-soft-limits"
            label="Clamp to soft limits"
            checked={settings.compensateSoftLimits}
            onChange={(e) => {
              updateSetting("compensateSoftLimits", e.target.checked);
              saveProbeSettings({ ...settings, compensateSoftLimits: e.target.checked }).catch(() => undefined);
            }}
          />
        </div>
      </div>

      <div className="probePanelFooter">
        {result && (
          <div className={"probePanelResult " + (result.success ? "success" : "failure")}>
            {result.success
              ? `Probe succeeded${result.probedPosition ? ` - contact at X${result.probedPosition.x.toFixed(3)} Y${result.probedPosition.y.toFixed(3)} Z${result.probedPosition.z.toFixed(3)}` : ""}`
              : result.message || "Probe failed"}
          </div>
        )}
        <Button variant="warning" disabled={!isIdle || !loaded || isRunning} onClick={startRun} className="probePanelRun">
          <FontAwesomeIcon icon={faBullseye} /> {isRunning ? "Probing..." : `Run Probe ${travelLabel}`}
          {isRunning && <Spinner size="sm" />}
        </Button>
        {!isIdle && <span className="probePanelDisabledHint">Machine must be connected and idle to probe.</span>}
      </div>

      {showConfirm && (
        <ConfirmDialog
          show={true}
          title="Run probe?"
          message={`This will move the machine toward ${travelLabel === operation ? "the target" : travelLabel} using the current settings. Make sure the path is clear.`}
          confirmLabel="Run"
          confirmVariant="warning"
          onConfirm={confirmRun}
          onCancel={() => setShowConfirm(false)}
        />
      )}
    </div>
  );
};

export default ProbePanel;
