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
import Footer from "./components/Footer";

function App() {
  const status = useAppSelector((state) => state.status);
  const isConnected = useAppSelector((state) => state.socket.isConnected);
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(socketActions.connect());
  }, [dispatch]);

  return (
    <div className="app">
      <Header />
      {!isConnected && (status.state === "DISCONNECTED" || status.state === "CONNECTING") && <WaitingPage />}
      {isConnected && status.state === "DISCONNECTED" && <ConnectPage />}
      {isConnected && (status.state !== "DISCONNECTED" && status.state !== "CONNECTING") && <MainPage />}
      {status.state === "ALARM" && <AlarmModal/>}
      <Footer/>
    </div>
  );
}

export default App;
