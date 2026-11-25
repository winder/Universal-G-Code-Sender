export interface ToolpathResponse {
  hasFile: boolean;
  fileName?: string;
  truncated: boolean;
  totalSegments: number;
  completedRows: number;
  minX: number;
  maxX: number;
  minY: number;
  maxY: number;
  minZ: number;
  maxZ: number;
  // [x1, y1, z1, x2, y2, z2, type, lineNumber]
  segments: number[][];
}

export const getToolpath = async (): Promise<ToolpathResponse> => {
  const response = await fetch("/api/v1/visualizer/toolpath");
  if (!response.ok) {
    throw new Error("Failed to load toolpath");
  }
  return response.json();
};
