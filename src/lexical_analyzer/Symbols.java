package lexical_analyzer;

public class Symbols {
	private String symbol, value;
	
	public Symbols(String symbol, String value) {
		this.symbol = symbol;
		this.value = value;
	}
	
	public String getSymbol() {
		return this.symbol;
	}
	
	public String getValue() {
		return this.value;
	}
}
