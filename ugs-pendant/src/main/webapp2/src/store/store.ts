import { combineReducers, configureStore } from "@reduxjs/toolkit";
import counterReducer from "./counterSlice";
import statusReducer from "./statusSlice";
import socketReducer from "./socketSlice";
import settingsReducer from "./settingsSlice";
import { socketMiddleware } from "./socketMiddleware";

const rootReducer = combineReducers({
  counter: counterReducer,
  status: statusReducer,
  socket: socketReducer,
  settings: settingsReducer
});

export const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(socketMiddleware),
});

export type RootState = ReturnType<typeof rootReducer>;
export type AppDispatch = typeof store.dispatch;
