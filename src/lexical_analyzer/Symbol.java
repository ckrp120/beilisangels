package lexical_analyzer;

public class Symbol {
	private String symbol, value;
	
	public Symbol(String symbol, String value) {
		this.symbol = symbol;
		this.value = value;
	}
	
	public String getSymbol() {
		return this.symbol;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
