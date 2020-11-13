package lexical_analyzer;

public class Lexemes {
	public final static String HAI = "HAI";
	public final static String KTHXBYE = "KTHXBYE";
	public final static String BTW = "BTW";
	public final static String OBTW = "OBTW";
	public final static String TLDR = "TLDR";
	public final static String I_HAS_A = "I HAS A";
	public final static String ITZ = "ITZ";
	public final static String R = "R";
	public final static String SUM_OF = "SUM OF";
	public final static String DIFF_OF = "DIFF OF";
	public final static String PRODUKT_OF = "PRODUKT OF";
	public final static String QUOSHUNT_OF = "QUOSHUNT OF";
	public final static String MOD_OF = "MOD OF";
	public final static String BIGGR_OF = "BIGGR OF";
	public final static String SMALLR_OF = "SMALLR OF";
	public final static String BOTH_OF = "BOTH OF";
	public final static String EITHER_OF = "EITHER OF";
	public final static String WON_OF = "WON OF";
	public final static String NOT = "NOT";
	public final static String ANY_OF = "ANY OF";
	public final static String ALL_OF = "ALL OF";
	public final static String BOTH_SAEM = "BOTH SAEM";
	public final static String DIFFRINT = "DIFFRINT";
	public final static String SMOOSH = "SMOOSH";
	public final static String MAEK = "MAEK";
	public final static String A = "A";
	public final static String IS_NOW_A = "IS NOW A";
	public final static String VISIBLE = "VISIBLE";
	public final static String GIMMEH = "GIMMEH";
	public final static String O_RLY = "O RLY?";
	public final static String YA_RLY = "YA RLY";
	public final static String MEBBE = "MEBBE";
	public final static String NO_WAI = "NO WAI";
	public final static String OIC = "OIC";
	public final static String WTF = "WTF?";
	public final static String OMG = "OMG";
	public final static String OMGWTF = "OMGWTF";
	public final static String IM_IN_YR = "IM IN YR";
	public final static String UPPIN = "UPPIN";
	public final static String NERFIN = "NERFIN";
	public final static String YR = "YR";
	public final static String TIL = "TIL";
	public final static String WILE = "WILE";
	public final static String IM_OUTTA_YR = "IM OUTTA YR";
	
	private String lexeme;
	
	public Lexemes(String lexeme) {
		this.lexeme = lexeme;
	}
	
	public String getLexeme() {
		return this.lexeme;
	}
}
