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
  state: "DISCONNECTED",
  rowCount: 0,
  completedRowCount: 0,
  remainingRowCount: 0,
  fileName: "",
  sendDuration: 0,
  sendRemainingDuration: 0,
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
      state.state = action.payload.state;
      state.rowCount = action.payload.rowCount;
      state.completedRowCount = action.payload.completedRowCount;
      state.remainingRowCount = action.payload.remainingRowCount;
      state.fileName = action.payload.fileName;
      state.sendDuration = action.payload.sendDuration;
      state.sendRemainingDuration = action.payload.sendRemainingDuration;
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
