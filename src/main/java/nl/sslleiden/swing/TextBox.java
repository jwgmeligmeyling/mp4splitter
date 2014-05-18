package nl.sslleiden.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class TextBox {
	
	private final JLabel myLabel;
	private final JTextField myTextfield;
	private final JPanel panel;
	
	public TextBox(final String label) {
		myLabel = new JLabel(label);
		myLabel.setPreferredSize(new Dimension(100, 10));
		myLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

    	myTextfield = new JTextField(getValue());
    	panel = new JPanel(new BorderLayout());
    	panel.add(myLabel, BorderLayout.WEST);
    	panel.add(myTextfield, BorderLayout.CENTER);
    	
    	myTextfield.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				TextBox.this.setValue();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				TextBox.this.setValue();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				TextBox.this.setValue();
			}
    		
    	});
	}
	
	private void setValue() {
		setValue(myTextfield.getText());
	}
	
	public abstract String getValue();
	
	public abstract void setValue(String value);
	
	public String getLabel() {
		return myLabel.getText();
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
}
