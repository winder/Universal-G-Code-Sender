import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { useAppSelector } from "./hooks/useAppSelector";
import "./App.scss";
import Header from "./components/Header";
import ConnectPage from "./pages/ConnectPage";
import MainPage from "./pages/MainPage";
import { socketActions } from "./store/socketSlice";
import WaitingPage from "./pages/WaitingPage";
import AlarmModal from "./components/AlarmModal";
import { RouterProvider, createBrowserRouter } from "react-router-dom";
import JogPanel from "./components/JogPanel";
import MacrosPanel from "./components/MacrosPanel";
import RunPanel from "./components/RunPanel";

function App() {
  const status = useAppSelector((state) => state.status);
  const isConnected = useAppSelector((state) => state.socket.isConnected);
  const dispatch = useDispatch();
  
  useEffect(() => {
    dispatch(socketActions.connect());
  }, [dispatch]);

  const router = createBrowserRouter([
    {
      path: "/",
      element: <MainPage />,
      children: [
        {
            path: "/",
            element: <JogPanel />,
        },
        {
          path: "jog",
          element: <JogPanel />,
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
      <Header />
      <div className="appContainer">
        {!isConnected && <WaitingPage />}
        {isConnected && status.state === "DISCONNECTED" && <ConnectPage />}
        {isConnected && (status.state !== "DISCONNECTED" && status.state !== "CONNECTING") && <RouterProvider router={router} />}
        {status.state === "ALARM" && <AlarmModal/>}
      </div>
    </div>
  );
}

export default App;
