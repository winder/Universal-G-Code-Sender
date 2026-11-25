import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useAppSelector } from "./hooks/useAppSelector";
import "./App.scss";
import Header from "./components/Header";
import ConnectPage from "./pages/ConnectPage";
import LoginPage from "./pages/LoginPage";
import MainPage from "./pages/MainPage";
import { socketActions } from "./store/socketSlice";
import WaitingPage from "./pages/WaitingPage";
import AlarmModal from "./components/AlarmModal";
import {RouterProvider, createBrowserRouter, Navigate} from "react-router-dom";
import JogPanel from "./components/JogPanel";
import MacrosPanel from "./components/MacrosPanel";
import RunPanel from "./components/RunPanel";
import ConsolePanel from "./components/ConsolePanel.tsx";
import VisualizerPanel from "./components/VisualizerPanel";
import { getAuthStatus, logout } from "./services/auth";

function App() {
  const status = useAppSelector((state) => state.status);
  const isConnected = useAppSelector((state) => state.socket.isConnected);
  const dispatch = useDispatch();
  const [authChecked, setAuthChecked] = useState(false);
  const [authEnabled, setAuthEnabled] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);

  const checkAuth = () => {
    getAuthStatus()
      .then((res) => {
        setAuthEnabled(res.authEnabled);
        setAuthenticated(res.authenticated);
        setAuthChecked(true);
      })
      .catch(() => {
        setAuthChecked(true);
        setAuthenticated(false);
      });
  };

  useEffect(() => {
    checkAuth();
  }, []);

  useEffect(() => {
    if (authChecked && authenticated) {
      dispatch(socketActions.connect());
    }
  }, [authChecked, authenticated, dispatch]);

  const handleLogout = async () => {
    await logout();
    setAuthenticated(false);
    window.location.reload();
  };

  const router = createBrowserRouter([
    {
      path: "/",
      element: <MainPage />,
      children: [
        { index: true, element: <Navigate to="/jog" replace /> },
        {
          path: "jog",
          element: <JogPanel />,
        },
        {
          path: "visualizer",
          element: <VisualizerPanel />,
        },
        {
          path: "console",
          element: <ConsolePanel />,
        },
        {
          path: "macros",
          element: <MacrosPanel />,
        },
        {
          path: "run",
          element: <RunPanel />,
        },
      ],
    },
  ]);

  return (
    <div className="app">
      <Header authEnabled={authEnabled} authenticated={authenticated} onLogout={handleLogout} />
      <div className="appContainer">
        {!authChecked && <WaitingPage />}
        {authChecked && !authenticated && <LoginPage onSuccess={checkAuth} />}
        {authChecked && authenticated && !isConnected && <WaitingPage />}
        {authChecked && authenticated && isConnected && status.state === "DISCONNECTED" && <ConnectPage />}
        {authChecked && authenticated && isConnected && (status.state !== "DISCONNECTED" && status.state !== "CONNECTING") && <RouterProvider router={router} />}
        {authChecked && authenticated && status.state === "ALARM" && <AlarmModal/>}
      </div>
    </div>
  );
}

export default App;
