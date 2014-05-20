package nl.youngmediaexperts.data;

import java.io.Serializable;
import java.text.ParseException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of={"hours", "minutes", "seconds"})
public class Timestamp implements Comparable<Timestamp>, Serializable {
	
	//Since timestamps are immutable, we can use a static final timestamp for the start values
	public final static Timestamp START = new Timestamp(0,0,0);
	
	private static final long serialVersionUID = -3087782637605354300L;

	private final int hours, minutes, seconds;
	
	/**
	 * Parse a {@code Timecode} from a String
	 * @param string String in the MM:SS or HH:MM:SS format
	 * @throws ParseException
	 */
	public Timestamp(String string) throws ParseException {
		assert string != null;
		String[] parts =
				string.split(":");
		try {
			if(parts.length == 2) {
				hours = 0;
				minutes = Integer.parseInt(parts[0]);
				seconds = Integer.parseInt(parts[1]);
			} else if (parts.length == 3 ){
				hours = Integer.parseInt(parts[0]);
				minutes = Integer.parseInt(parts[1]);
				seconds = Integer.parseInt(parts[2]);
			} else {
				throw new ParseException("Invalid input for Timestamp", 0);
			}
		} catch (NumberFormatException e) {
			throw new ParseException(e.getMessage(), 0);
		}
	}
	
	/**
	 * Construct a new {@code Timestamp}
	 * @param position the position in seconds
	 */
	public Timestamp(long position) {
		hours = (int) (position / 3600);
		minutes = (int) (position % 3600 / 60);
		seconds = (int) (position % 60);
	}
	
	/**
	 * Create a {@code Timestamp}
	 * @param hours amount of hours
	 * @param minutes amount of minutes
	 * @param seconds amount of seconds
	 */
	public Timestamp(int hours, int minutes, int seconds) {
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}
	
	/**
	 * @return the total length from begin to this {@code Timestamp}
	 */
	public long getTotalLength() {
		return (long) hours * 3600 + minutes * 60 + seconds;
	}

	@Override
	public String toString() {
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	@Override
	public int compareTo(Timestamp o) {
		int diff = hours - o.hours;
		if(diff == 0)
			diff = minutes - o.minutes;
		if(diff == 0)
			diff = seconds - o.seconds;
		return diff;
	}
	
	// SOME ARITHMETIC FOR TIMESTAMPS
	
	/**
	 * Subtract one {@code Timestamp} from another
	 * @param a
	 * @param b
	 * @return subtraction of b from a
	 */
	public static Timestamp subtract(Timestamp a, Timestamp b) {
		return new Timestamp(a.getTotalLength() - b.getTotalLength());
	}
	
	/**
	 * Add one {@code Timestamp} to another
	 * @param a
	 * @param b
	 * @return addition of a and b
	 */
	public static Timestamp add(Timestamp a, Timestamp b) {
		return new Timestamp(a.getTotalLength() + b.getTotalLength());
	}
	
	/**
	 * Subtract {@code Timestamp} from this {@code Timestamp}
	 * @param t
	 * @return subtraction of t from this
	 */
	public Timestamp subtract(Timestamp t) {
		return subtract(this, t);
	}
	
	/**
	 * Add {@code Timestamp} to this {@code Timestamp}
	 * @param t
	 * @return addition of this and t
	 */
	public Timestamp add(Timestamp t) {
		return add(this, t);
	}
	
}
