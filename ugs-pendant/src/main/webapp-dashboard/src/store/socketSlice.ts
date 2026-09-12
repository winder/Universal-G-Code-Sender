import { createSlice } from "@reduxjs/toolkit";

export interface SocketState {
  isEstablishingConnection: boolean;
  isConnected: boolean;
  // Timestamp of the last WebSocket message received, of any type - not just
  // status pushes. Lets the UI distinguish "connected" from "actually
  // receiving live data", since a WebSocket can stay technically open for a
  // while after the underlying connection has gone stale.
  lastMessageAt: number | null;
}

const initialState: SocketState = {
  isEstablishingConnection: false,
  isConnected: false,
  lastMessageAt: null,
};

const socketSlice = createSlice({
  name: "socket",
  initialState,
  reducers: {
    connect: (state) => {
      state.isEstablishingConnection = true;
    },
    connectionEstablished: (state) => {
      state.isConnected = true;
      state.isEstablishingConnection = true;
      state.lastMessageAt = Date.now();
    },
    connectionClosed: (state) => {
      state.isConnected = false;
      state.isEstablishingConnection = false;
      state.lastMessageAt = null;
    },
    messageReceived: (state) => {
      state.lastMessageAt = Date.now();
    },
  },
});

export const socketActions = socketSlice.actions;

export default socketSlice.reducer;
