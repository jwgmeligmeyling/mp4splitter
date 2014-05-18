package nl.sslleiden.swing;

public interface Column {

	String getHeader();

	Class<?> getColumnClass();

	Object getValueAt(int rowIndex);

	void setValueAt(int rowIndex, Object value);
	
	boolean isEditable();

}
