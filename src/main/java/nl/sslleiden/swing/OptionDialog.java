package nl.sslleiden.swing;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.JRootPane;

import nl.sslleiden.util.Proposal;

public class OptionDialog extends JOptionPane {

	private static final long serialVersionUID = 5665097740793958984L;
	
	public enum OptionType {
		DEFAULT_OPTION(JOptionPane.DEFAULT_OPTION),
		YES_NO_OPTION(JOptionPane.YES_NO_OPTION),
		YES_NO_CANCEL_OPTION(JOptionPane.YES_NO_CANCEL_OPTION),
		OK_CANCEL_OPTION(JOptionPane.OK_CANCEL_OPTION);
		
		public final int value;
		
		private OptionType(int value) {
			this.value = value;
		}
	}
	
	public enum MessageType {
		ERROR_MESSAGE(JOptionPane.ERROR_MESSAGE, JRootPane.ERROR_DIALOG),
		INFORMATION_MESSAGE(JOptionPane.INFORMATION_MESSAGE, JRootPane.INFORMATION_DIALOG),
		WARNING_MESSAGE(JOptionPane.WARNING_MESSAGE, JRootPane.WARNING_DIALOG),
		QUESTION_MESSAGE(JOptionPane.QUESTION_MESSAGE, JRootPane.QUESTION_DIALOG),
		PLAIN_MESSAGE(JOptionPane.PLAIN_MESSAGE, JRootPane.PLAIN_DIALOG);
		
		public final int value, style;
		
		private MessageType(int value, int style) {
			this.value = value;
			this.style = style;
		}
	}
	
	public static class Builder {
		
		private final Component parentComponent;
		private final Proposal<?> proposal;
		
		private String message, title;
		
		
		private OptionType optionType = OptionType.DEFAULT_OPTION;
		private MessageType messageType = MessageType.PLAIN_MESSAGE;
		
		public Builder(Component parentComponent, Proposal<?> proposal) {
			assert proposal != null;
			this.parentComponent = parentComponent;
			this.proposal = proposal;
		}
		
		public Builder setMessage(String message) {
			assert message != null;
			this.message = message;
			return this;
		}
		
		public Builder setTitle(String title) {
			assert title != null;
			this.title = title;
			return this;
		}
		
		public Builder setOptionType(OptionType type) {
			this.optionType = type;
			return this;
		}
		
		public Builder setMessageType(MessageType type) {
			this.messageType = type;
			return this;
		}
		
		public void show() {
			int result = JOptionPane.showOptionDialog(parentComponent, message, title, optionType.value, messageType.value, null, null, null);
			switch(result) {
			case JOptionPane.OK_OPTION:
				proposal.accept();
			case JOptionPane.NO_OPTION:
				proposal.decline();
			case JOptionPane.CANCEL_OPTION:
				proposal.cancel();
				break;
			case JOptionPane.CLOSED_OPTION:
				proposal.close();
				break;
			}
		}
		
	}
	
	public static Builder builder(Component parent, Proposal<?> proposal) {
		return new Builder(parent, proposal);
	}

}
