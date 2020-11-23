package lexical_analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Token {
	//REGULAR EXPRESSIONS OF TOKENS
	
	//IDENTIFIERS
	public final static Pattern VARIABLE_IDENTIFIER = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");
	public final static Pattern FUNCTION_LOOP_IDENTIFIER = Pattern.compile("[A-Z][A-Z0-9_]*");
	
	//LITERALS
	public final static Pattern NUMBR_LITERAL = Pattern.compile("-?[0-9]+");
	public final static Pattern NUMBAR_LITERAL = Pattern.compile("-?[0-9]+\\.[0-9]+");
	public final static Pattern YARN_LITERAL = Pattern.compile("(?<=\").*(?=\")");
	public final static String WIN_TROOF_LITERAL = "WIN";
	public final static String FAIL_TROOF_LITERAL = "FAIL";
	public final static String NUMBR_TYPE_LITERAL = "NUMBR";
	public final static String NUMBAR_TYPE_LITERAL = "NUMBAR";
	public final static String YARN_TYPE_LITERAL = "YARN";
	public final static String TROOF_TYPE_LITERAL = "TROOF";
	public final static String NOOB_TYPE_LITERAL = "NOOB";
	public final static String AN_TYPE_LITERAL = "AN";
	
	//KEYWORDS
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
	
	//DELIMITERS
	public final static String STRING_DELIMITER = "\"";
	public final static String OPEN_PARENTHESIS_DELIMETER = "(";
	public final static String CLOSE_PARENTHESIS_DELIMETER = ")";
	
	
	//CLASSIFICATION OF TOKENS
	
	//FOR IDENTIFIERS
	public final static String VARIABLE_IDENTIFIER_CLASSIFIER = "Variable Identifier";
	public final static String FUNCTION_LOOP_IDENTIFIER_CLASSIFIER = "Function/Loop Identifier";   
	
	//FOR LITERALS
	public final static String NUMBR_LITERAL_CLASSIFIER = "Numbr Literal";  
	public final static String NUMBAR_LITERAL_CLASSIFIER = "Numbar Literal"; 
	public final static String YARN_LITERAL_CLASSIFIER = "Yarn Literal";  
	public final static String TROOF_LITERAL_CLASSIFIER = "Troof Literal";   
	public final static String TYPE_LITERAL_CLASSIFIER = "Literal";   
	
	//FOR KEYWORDS
	public final static String HAI_CLASSIFIER = "Program Initializer";
	public final static String KTHXBYE_CLASSIFIER = "Program Terminator";
	public final static String BTW_CLASSIFIER = "Comment (one line)";
	public final static String OBTW_CLASSIFIER = "Comment (multiline) Signifier";
	public final static String TLDR_CLASSIFIER = "Comment (multiline) Ender";
	public final static String I_HAS_A_CLASSFIER = "Variable Declaration";
	public final static String ITZ_CLASSIFIER = "Variable Assignment";
	public final static String R_CLASSIFIER = "Variable Assignment";
	public final static String SUM_OF_CLASSIFIER = "Sum Operator";
	public final static String DIFF_OF_CLASSIFIER = "Difference Operator";
	public final static String PRODUKT_OF_CLASSIFIER = "Product Operator";
	public final static String QUOSHUNT_OF_CLASSIFIER = "Quotient Operator";
	public final static String MOD_OF_CLASSIFIER = "Modulo Operator";
	public final static String BIGGR_OF_CLASSIFIER = "Bigger Operator";
	public final static String SMALLR_OF_CLASSIFIER = "Smaller Operator";
	public final static String BOTH_OF_CLASSIFIER = "Both of Operator";
	public final static String EITHER_OF_CLASSIFIER = "Either of Operator";
	public final static String WON_OF_CLASSIFIER = "Won of Operator";
	public final static String NOT_CLASSIFIER = "Not Operator";
	public final static String ANY_OF_CLASSIFIER = "Any of Operator";
	public final static String ALL_OF_CLASSIFIER = "All of Operator";
	public final static String BOTH_SAEM_CLASSIFIER = "Both Same Operator";
	public final static String DIFFRINT_CLASSIFIER = "Diffrint Operator";
	public final static String SMOOSH_CLASSIFIER = "Concatenation Keyword";
	public final static String A_CLASSIFIER = "A Keyword";
	public final static String IS_NOW_A_CLASSIFIER = "Is Now A Keyword";
	public final static String VISIBLE_CLASSIFIER = "Output Keyword";
	public final static String MAEK_CLASSIFIER = "Typecast Keyword";
	public final static String GIMMEH_CLASSIFIER = "Input Keyword";
	public final static String O_RLY_CLASSIFIER = "If-then Initializer";
	public final static String YA_RLY_CLASSIFIER = "If Keyword";
	public final static String MEBBE_CLASSIFIER = "Else-if Keyword";
	public final static String NO_WAI_CLASSIFIER = "Else Keyword";
	public final static String OIC_CLASSIFIER = "If-then Terminator";
	public final static String WTF_CLASSIFIER = "Switch case Initializer";
	public final static String OMG_CLASSIFIER = "Case keyword";
	public final static String OMGWTF_CLASSIFIER = "Default Case keyword";
	public final static String IM_IN_YR_CLASSIFIER = "Loop Initializer";
	public final static String UPPIN_CLASSIFIER = "Increment Keyword";
	public final static String NERFIN_CLASSIFIER = "Decrement Keyword";
	public final static String YR_CLASSIFIER = "YR Keyword";
	public final static String TIL_CLASSIFIER = "Til Keyword";
	public final static String WILE_CLASSIFIER = "Wile Keyword";
	public final static String IM_OUTTA_YR_CLASSIFIER = "Loop Terminator";
	
	//FOR DELIMITERS
	public final static String STRING_DELIMITER_CLASSIFIER = "String Delimiter";
	public final static String OPEN_PARENTHESIS_CLASSIFIER = "Open Parenthesis Delimiter";
	public final static String CLOSE_PARENTHESIS_CLASSIFIER = "Close Parenthesis Delimiter";
	
	
	//MAPPING TOKEN (FOR BOOLEAN & TYPE LITERALS AND KEYWORDS) TO ITS CORRESPONDING CLASSIFICATION
	public final static Map<String, String> TOKEN_CLASSIFIER1 = new HashMap<String, String>(){
		private static final long serialVersionUID = 1L;

	{
		put(Token.WIN_TROOF_LITERAL,Token.TROOF_LITERAL_CLASSIFIER);   
		put(Token.FAIL_TROOF_LITERAL,Token.TROOF_LITERAL_CLASSIFIER);   
		put(Token.NUMBR_TYPE_LITERAL,Token.TYPE_LITERAL_CLASSIFIER); 
		put(Token.NUMBAR_TYPE_LITERAL,Token.TYPE_LITERAL_CLASSIFIER); 
		put(Token.YARN_TYPE_LITERAL,Token.TYPE_LITERAL_CLASSIFIER); 
		put(Token.TROOF_TYPE_LITERAL,Token.TYPE_LITERAL_CLASSIFIER); 
		put(Token.NOOB_TYPE_LITERAL,Token.TYPE_LITERAL_CLASSIFIER);    
		put(Token.AN_TYPE_LITERAL,Token.TYPE_LITERAL_CLASSIFIER);    
		put(Token.HAI,    Token.HAI_CLASSIFIER);
	    put(Token.KTHXBYE, Token.KTHXBYE_CLASSIFIER);
	    put(Token.BTW,   Token.BTW_CLASSIFIER);
	    put(Token.OBTW, Token.OBTW_CLASSIFIER);
	    put(Token.TLDR, Token.TLDR_CLASSIFIER);
	    put(Token.I_HAS_A, Token.I_HAS_A_CLASSFIER);
	    put(Token.ITZ, Token.ITZ_CLASSIFIER);
	    put(Token.R, Token.R_CLASSIFIER);
	    put(Token.SUM_OF, Token.SUM_OF_CLASSIFIER);
	    put(Token.DIFF_OF, Token.DIFF_OF_CLASSIFIER);
	    put(Token.PRODUKT_OF, Token.PRODUKT_OF_CLASSIFIER);
	    put(Token.QUOSHUNT_OF, Token.QUOSHUNT_OF_CLASSIFIER);
	    put(Token.MOD_OF, Token.MOD_OF_CLASSIFIER);
	    put(Token.BIGGR_OF, Token.BIGGR_OF_CLASSIFIER);
	    put(Token.SMALLR_OF, Token.SMALLR_OF_CLASSIFIER);
	    put(Token.BOTH_OF, Token.BOTH_OF_CLASSIFIER);
	    put(Token.EITHER_OF, Token.EITHER_OF_CLASSIFIER);
	    put(Token.WON_OF, Token.WON_OF_CLASSIFIER);
	    put(Token.NOT, Token.NOT_CLASSIFIER);
	    put(Token.ANY_OF, Token.ANY_OF_CLASSIFIER);
	    put(Token.ALL_OF, Token.ALL_OF_CLASSIFIER);
	    put(Token.BOTH_SAEM, Token.BOTH_SAEM_CLASSIFIER);
	    put(Token.DIFFRINT, Token.DIFFRINT_CLASSIFIER);
	    put(Token.SMOOSH, Token.SMOOSH_CLASSIFIER);
	    put(Token.MAEK, Token.MAEK_CLASSIFIER);
	    put(Token.A, Token.A_CLASSIFIER);
	    put(Token.IS_NOW_A, Token.IS_NOW_A_CLASSIFIER);
	    put(Token.VISIBLE, Token.VISIBLE_CLASSIFIER);
	    put(Token.GIMMEH, Token.GIMMEH_CLASSIFIER);
	    put(Token.O_RLY, Token.O_RLY_CLASSIFIER);
	    put(Token.YA_RLY, Token.YA_RLY_CLASSIFIER);
	    put(Token.MEBBE, Token.MEBBE_CLASSIFIER);
	    put(Token.NO_WAI, Token.NO_WAI_CLASSIFIER);
	    put(Token.OIC, Token.OIC_CLASSIFIER);
	    put(Token.WTF, Token.WTF_CLASSIFIER);
	    put(Token.OMG, Token.OMG_CLASSIFIER);
	    put(Token.OMGWTF, Token.OMGWTF_CLASSIFIER);
	    put(Token.IM_IN_YR, Token.IM_IN_YR_CLASSIFIER);
	    put(Token.UPPIN, Token.UPPIN_CLASSIFIER);
	    put(Token.NERFIN, Token.NERFIN_CLASSIFIER);
	    put(Token.YR, Token.YR_CLASSIFIER);
	    put(Token.TIL, Token.TIL_CLASSIFIER);
	    put(Token.WILE, Token.WILE_CLASSIFIER);
	    put(Token.IM_OUTTA_YR, Token.IM_OUTTA_YR_CLASSIFIER);
	    put(Token.STRING_DELIMITER, Token.STRING_DELIMITER_CLASSIFIER);
	    put(Token.OPEN_PARENTHESIS_DELIMETER, Token.OPEN_PARENTHESIS_CLASSIFIER);
	    put(Token.CLOSE_PARENTHESIS_DELIMETER, Token.CLOSE_PARENTHESIS_CLASSIFIER);
	    
	}};
	
	//MAPPING TOKEN (FOR IDENTIFIERS AND NUMBER & STRING LITERALS) TO ITS CORRESPONDING CLASSIFICATION
	public final static Map<Pattern, String> TOKEN_CLASSIFIER2 = new HashMap<Pattern, String>(){
		private static final long serialVersionUID = 1L;
	{
		put(Token.VARIABLE_IDENTIFIER,Token.VARIABLE_IDENTIFIER_CLASSIFIER);   
		put(Token.FUNCTION_LOOP_IDENTIFIER,FUNCTION_LOOP_IDENTIFIER_CLASSIFIER);   
		put(Token.NUMBR_LITERAL,Token.NUMBR_LITERAL_CLASSIFIER); 
		put(Token.NUMBAR_LITERAL,Token.NUMBAR_LITERAL_CLASSIFIER); 
		put(Token.YARN_LITERAL,Token.YARN_LITERAL_CLASSIFIER); 
	}};
	
	private String lexeme;
	private String classification;
	
	public Token(String lexeme,String classification) {
		this.lexeme = lexeme;
		this.classification = classification;
	}
	
	public String getLexeme() {
		return this.lexeme;
	}
	
	public String getClassification() {
		return this.classification;
	}
}
