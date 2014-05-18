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
	
	public Duration(long duration, long timescale) {
		super(duration / timescale);
	}
	
	public Duration(String string) throws ParseException {
		super(string);
	}
	
	public Duration(Timestamp timestamp) {
		super(timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds());
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
}
