export type FileStatus = {
  fileName: string;
  rowCount: number;
  completedRowCount: number;
  remainingRowCount: number;
  sendDuration: number;
  sendRemainingDuration: number;
  // Unlike completedRowCount (a plain count from zero for this stream), this is the original
  // file's own command number of the most recently completed row - the one that lines up with
  // ToolpathSegment.lineNumber even when only part of the file is streaming (e.g. "run from
  // here"), where completedRowCount alone would badly undercount. -1 = nothing completed yet.
  lastCompletedLineNumber: number;
};
