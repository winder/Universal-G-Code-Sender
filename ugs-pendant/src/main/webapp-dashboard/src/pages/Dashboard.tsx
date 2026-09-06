import { useAppSelector } from "../hooks/useAppSelector";
import TopBar from "../components/TopBar";
import DroPanel from "../components/DroPanel";
import JogPad from "../components/JogPad";
import CenterPanel from "../components/CenterPanel";
import RightRail from "../components/RightRail";
import JobBar from "../components/JobBar";
import AlarmModal from "../components/AlarmModal";
import "./Dashboard.scss";

const Dashboard = () => {
  const status = useAppSelector((state) => state.status);

  return (
    <div className="dashboard">
      <TopBar />

      <div className="dashboardBody">
        <div className="dashboardLeft">
          <DroPanel />
          <JogPad />
        </div>

        <div className="dashboardCenter">
          <CenterPanel />
        </div>

        <div className="dashboardRight">
          <RightRail />
        </div>
      </div>

      <JobBar />

      {status.state === "ALARM" && <AlarmModal />}
    </div>
  );
};

export default Dashboard;
