package com.willwinder.universalgcodesender.uielements.components;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class GCodeTableModel extends AbstractTableModel implements RemovalListener<Integer, GcodeCommand> {
    public static final int COL_INDEX_COMMAND       = 0;
    public static final int COL_INDEX_ORIG_COMMAND  = 1;
    public static final int COL_INDEX_SENT          = 2;
    public static final int COL_INDEX_DONE          = 3;
    public static final int COL_INDEX_RESPONSE      = 4;

    private static final long MAX_SIZE = 10000;

    private static final String[] COLUMN_NAMES = {
            Localization.getString("gcodeTable.command"),
            Localization.getString("gcodeTable.originalCommand"),
            Localization.getString("gcodeTable.sent"),
            Localization.getString("gcodeTable.done"),
            Localization.getString("gcodeTable.response")
    };

    private static final Class[] COLUMN_TYPES = {
            String.class,
            String.class,
            Boolean.class,
            Boolean.class,
            String.class
    };

    private final Cache<Integer, GcodeCommand> gcodeCommandMap = CacheBuilder.newBuilder()
            .maximumSize(MAX_SIZE)
            .removalListener(this)
            .build();

    private final List<GcodeCommand> gcodeCommandList = new ArrayList<>();

    public GCodeTableModel() {
    }

    @Override
    public int getRowCount() {
        return gcodeCommandList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_TYPES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(rowIndex >= gcodeCommandList.size()) {
            return null;
        }

        GcodeCommand command = gcodeCommandList.get(rowIndex);
        switch (columnIndex) {
            case COL_INDEX_COMMAND:
                return command.getCommandString();
            case COL_INDEX_ORIG_COMMAND:
                return command.getOriginalCommandString();
            case COL_INDEX_SENT:
                return command.isSent();
            case COL_INDEX_DONE:
                return command.isDone();
            case COL_INDEX_RESPONSE:
                return command.getResponse();
            default:
                return "";
        }
    }
    
    @Override
    public void onRemoval(RemovalNotification<Integer, GcodeCommand> removedGcodeCommand) {
        int index = gcodeCommandList.indexOf(removedGcodeCommand.getValue());
        gcodeCommandList.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public void add(GcodeCommand command) {
        gcodeCommandMap.put(command.getId(), command);
        gcodeCommandList.add(command);
        fireTableRowsInserted(gcodeCommandList.size(), gcodeCommandList.size());
    }

    public void removeRow(int index) {
        GcodeCommand command = gcodeCommandList.get(index);
        gcodeCommandList.remove(command);
        gcodeCommandMap.invalidate(command);
    }
}
