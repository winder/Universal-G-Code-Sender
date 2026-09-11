import { createSlice } from "@reduxjs/toolkit";

// Cross-panel UI state that doesn't belong to any one feature - currently
// just which CenterPanel tab is active, so the RightRail's macro edit
// button can switch to it without CenterPanel needing to know about
// RightRail (or vice versa).
export type CenterView = "visualize" | "edit" | "split" | "macros" | "probe";

type UiState = {
  centerView: CenterView;
};

const initialState: UiState = {
  centerView: "visualize",
};

const uiSlice = createSlice({
  name: "ui",
  initialState,
  reducers: {
    setCenterView: (state, action: { payload: CenterView }) => {
      state.centerView = action.payload;
    },
  },
});

export const uiActions = uiSlice.actions;
export default uiSlice.reducer;
