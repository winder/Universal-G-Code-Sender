import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { ConnectionParameters } from "../model/ConnectionParameters";

export const fetchConnectionParameters = createAsyncThunk(
  "connectionParameters/fetch",
  async () =>
    fetch("api/v1/machine/getParameters")
      .then((response) => response.json())
      .then((data) => data as ConnectionParameters)
);

const initialState: ConnectionParameters = {
  baudRates: [],
};

export const statusSlice = createSlice({
  name: "status",
  initialState,
  reducers: {},
  extraReducers(builder) {
    builder.addCase(fetchConnectionParameters.pending, () => {
      return initialState;
    });
    builder.addCase(fetchConnectionParameters.fulfilled, (_state, action) => {
      return action.payload;
    });
  },
});

// Action creators are generated for each case reducer function

export default statusSlice.reducer;
