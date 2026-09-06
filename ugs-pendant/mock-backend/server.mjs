import { createServer } from "http";
import { WebSocketServer } from "ws";

const settings = {
  jogFeedRate: 1000,
  jogStepSizeXY: 1,
  preferredUnits: "MM",
  jogStepSizeZ: 1,
  port: "COM3",
  portRate: "115200",
  firmwareVersion: "GRBL",
  useZStepSize: true,
};

const status = {
  machineCoord: { x: 12.5, y: -4.2, z: 0.75, a: 0, b: 0, c: 0, units: "MM" },
  workCoord: { x: 12.5, y: -4.2, z: 0.75, a: 0, b: 0, c: 0, units: "MM" },
  feedSpeed: 0,
  spindleSpeed: 0,
  state: "IDLE",
  pins: {
    x: false, y: false, z: false, a: false, b: false, c: false,
    probe: false, door: false, hold: false, softReset: false, cycleStart: false,
  },
};

const macros = [
  { name: "Home", description: "Home all axes", gcode: "$H" },
  { name: "Zero XY", description: "Zero X/Y work offset", gcode: "G10 L20 P1 X0 Y0" },
  { name: "Spindle On", description: undefined, gcode: "M3 S1000" },
];

const fileStatus = {
  fileName: "test-part.nc",
  rowCount: 4200,
  completedRowCount: 0,
  remainingRowCount: 4200,
  sendDuration: 0,
  sendRemainingDuration: 0,
};

let fileContent = `; sample part
G21 G90
G0 Z5
G0 X0 Y0
G1 Z-1 F200
G1 X50 Y0 F800
G1 X50 Y30
G1 X0 Y30
G1 X0 Y0
G0 Z5
`;

const toolpath = [
  { start: { x: 0, y: 0, z: 5 }, end: { x: 0, y: 0, z: -1 }, rapid: false, arc: false },
  { start: { x: 0, y: 0, z: -1 }, end: { x: 50, y: 0, z: -1 }, rapid: false, arc: false },
  { start: { x: 50, y: 0, z: -1 }, end: { x: 50, y: 30, z: -1 }, rapid: false, arc: false },
  { start: { x: 50, y: 30, z: -1 }, end: { x: 0, y: 30, z: -1 }, rapid: false, arc: false },
  { start: { x: 0, y: 30, z: -1 }, end: { x: 0, y: 0, z: -1 }, rapid: false, arc: false },
  { start: { x: 0, y: 0, z: -1 }, end: { x: 0, y: 0, z: 5 }, rapid: true, arc: false },
];

function json(res, data, status_ = 200) {
  res.writeHead(status_, { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" });
  res.end(JSON.stringify(data));
}

const server = createServer((req, res) => {
  const url = new URL(req.url, "http://localhost");
  const p = url.pathname;
  console.log(req.method, p);

  if (p === "/api/v1/settings/getSettings") return json(res, settings);
  if (p === "/api/v1/settings/setSettings") {
    let body = "";
    req.on("data", (c) => (body += c));
    req.on("end", () => {
      Object.assign(settings, JSON.parse(body || "{}"));
      json(res, settings);
    });
    return;
  }
  if (p === "/api/v1/machine/getPortList") return json(res, ["COM3", "COM4"]);
  if (p === "/api/v1/machine/getSelectedPort") return json(res, { selectedPort: "COM3" });
  if (p === "/api/v1/machine/getSelectedFirmware") return json(res, { selectedFirmware: "GRBL" });
  if (p === "/api/v1/machine/getSelectedBaudRate") return json(res, { selectedBaudRate: "115200" });
  if (p === "/api/v1/machine/getFirmwareList") return json(res, ["GRBL", "Smoothie", "TinyG", "g2core"]);
  if (p === "/api/v1/machine/getBaudRateList") return json(res, ["9600", "115200", "250000"]);
  if (p === "/api/v1/status/getStatus") return json(res, status);
  if (p.startsWith("/api/v1/machine/")) return json(res, {});
  if (p === "/api/v1/files/getFileStatus") return json(res, fileStatus);
  if (p === "/api/v1/files/getWorkspaceFileList") return json(res, { fileList: ["part1.nc", "part2.nc", "test-part.nc"] });
  if (p === "/api/v1/files/getFileContent") {
    res.writeHead(200, { "Content-Type": "text/plain", "Access-Control-Allow-Origin": "*" });
    return res.end(fileContent);
  }
  if (p === "/api/v1/files/saveFileContent") {
    let body = "";
    req.on("data", (c) => (body += c));
    req.on("end", () => {
      fileContent = body;
      json(res, {});
    });
    return;
  }
  if (p.startsWith("/api/v1/files/")) return json(res, {});
  if (p === "/api/v1/macros/getMacroList") return json(res, macros);
  if (p.startsWith("/api/v1/macros/")) return json(res, {});
  if (p === "/api/v1/visualizer/getToolpath") return json(res, toolpath);

  json(res, { error: "not found" }, 404);
});

const wss = new WebSocketServer({ server, path: "/ws/v1/events" });
wss.on("connection", (ws) => {
  console.log("WS connected");
  const timer = setInterval(() => {
    ws.send(
      JSON.stringify({
        eventType: "ControllerStatusEvent",
        event: { status, previousStatus: status },
      })
    );
  }, 500);
  ws.on("message", (msg) => {
    if (msg.toString() !== "ping") console.log("WS msg", msg.toString());
  });
  ws.on("close", () => clearInterval(timer));
});

server.listen(8080, () => console.log("Mock UGS backend on :8080"));
