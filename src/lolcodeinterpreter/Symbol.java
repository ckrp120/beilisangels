package lolcodeinterpreter;

public class Symbol {
	
	public final static String UNINITIALIZED = "NOOB";
	public final static String INTEGER = "NUMBR";
	public final static String FLOAT = "NUMBAR";
	public final static String STRING = "YARN";
	public final static String BOOLEAN = "TROOF";
	
	private String symbol, value, dataType;
	
	public Symbol(String symbol, String value, String dataType) {
		this.symbol = symbol;
		this.value = value;
		this.dataType = dataType;
	}
		
	public String getSymbol() {
		return this.symbol;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public String getDataType() {
		return this.dataType;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
