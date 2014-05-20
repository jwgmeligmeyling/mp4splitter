package nl.youngmediaexperts.data;

import java.io.Serializable;
import java.text.ParseException;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @author Jan-Willem Gmelig Meyling
 *
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class Duration extends Timestamp implements Serializable {
	
	private static final long serialVersionUID = -6702460877507781706L;
	
	public final static Duration EMPTY = new Duration(Timestamp.START);
	
	/**
	 * Create a new {@code Duration}
	 * @param duration duration in frames
	 * @param timescale timescale for this video
	 */
	public Duration(long duration, long timescale) {
		super(duration / timescale);
	}
	
	/**
	 * Create a new {@code Duration} from a {@code Timestamp} string
	 * @param string String in the format MM:SS or HH:MM:SS
	 * @throws ParseException
	 */
	public Duration(String string) throws ParseException {
		super(string);
	}
	
	/**
	 * Create a new {@code Duration} from a {@code Timestamp}
	 * @param timestamp
	 */
	public Duration(Timestamp timestamp) {
		super(timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds());
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
}
