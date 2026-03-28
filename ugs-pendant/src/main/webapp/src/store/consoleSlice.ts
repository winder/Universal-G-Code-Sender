import { createSlice, PayloadAction } from "@reduxjs/toolkit";

type Message = {
  type: "ok" | "error" | "info";
  text: string;
};

type ConsoleState = {
  messages: Message[];
};

const initialState: ConsoleState = {
  messages: [],
};

const consoleSlice = createSlice({
  name: "console",
  initialState,
  reducers: {
    addMessage: (state, action: PayloadAction<Message>) => {
      state.messages.push(action.payload);
    },
  },
});

// Action creators are generated for each case reducer function
export const consoleActions = consoleSlice.actions;
export default consoleSlice.reducer;
