package lexical_analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LexicalAnalyzer {
	private Stage stage;
	private Scene scene;
	private Group root;
	private Canvas canvas;
	public final static int WINDOW_WIDTH = 1500;
	public final static int WINDOW_HEIGHT = 950;
	
	//FOR FILE READING
	private FileChooser fileChooser = new FileChooser();
	private File file = new File("testcases/ops/arithop.lol");
	private String fileString="";
	private Scanner scanner;

	//FOR UI
	private Button fileButton = new Button("Select LOLCODE file");
	private Button executeButton = new Button("EXECUTE");
	private TextArea codeDisplay = new TextArea();
	private TextArea outputDisplay = new TextArea();
	private ImageView passIndicator = new ImageView(new Image("imgs/neutral.PNG", 150, 150, true,true));
	private ImageView lexicalIndicator = new ImageView();
	private ImageView syntaxIndicator = new ImageView();
	private ImageView semanticIndicator = new ImageView();
	private Image happyImg = new Image("imgs/laughing.png", 150, 150, true,true);
	private Image neutralImg = new Image("imgs/neutral.PNG", 150, 150, true,true);
	private Image cryingImg = new Image("imgs/crying.png", 150, 150, true,true);
	private Image lexicalPassImg = new Image("imgs/lexicalpassed.png", 150, 150, true,true);
	private Image syntaxPassImg = new Image("imgs/syntaxpassed.png", 150, 150, true,true);
	private Image semanticPassImg = new Image("imgs/semanticpassed.png", 150, 150, true,true);
	private Image lexicalFailImg = new Image("imgs/lexicalfailed.png", 150, 150, true,true);
	private Image syntaxFailImg = new Image("imgs/syntaxfailed.png", 150, 150, true,true);
	private Image semanticFailImg = new Image("imgs/semanticfailed.png", 150, 150, true,true);
    private TableColumn<Token, String> lexemefirstDataColumn, lexemesecondDataColumn;
    private TableColumn<Symbol, Symbol> symbolfirstDataColumn, symbolsecondDataColumn;
    private TableView<Token> lexemeTableView = new TableView<Token>();
    private TableView<Symbol> symbolTableView = new TableView<Symbol>(); 
	
    //FOR LEXICAL ANALYSIS
    String[] lines;
    String currentLexeme;
    private int wordCheck,lineCheck,status;
    private boolean invalidLexeme,possibleKeywordDetected,readBack;
	ArrayList<Token> tokens = new ArrayList<Token>();
	ArrayList<Token> tokensPerLine = new ArrayList<Token>();
	ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	
	private String outputDisplayText="";
	
	
	public LexicalAnalyzer() {
		root = new Group();
		scene = new Scene(this.root,WINDOW_WIDTH,WINDOW_HEIGHT, Color.BISQUE);
		canvas = new Canvas(WINDOW_HEIGHT,WINDOW_HEIGHT);
		canvas.getGraphicsContext2D();
		symbols.add(new Symbol(Token.IT,""));
	}
	
	public void setStage(Stage stage) {
		//set preferences for "select LOLCODE file" button
        this.fileButton.setLayoutX(0);
        this.fileButton.setLayoutY(50);
        this.fileButton.setMinWidth(500);
        
        //set preferences for "EXECUTE" button
        this.executeButton.setLayoutX(0);
        this.executeButton.setLayoutY(550);
        this.executeButton.setMinWidth(1500);
        
        //set preferences for displaying code
        this.codeDisplay.setLayoutX(0);
        this.codeDisplay.setLayoutY(80);
        this.codeDisplay.setPrefWidth(500);
        this.codeDisplay.setPrefHeight(470);
        this.codeDisplay.setEditable(false);
        
        //set preferences for displaying output
        this.outputDisplay.setLayoutX(10);
        this.outputDisplay.setLayoutY(600);
        this.outputDisplay.setPrefWidth(1200);
        this.outputDisplay.setPrefHeight(270);
        this.outputDisplay.setEditable(false);
        
        //set preferences for imageview of pass indicator
        this.passIndicator.setLayoutX(1270);
        this.passIndicator.setLayoutY(600);
        
        //set preferences for imageview of lexical analysis indicator
        this.lexicalIndicator.setLayoutX(1270);
        this.lexicalIndicator.setLayoutY(760);
        
        //set preferences for imageview of syntax analysis indicator
        this.syntaxIndicator.setLayoutX(1270);
        this.syntaxIndicator.setLayoutY(800);
        
        //set preferences for imageview of semantic analysis indicator
        this.semanticIndicator.setLayoutX(1270);
        this.semanticIndicator.setLayoutY(840);
        

        //call to functions
		openFile();	
		generateLexemes();
		createTable("lexemes");
		createTable("symbols");
		
		root.getChildren().addAll(canvas, codeDisplay, fileButton, executeButton, outputDisplay, passIndicator, lexicalIndicator, syntaxIndicator, semanticIndicator);
		this.stage = stage;
		this.stage.setTitle("LOLCode Interprete");
		this.stage.setMinWidth(WINDOW_WIDTH);
		this.stage.setMinHeight(WINDOW_HEIGHT);
		this.stage.setScene(this.scene);
		this.stage.show();
	}
	
	
	//FUNCTIONS FOR EXECUTING LEXICAL ANALYZER
	
	private void getLexemes() {		
		//process every line
		while(lineCheck<lines.length) {
			possibleKeywordDetected = false;
			readBack=false;
			wordCheck = 0;
			
			//check status of the current line
			//0 - valid lexeme
			//1 - invalid lexeme
			//2 - invalid lexeme, but process again because there's
			//	  a variable identifier detected as a possible keyword
			
			status = checkLexemes(lines[lineCheck]);
			
			//case 2
			if(status == 2) {
				lineCheck--;
				//process again starting from where an invalid lexeme is detected
				status = checkLexemes(currentLexeme);
			}  
			//case 1 or case 2 and there's still an invalid lexeme
			if(status == 1) break;
			
			checkSymbols();
			checkSyntax();
	    	executeTerminal();
			tokensPerLine.clear();
		}
	
		
//		System.out.println("\nLEXEMES");
//		for(int i=0;i<tokens.size();i++) {
//			System.out.println(i+1 + ". " + tokens.get(i).getLexeme()+ ":" + tokens.get(i).getClassification() + "\n");
//		}		
	}
	
	private int checkLexemes(String line) {		
		int currPos=0, commentDetected=0;
	    char currChar;
	    boolean acceptedLexeme=false;
		String classification;
		
		lineCheck++;
		
		//if the current line has no code, continue to the next line
		if(isEmpty(line)) return 0;
					
		currentLexeme = "";
		
		//ignore spaces/tabs at the beginning of the line
		while(isSpace(line.charAt(currPos))) currPos++;
				
		//start checking the lexemes 
		while(currPos < line.length()) {
			//get current character and increment position
			currChar = line.charAt(currPos);
			currPos++;

			//if the previous formed lexeme is accepted, ignore the next white space/s
			if(acceptedLexeme) {
				acceptedLexeme = false;

				while(isSpace(line.charAt(currPos))) currPos++;
				
				currChar = line.charAt(currPos);
				currPos++;
			}

			//concatenate the current character to the current lexeme
			currentLexeme += currChar;
			//System.out.println(currentLexeme);

			
			//if the end of the line is reached or the next char is a space, check if the current lexeme is a token
			if(currPos==line.length() || isSpace(line.charAt(currPos))) {
				classification = checkLexeme(currentLexeme);
				
				//if it is, then add it to the list of tokens
				if(classification != null) {
					acceptedLexeme = true;
					
					//if a string is detected, add the start quote, string literal, and end quote individually
					if(classification.equals(Token.YARN_LITERAL_CLASSIFIER)) {						
						//matcher to capture group
						Matcher m = Token.YARN_LITERAL.matcher(currentLexeme);
	
						if(m.find()) {
							tokens.add(new Token(m.group(1), Token.STRING_DELIMITER_CLASSIFIER));
							tokens.add(new Token(m.group(2), classification));
							tokens.add(new Token(m.group(3), Token.STRING_DELIMITER_CLASSIFIER));
							
							tokensPerLine.add(new Token(m.group(1), Token.STRING_DELIMITER_CLASSIFIER));
							tokensPerLine.add(new Token(m.group(2), classification));
							tokensPerLine.add(new Token(m.group(3), Token.STRING_DELIMITER_CLASSIFIER));
						}
					
					//if a comment is detected, ignore whatever comes after it
					//0 - not a comment
					//1 - one line comment (BTW)
					//2 - multiline comment (OBTW)
					} else if((commentDetected = isAComment(currentLexeme)) != 0) {
						//case 1: BTW (skip the current line)
						if(commentDetected == 1) {
							tokens.add(new Token(currentLexeme,classification));
							tokensPerLine.add(new Token(currentLexeme,classification));
							currentLexeme = "";
						//case 2: OBTW .. TLDR (must have their own lines)
						} else if(wordCheck == 0) {
							tokens.add(new Token(currentLexeme,classification));
							tokensPerLine.add(new Token(currentLexeme,classification));
							currentLexeme = "";
							String commentEnder;
							
							//ignore lines until a TLDR is detected
							do {
								commentEnder="";
								lineCheck++;
								line = lines[lineCheck];
								String[] lexemes = line.split(" ");
								
								
								for(int i=0;i<lexemes.length;i++) {
									if(!lexemes[i].equals("")) commentEnder+=lexemes[i];
								}	
							} while(!commentEnder.equals(Token.TLDR));		
						}
						break;
					
					//if not a string or a comment, add as is
					} else{
						tokens.add(new Token(currentLexeme,classification));
						tokensPerLine.add(new Token(currentLexeme,classification));
					}
						
					currentLexeme ="";
					wordCheck++;
				}
			}	
		}
		
		//ERROR DETECTION
		
		//there's an invalid lexeme, but process again because a variable identifier is detected as a possible keyword
		if(!currentLexeme.equals("") && possibleKeywordDetected && status!=2) {
			readBack=true;
			return 2;
		//there's an invalid lexeme, stop iteration for getting lexemes
		} else if(currentLexeme!="") {
			invalidLexeme = true;
			return 1;
		}		
		
		return 0;
	}
	
	private void checkSyntax() {
		if(tokensPerLine.size() > 0) {
			//check if starting token is an arithmetic expression
			if(Token.ARITHMETIC_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification())) {
				if(arithmeticSyntax()) {
					System.out.println("Line: "+lineCheck+" passed!");
					System.out.println("Answer: "+arithmeticExecute());
				}
			}
		}
	}
	
	private Number arithmeticExecute() {
		Stack<Number> operation = new Stack<Number>();
			
		//since operations are in prefix, reverse the tokens 
		Collections.reverse(tokensPerLine);
		
		for(Token tkn: tokensPerLine) {
			//parse to float if operand is numbar
			if(tkn.getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER)) {
				operation.push(parseFloat(tkn));
			//parse to int if operand is numbr
			}else if(tkn.getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER)) {
				operation.push(parseInt(tkn));
			//if varident is detected, check its value's data type
			}else if(tkn.getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
				for(Symbol s:symbols) {
					if(s.getSymbol().equals(tkn.getLexeme())) {						
						String classification = checkLexeme(symbols.get(symbols.indexOf(s)).getValue());
						
						//parse to float if varident is numbar
						if(classification.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) operation.push(parseFloat(symbols.indexOf(s)));
						
						//parse to int if varident is numbr
						else if(classification.equals(Token.NUMBR_LITERAL_CLASSIFIER)) operation.push(parseInt(symbols.indexOf(s)));
						break;
					}
				}
			//if operation is detected, pop 2 operands and perform the operation
			}else if(Token.ARITHMETIC_EXPRESSIONS.contains(tkn.getClassification())){
				boolean resultIsNumbar = false;
				Number op1 = operation.pop();
				Number op2 = operation.pop();
				
				//check if one of the operands is numbar
				if(op1 instanceof Float || op2 instanceof Float) resultIsNumbar = true;
				
				//if numbar, result must be float
				if(resultIsNumbar) {
					Float o1 = op1.floatValue();
					Float o2 = op2.floatValue();
					
					//perform the operation then push to stack
					switch(tkn.getClassification()) {
					case Token.SUM_OF_CLASSIFIER:
						operation.push(o1 + o2);
						break;
					case Token.DIFF_OF_CLASSIFIER:
						operation.push(o1 - o2);
						break;
						
					case Token.PRODUKT_OF_CLASSIFIER:
						operation.push(o1 * o2);
						break;
						
					case Token.QUOSHUNT_OF_CLASSIFIER:
						operation.push(o1 / o2);
						break;
						
					case Token.MOD_OF_CLASSIFIER:
						operation.push(o1 % o2);
						break;
					
					case Token.BIGGR_OF_CLASSIFIER:
						
						if(o1 > o2) operation.push(o1);
						else operation.push(o2);
						break;
					
					case Token.SMALLR_OF_CLASSIFIER:
						if(o1 < o2) operation.push(o1);
						else operation.push(o2);
						break;
					
					}
					
				}else {
					//since no numbar val is detected, operands are assumed to be both int
					int o1 = op1.intValue();
					int o2 = op2.intValue();
					
					//perform the operation then push to stack
					switch(tkn.getClassification()) {
					case Token.SUM_OF_CLASSIFIER:
						operation.push(o1 + o2);
						break;
					case Token.DIFF_OF_CLASSIFIER:
						operation.push(o1 - o2);
						break;
						
					case Token.PRODUKT_OF_CLASSIFIER:
						operation.push(o1 * o2);
						break;
						
					case Token.QUOSHUNT_OF_CLASSIFIER:
						operation.push(o1 / o2);
						break;
						
					case Token.MOD_OF_CLASSIFIER:
						operation.push(o1 % o2);
						break;
					
					case Token.BIGGR_OF_CLASSIFIER:
						
						if(o1 > o2) operation.push(o1);
						else operation.push(o2);
						break;
					
					case Token.SMALLR_OF_CLASSIFIER:
						if(o1 < o2) operation.push(o1);
						else operation.push(o2);
						break;
					
					}
					
				}
			}
		}
		
		Number num = operation.pop();
		symbols.get(0).setValue(num.toString()); 
		
		//last item on the stack is the result
		return num;
	}
	
	private float parseFloat(Token tkn) {
		return Float.parseFloat(tkn.getLexeme());
	}
	
	private int parseInt(Token tkn) {
		return Integer.parseInt(tkn.getLexeme());
	}
	
	private float parseFloat(int idx) {		
		return Float.parseFloat(symbols.get(idx).getValue());
	}
	
	private float parseInt(int idx) {		
		return Integer.parseInt(symbols.get(idx).getValue());
	}
	
	private boolean arithmeticSyntax() {
		Stack<Token> checker = new Stack<Token>();
		int exprCount = 0, opCount = 0, anCount = 0;
		boolean startingPopped = false;
		
		for(int i = 0; i < tokensPerLine.size(); i++) {
			//implies that another operation has started in the same line
			if(startingPopped) {
				return false; 
			}
			//add keywords to stack
			if(Token.ARITHMETIC_EXPRESSIONS.contains(tokensPerLine.get(i).getClassification())) {
				checker.add(tokensPerLine.get(i));
				
				//if not starting arithmetic expression, inc exprCount (meaning it is a nested expression)
				if(i > 0) exprCount++;
			}else if(tokensPerLine.get(i).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
				//if an is encountered, add to an count
				anCount++;
			}else if(tokensPerLine.get(i).getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER) | tokensPerLine.get(i).getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER) | tokensPerLine.get(i).getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
				//if num/var is encountered, add to an operand count
				opCount++;
			}else {
				//lexeme does not belong in this expression
				return false;
			}
			
			
			//pop stack after detecting two operands
			 
			if(anCount >= 2) return false;
			if((opCount == 2 && anCount == 1) || (exprCount >= 1 && opCount >= 1 && anCount == 1)) {
				if(!checker.isEmpty()) {
					
					if(checker.size() == 1) startingPopped = true;
					checker.pop();
					
					if((opCount == 2 && anCount == 1)) {
						opCount = 0;
					}
					if(((exprCount >= 1 && opCount >= 1 && anCount == 1))) {
						opCount--;
						exprCount--;
					}
					
					anCount--;
				}else {
					return false;
				}
			}
			
		}
		
		if(checker.isEmpty() && opCount == 0 && anCount == 0 && exprCount == 0) return true;
		else return false;
	}
	
	
	private void checkSymbols() {
		String identifier = "";
		String value = "";
		
		for(int i=1; i < tokensPerLine.size(); i++) {

			//IF VARIABLE IDENTIFIER
			if(tokensPerLine.get(i).getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)){
				//CHECK IF DECLARED
				if(tokensPerLine.get(i-1).getClassification().equals(Token.I_HAS_A_CLASSIFIER)) { //if I HAS A yung i-1
					identifier = tokensPerLine.get(i).getLexeme(); //place lexeme in identifier
					if(tokensPerLine.get(i+1).getClassification().equals(Token.ITZ_CLASSIFIER)){ //if i+1 == ITZ
						value = tokensPerLine.get(i+2).getLexeme(); //place lexeme in value
						symbols.add(new Symbol(identifier, value));
					} else {
						value = "NOOB"; //uninitialized var therefore value is NOOB
						symbols.add(new Symbol(identifier, value));					}
				}
			}
		} //end of for loop()
	}
	
	private void executeTerminal() { //MALI PA PU ITOO TESTING LANG
		for(int i=1; i < tokensPerLine.size(); i++) {
			if(tokensPerLine.get(i).getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER) || tokensPerLine.get(i).getClassification().equals(Token.IT_CLASSIFIER)){
				if(tokensPerLine.get(i-1).getClassification().equals(Token.VISIBLE_CLASSIFIER)) {
					for(int j=0; j < symbols.size(); j++) {
						System.out.println("=============terminal section=============");
						System.out.println(tokensPerLine.get(i).getLexeme() + " = " + symbols.get(j).getSymbol() + "?");
						if(tokensPerLine.get(i).getLexeme().equals(symbols.get(j).getSymbol())){
							System.out.println("true");
							outputDisplayText += symbols.get(j).getValue() + "\n";
						} else System.out.println("false");
						
					}
				}
			}
		}
		
	}
	
	//return classification if the current lexeme is a token
	public String checkLexeme(String currentLexeme) {
		if(Token.TOKEN_CLASSIFIER.containsKey(currentLexeme)) return Token.TOKEN_CLASSIFIER.get(currentLexeme);
		if(!possibleKeyword(currentLexeme)) {
			if(Token.VARIABLE_IDENTIFIER.matcher(currentLexeme).matches()) return Token.VARIABLE_IDENTIFIER_CLASSIFIER;
			if(Token.FUNCTION_LOOP_IDENTIFIER.matcher(currentLexeme).matches()) return Token.FUNCTION_LOOP_IDENTIFIER_CLASSIFIER;
		}
		if(Token.NUMBR_LITERAL.matcher(currentLexeme).matches()) return Token.NUMBR_LITERAL_CLASSIFIER;
		if(Token.NUMBAR_LITERAL.matcher(currentLexeme).matches()) return Token.NUMBAR_LITERAL_CLASSIFIER;
		if(Token.YARN_LITERAL.matcher(currentLexeme).matches()) return Token.YARN_LITERAL_CLASSIFIER;
		return null;
	} 

	//check if the current lexeme is a possible keyword
	public boolean possibleKeyword(String s) {
		if(readBack) return false;
		
		//iterate through keys in the hashmap of classifiers
		for(Entry<String, String> t: Token.TOKEN_CLASSIFIER.entrySet()) {
			//if the current lexeme is a substring of a keyword, return true
			if(t.getKey().contains(s)) {
				possibleKeywordDetected = true;
				return true;
			}
		}
		//if the current lexeme is not a substring of any keyword, return false
		return false;
	}
	
	//check if the character is a space
	public boolean isSpace(char c) {
		return c == ' ' || c == '\t';                                 
	}
	
	//check if the line has no code
	public boolean isEmpty(String s) {
		if(s.isEmpty()) return true;
		
		for(int i=0;i<s.length();i++) {
			if(!isSpace(s.charAt(i))) return false;
		}
		
		return true;                     
	}
	
	//check if the current lexeme is a comment
	public int isAComment(String s) {
		if(s.equals(Token.BTW)) return 1;
		if(s.equals(Token.OBTW)) return 2;
		return 0;                    
	}
	
	
	//FUNCTIONS FOR FILE READING

	private void openFile() {
		//action for "select LOLCODE file" button
        fileButton.setOnAction(e -> {

        	file = fileChooser.showOpenDialog(stage);
        	
        	//no file chosen
            if(file == null) {
            	System.out.println("[!] User cancelled input dialog");
            } else { //file chosen
            	//check if file extension ends with .lol
            	if(file.getAbsolutePath().matches(".*.lol$")) {
            		readFile();
            		resetAnalyzer();
            	}
            	else System.out.println("Invalid file!");
            }
        });
	}
	
	private void readFile() {
		resetAnalyzer();
		try {
			scanner = new Scanner(file);
			
			//save file to a string
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				fileString += line += '\n';
			} 
			
			//split file into lines
			lines = fileString.split("\n");
			
			//add to text area the content of file read
			this.codeDisplay.setText(fileString); 
			System.out.println(fileString);
		} catch(Exception a){
			System.out.println("file not found!");
		}
		
		//execute lexical analyzer after selecting lolcode file
		
	}
	
	private void resetAnalyzer() {
		//clear all values
		fileString = "";
		tokens.clear();
		lexemeTableView.getItems().clear();
		invalidLexeme = false;
		lineCheck = 0;
		passIndicator.setImage(neutralImg);
		lexicalIndicator.setImage(null);
		syntaxIndicator.setImage(null);
		semanticIndicator.setImage(null);
		outputDisplay.setText("");
	}
	
	
	//FUNCTIONS FOR UI
	
	//add this to remove warnings for table views
    @SuppressWarnings("unchecked")
	private void createTable(String type) {
    	if(type == "lexemes") {
    		//column header naming
        	lexemefirstDataColumn = new TableColumn<>("Lexeme");
        	lexemesecondDataColumn = new TableColumn<>("Classification"); 
        	        	
        	//set table view column width preference
        	lexemefirstDataColumn.setMinWidth(250);
        	lexemesecondDataColumn.setMinWidth(250);
        	
        	//set table view size preference
        	lexemeTableView.setLayoutX(500);
        	lexemeTableView.setLayoutY(50);
        	lexemeTableView.setPrefHeight(500);
        	
        	//not editable, output should be based on analyzer
        	lexemeTableView.setEditable(false);
        	lexemeTableView.getSelectionModel().setCellSelectionEnabled(true);
        	lexemeTableView.getColumns().addAll(lexemefirstDataColumn, lexemesecondDataColumn);
            root.getChildren().add(lexemeTableView);
    	} else if(type == "symbols"){
        	symbolfirstDataColumn = new TableColumn<>("Identifier"); 
        	symbolsecondDataColumn = new TableColumn<>("Value"); 
        	
        	//set table view column width preference
        	symbolfirstDataColumn.setMinWidth(250);
        	symbolsecondDataColumn.setMinWidth(250);
        	
        	//set table view size preference
        	symbolTableView.setLayoutX(1000);
        	symbolTableView.setLayoutY(50);
        	symbolTableView.setPrefHeight(500);

        	//not editable, output should be based on analyzer
        	symbolTableView.setEditable(false);
        	symbolTableView.getSelectionModel().setCellSelectionEnabled(true);
        	symbolTableView.getColumns().addAll(symbolfirstDataColumn, symbolsecondDataColumn);
            root.getChildren().add(symbolTableView);
    	} 
    }
    
    private void populateTable() {
    	//select attribute to show in the column
    	lexemefirstDataColumn.setCellValueFactory(new PropertyValueFactory<>("lexeme"));
    	lexemesecondDataColumn.setCellValueFactory(new PropertyValueFactory<>("classification"));
    	
    	symbolfirstDataColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
    	symbolsecondDataColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    	
    	//populate table
    	for(Token token: tokens) lexemeTableView.getItems().add(token);
    	for(Symbol symbol: symbols) symbolTableView.getItems().add(symbol);
    }
    
    private void showError() {  	
    	//update GUI to show fail
    	passIndicator.setImage(cryingImg);
		lexicalIndicator.setImage(lexicalFailImg);
		outputDisplay.setText("[!] Error detected in line " + lineCheck);
		
		//prompt error dialog
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText("[!] Errors were found in your code.");
		alert.setTitle("Error Dialog");
		alert.setHeaderText(null);
		alert.show();
    }
    
    private void showPass() {
    	populateTable();
		outputDisplay.setText(outputDisplayText);
		passIndicator.setImage(happyImg);
		lexicalIndicator.setImage(lexicalPassImg);
    }
    
	private void generateLexemes() {
		executeButton.setOnAction(e -> {
			readFile();
			getLexemes();
			if(!invalidLexeme) showPass();
			else showError();
        });
	}
}
