// Forwarded by EventsSocket.java only to sessions that opted into verbose
// output (see consoleSlice.ts's verboseEnabled / socketMiddleware.ts).
export type ConsoleMessageEvent = {
  message: string;
};
