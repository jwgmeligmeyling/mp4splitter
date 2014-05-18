package nl.sslleiden.swing;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public abstract class CustomTableModel extends AbstractTableModel implements TableModel {
	
	private static final long serialVersionUID = -4844281628685914907L;
	
	private final Column[] columns;

	public CustomTableModel(Column[] columns) {
		assert columns != null;
		this.columns = columns;
	}
	
	@Override
	public String getColumnName(int column) {
		assert column < columns.length;
        return columns[column].getHeader();
    }

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		assert columnIndex < columns.length;
		Column column = columns[columnIndex];
		return column.getValueAt(rowIndex);
	}
	
	@Override
    public Class<?> getColumnClass(int columnIndex) {
		assert columnIndex < columns.length;
        return columns[columnIndex].getColumnClass();
    }

   
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		assert columnIndex < columns.length;
		Column column = columns[columnIndex];
		return column.isEditable();
    }
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		assert columnIndex < columns.length;
		Column column = columns[columnIndex];
		assert column.isEditable();
		column.setValueAt(rowIndex, aValue);
	}

}
