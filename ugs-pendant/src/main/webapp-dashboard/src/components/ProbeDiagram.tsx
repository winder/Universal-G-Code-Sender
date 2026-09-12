import { ProbeOperation } from "../model/Probe";
import "./ProbeDiagram.scss";

type Props = {
  operation: ProbeOperation;
};

const STOCK = "#2a2d2e";
const STOCK_LINE = "#454a4c";
const CAVITY = "#16181a";
const TOOL = "#60a5fa";
const ARROW = "#fb923c";
const ZERO = "#4ade80";
const AXIS_REF = "#6b7280";

// A single-face probe: a solid stock block on one side, the tool on the other, a dashed arrow
// showing the probe direction, and a green line/label marking where the work-zero lands once
// contact is made. Geometry is defined per direction so the block/tool/arrow/label all read
// naturally regardless of which of the four sides is being probed.
const SINGLE_FACE: Record<string, { stock: string; tool: [number, number]; arrow: [number, number, number, number]; zero: string; zeroLabelPos: [number, number]; label: string }> = {
  X_NEG: {
    stock: "M20,20 H70 V150 H20 Z",
    tool: [155, 85],
    arrow: [140, 85, 78, 85],
    zero: "M70,15 V155",
    zeroLabelPos: [74, 28],
    label: "X0",
  },
  X_POS: {
    stock: "M150,20 H200 V150 H150 Z",
    tool: [65, 85],
    arrow: [80, 85, 142, 85],
    zero: "M150,15 V155",
    zeroLabelPos: [124, 28],
    label: "X0",
  },
  Y_NEG: {
    stock: "M20,110 H200 V150 H20 Z",
    tool: [110, 40],
    arrow: [110, 55, 110, 103],
    zero: "M15,110 H205",
    zeroLabelPos: [18, 104],
    label: "Y0",
  },
  Y_POS: {
    stock: "M20,20 H200 V60 H20 Z",
    tool: [110, 130],
    arrow: [110, 115, 110, 67],
    zero: "M15,60 H205",
    zeroLabelPos: [18, 78],
    label: "Y0",
  },
  Z: {
    stock: "M20,110 H200 V150 H20 Z",
    tool: [110, 40],
    arrow: [110, 55, 110, 103],
    zero: "M15,110 H205",
    zeroLabelPos: [18, 104],
    label: "Z0",
  },
};

// Center-finding: an outer stock block with a cavity cut into it, the tool starting in the
// middle, and arrows radiating out to whichever walls get probed - one pair for X or Y center,
// all four for a full bore/rectangle center. Same layout serves a round or rectangular
// cavity since the probing math doesn't care about the shape, only the two/four contact points.
const CENTER_ARROWS: Record<"X_CENTER" | "Y_CENTER" | "CENTER", [number, number, number, number][]> = {
  X_CENTER: [
    [110, 85, 60, 85],
    [110, 85, 160, 85],
  ],
  Y_CENTER: [
    [110, 85, 110, 40],
    [110, 85, 110, 130],
  ],
  CENTER: [
    [110, 85, 60, 85],
    [110, 85, 160, 85],
    [110, 85, 110, 40],
    [110, 85, 110, 130],
  ],
};

const ProbeDiagram = ({ operation }: Props) => {
  const isCenter = operation === "X_CENTER" || operation === "Y_CENTER" || operation === "CENTER";

  return (
    <svg className="probeDiagram" viewBox="0 0 220 170" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <pattern id="probeStockHatch" width="8" height="8" patternTransform="rotate(45)" patternUnits="userSpaceOnUse">
          <rect width="8" height="8" fill={STOCK} />
          <line x1="0" y1="0" x2="0" y2="8" stroke={STOCK_LINE} strokeWidth="2" />
        </pattern>
        <marker id="probeArrowHead" markerWidth="8" markerHeight="8" refX="6" refY="4" orient="auto">
          <path d="M0,0 L8,4 L0,8 Z" fill={ARROW} />
        </marker>
        <marker id="probeAxisRefHead" markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L6,3 L0,6 Z" fill={AXIS_REF} />
        </marker>
      </defs>

      {isCenter ? (
        <>
          <rect x="15" y="15" width="190" height="140" rx="6" fill="url(#probeStockHatch)" />
          <rect x="55" y="45" width="110" height="80" rx="4" fill={CAVITY} stroke={STOCK_LINE} strokeWidth="1" />
          {CENTER_ARROWS[operation as "X_CENTER" | "Y_CENTER" | "CENTER"].map(([x1, y1, x2, y2], i) => (
            <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke={ARROW} strokeWidth="2.5" strokeDasharray="5 4" markerEnd="url(#probeArrowHead)" />
          ))}
          <circle cx="110" cy="85" r="4" fill={ZERO} />
          <text x="117" y="82" fill={ZERO} fontSize="11" fontWeight="600">
            0
          </text>
          {renderTool(110, 85)}
        </>
      ) : (
        <>
          <path d={SINGLE_FACE[operation].stock} fill="url(#probeStockHatch)" />
          <path d={SINGLE_FACE[operation].zero} stroke={ZERO} strokeWidth="2" />
          <text x={SINGLE_FACE[operation].zeroLabelPos[0]} y={SINGLE_FACE[operation].zeroLabelPos[1]} fill={ZERO} fontSize="12" fontWeight="600">
            {SINGLE_FACE[operation].label}
          </text>
          <line
            x1={SINGLE_FACE[operation].arrow[0]}
            y1={SINGLE_FACE[operation].arrow[1]}
            x2={SINGLE_FACE[operation].arrow[2]}
            y2={SINGLE_FACE[operation].arrow[3]}
            stroke={ARROW}
            strokeWidth="2.5"
            strokeDasharray="5 4"
            markerEnd="url(#probeArrowHead)"
          />
          {renderTool(SINGLE_FACE[operation].tool[0], SINGLE_FACE[operation].tool[1])}
        </>
      )}

      {operation !== "Z" && (
        <g className="probeDiagramAxisRef" transform="translate(12, 158)">
          <line x1="0" y1="0" x2="16" y2="0" stroke={AXIS_REF} strokeWidth="1.5" markerEnd="url(#probeAxisRefHead)" />
          <text x="19" y="3" fill={AXIS_REF} fontSize="8">
            X+
          </text>
          <line x1="0" y1="0" x2="0" y2="-16" stroke={AXIS_REF} strokeWidth="1.5" markerEnd="url(#probeAxisRefHead)" />
          <text x="-4" y="-19" fill={AXIS_REF} fontSize="8">
            Y+
          </text>
        </g>
      )}
    </svg>
  );
};

function renderTool(cx: number, cy: number) {
  return (
    <g>
      <circle cx={cx} cy={cy} r="10" fill="none" stroke={TOOL} strokeWidth="2" />
      <line x1={cx - 5} y1={cy} x2={cx + 5} y2={cy} stroke={TOOL} strokeWidth="1.5" />
      <line x1={cx} y1={cy - 5} x2={cx} y2={cy + 5} stroke={TOOL} strokeWidth="1.5" />
    </g>
  );
}

export default ProbeDiagram;
