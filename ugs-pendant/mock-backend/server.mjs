import { createServer } from "http";
import { readFileSync } from "fs";
import { fileURLToPath } from "url";
import { dirname, join } from "path";
import { WebSocketServer } from "ws";

const __dirname = dirname(fileURLToPath(import.meta.url));

const settings = {
  jogFeedRate: 1000,
  jogStepSizeXY: 1,
  preferredUnits: "MM",
  jogStepSizeZ: 1,
  port: "COM3",
  portRate: "115200",
  firmwareVersion: "GRBL",
  useZStepSize: true,
  workspaceDirectory: "C:\\Users\\qvipe\\gcode-workspace",
};

let statusBroadcastPaused = false;

const status = {
  machineCoord: { x: 12.5, y: -4.2, z: 0.75, a: 0, b: 0, c: 0, units: "MM" },
  workCoord: { x: 12.5, y: -4.2, z: 0.75, a: 0, b: 0, c: 0, units: "MM" },
  feedSpeed: 0,
  spindleSpeed: 0,
  accessoryStates: { spindleCW: false, flood: false, mist: false },
  overrides: { feed: 100, rapid: 100, spindle: 100 },
  floodCoolantOn: false,
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

// A small set of files simulating the "workspace directory" - keyed by the bare
// filename the real backend would use.
const files = {
  "test-part.nc": `; sample part
G21 G90
G0 Z5
G0 X0 Y0
G1 Z-1 F200
G1 X50 Y0 F800
G1 X50 Y30
G1 X0 Y30
G1 X0 Y0
G0 Z5
`,
  "yeheart.gcode": readFileSync(join(__dirname, "yeheart.gcode"), "utf8"),
};

let activeFile = "yeheart.gcode";

const fileStatus = {
  fileName: activeFile,
  rowCount: files[activeFile].split(/\r?\n/).length,
  completedRowCount: 0,
  remainingRowCount: files[activeFile].split(/\r?\n/).length,
  sendDuration: 0,
  sendRemainingDuration: 0,
};

/**
 * A rough gcode-to-toolpath approximation for mock/dev purposes only - handles modal
 * G0/G1/G2/G3, absolute X/Y/Z, and I/J arc centers. Not a substitute for the real
 * backend's GcodeViewParse, just enough to visualize real-world files while iterating
 * on the dashboard UI without a Java build.
 */
function gcodeToSegments(text) {
  const segments = [];
  let x = 0, y = 0, z = 0;
  let lastG = null;

  const getNum = (line, letter) => {
    const m = line.match(new RegExp(letter + "(-?[0-9.]+)"));
    return m ? parseFloat(m[1]) : null;
  };

  for (const raw of text.split(/\r?\n/)) {
    const line = raw.replace(/;.*/, "").replace(/\([^)]*\)/g, "").trim();
    if (!line || /^#/.test(line) || /^o\d/i.test(line)) continue;

    const gMatch = line.match(/G(\d+)/);
    const g = gMatch ? parseInt(gMatch[1], 10) : lastG;

    const nx = getNum(line, "X");
    const ny = getNum(line, "Y");
    const nz = getNum(line, "Z");
    const i = getNum(line, "I");
    const j = getNum(line, "J");

    if (nx === null && ny === null && nz === null) {
      if (gMatch) lastG = g;
      continue;
    }

    const targetX = nx !== null ? nx : x;
    const targetY = ny !== null ? ny : y;
    const targetZ = nz !== null ? nz : z;

    if (g === 0 || g === 1) {
      segments.push({ start: { x, y, z }, end: { x: targetX, y: targetY, z: targetZ }, rapid: g === 0, arc: false });
      x = targetX; y = targetY; z = targetZ;
    } else if (g === 2 || g === 3) {
      const cx = x + (i || 0);
      const cy = y + (j || 0);
      const radius = Math.hypot(x - cx, y - cy);
      const startAngle = Math.atan2(y - cy, x - cx);
      let endAngle = Math.atan2(targetY - cy, targetX - cx);
      const clockwise = g === 2;
      let delta = endAngle - startAngle;
      if (clockwise) {
        while (delta >= 0) delta -= 2 * Math.PI;
      } else {
        while (delta <= 0) delta += 2 * Math.PI;
      }
      const steps = Math.max(2, Math.round((Math.abs(delta) / (2 * Math.PI)) * 96));
      let px = x, py = y, pz = z;
      for (let s = 1; s <= steps; s++) {
        const a = startAngle + delta * (s / steps);
        const sx = cx + radius * Math.cos(a);
        const sy = cy + radius * Math.sin(a);
        const sz = z + (targetZ - z) * (s / steps);
        segments.push({ start: { x: px, y: py, z: pz }, end: { x: sx, y: sy, z: sz }, rapid: false, arc: true });
        px = sx; py = sy; pz = sz;
      }
      x = targetX; y = targetY; z = targetZ;
    } else {
      x = targetX; y = targetY; z = targetZ;
    }
    if (gMatch) lastG = g;
  }

  return segments;
}

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
  if (p === "/api/v1/machine/sendGcode" && req.method === "POST") {
    let body = "";
    req.on("data", (c) => (body += c));
    req.on("end", () => {
      // Simulate just enough of a real controller's response to spindle/coolant
      // commands so the on/off buttons have something real to reflect in the mock.
      const commands = (JSON.parse(body || "{}").commands || "").toUpperCase();
      if (/\bM0?3\b/.test(commands)) {
        const sMatch = commands.match(/S(\d+)/);
        status.spindleSpeed = sMatch ? parseInt(sMatch[1], 10) : 1000;
        status.accessoryStates.spindleCW = true;
      } else if (/\bM0?4\b/.test(commands)) {
        const sMatch = commands.match(/S(\d+)/);
        status.spindleSpeed = sMatch ? parseInt(sMatch[1], 10) : 1000;
        status.accessoryStates.spindleCW = true;
      } else if (/\bM0?5\b/.test(commands)) {
        status.spindleSpeed = 0;
        status.accessoryStates.spindleCW = false;
      } else if (/\bM0?8\b/.test(commands)) {
        status.accessoryStates.flood = true;
        status.floodCoolantOn = true;
      } else if (/\bM0?9\b/.test(commands)) {
        status.accessoryStates.flood = false;
        status.accessoryStates.mist = false;
        status.floodCoolantOn = false;
      }
      // Mirrors the real backend's CommandEvent push - the dashboard's
      // console panel and its "refresh coolant state on M7/M8/M9" logic
      // (socketMiddleware.ts) both depend on this arriving over the socket,
      // not just the plain REST response.
      broadcast({
        eventType: "CommandEvent",
        event: { commandEventType: "COMMAND_SENT", command: { command: commands, response: "", isError: false, isOk: false } },
      });
      setTimeout(() => {
        broadcast({
          eventType: "CommandEvent",
          event: { commandEventType: "COMMAND_COMPLETE", command: { command: commands, response: "ok", isError: false, isOk: true } },
        });
      }, 50);
      json(res, {});
    });
    return;
  }
  if (p === "/api/v1/machine/sendOverride" && req.method === "POST") {
    const command = url.searchParams.get("command");
    const clamp = (value) => Math.min(200, Math.max(10, value));
    if (command === "CMD_FEED_OVR_RESET") status.overrides.feed = 100;
    else if (command === "CMD_FEED_OVR_COARSE_PLUS") status.overrides.feed = clamp(status.overrides.feed + 10);
    else if (command === "CMD_FEED_OVR_COARSE_MINUS") status.overrides.feed = clamp(status.overrides.feed - 10);
    else if (command === "CMD_RAPID_OVR_RESET") status.overrides.rapid = 100;
    else if (command === "CMD_RAPID_OVR_MEDIUM") status.overrides.rapid = 50;
    else if (command === "CMD_RAPID_OVR_LOW") status.overrides.rapid = 25;
    else if (command === "CMD_SPINDLE_OVR_RESET") status.overrides.spindle = 100;
    else if (command === "CMD_SPINDLE_OVR_COARSE_PLUS") status.overrides.spindle = clamp(status.overrides.spindle + 10);
    else if (command === "CMD_SPINDLE_OVR_COARSE_MINUS") status.overrides.spindle = clamp(status.overrides.spindle - 10);
    return json(res, {});
  }
  if (p === "/api/v1/machine/killAlarm" || p === "/api/v1/machine/softReset") {
    status.state = "IDLE";
    return json(res, {});
  }
  // Mock-only hook (not part of the real API) for exercising the alarm modal
  // and its per-alarm-type message without real hardware. e.g.:
  //   curl "http://localhost:8080/debug/triggerAlarm?type=SOFT_LIMIT"
  if (p === "/debug/pauseStatusBroadcast") {
    statusBroadcastPaused = url.searchParams.get("value") !== "false";
    return json(res, { statusBroadcastPaused });
  }
  if (p === "/debug/triggerAlarm") {
    const type = url.searchParams.get("type") || "HARD_LIMIT";
    status.state = "ALARM";
    broadcast({ eventType: "AlarmEvent", event: { alarm: type } });
    return json(res, {});
  }
  if (p.startsWith("/api/v1/machine/")) return json(res, {});
  if (p === "/api/v1/files/getFileStatus") return json(res, fileStatus);
  if (p === "/api/v1/files/getWorkspaceFileList") return json(res, { fileList: Object.keys(files) });
  if (p === "/api/v1/files/openWorkspaceFile" && req.method === "POST") {
    const file = url.searchParams.get("file");
    if (file && files[file] !== undefined) {
      activeFile = file;
      fileStatus.fileName = file;
      fileStatus.rowCount = files[file].split(/\r?\n/).length;
      fileStatus.completedRowCount = 0;
      fileStatus.remainingRowCount = fileStatus.rowCount;
      broadcast({ eventType: "FileStateEvent", event: {} });
    }
    return json(res, {});
  }
  if (p === "/api/v1/files/getFileContent") {
    const file = url.searchParams.get("file") || activeFile;
    res.writeHead(200, { "Content-Type": "text/plain", "Access-Control-Allow-Origin": "*" });
    return res.end(files[file] ?? "");
  }
  if (p === "/api/v1/files/saveFileContent") {
    const file = url.searchParams.get("file") || activeFile;
    let body = "";
    req.on("data", (c) => (body += c));
    req.on("end", () => {
      files[file] = body;
      json(res, {});
    });
    return;
  }
  if (p === "/api/v1/files/saveFileContentAs" && req.method === "POST") {
    const filename = url.searchParams.get("filename");
    let body = "";
    req.on("data", (c) => (body += c));
    req.on("end", () => {
      files[filename] = body;
      activeFile = filename;
      fileStatus.fileName = filename;
      fileStatus.rowCount = body.split(/\r?\n/).length;
      fileStatus.completedRowCount = 0;
      fileStatus.remainingRowCount = fileStatus.rowCount;
      broadcast({ eventType: "FileStateEvent", event: {} });
      json(res, {});
    });
    return;
  }
  if (p === "/api/v1/files/closeFile" && req.method === "POST") {
    activeFile = null;
    fileStatus.fileName = "";
    fileStatus.rowCount = 0;
    fileStatus.completedRowCount = 0;
    fileStatus.remainingRowCount = 0;
    broadcast({ eventType: "FileStateEvent", event: {} });
    return json(res, {});
  }
  if (p.startsWith("/api/v1/files/")) return json(res, {});
  if (p === "/api/v1/macros/getMacroList") return json(res, macros);
  if (p.startsWith("/api/v1/macros/")) return json(res, {});
  if (p === "/api/v1/visualizer/getToolpath") return json(res, gcodeToSegments(files[activeFile] ?? ""));

  json(res, { error: "not found" }, 404);
});

const wss = new WebSocketServer({ server, path: "/ws/v1/events" });

// The real backend pushes a FileStateEvent over the websocket whenever a file is
// opened/loaded - the dashboard relies on that push (not just the HTTP response)
// to know a *different* file is now active. Mirror that here so mock testing
// actually exercises the same code path the real app does.
function broadcast(event) {
  wss.clients.forEach((client) => {
    if (client.readyState === client.OPEN) {
      client.send(JSON.stringify(event));
    }
  });
}

wss.on("connection", (ws) => {
  console.log("WS connected");
  let verboseEnabled = false;
  const timer = setInterval(() => {
    // Debug-only pause (see /debug/pauseStatusBroadcast) for exercising the
    // connection-health dot with no status traffic at all, matching how a
    // real controller goes quiet once idle (see FluidNCController.java's
    // dedup check) - the unconditional broadcast here otherwise never
    // reproduces that.
    if (statusBroadcastPaused) return;
    ws.send(
      JSON.stringify({
        eventType: "ControllerStatusEvent",
        event: { status, previousStatus: status },
      })
    );
  }, 500);
  // Mirrors EventsSocket.java's MessageType.VERBOSE stream - a real
  // connection's raw status-poll traffic, only sent to this session while it
  // has asked for it (see the "verbose:on"/"verbose:off" handling below).
  const verboseTimer = setInterval(() => {
    if (!verboseEnabled) return;
    const { x, y, z } = status.machineCoord;
    ws.send(
      JSON.stringify({
        eventType: "ConsoleMessageEvent",
        event: { message: `<${status.state}|MPos:${x.toFixed(3)},${y.toFixed(3)},${z.toFixed(3)}|FS:${status.feedSpeed},${status.spindleSpeed}>` },
      })
    );
  }, 300);
  ws.on("message", (msg) => {
    const text = msg.toString();
    if (text === "ping") {
      // Mirrors EventsSocket.java's new pong reply, so the connection-health
      // dot has a heartbeat independent of status pushes to test against.
      ws.send(JSON.stringify({ eventType: "Pong" }));
    } else if (text === "verbose:on") {
      verboseEnabled = true;
    } else if (text === "verbose:off") {
      verboseEnabled = false;
    } else {
      console.log("WS msg", text);
    }
  });
  ws.on("close", () => {
    clearInterval(timer);
    clearInterval(verboseTimer);
  });
});

server.listen(8080, () => console.log("Mock UGS backend on :8080"));
