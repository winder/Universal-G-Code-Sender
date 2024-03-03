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
  console.log(currentState.settings, settings);
  if (
    currentState.settings.jogFeedRate === settings.jogFeedRate &&
    currentState.settings.jogStepSizeXY === settings.jogStepSizeXY &&
    currentState.settings.jogStepSizeZ === settings.jogStepSizeZ
  ) {
    return currentState.settings;
  }

  return putSettings(settings);
});

const initialState: Settings = {
  jogFeedRate: 100,
  jogStepSizeXY: 1,
  preferredUnits: "mm",
  jogStepSizeZ: 1,
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
