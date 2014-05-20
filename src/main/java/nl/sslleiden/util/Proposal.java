package nl.sslleiden.util;

/**
 * The {@code Proposal} interface provides some method to invoke or cancel
 * scheduled actions.
 * 
 * @author Jan-Willem Gmelig Meyling
 * 
 * @param <T>
 */
public interface Proposal<T> {
	
	/**
	 * @return the value for this {@code Proposal}
	 */
	T get();
	
	/**
	 * The method called when the {@code Proposal} is accepted
	 */
	void accept();
	
	/**
	 * The method called when the {@code Proposal} is declined
	 */
	void decline();
	
	/**
	 * The method called when the {@code Proposal} is cancelled
	 */
	void cancel();
	
	/**
	 * The method called when the {@code Proposal} is closed
	 */
	void close();
	
}
