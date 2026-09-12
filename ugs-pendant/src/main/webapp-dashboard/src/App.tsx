import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { useAppSelector } from "./hooks/useAppSelector";
import "./App.scss";
import WaitingPage from "./pages/WaitingPage";
import Dashboard from "./pages/Dashboard";
import { socketActions } from "./store/socketSlice";

function App() {
  const isConnected = useAppSelector((state) => state.socket.isConnected);
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(socketActions.connect());
  }, [dispatch]);

  // The dashboard itself now shows connection state and a way to connect right in
  // the top bar, so there's no separate full-page "disconnected" screen to get
  // stuck on - once the websocket is up, the dashboard is always what you see.
  return <div className="app">{isConnected ? <Dashboard /> : <WaitingPage />}</div>;
}

export default App;
