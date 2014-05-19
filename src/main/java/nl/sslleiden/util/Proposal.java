package nl.sslleiden.util;

public interface Proposal<T> {
	
	T get();
	
	void accept();
	
	void decline();
	
	void cancel();
	
	void close();
	
}
