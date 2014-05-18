package nl.sslleiden.model;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public enum Niveau {
	ONBEKEND("O", "Onbekend"),
	VWO("V", "VWO"),
	HAVO("H", "HAVO");
	
	private final String code, omschrijving;
	
	private Niveau(String code, String omschrijving) {
		this.code = code;
		this.omschrijving = omschrijving;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getOmschrijving() {
		return omschrijving;
	}
	
}
