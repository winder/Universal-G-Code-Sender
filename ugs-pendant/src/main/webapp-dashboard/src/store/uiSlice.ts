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
  // The 1-based editor line "Run from here" last armed on the backend, or 0
  // for a normal full run. Lives here (not in GcodeEditor's own state)
  // because the job bar - always visible, regardless of which center tab is
  // active - needs to show it and offer a way to clear it back to 0.
  runFromLine: number;
  // The 1-based line the editor's cursor is currently on, or 0 for none -
  // unlike runFromLine this updates live on every cursor move, not just on
  // confirm. Lives here so Visualizer3D can highlight the matching toolpath
  // segment without GcodeEditor and Visualizer3D needing to know about each
  // other directly.
  editorCursorLine: number;
};

const initialState: UiState = {
  centerView: "visualize",
  splitLeft: "visualize",
  splitRight: "edit",
  runFromLine: 0,
  editorCursorLine: 0,
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
    setRunFromLine: (state, action: { payload: number }) => {
      state.runFromLine = action.payload;
    },
    setEditorCursorLine: (state, action: { payload: number }) => {
      state.editorCursorLine = action.payload;
    },
  },
});

export const uiActions = uiSlice.actions;
export default uiSlice.reducer;
