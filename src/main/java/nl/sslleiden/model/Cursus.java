package nl.sslleiden.model;

/**
 * Enumeration for the various type of courses
 * @author Jan-Willem Gmelig Meyling
 *
 */
public enum Cursus {
	EXAMENCURSUS("E", "Examencursus"),
	HERHANSINGSCURSUS("H", "Herkansingscursus"),
	STOOMCURSUS("S", "Stoomcursus"),
	OVERIG("O", "Overig");
	
	private final String code, omschrijving;
	
	private Cursus(String code, String omschrijving) {
		this.code = code;
		this.omschrijving = omschrijving;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getOmschrijving() {
		return omschrijving;
	}
	
	public static Cursus getTypeForStr(String value) {
		for(Cursus cursus : Cursus.values()) {
			if(cursus.omschrijving.equalsIgnoreCase(value)) {
				return cursus;
			}
		}
		return Cursus.OVERIG;
	}
}
