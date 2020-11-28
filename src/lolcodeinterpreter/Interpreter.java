package lolcodeinterpreter;

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

public class Interpreter {
	private Stage stage;
	private Scene scene;
	private Group root;
	private Canvas canvas;
	public final static int WINDOW_WIDTH = 1500;
	public final static int WINDOW_HEIGHT = 950;
	
	//FOR FILE READING
	private FileChooser fileChooser = new FileChooser();
	private File file = new File("testcases/ops/arithop.lol");
//	private File file = new File("testcases/vardecinit.lol");
	private String fileString="";
	private Scanner scanner;

	//FOR UI
	private Button fileButton = new Button("Select LOLCODE file");
	private Button executeButton = new Button("EXECUTE");
	private TextArea codeDisplay = new TextArea();
	private TextArea outputDisplay = new TextArea();
	private String outputDisplayText="";
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
	
    //FOR LEXICAL/SYNTAX/SEMANTIC ANALYSES
    String[] lines;
    String currentLexeme,literalClassification;
    private int wordCheck,lineCheck,status;
    private boolean validLexeme,validSyntax,validSemantics,possibleKeywordDetected,readBack;
	ArrayList<Token> tokens = new ArrayList<Token>();
	ArrayList<Token> tokensPerLine = new ArrayList<Token>();
	ArrayList<Symbol> symbols = new ArrayList<Symbol>();
	
	
	public Interpreter() {
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
	
	
	//FUNCTION FOR ANALYZING LOLCODE FILE
	private void analyzeFile() {		
		//process every line
		while(lineCheck<lines.length) {
			possibleKeywordDetected = false;
			readBack=false;
			wordCheck = 0;
			
			//check status of the current line
			//0 - valid lexeme; 1 - invalid lexeme; 2 - invalid lexeme, but process again bc a varident is detected as a possible keyword
			
			status = checkLexeme(lines[lineCheck]);
			
			//case 2
			if(status == 2) {
				lineCheck--;
				//process again starting from where an invalid lexeme is detected
				status = checkLexeme(currentLexeme);
			}  
			//case 1 or case 2 and there's still an invalid lexeme
			if(status == 1) break;
					
			checkSyntaxAndSemantics();
	    	if(!validSyntax || !validSemantics) break;
	    	
			tokensPerLine.clear();
		}
		
//		System.out.println("\nLEXEMES");
//		for(int i=0;i<tokens.size();i++) {
//			System.out.println(i+1 + ". " + tokens.get(i).getLexeme()+ ":" + tokens.get(i).getClassification() + "\n");
//		}		
	}
	
	
	//FUNCTIONS FOR SYNTAX AND SEMANTIC ANALYSES
		
	private void checkSyntaxAndSemantics() {
		if(tokensPerLine.size() > 1) {
			//PRINT = VISIBLE
			if(tokensPerLine.get(0).getLexeme().equals(Token.VISIBLE)) {
				if(printSyntax()) printExecute();
				else validSyntax = true;
			}
			
			//VARIABLE DECLARATION = I HAS A
			else if(tokensPerLine.get(0).getLexeme().equals(Token.I_HAS_A)) {
				String literalClassification = varDeclarationSyntax();
				if(literalClassification != null) varDeclarationExecute(literalClassification);
				else validSyntax = true;				
			}

			//ASSIGNMENT STATEMENT = R
			else if(tokensPerLine.get(1).getLexeme().equals(Token.R)) {
				String literalClassification = varAssignmentSyntax();
				if(literalClassification != null) varAssignmentExecute(literalClassification);
				else validSyntax = true;
			}
			
			//ARITHMETIC OPERATIONS
			else if(Token.ARITHMETIC_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification())) {
				if(arithmeticSyntax(tokensPerLine)) arithmeticExecute(Token.IT,tokensPerLine);
				else validSyntax = true;
			}	

			//BOOLEAN OPERATIONS
			else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification()) || 
					Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification())) {
				if(booleanSyntax(tokensPerLine)) System.out.println("Line: "+lineCheck+" passed!");
				else {
					System.out.println("Line: "+lineCheck+" failed :(");
					validSyntax = true;
				}
			}
		} else validSyntax = false;
	}	
	
	//SYNTAX FOR PRINT = VISIBLE
	private boolean printSyntax() {
		String c;
		
		//return false if not a varident/it, literal, or expr
		if(tokensPerLine.size() > 1) {
			for(int i=1;i<tokensPerLine.size();i++) {
				c = tokensPerLine.get(i).getClassification();
				if(!(isAVarident(c) || isALitOrExpr(c)))
					return false; 
			}
		} else return false;
		
		return true;
	}
	
	//SEMANTICS FOR PRINT = VISIBLE
	private void printExecute() {
		Token tkn;
		int i=1;
		while(i<tokensPerLine.size()) {
			tkn = tokensPerLine.get(i);
			
			//case 1: varident/it
			if(isAVarident(tkn.getClassification())) {
				for(Symbol s:symbols) {
					//get the value of the varident/it in the symbols
					if(s.getSymbol().equals(tkn.getLexeme())) {
						outputDisplayText += s.getValue();													
					}
				}
			} 
			//case 2: expr
			else if(Token.ARITHMETIC_EXPRESSIONS.contains(tkn.getClassification())) {
				ArrayList<Token> arithToken = new ArrayList<Token>();
				
				//copy the tokens starting from the arithmetic operation
				while(i<tokensPerLine.size()) {
					arithToken.add(tokensPerLine.get(i));
					i++;
				}
				
				//check if the arithop has a valid syntax
				if(arithmeticSyntax(arithToken)) {
					arithmeticExecute(Token.IT,arithToken);
					outputDisplayText += symbols.get(0).getValue();													
				}
				else validSyntax = true;
			}
			
			//case 3: literals
			else if(Token.LITERALS.contains(tkn.getClassification())) {
				outputDisplayText += tkn.getLexeme();													
			}
			//skip when the token is a string delimiter of a yarn literal
			else if(tkn.getLexeme().equals(Token.STRING_DELIMITER))
				continue;
			else {
				validSemantics = true;
				break;
			}
			i++;
		}
		
		outputDisplayText += "\n";						
	}
	
	//SYNTAX FOR VARIABLE DECLARATION = I HAS A
	private String varDeclarationSyntax() {		
		if(tokensPerLine.size() > 1) {
			if(isAVarident(tokensPerLine.get(1).getClassification())) {	
				//case 1: I HAS A var
				if(tokensPerLine.size() == 2) return "";
				//case 2: I HAS A var ITZ var/lit/expr
				else if(tokensPerLine.get(2).getClassification().equals(Token.ITZ_CLASSIFIER)) {
					if(isAVarident(tokensPerLine.get(3).getClassification()) ||
						isALitOrExpr(tokensPerLine.get(3).getClassification()))
						return tokensPerLine.get(3).getClassification();	
					if(Token.YARN_LITERAL_CLASSIFIER.equals(tokensPerLine.get(4).getClassification())) 
						return tokensPerLine.get(4).getClassification();	
				}
			}
		}
		//return null if what's declared is not a varident/it
		//or the value given is not a varident/it, literal, or expr
		return null;
	}
	
	//SEMANTICS FOR VARIABLE DECLARATION = I HAS A
	public void varDeclarationExecute(String litClass) {
		String identifier = tokensPerLine.get(1).getLexeme();
		
		//case 1: I HAS A var
		if(tokensPerLine.size() == 2) {
			symbols.add(new Symbol(identifier,Token.NOOB_TYPE_LITERAL));	
		//case 2: I HAS A var ITZ var/lit/expr
		} else if(tokensPerLine.get(2).getClassification().equals(Token.ITZ_CLASSIFIER)) {
			//case 2.1: varident
			if(isAVarident(litClass)) {							
				for(Symbol s:symbols) {
					if(s.getSymbol().equals(tokensPerLine.get(3).getLexeme())) {	
						symbols.add(new Symbol(identifier,s.getValue()));
						break;
					}
				}							
			}
			
			//case 2.1: expr
			else if(Token.ARITHMETIC_EXPRESSIONS.contains(litClass)) {
				ArrayList<Token> arithToken = new ArrayList<Token>();
				
				//copy the tokens starting from the arithmetic operation
				for(int i=3;i<tokensPerLine.size();i++)
					arithToken.add(tokensPerLine.get(i));
				
				//check if the arithop has a valid syntax
				if(arithmeticSyntax(arithToken)) {
					symbols.add(new Symbol(identifier,""));
					arithmeticExecute(identifier,arithToken);
				}
				else validSyntax = true;
			}

			//case 2.3: literal
			//a yarn literal
			else if(litClass.equals(Token.YARN_LITERAL_CLASSIFIER))
				symbols.add(new Symbol(identifier, tokensPerLine.get(4).getLexeme()));
			//or other type literals
			else symbols.add(new Symbol(identifier, tokensPerLine.get(3).getLexeme()));
		}
	}
		
	//SYNTAX FOR ASSIGNMENT STATEMENT = R
	private String varAssignmentSyntax() {
		//check if it is a valid varident
		if(isAVarident(tokensPerLine.get(0).getClassification())) {
			//return value if it is a varident/it, literal, or expr
			if(isAVarident(tokensPerLine.get(2).getClassification()) ||
				isALitOrExpr(tokensPerLine.get(2).getClassification()))
				return tokensPerLine.get(2).getClassification();	
			if(Token.YARN_LITERAL_CLASSIFIER.equals(tokensPerLine.get(3).getClassification())) 
				return tokensPerLine.get(3).getClassification();			
		}
		
		return null;
	}
	
	//SEMANTICS FOR ASSIGNMENT STATEMENT = R
	private void varAssignmentExecute(String litClass) {
		for(Symbol s:symbols) {
			//get the symbol, then set the value
			if(s.getSymbol().equals(tokensPerLine.get(0).getLexeme())) {				
				//case 1: varident
				
				//case 2: expr
				if(Token.ARITHMETIC_EXPRESSIONS.contains(litClass)) {
					ArrayList<Token> arithToken = new ArrayList<Token>();
					
					//copy the tokens starting from the arithmetic operation
					for(int i=2;i<tokensPerLine.size();i++)
						arithToken.add(tokensPerLine.get(i));
					
					//check if the arithop has a valid syntax
					if(arithmeticSyntax(arithToken)) arithmeticExecute(tokensPerLine.get(0).getLexeme(),arithToken);
					else validSyntax = true;
				}
				
				//case 3: literals
				//a yarn literal
				else if(litClass.equals(Token.YARN_LITERAL_CLASSIFIER)) s.setValue(tokensPerLine.get(3).getLexeme());
				//or other type literals
				else s.setValue(tokensPerLine.get(2).getLexeme());
				break;
			}
		}
	}
	
	//SYNTAX FOR ARITHMETIC OPERATIONS
	private boolean arithmeticSyntax(ArrayList<Token> arithToken) {
		Stack<Token> checker = new Stack<Token>();
		int exprCount = 0, opCount = 0, anCount = 0;
		boolean startingPopped = false;
		
		for(int i=0; i<arithToken.size(); i++) {
			//implies that another operation has started in the same line
			if(startingPopped) return false; 
			
			//add keywords to stack
			if(Token.ARITHMETIC_EXPRESSIONS.contains(arithToken.get(i).getClassification())) {
				checker.add(arithToken.get(i));
				
				//if not starting arithmetic expression, increment exprCount (meaning it is a nested expression)
				if(i > 0) exprCount++;
			} else if(arithToken.get(i).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
				//if an is encountered, add to an count
				anCount++;
			} else //else, add to an operand count
				if(isADigit(arithToken.get(i).getClassification()) || isAVarident(arithToken.get(i).getClassification())) {
				opCount++;
			} else //lexeme does not belong in this expression
				return false;
			
			
			//return false after detecting more than two operands
			if(anCount >= 2) return false;
			
			//if operands are varident/literal or operands have atleast one expr 
			if((opCount == 2 && anCount == 1) || (exprCount >= 1 && opCount >= 1 && anCount == 1)) {
				if(!checker.isEmpty()) {
					
					if(checker.size() == 1) startingPopped = true;
					
					checker.pop();
					
					if(opCount == 2 && anCount == 1) opCount = 0;
				
					if(exprCount >= 1 && opCount >= 1 && anCount == 1) {
						opCount--;
						exprCount--;
					}
					
					anCount--;
				}
				else return false;
			}
		}
		
		if(checker.isEmpty() && opCount == 0 && anCount == 0 && exprCount == 0) return true;
		else return false;
	}
	
	//SEMANTICS FOR ARITHMETIC OPERATIONS
	private Number arithmeticExecute(String dataHolder,ArrayList<Token> arithToken) {
		Stack<Number> operation = new Stack<Number>();
			
		//since operations are in prefix, reverse the tokens 
		Collections.reverse(arithToken);
		
		for(Token tkn: arithToken) {
			//case 1: numbar
			if(tkn.getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER)) {
				operation.push(parseFloat(tkn));
			//case 2: numbr
			} else if(tkn.getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER)) {
				operation.push(parseInt(tkn));
			//case 3: varident
			} else if(tkn.getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
				for(Symbol s:symbols) {
					if(s.getSymbol().equals(tkn.getLexeme())) {						
						//check its value's data type
						String classification = isAValidLexeme(symbols.get(symbols.indexOf(s)).getValue());
						
						//varident is a numbar
						if(classification.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) operation.push(parseFloat(symbols.indexOf(s)));
						
						//varident is a numbr
						else if(classification.equals(Token.NUMBR_LITERAL_CLASSIFIER)) operation.push(parseInt(symbols.indexOf(s)));
						break;
					}
				}
			//if operation is detected, pop 2 operands and perform the operation
			} else if(Token.ARITHMETIC_EXPRESSIONS.contains(tkn.getClassification())){
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
				} else {
					//since no numbar val is detected, operands are assumed to be both numbr
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

		//last item on the stack is the result
		Number num = operation.pop();
		
		//set the value of the varident to the result
		for(Symbol s:symbols) {
			if(dataHolder.equals(s.getSymbol())) {					
				s.setValue(num.toString());
				break;
			}
		}
		
		return num;
	}
	

	//SYNTAX FOR BOOLEAN OPERATIONS
	private boolean booleanSyntax(ArrayList<Token> booleanTokens) {
		Stack<Token> checker = new Stack<Token>();
		Token currentToken;
		int anCount = 0, popCount = 0;
		
		//since prefix, read the line in reverse
		Collections.reverse(booleanTokens);
		
		for(int i = 0; i < booleanTokens.size(); i++) {
			currentToken = booleanTokens.get(i);
			
			//if AN is detected, it must not be the last or starting token, and must not be followed by an AN
			if(currentToken.getLexeme().equals(Token.AN_TYPE_LITERAL)) {
				
				//AN is starting/last token
				if(i == 0 || i == (booleanTokens.size()-1)) {
					System.out.println("AN should not be last token");
					return false;
				}
				
				//followed by AN
				else if(booleanTokens.get(i-1).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
					System.out.println("unexpected next token: AN");
					return false;
				}
				
				else anCount++;
			}else if(currentToken.getLexeme().equals(Token.NOT)) {
							
				//NOT is last token
				if(i == 0) {
					System.out.println("operand exprected");
					return false;
				}
				
				//followed by AN
				else if(booleanTokens.get(i-1).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
					System.out.println("unexpected token: AN");
					return false;
				}
				
				else continue;
			}else if(currentToken.getLexeme().equals(Token.ALL_OF) || currentToken.getLexeme().equals(Token.ANY_OF)) {
				
				//if it starts with ANY OF/ALL OF then num of stack is ignored since these are infinite arity operations
				if(i == booleanTokens.size()-1 && !booleanTokens.get(i-1).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
					System.out.println("i: "+i);
					System.out.println(booleanTokens.get(i-1).getLexeme());
					return true; 
				}
				
				//operation cannot be nested
				else {
					System.out.println("ANY OF/ALL OF cannot be nested");
					return false;
				}
			
			}else if(currentToken.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER) | isAVarident(currentToken.getClassification())) {
				//if last token, it must be preceeded with an AN or NOT
				if(i == 0) {
					
					
					if(!(booleanTokens.get(i+1).getLexeme().equals(Token.AN_TYPE_LITERAL) || booleanTokens.get(i+1).getLexeme().equals(Token.NOT))) {
						System.out.println("last token: terminal not preceeded with AN/NOT");
						return false;
					}
				}else {
					//if not last token, it must be followed with an AN
					
					if(!booleanTokens.get(i-1).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
						System.out.println("terminal not followed by AN");
						return false;
					}
				}
				
				//push to stack
				checker.push(currentToken);
			}else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(currentToken.getClassification())) {
				//make sure it is not followed by an 'AN'
				if(booleanTokens.get(i-1).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
					System.out.println("followed by an AN");
					return false;
				}
				
				//make sure it is not the last token
				if(i == 0) {
					System.out.println("unexpected end of operation: binary");
					return false;
				}
				
				//pop one operand
				if(checker.size() > 1) {
					checker.pop();
					popCount++;
				}
				
				//insufficient amount of operands
				else{
					System.out.println("insufficient amount of operands");
					return false;
				}
			}else {
				System.out.println("UNMATCHED LEXEME: "+currentToken.getLexeme());
				//lexeme does not belong in the expression
				return false;
			}
			
			
		}
		
		//there should only be 1 operand left and the number of ANs must match the number of operands
		if((checker.size() == 1) && (anCount == popCount)) return true;
		else{
			
			System.out.println("Checker size: "+checker.size()+"\n Contents: ");
			for(Token tkn: checker)
				System.out.println(tkn.getLexeme());
			System.out.println("anCount = "+anCount+" popCount: "+popCount);
			return false;
		}
	}
	
	//check if the classification of a token is a literal or an expression
	private boolean isALitOrExpr(String classification) {
		if(Token.LITERALS.contains(classification) || 
			isAnExpr(classification)) return true;
		return false;
	}	
	
	private boolean isAnExpr(String classification) {
		if(Token.ARITHMETIC_EXPRESSIONS.contains(classification) || 
			Token.BINARY_BOOLEAN_EXPRESSIONS.contains(classification) ||
			Token.OTHER_BOOLEAN_EXPRESSIONS.contains(classification)) 
			return true;
		return false;
	}	
	
	//check if the classification of a token is a varident
	private boolean isAVarident(String classification) {
		if(classification.equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER) || 
			classification.equals(Token.IT_CLASSIFIER)) return true;
		return false;
	}
	
	//check if the classification of a token is a numbr or numbar
	private boolean isADigit(String classification) {
		if(classification.equals(Token.NUMBAR_LITERAL_CLASSIFIER) || 
			classification.equals(Token.NUMBR_LITERAL_CLASSIFIER)) return true;
		return false;
	}
	
	//parses a string and returns a float
	private float parseFloat(Token tkn) {
		return Float.parseFloat(tkn.getLexeme());
	}

	private float parseFloat(int idx) {		
		return Float.parseFloat(symbols.get(idx).getValue());
	}
	
	//parses a string and returns an integer	
	private int parseInt(Token tkn) {
		return Integer.parseInt(tkn.getLexeme());
	}
		
	private int parseInt(int idx) {		
		return Integer.parseInt(symbols.get(idx).getValue());
	}
	
	
	//FUNCTIONS FOR THE LEXICAL ANALYSIS
	
	private int checkLexeme(String line) {		
		int currPos=0, commentDetected=0;
	    char currChar;
	    boolean acceptedLexeme=false;
		String classification;
		
		lineCheck++;
		
		//if the current line has no code, continue to the next line
		if(isEmpty(line)) return 0;
					
		currentLexeme = "";
		
		//ignore spaces/tabs at the beginning of the line
		while(isASpace(line.charAt(currPos))) currPos++;
				
		//start forming and checking the lexemes 
		while(currPos < line.length()) {
			//get current character and increment position
			currChar = line.charAt(currPos);
			currPos++;

			//if the previous formed lexeme is accepted, ignore the next white space/s
			if(acceptedLexeme) {
				acceptedLexeme = false;

				while(isASpace(line.charAt(currPos))) currPos++;
				
				currChar = line.charAt(currPos);
				currPos++;
			}

			//concatenate the current character to the current lexeme
			currentLexeme += currChar;
			
			//System.out.println(currentLexeme);
			
			//if the end of the line is reached or the next char is a space, check if the current lexeme is a token
			if(currPos==line.length() || isASpace(line.charAt(currPos))) {
				classification = isAValidLexeme(currentLexeme);
				
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
					//0 - not a comment; 1 - one line comment (BTW); 2 - multiline comment (OBTW)
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
			validLexeme = false;
			return 1;
		}		
		
		return 0;
	}
	
	//return classification if the current lexeme is a token
	public String isAValidLexeme(String currentLexeme) {
		if(Token.TOKEN_CLASSIFIER.containsKey(currentLexeme)) return Token.TOKEN_CLASSIFIER.get(currentLexeme);
		if(!isAPossibleKeyword(currentLexeme)) {
			if(Token.VARIABLE_IDENTIFIER.matcher(currentLexeme).matches()) return Token.VARIABLE_IDENTIFIER_CLASSIFIER;
			if(Token.FUNCTION_LOOP_IDENTIFIER.matcher(currentLexeme).matches()) return Token.FUNCTION_LOOP_IDENTIFIER_CLASSIFIER;
		}
		if(Token.NUMBR_LITERAL.matcher(currentLexeme).matches()) return Token.NUMBR_LITERAL_CLASSIFIER;
		if(Token.NUMBAR_LITERAL.matcher(currentLexeme).matches()) return Token.NUMBAR_LITERAL_CLASSIFIER;
		if(Token.YARN_LITERAL.matcher(currentLexeme).matches()) return Token.YARN_LITERAL_CLASSIFIER;
		return null;
	} 
	
	//check if the current lexeme is a possible keyword
	public boolean isAPossibleKeyword(String s) {
		//if the line is to be processed again because a variable identifier is detected as a possible keyword
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
	public boolean isASpace(char c) {
		return c == ' ' || c == '\t';                                 
	}
	
	//check if the line is empty (has no code)
	public boolean isEmpty(String s) {
		if(s.isEmpty()) return true;
		
		for(int i=0;i<s.length();i++) {
			if(!isASpace(s.charAt(i))) return false;
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
	}
	
	private void resetAnalyzer() {
		//clear all values
		fileString = "";
		lineCheck = 0;		
		validLexeme = true;
		validSyntax = true;
		validSemantics = true;
		tokens.clear();
		lexemeTableView.getItems().clear();
		symbolTableView.getItems().clear();
		outputDisplay.setText("");
		passIndicator.setImage(neutralImg);
		lexicalIndicator.setImage(null);
		syntaxIndicator.setImage(null);
		semanticIndicator.setImage(null);
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
		outputDisplay.setText("[!] Error detected in line " + lineCheck);
    	
    	if(!validLexeme) lexicalIndicator.setImage(lexicalFailImg);
    	else {
    		lexicalIndicator.setImage(lexicalPassImg);
    		
    		if(!validSyntax) syntaxIndicator.setImage(syntaxFailImg);
        	else {
        		syntaxIndicator.setImage(syntaxPassImg);
        		
            	if(!validSemantics) semanticIndicator.setImage(semanticFailImg);
            	else semanticIndicator.setImage(semanticPassImg);
        	}
    	}

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
		syntaxIndicator.setImage(syntaxPassImg);
		semanticIndicator.setImage(semanticPassImg);
    }
    
	private void generateLexemes() {
		executeButton.setOnAction(e -> {
			readFile();
			analyzeFile();
			if(validLexeme && validSyntax && validSemantics) showPass();
			else showError();
        });
	}
}
