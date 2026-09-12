import { createSlice } from "@reduxjs/toolkit";

export interface AlarmState {
  lastAlarm: string | null;
}

const initialState: AlarmState = {
  lastAlarm: null,
};

const alarmSlice = createSlice({
  name: "alarm",
  initialState,
  reducers: {
    setAlarm: (state, action: { payload: string }) => {
      state.lastAlarm = action.payload;
    },
  },
});

export const alarmActions = alarmSlice.actions;
export default alarmSlice.reducer;
