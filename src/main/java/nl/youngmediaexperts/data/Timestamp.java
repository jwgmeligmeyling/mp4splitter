package nl.youngmediaexperts.data;

import java.io.Serializable;
import java.text.ParseException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of={"hours", "minutes", "seconds"})
public class Timestamp implements Comparable<Timestamp>, Serializable {
	
	private static final long serialVersionUID = -3087782637605354300L;

	private final int hours, minutes, seconds;
	
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

	public Timestamp(long lengthInSeconds) {
		hours = (int) (lengthInSeconds / 3600);
		minutes = (int) (lengthInSeconds % 3600 / 60);
		seconds = (int) (lengthInSeconds % 60);
	}
	
	public Timestamp(int hours, int minutes, int seconds) {
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
	}
	
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
	
	public static Timestamp subtract(Timestamp a, Timestamp b) {
		return new Timestamp(a.getTotalLength() - b.getTotalLength());
	}
	
	public static Timestamp add(Timestamp a, Timestamp b) {
		return new Timestamp(a.getTotalLength() + b.getTotalLength());
	}

	public Timestamp subtract(Timestamp t) {
		return subtract(this, t);
	}
	
	public Timestamp add(Timestamp t) {
		return add(this, t);
	}
	
}
