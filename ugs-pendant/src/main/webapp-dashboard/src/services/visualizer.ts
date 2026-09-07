export type ToolpathPoint = { x: number; y: number; z: number };

export type ToolpathSegment = {
  start: ToolpathPoint;
  end: ToolpathPoint;
  rapid: boolean;
  arc: boolean;
};

export const getToolpath = (): Promise<ToolpathSegment[]> => {
  return fetch("/api/v1/visualizer/getToolpath").then((response) => {
    if (!response.ok) {
      throw new Error(`Couldn't load toolpath (${response.status})`);
    }
    return response.json();
  });
};
