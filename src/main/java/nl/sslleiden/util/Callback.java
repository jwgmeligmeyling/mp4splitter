package nl.sslleiden.util;

/**
 * A simple wrapper to work with {@code Callbacks} in Java
 * @author Jan-Willem Gmelig Meyling
 *
 * @param <T> argument type
 */
public interface Callback<T> {
	
	/**
	 * Call the {@code Callback} with an argument
	 * @param elem Argument to pass to the {@code Callback}
	 */
	void call(T elem);
	
}
