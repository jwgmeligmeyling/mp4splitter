package nl.sslleiden.swing;

public abstract class AbstractColumn implements Column {
	
	private final String header; 
	private final boolean editable;
	
	public AbstractColumn(String header) {
		this(header, false);
	}
	
	public AbstractColumn(String header, boolean editable) {
		this.header = header;
		this.editable = editable;
	}
	
	@Override
	public String getHeader() {
		return header;
	}
	
	@Override
	public Class<?> getColumnClass() {
		return String.class;
	}
	
	@Override
	public void setValueAt(int rowIndex, Object value) {
		// We override it here for Columns that not need to be editable
	}
		
	@Override
	public boolean isEditable() {
		return editable;
	}
	
}
