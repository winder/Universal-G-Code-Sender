export type ToolpathPoint = { x: number; y: number; z: number };

export type ToolpathSegment = {
  start: ToolpathPoint;
  end: ToolpathPoint;
  rapid: boolean;
  arc: boolean;
};

export const getToolpath = (): Promise<ToolpathSegment[]> => {
  return fetch("/api/v1/visualizer/getToolpath").then((response) => response.json());
};
