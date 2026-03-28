import { combineReducers, configureStore } from "@reduxjs/toolkit";
import statusReducer from "./statusSlice";
import socketReducer from "./socketSlice";
import settingsReducer from "./settingsSlice";
import fileStatusReducer from "./fileStatusSlice";
import consoleReducer from "./consoleSlice";

import { socketMiddleware } from "./socketMiddleware";

const rootReducer = combineReducers({
  status: statusReducer,
  socket: socketReducer,
  settings: settingsReducer,
  fileStatus: fileStatusReducer,
  console: consoleReducer,
});

export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(socketMiddleware),
});

export type RootState = ReturnType<typeof rootReducer>;
export type AppDispatch = typeof store.dispatch;
