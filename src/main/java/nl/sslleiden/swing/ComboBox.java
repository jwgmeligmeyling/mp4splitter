package nl.sslleiden.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public abstract class ComboBox<T> {
	

	private final JLabel myLabel;
	private final JComboBox<T> myCombobox;
	private final JPanel panel;

	public ComboBox(String label, T selectedItem) {
		myLabel = new JLabel(label);
		myLabel.setPreferredSize(new Dimension(100, 10));
		myLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		myCombobox = new JComboBox<>(getOptions());
    	panel = new JPanel(new BorderLayout());
    	panel.add(myLabel, BorderLayout.WEST);
    	panel.add(myCombobox, BorderLayout.CENTER);
    	
    	myCombobox.addActionListener (new ActionListener() {
    		
    	    @SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
    	        ComboBox.this.setOption((T) myCombobox.getSelectedItem());
    	    }
    	    
    	});
    	
    	if(selectedItem != null)
    		myCombobox.setSelectedItem(selectedItem);
	}
	
	public abstract T[] getOptions();
	
	public abstract void setOption(T option);
	
	public JPanel getPanel() {
		return panel;
	}
	
}
