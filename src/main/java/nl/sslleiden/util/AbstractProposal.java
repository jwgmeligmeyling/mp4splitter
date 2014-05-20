package nl.sslleiden.util;

/**
 * An simple implementation of {@code Proposal} that defines noop
 * behavior for the decline, cancel and close methods.
 * 
 * @author Jan-Willem Gmelig Meyling
 * 
 * @param <T> argument type
 */
public abstract class AbstractProposal<T> implements Proposal<T> {

	@Override
	public void decline() {
		// Do nothing
	}

	@Override
	public void cancel() {
		// Do nothing
	}

	@Override
	public void close() {
		// Do nothing
	}

}
