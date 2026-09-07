import { useAppSelector } from "../hooks/useAppSelector";
import TopBar from "../components/TopBar";
import DroPanel from "../components/DroPanel";
import FeedSpindleReadout from "../components/FeedSpindleReadout";
import PinsStatus from "../components/PinsStatus";
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
          <FeedSpindleReadout />
          <PinsStatus />

          <div className="dashboardLeftJog">
            <h6 className="dashboardSectionHeading">Jog</h6>
            <JogPad />
          </div>
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
