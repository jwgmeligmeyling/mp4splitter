package nl.youngmediaexperts.data;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * A Marker describes a specific point in time for a {@code MediaFile}
 * 
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Slf4j
public class Marker implements Comparable<Marker>, Serializable {
	
	private static final long serialVersionUID = 4173433154013055010L;

	private Timestamp timestamp;
	
	private String description;
	
	/**
	 * Construct an empty marker
	 */
	public Marker() {
		this(new Timestamp(0,0,0), "");
	}
	
	/**
	 * Construct a new Marker
	 * @param hours
	 * @param minutes
	 * @param seconds
	 * @param description
	 */
	public Marker(Timestamp timestamp, String description) {
		this.timestamp = timestamp;
		this.description = description;
		log.info("Created marker {}", this);
	}

	@Override
	public int compareTo(Marker o) {
		return timestamp.compareTo(o.timestamp);
	}
	
	/**
	 * Substract a {@code Timestamp} from this {@code Marker}
	 * @param t
	 * @return a copy of this {@code Marker} with the subtraction applied to it
	 */
	public Marker subtract(Timestamp t) {
		return new Marker(timestamp.subtract(t), description);
	}
	
	/**
	 * Add a {@code Timestamp} from this {@code Marker}
	 * @param t
	 * @return a copy of this {@code Marker} with the addition applied to it
	 */
	public Marker add(Timestamp t) {
		return new Marker(timestamp.add(t), description);
	}
	
	/**
	 * Write this {@code Marker} to a {@code Writer}
	 * @param writer
	 * @throws IOException
	 */
	public void write(Writer writer) throws IOException {
		final String EOL = System.lineSeparator();
		writer.append('\t').append(timestamp.toString()).append(' ')
			.append(description).append(EOL);
	}
	
}