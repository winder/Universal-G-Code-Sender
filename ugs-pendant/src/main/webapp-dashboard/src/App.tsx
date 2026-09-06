import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { useAppSelector } from "./hooks/useAppSelector";
import "./App.scss";
import ConnectPage from "./pages/ConnectPage";
import WaitingPage from "./pages/WaitingPage";
import Dashboard from "./pages/Dashboard";
import { socketActions } from "./store/socketSlice";

function App() {
  const status = useAppSelector((state) => state.status);
  const isConnected = useAppSelector((state) => state.socket.isConnected);
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(socketActions.connect());
  }, [dispatch]);

  return (
    <div className="app">
      {!isConnected && <WaitingPage />}
      {isConnected && status.state === "DISCONNECTED" && <ConnectPage />}
      {isConnected &&
        status.state !== "DISCONNECTED" &&
        status.state !== "CONNECTING" && <Dashboard />}
    </div>
  );
}

export default App;
