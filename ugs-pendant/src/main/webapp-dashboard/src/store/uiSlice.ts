import { createSlice } from "@reduxjs/toolkit";

// Cross-panel UI state that doesn't belong to any one feature - currently
// which CenterPanel tab is active (or, in split mode, which content is
// assigned to each pane), so the RightRail's macro edit button can jump
// here without CenterPanel and RightRail needing to know about each other.
export type CenterView = "visualize" | "edit" | "split" | "macros" | "probe";

// The four things a split pane can show - everything CenterView has minus
// "split" itself, since a pane can't contain another split.
export type PaneContent = "visualize" | "edit" | "macros" | "probe";

type UiState = {
  centerView: CenterView;
  splitLeft: PaneContent;
  splitRight: PaneContent;
};

const initialState: UiState = {
  centerView: "visualize",
  splitLeft: "visualize",
  splitRight: "edit",
};

const uiSlice = createSlice({
  name: "ui",
  initialState,
  reducers: {
    setCenterView: (state, action: { payload: CenterView }) => {
      state.centerView = action.payload;
    },
    // Assigning a pane's content swaps with the other pane if that content
    // is already showing there, rather than ending up with two of the same
    // thing - both fields always stay in sync with what's actually visible.
    setSplitLeft: (state, action: { payload: PaneContent }) => {
      if (action.payload === state.splitRight) {
        state.splitRight = state.splitLeft;
      }
      state.splitLeft = action.payload;
    },
    setSplitRight: (state, action: { payload: PaneContent }) => {
      if (action.payload === state.splitLeft) {
        state.splitLeft = state.splitRight;
      }
      state.splitRight = action.payload;
    },
  },
});

export const uiActions = uiSlice.actions;
export default uiSlice.reducer;
