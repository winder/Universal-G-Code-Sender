package com.willwinder.universalgcodesender.pendantui.v1.model;

public record FileStatus(String fileName, long rowCount, long completedRowCount, long remainingRowCount,
                         long sendDuration, long sendRemainingDuration) {
}
