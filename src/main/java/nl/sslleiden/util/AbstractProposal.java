package nl.sslleiden.util;

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
