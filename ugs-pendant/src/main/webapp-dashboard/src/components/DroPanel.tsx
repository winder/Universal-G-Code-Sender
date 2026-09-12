import AxisRow, { AxisType } from "./AxisRow";
import ModalStatusRow from "./ModalStatusRow";

const DroPanel = () => {
  return (
    <div className="droPanel">
      <ModalStatusRow />
      <AxisRow axisType={AxisType.X} />
      <AxisRow axisType={AxisType.Y} />
      <AxisRow axisType={AxisType.Z} />
      <AxisRow axisType={AxisType.A} />
      <AxisRow axisType={AxisType.B} />
      <AxisRow axisType={AxisType.C} />
    </div>
  );
};

export default DroPanel;
