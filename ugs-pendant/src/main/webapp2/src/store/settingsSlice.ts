import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { Settings } from "../model/Settings";
import { fetchSettings, putSettings } from "../services/settings";
import { RootState } from "./store";

export const getSettings = createAsyncThunk<Settings, void>(
  "settings/get",
  fetchSettings
);

export const setSettings = createAsyncThunk<
  Settings,
  Settings,
  { state: RootState }
>("settings/set", async (settings, thunkApi) => {
  const currentState = thunkApi.getState();
  if (
    currentState.settings.jogFeedRate === settings.jogFeedRate &&
    currentState.settings.jogStepSizeXY === settings.jogStepSizeXY &&
    currentState.settings.jogStepSizeZ === settings.jogStepSizeZ &&
    currentState.settings.port === settings.port &&
    currentState.settings.portRate === settings.portRate &&
    currentState.settings.firmwareVersion === settings.firmwareVersion &&
    currentState.settings.useZStepSize === settings.useZStepSize
  ) {
    return currentState.settings;
  }

  return putSettings(settings);
});

const initialState: Settings = {
  jogFeedRate: 100,
  jogStepSizeXY: 1,
  preferredUnits: "MM",
  jogStepSizeZ: 1,
  firmwareVersion: "GRBL",
  port: "COM1",
  portRate: "115200",
  useZStepSize: true
};

const settingsSlice = createSlice({
  name: "status",
  initialState,
  reducers: {},
  extraReducers(builder) {
    builder.addCase(getSettings.pending, () => {
      return initialState;
    });
    builder.addCase(getSettings.fulfilled, (_state, action) => {
      return action.payload;
    });

    builder.addCase(setSettings.fulfilled, (_state, action) => {
      return action.payload;
    });
  },
});

// Action creators are generated for each case reducer function
export const settingsActions = settingsSlice.actions;
export default settingsSlice.reducer;
