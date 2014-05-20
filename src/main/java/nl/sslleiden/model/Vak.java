package nl.sslleiden.model;

public enum Vak {
	OVERIG("O", "Overig"),
	AARDRIJKSKUNDE("AK", "Aardrijkskunde"),
	ECONOMIE("EC", "Economie"),
	MO("MO", "M&O"),
	NEDERLANDS("NE", "Nederlands"),
	ENGELS("EN", "Engels"),
	FRANS("FA", "Frans"),
	DUITS("DU", "Duits"),
	WISKUNDE_A("WA", "Wiskunde A"),
	WISKUNDE_B("WB", "Wiskunde B"),
	WISKUNDE_C("WC", "Wiskunde C"),
	NATUURKUNDE("NK", "Natuurkunde"),
	SCHEIKUNDE("SK", "Scheikunde"),
	BIOLOGIE("BIO", "Biologie"),
	KUA("KUA", "Kunst algemeen");
	
	private final String vakcode, naam;
	
	private Vak(String vakcode, String naam) {
		this.vakcode = vakcode;
		this.naam = naam;
	}
	
	public String getVakcode() {
		return vakcode;
	}
	
	public String getNaam() {
		return naam;
	}
	
	public static Vak getCourseForStr(String value) {
		for(Vak vak : Vak.values()) {
			if(vak.naam.equalsIgnoreCase(value)) {
				return vak;
			}
		}
		return Vak.OVERIG;
	}
}
