import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { Status } from "../model/Status";
import getStatus from "../services/status";

export const fetchStatus = createAsyncThunk("status/fetch", getStatus);

const initialState: Status = {
  machineCoord: {
    x: 0,
    y: 0,
    z: 0,
    a: 0,
    b: 0,
    c: 0,
    units: "MM",
  },
  workCoord: {
    x: 0,
    y: 0,
    z: 0,
    a: 0,
    b: 0,
    c: 0,
    units: "MM",
  },
  feedSpeed: 0,
  spindleSpeed: 0,
  accessoryStates: {
    spindleCW: false,
    flood: false,
    mist: false,
  },
  overrides: {
    feed: 100,
    rapid: 100,
    spindle: 100,
  },
  floodCoolantOn: false,
  motionMode: "",
  coordinateSystem: "",
  plane: "",
  distanceMode: "",
  feedMode: "",
  units: "",
  spindleMode: "",
  toolNumber: 0,
  state: "DISCONNECTED",
  pins: {
    x: false,
    y: false,
    z: false,
    a: false,
    b: false,
    c: false,
    cycleStart: false,
    hold: false,
    probe: false,
    door: false,
    softReset: false,
  },
};

const statusSlice = createSlice({
  name: "status",
  initialState,
  reducers: {
    setStatus: (state, action) => {
      state.machineCoord = action.payload.machineCoord;
      state.workCoord = action.payload.workCoord;
      state.feedSpeed = action.payload.feedSpeed;
      state.spindleSpeed = action.payload.spindleSpeed;
      state.accessoryStates = {
        spindleCW: action.payload.accessoryStates?.spindleCW ?? false,
        flood: action.payload.accessoryStates?.flood ?? false,
        mist: action.payload.accessoryStates?.mist ?? false,
      };
      state.overrides = {
        feed: action.payload.overrides?.feed ?? 100,
        rapid: action.payload.overrides?.rapid ?? 100,
        spindle: action.payload.overrides?.spindle ?? 100,
      };
      // Not carried by the WebSocket push (only the REST getStatus response
      // has it) - preserve whatever was last fetched instead of resetting to
      // false/empty on every WS tick.
      state.floodCoolantOn = action.payload.floodCoolantOn ?? state.floodCoolantOn;
      state.motionMode = action.payload.motionMode ?? state.motionMode;
      state.coordinateSystem = action.payload.coordinateSystem ?? state.coordinateSystem;
      state.plane = action.payload.plane ?? state.plane;
      state.distanceMode = action.payload.distanceMode ?? state.distanceMode;
      state.feedMode = action.payload.feedMode ?? state.feedMode;
      state.units = action.payload.units ?? state.units;
      state.spindleMode = action.payload.spindleMode ?? state.spindleMode;
      state.toolNumber = action.payload.toolNumber ?? state.toolNumber;
      state.state = action.payload.state;
      state.pins = {
        x: action.payload.pins.x,
        y: action.payload.pins.y,
        z: action.payload.pins.z,
        a: action.payload.pins.a,
        b: action.payload.pins.b,
        c: action.payload.pins.c,
        cycleStart: action.payload.pins.cycleStart,
        hold: action.payload.pins.hold,
        probe: action.payload.pins.probe,
        softReset: action.payload.pins.softReset,
        door: action.payload.pins.door,
      };
    },
  },
  extraReducers(builder) {
    builder.addCase(fetchStatus.pending, (state) => {
      return state;
    });
    builder.addCase(fetchStatus.fulfilled, (_state, action) => {
      return action.payload;
    });
    builder.addCase(fetchStatus.rejected, () => {
      return initialState;
    });
  },
});

// Action creators are generated for each case reducer function
export const statusActions = statusSlice.actions;
export default statusSlice.reducer;
