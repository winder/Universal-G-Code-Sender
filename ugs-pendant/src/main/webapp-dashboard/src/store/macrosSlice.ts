import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { Macro } from "../model/Macro";
import { getMacroList } from "../services/macros";

// Single source of truth for the macro list, shared between the right
// rail's run buttons (MacrosPanel) and the editor (CenterPanel's Macros
// tab), so saving in one place is reflected in the other without either
// needing to know about the other's existence.
export const fetchMacros = createAsyncThunk("macros/fetch", getMacroList);

type MacrosState = {
  macros: Macro[];
  loaded: boolean;
};

const initialState: MacrosState = {
  macros: [],
  loaded: false,
};

const macrosSlice = createSlice({
  name: "macros",
  initialState,
  reducers: {
    // Called after a successful saveMacroList() - the backend's response is
    // the new authoritative list, so this replaces rather than merges.
    setMacros: (state, action: { payload: Macro[] }) => {
      state.macros = action.payload;
      state.loaded = true;
    },
  },
  extraReducers: (builder) => {
    builder.addCase(fetchMacros.fulfilled, (state, action) => {
      state.macros = action.payload;
      state.loaded = true;
    });
  },
});

export const macrosActions = macrosSlice.actions;
export default macrosSlice.reducer;
