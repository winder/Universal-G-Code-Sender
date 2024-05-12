import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { FileStatus } from "../model/FileStatus";
import { getFileStatus } from "../services/files";

export const fetchFileStatus = createAsyncThunk(
  "fileStatus/fetch",
  getFileStatus
);

const initialState: FileStatus = {
  fileName: "",
  rowCount: 0,
  completedRowCount: 0,
  remainingRowCount: 0,
  sendDuration: 0,
  sendRemainingDuration: 0,
};

const statusSlice = createSlice({
  name: "fileStatus",
  initialState,
  reducers: {
    setFileStatus: (state, action) => {
      state.fileName = action.payload.fileName;
      state.rowCount = action.payload.rowCount;
      state.completedRowCount = action.payload.completedRowCount;
      state.remainingRowCount = action.payload.remainingRowCount;
      state.sendDuration = action.payload.sendDuration;
      state.sendRemainingDuration = action.payload.sendRemainingDuration;
    },
  },
  extraReducers(builder) {
    builder.addCase(fetchFileStatus.pending, (state) => {
      return state;
    });
    builder.addCase(fetchFileStatus.fulfilled, (_state, action) => {
      return action.payload;
    });
    builder.addCase(fetchFileStatus.rejected, () => {
      return initialState;
    });
  },
});

// Action creators are generated for each case reducer function
export const statusActions = statusSlice.actions;
export default statusSlice.reducer;
