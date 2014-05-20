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
	
	public static Niveau getLevelForStr(String value) {
		for(Niveau niveau : Niveau.values()) {
			if(niveau.omschrijving.equalsIgnoreCase(value)) {
				return niveau;
			}
		}
		return Niveau.ONBEKEND;
	}
	
}
