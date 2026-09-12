import { createSlice, PayloadAction } from "@reduxjs/toolkit";

type Message = {
  type: "ok" | "error" | "info" | "verbose";
  text: string;
};

type ConsoleState = {
  messages: Message[];
  // Off by default - see EventsSocket.java's verboseSessionIds. Kept here
  // (rather than only as local component state) so socketMiddleware.ts can
  // re-send the toggle after a reconnect, and so it survives ConsolePanel
  // unmounting/remounting.
  verboseEnabled: boolean;
};

// VERBOSE messages arrive as fast as every status poll - without a cap this
// would grow without bound over a long session.
const MAX_MESSAGES = 500;

const initialState: ConsoleState = {
  messages: [],
  verboseEnabled: false,
};

const consoleSlice = createSlice({
  name: "console",
  initialState,
  reducers: {
    addMessage: (state, action: PayloadAction<Message>) => {
      state.messages.push(action.payload);
      if (state.messages.length > MAX_MESSAGES) {
        state.messages.splice(0, state.messages.length - MAX_MESSAGES);
      }
    },
    setVerboseEnabled: (state, action: PayloadAction<boolean>) => {
      state.verboseEnabled = action.payload;
    },
  },
});

// Action creators are generated for each case reducer function
export const consoleActions = consoleSlice.actions;
export default consoleSlice.reducer;
