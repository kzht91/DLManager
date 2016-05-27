package javaapp;

import java.util.*;
import javax.swing.table.*;

class DTableModel extends AbstractTableModel implements Observer {

    private static final String[] columnNames = {"File Name", "File Size", "Left", "Downloaded", "Status"};
    ArrayList downloads = new ArrayList();

    public void addDownload(Download download) {
        download.addObserver(this);
        downloads.add(download);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public Download getDownload(int row) {
        return (Download) downloads.get(row);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getRowCount() {
        return downloads.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Download download = (Download) downloads.get(row);
        switch (col) {
            case 0:
                return download.getFileName();
            case 1:
                int size = download.getSize();
                return (size == -1) ? "" : Integer.toString(size);
            case 2:
                return download.getLeft();
            case 3:
                return download.getDone();
            case 4:
                return Download.STATUSES[download.getStatus()];
        }
        return "";
    }

    @Override
    public void update(Observable o, Object arg) {
        int index = downloads.indexOf(o);
        fireTableRowsUpdated(index, index);
    }
}
