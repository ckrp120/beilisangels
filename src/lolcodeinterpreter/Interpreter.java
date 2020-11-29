package lolcodeinterpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;

import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
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
	//private File file = new File("testcases/switch.lol");
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
	private Image happyImg = new Image("imgs/laughing.gif", 150, 150, true,true);
	private Image neutralImg = new Image("imgs/neutral.PNG", 150, 150, true,true);
	private Image cryingImg = new Image("imgs/crying.gif", 150, 150, true,true);
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
    private String[] lines;
    private String currentLexeme,dialogText;
    private int wordCheck,lineCheck,status;
    private boolean validLexeme,validSyntax,validSemantics,readBack;
    private ArrayList<Token> tokens = new ArrayList<Token>();
    private ArrayList<Token> tokensPerLine = new ArrayList<Token>();
    private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
    
    //process queue for if/switch
    private Queue<ArrayList<Token>> pQueue = new LinkedList<>();
    private boolean checkingSwitchStatement = false;
    private boolean executingSwitchStatement = false;
	
	public Interpreter() {
		root = new Group();
		scene = new Scene(this.root,WINDOW_WIDTH,WINDOW_HEIGHT, Color.web("#315f72"));
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
		//this.scene.getStylesheets().add("lolcodeinterpreter.css");
		root.getStylesheets().add(getClass().getResource("lolcodeinterpreter.css").toString());
		this.stage = stage;
		this.stage.setTitle("LOLCode Interpreter");
		this.stage.setMinWidth(WINDOW_WIDTH);
		this.stage.setMinHeight(WINDOW_HEIGHT);
		this.stage.setScene(this.scene);
		this.stage.show();
	}
	
	
	//FUNCTION FOR ANALYZING LOLCODE FILE
	private void analyzeFile() {		
		//process every line
		while(lineCheck<lines.length) {
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
			if(status == 1) {
				validSyntax = false;
				validSemantics = false;
				break;
			}	
			if(!tokensPerLine.isEmpty()) {
				checkSyntaxAndSemantics();
		    	if(!validSyntax || !validSemantics) {
		    		if(!validSyntax) validSemantics = false;
		    		break;
		    	}		
			}

			tokensPerLine.clear();
		}
		
	}
	
	
	//FUNCTIONS FOR SYNTAX AND SEMANTIC ANALYSIS
	private void checkSyntaxAndSemantics() {
		if(tokensPerLine.size() > 1) {
			
			//IF WTF? was the previous operation, it must be followed by an OMG keyword
			if(checkingSwitchStatement && pQueue.size() == 1) {
				if(tokensPerLine.get(0).getClassification().equals(Token.OMG_CLASSIFIER) && tokensPerLine.size() == 2) {
					if(Token.LITERALS.contains(tokensPerLine.get(1).getClassification())) {
						storeTokensToQueue();
						validSyntax = true;
					}
					else validSyntax = false;
				}
				
				else validSyntax = false;
				
			}
			
			//OMG
			else if(tokensPerLine.get(0).getLexeme().equals(Token.OMG)) {
				
				//check if the line next to OMG is a literal
				if(Token.LITERALS.contains(tokensPerLine.get(1).getClassification()) && tokensPerLine.size() == 2) {
					storeTokensToQueue();
					validSyntax = true;
				}
				else validSyntax = false;
			}
			
			//PRINT = VISIBLE
			else if(tokensPerLine.get(0).getLexeme().equals(Token.VISIBLE)) {
				if(printSyntax()) {
					if(!checkingSwitchStatement) printExecute();
					else storeTokensToQueue();
					validSyntax = true;
				}
				else validSyntax = false;
			}
			
			//ACCEPT = GIMMEH
			else if(tokensPerLine.get(0).getLexeme().equals(Token.GIMMEH)) {
				if(acceptSyntax()) {
					
					if(!checkingSwitchStatement)acceptExecute();
					else storeTokensToQueue();
					validSyntax = true;
					
				}
				else validSyntax = false;
			}
			
			//VARIABLE DECLARATION = I HAS A
			else if(tokensPerLine.get(0).getLexeme().equals(Token.I_HAS_A)) {
				String literalClassification = varDeclarationSyntax();
				if(literalClassification != null) {
					if(!checkingSwitchStatement) varDeclarationExecute(literalClassification);
					else storeTokensToQueue();
					validSyntax = true;
					
				}
				else validSyntax = false;				
			}

			//ASSIGNMENT STATEMENT = R
			else if(tokensPerLine.get(1).getLexeme().equals(Token.R)) {
				String literalClassification = varAssignmentSyntax();
				if(literalClassification != null) {
					
					if(!checkingSwitchStatement) varAssignmentExecute(literalClassification);
					else storeTokensToQueue();
					validSyntax = true;
				}
				else validSyntax = false;
			}
			
			//ARITHMETIC OPERATIONS
			else if(Token.ARITHMETIC_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification())) {
				if(arithmeticSyntax(tokensPerLine)) {
					if(!checkingSwitchStatement) arithmeticExecute(Token.IT,tokensPerLine);
					else storeTokensToQueue();
					validSyntax = true;
				}
				else validSyntax = false;
			}	

			//BOOLEAN OPERATIONS
			else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification()) || 
					Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification())) {
				if(booleanSyntax(tokensPerLine)) {
					if(!checkingSwitchStatement) booleanExecute(Token.IT, tokensPerLine);
					else storeTokensToQueue();
					validSyntax = true;
				}
				else validSyntax = false;
			}
			
			//COMPARISON OPERATORS
			else if(Token.COMPARISON_OPERATORS.contains(tokensPerLine.get(0).getClassification()) ) {
				if(comparisonSyntax(tokensPerLine)) {
					if(!checkingSwitchStatement) comparisonExecute(Token.IT,tokensPerLine);
					else storeTokensToQueue();
					validSyntax = true;
				}
				else validSyntax = false;
			}
		} else {
			switch(tokensPerLine.get(0).getClassification()) {
				case Token.HAI_CLASSIFIER:
					validSyntax=true;
					break;
				case Token.KTHXBYE_CLASSIFIER:
					validSyntax=true;
					break;
				case Token.BTW_CLASSIFIER:
					validSyntax=true;
					break;
				case Token.OBTW_CLASSIFIER:
					validSyntax=true;
					break;	
				case Token.TLDR_CLASSIFIER:
					validSyntax=true;
					break;
				case Token.WTF_CLASSIFIER:
					validSyntax = true;
					checkingSwitchStatement = true;
					storeTokensToQueue();
					break;
				
				case Token.OIC_CLASSIFIER:
					//check if WTF and OMGs are already in the switch statement
					if((inProcessQueue(Token.WTF) && inProcessQueue(Token.OMG) && checkingSwitchStatement) || executingSwitchStatement) {
						validSyntax = true;
						storeTokensToQueue();
						executeSwitch();	
						break;
					}else {
						validSyntax = false;
						break;
					}
					
				case Token.GTFO_CLASSIFIER:
					storeTokensToQueue();
					break;
					
				case Token.OMGWTF_CLASSIFIER:
					storeTokensToQueue();
					
				case Token.O_RLY_CLASSIFIER:
					validSyntax=true;
					break;
				case Token.YA_RLY_CLASSIFIER:
					validSyntax=true;
					break;
				case Token.NO_WAI_CLASSIFIER:
					validSyntax=true;
					break;
				default:
					validSyntax=false;
					break;
			}
		}
	}	
	
	//SYNTAX FOR PRINT = VISIBLE
	private boolean printSyntax() {
		if(tokensPerLine.size() > 1) return true; 
		//return false if VISIBLE does not have anything to print
		return false;
	}
	
	//FUNCTION TO MAKE DEEP COPY OF TOKENS PER LINE
	private void storeTokensToQueue() {
		ArrayList<Token> lineTokens = new ArrayList<Token>();
		for(Token tkn: tokensPerLine) {
			lineTokens.add(new Token(tkn.getLexeme(), tkn.getClassification()));
		}
		
		pQueue.add(lineTokens);
	}
	
	//check if instruction exists in pQueue
	private boolean inProcessQueue(String lexeme) {
		for(ArrayList<Token> line: pQueue) {
			if(line.get(0).getLexeme().equals(lexeme)) return true;
		} return false;
	}
	
	private void executeSwitch() {
		
		executingSwitchStatement = true;
		//checks if it has entered case
		boolean enteredCase = false;
		
		//set checking switch statement to false so that it would execute instructions
		checkingSwitchStatement = false;
		
		//get current queue size to get length of loop
		int queueSize = pQueue.size();
		
		//execute instructions in pQueue
		for(int i = 0; i < queueSize; i++) {
			
			//dequeues the process queue
			tokensPerLine = pQueue.remove();
			
			//skip WTF
			if(i == 0) continue; 
			
			//detects OMG
			else if(tokensPerLine.get(0).getLexeme().equals(Token.OMG)) {
				
				//if has yet to enter a case, check condition
				if(!enteredCase) {
					
					/* compare IT and literal */
					
					//check if same datatype
					String classificationIT = isAValidLexeme(getIT().getValue());
					String classificationCase = isAValidLexeme(tokensPerLine.get(1).getLexeme());
					
					//if classification is the same, check if value is the same
					if(classificationIT.equals(classificationCase)) {
						
						//if same, activate flag
						if(getIT().getValue().equals(tokensPerLine.get(1).getLexeme())) enteredCase = true;
					}
				}else continue;
			
			//if GTFO, clear the process queue and exit switch statement
			}else if(tokensPerLine.get(0).getLexeme().equals(Token.GTFO) && enteredCase) {
				pQueue.clear();
				executingSwitchStatement = false;
				break;
				
			//if OIC, clear the process queue and exit the switch statement	
			}else if(tokensPerLine.get(0).getLexeme().equals(Token.OIC)){
				pQueue.clear();
				executingSwitchStatement = false;
				break;
			
			//default case
			}else if(tokensPerLine.get(0).getLexeme().equals(Token.OMGWTF)){
				enteredCase = true;
				
			//execute instruction
			}else {
				if(enteredCase) checkSyntaxAndSemantics();
			}
			
			
		}
	}
	
	//FUNCTION TO GET IT
	private Symbol getIT() {
		for(Symbol s: symbols) {
			if(s.getSymbol().equals(Token.IT)) return s;
		} return null;
	}
	
	
	//SEMANTICS FOR PRINT = VISIBLE
	private void printExecute() {
		Token tkn;
		int i=1;
		boolean appendNewLine=true;
		
		while(i<tokensPerLine.size()) {
			tkn = tokensPerLine.get(i);
			
			//case 1: varident/it
			if(isAVarident(tkn.getClassification())) {
				boolean validSymbol=false;
				for(Symbol s:symbols) {
					//get the value of the varident/it in the symbols
					if(s.getSymbol().equals(tkn.getLexeme())) {
						outputDisplayText += s.getValue();	
						validSymbol=true;
						break;
					}
				}
				
				if(!validSymbol) validSemantics = false;
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
				else validSyntax = false;
			}
			
			//case 3: literals
			else if(Token.LITERALS.contains(tkn.getClassification())) {
				outputDisplayText += tkn.getLexeme();
				dialogText = tkn.getLexeme();
			}
			//skip when the token is a string delimiter of a yarn literal
			else if(tkn.getLexeme().equals(Token.STRING_DELIMITER)) {
				i++;
				continue;
			}
			//case where the visible ends with an exclamation
			else if(tkn.getLexeme().equals(Token.EXCLAMATION_POINT)) {
				if(i+1 == tokensPerLine.size()) appendNewLine = false;
				else {
					validSemantics = false;
					break;
				}
			}
			else {
				validSemantics = false;
				break;
			}
			i++;
		}	
		
		if(appendNewLine) outputDisplayText += "\n";						
	}
	
	//SYNTAX FOR ACCEPT = GIMMEH
	private boolean acceptSyntax() {
		if(tokensPerLine.size() == 2) {
			if(tokensPerLine.get(1).getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER))
				return true; 
			//return false if not a varident
			return false;
		}
		//return false if GIMMEH contains anything more than the varident
		return false;
	}
	
	//SEMANTICS FOR ACCEPT = GIMMEH
	private void acceptExecute() {
		String tkn = tokensPerLine.get(1).getLexeme();
			
		boolean validSymbol=false;
		for(Symbol s:symbols) {
			//get the value of the varident in the symbols
			if(s.getSymbol().equals(tkn)) {
				outputDisplay.setText(outputDisplayText);
				validSymbol=true;
				//get user input
		        getInput(s,dialogText);
				break;
			}
		}
				
		if(!validSymbol) validSemantics = false;
	}
	
	private void getInput(Symbol s,String dialogText) {
        TextInputDialog inputDialog = new TextInputDialog("Enter input");

        //set the title,header text, and context text of input dialog
        inputDialog.setTitle("USER INPUT");
        inputDialog.setHeaderText(null);
        inputDialog.setContentText(dialogText);
        
        Optional<String> input = inputDialog.showAndWait();
               
        //if user entered an input, set the varident's value to the input
        if(input.isPresent()) {
            input.ifPresent(value -> {
            	s.setValue(value);
            });	
        //else, error
        } else {
        	validSemantics = false;
        }
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
			
			//case 2.1: arith expr
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
				else validSyntax = false;
			} 
			
			//case 2.2: comp expr
			else if(Token.COMPARISON_OPERATORS.contains(litClass)) {
				ArrayList<Token> compToken = new ArrayList<Token>();
				
				//copy the tokens starting from the comparison operation
				for(int i=3;i<tokensPerLine.size();i++)
					compToken.add(tokensPerLine.get(i));
				
				//check if the compop has a valid syntax
				if(comparisonSyntax(compToken)) {
					symbols.add(new Symbol(identifier,""));
					comparisonExecute(identifier,compToken);
				}
				else validSyntax = false;
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
				
				//case 2: arith expr
				if(Token.ARITHMETIC_EXPRESSIONS.contains(litClass)) {
					ArrayList<Token> arithToken = new ArrayList<Token>();
					
					//copy the tokens starting from the arithmetic operation
					for(int i=2;i<tokensPerLine.size();i++)
						arithToken.add(tokensPerLine.get(i));
					
					//check if the arithop has a valid syntax
					if(arithmeticSyntax(arithToken)) arithmeticExecute(tokensPerLine.get(0).getLexeme(),arithToken);
					else validSyntax = false;
				} 
				
				//case 2.1: comparison operators
				else if(Token.COMPARISON_OPERATORS.contains(litClass)) {
					ArrayList<Token> compToken = new ArrayList<Token>();
					
					//copy the tokens starting from the arithmetic operation
					for(int i=2;i<tokensPerLine.size();i++)
						compToken.add(tokensPerLine.get(i));
					
					//check if the comparison op has a valid syntax
					if(comparisonSyntax(compToken)) comparisonExecute(tokensPerLine.get(0).getLexeme(),compToken);
					else validSyntax = false;
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
			if(startingPopped) {
				if(i+1==arithToken.size() && arithToken.get(i).getLexeme().equals(Token.STRING_DELIMITER)) return true;
				else return false; 
			}
			
			//add keywords to stack
			if(Token.ARITHMETIC_EXPRESSIONS.contains(arithToken.get(i).getClassification())) {
				checker.add(arithToken.get(i));
				
				//if not starting arithmetic expression, increment exprCount (meaning it is a nested expression)
				if(i > 0) exprCount++;
			} else if(arithToken.get(i).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
			//if an is encountered, add to an count
				anCount++;
			
			//if a varident or literal is detected, add to an operand count
			} else if(isADigit(arithToken.get(i).getClassification()) || isAVarident(arithToken.get(i).getClassification()) ||
						Token.LITERALS.contains(arithToken.get(i).getClassification())) {
				opCount++;
			}
			
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
			
			//case 4: IT
			}else if(tkn.getLexeme().equals(Token.IT)) {
				Symbol it = getIT();
				String classification = isAValidLexeme(it.getValue());
				
				//IT is a numbar
				if(classification.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) operation.push(Float.parseFloat(it.getValue()));
				
				//IT is a numbr
				else if(classification.equals(Token.NUMBR_LITERAL_CLASSIFIER)) operation.push(Integer.parseInt(it.getValue()));
				
				//invalid data type
				else{
					validSemantics = false;
					return null;
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
			} else if(!tkn.getLexeme().equals(Token.AN_TYPE_LITERAL)){
				validSemantics = false;
				return null;
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
	
	
	private boolean booleanExecute(String dataHolder, ArrayList<Token> booleanTokens) {
		Stack<Boolean> operation = new Stack<Boolean>();
		
		for(Token tkn: booleanTokens) {
			
			//case 1: troof literal
			if(tkn.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER)){
				operation.push(convertTroofToBoolean(tkn.getLexeme()));
			//case 2: varident
			}else if(tkn.getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
				for(Symbol s:symbols) {
					if(s.getSymbol().equals(tkn.getLexeme())) {
						
						operation.push(convertTroofToBoolean(s.getValue())); 
						
						break;
						
					}
				}
			//pop two operands for binary boolean expressions
			}else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tkn.getClassification())) {
				boolean op1 = operation.pop();
				boolean op2 = operation.pop();
				
				//perform the operation then push to stack
				switch(tkn.getClassification()) {
					case Token.BOTH_OF_CLASSIFIER:
						operation.push((op1 && op2));
						break;
					
					case Token.EITHER_OF_CLASSIFIER:
						operation.push((op1 || op2));
						break;
					
					case Token.WON_OF_CLASSIFIER:
						operation.push((op1 ^ op2));
						break;
					
				}
				
			}else if(Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tkn.getClassification())) {
				boolean op1;
				
				//since NOT is an unary operation, pop only 1 operand
				if(tkn.getLexeme().equals(Token.NOT)) {
					op1 = operation.pop();
					operation.push(!op1);
				}else {
					boolean op2;
					
					//since ANY OF/ALL OF are infinite arity operations, pop all operands and perform the operation
					int currentStackSize = operation.size();
					
					
					if(tkn.getLexeme().equals(Token.ALL_OF)) {
						
						//perform AND operation one at a time
						for(int i = 0; i < currentStackSize-1; i++) {
							op1 = operation.pop();
							op2 = operation.pop();
							operation.push((op1 && op2));
						}
					}else {
						
						//perform OR operation one at a time
						for(int i = 0; i < currentStackSize-1; i++) {
							op1 = operation.pop();
							op2 = operation.pop();
							operation.push((op1 || op2));
						}
					}
					
					
				}
			}
			
			
			
		}
		
		//last item on the stack is the result
		boolean result = operation.pop();
		
		//set the value of the varident to the result
		for(Symbol s:symbols) {
			if(dataHolder.equals(s.getSymbol())) {					
				if(result == true) s.setValue(Token.WIN_TROOF_LITERAL);
				else s.setValue(Token.FAIL_TROOF_LITERAL);
				break;
			}
		}
		
		return result;
		
	}
	
	private boolean comparisonSyntax(ArrayList<Token> comparisonTokens) {
		Stack<Token> checker = new Stack<Token>();
		int exprCount = 0, opCount = 0, anCount = 0;
		boolean startingPopped = false;
		
		
		for(int i = 0; i < comparisonTokens.size(); i++) {
			
			//implies that another operation has started in the same line
			if(startingPopped) {
				return false; 
			}
			//add keywords to stack
			if(Token.COMPARISON_OPERATORS.contains(comparisonTokens.get(i).getClassification()) || Token.ARITHMETIC_EXPRESSIONS.contains(comparisonTokens.get(i).getClassification())) {
				checker.add(comparisonTokens.get(i));
				//if not starting comparison operator, inc exprCount (meaning it is a nested expression)
				if(i > 0) exprCount++;
			}else if(comparisonTokens.get(i).getLexeme().equals(Token.AN_TYPE_LITERAL)) {
				//if an is encountered, add to an count
				anCount++;
			}else if(comparisonTokens.get(i).getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER) || comparisonTokens.get(i).getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER) || comparisonTokens.get(i).getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
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
					
					if((opCount == 2 && anCount == 1)) opCount = 0;
					
					if(((exprCount >= 1 && opCount >= 1 && anCount == 1))) {
						opCount--;
						exprCount--;
					}
					
					anCount--;
				}else return false;
			}	
		}
		
		if(checker.isEmpty() && opCount == 0 && anCount == 0 && exprCount == 0) return true;
		else return false;
	}
	
	//SEMANTICS FOR ARITHMETIC OPERATIONS
	private String comparisonExecute(String dataHolder,ArrayList<Token> compToken) {
			Stack<Number> operation = new Stack<Number>();
				
			//since operations are in prefix, reverse the tokens 
			Collections.reverse(compToken);
			for(Token tkn: compToken) {
				//case 1: numbar - floating points
				if(tkn.getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER)) {
					operation.push(parseFloat(tkn));
				//case 2: numbr - integer
				}else if(tkn.getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER)) {
					operation.push(parseInt(tkn));
				//case 3: varident
				}else if(tkn.getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
					for(Symbol s:symbols) {
						if(s.getSymbol().equals(tkn.getLexeme())) {	
							//check its value's data type
							String classification = isAValidLexeme(symbols.get(symbols.indexOf(s)).getValue());
							//varident is a numbar
							if(classification.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) operation.push(parseFloat(symbols.indexOf(s)));
							
							//varident is a numbr
							else if(classification.equals(Token.NUMBR_LITERAL_CLASSIFIER)) operation.push(parseInt(symbols.indexOf(s)));
							
							//varident is a WIN (true)
							else if(s.getValue().equals(Token.WIN_TROOF_LITERAL)) operation.push(1); 
							
							//varident is a FAIL (false)
							else if(s.getValue().equals(Token.FAIL_TROOF_LITERAL)) operation.push(0); 
							
							//varident is a yarn, pero nababasa ni valid lexeme as variable ident?
							else if(classification.equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) operation.push(0);  
							break;
						}
					}
					
				//if operation is detected, pop 2 operands and perform the operation
				}else if(Token.COMPARISON_OPERATORS.contains(tkn.getClassification()) || Token.ARITHMETIC_EXPRESSIONS.contains(tkn.getClassification())){
					
					boolean resultIsNumbar = false;
					boolean resultIsNumbr = false;
					Number op1 = operation.pop();
					Number op2 = operation.pop();
					
					//check if one of the operands is numbar
					if(op1 instanceof Float && op2 instanceof Float) resultIsNumbar = true;
					if(op1 instanceof Integer && op2 instanceof Integer) resultIsNumbar = true;
					
					//if numbar, result must be float
					if(resultIsNumbar) {
						Float o1 = op1.floatValue();
						Float o2 = op2.floatValue();
						
						//perform the operation then push to stack
						switch(tkn.getClassification()) {
						case Token.BOTH_SAEM_CLASSIFIER: // o1 == o2
							if(o1.equals(o2)) operation.push(1); //WIN
							else operation.push(0); //FAIL
							break;
						case Token.DIFFRINT_CLASSIFIER: //o1 != o2
							if(!o1.equals(o2)) operation.push(1); //WIN
							else operation.push(0); //FAIL
							break;
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
					} else if(resultIsNumbr) {
						//since no numbar val is detected, operands are assumed to be both numbr
						int o1 = op1.intValue();
						int o2 = op2.intValue();
						
						//perform the operation then push to stack
						switch(tkn.getClassification()) {
						case Token.BOTH_SAEM_CLASSIFIER: // o1 == o2
							if(o1 == o2) operation.push(1); //WIN
							else operation.push(0); //FAIL
							break;
						case Token.DIFFRINT_CLASSIFIER: //o1 != o2
							if(o1 != o2) operation.push(1); //WIN
							else operation.push(0); //FAIL
							break;
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
						operation.push(0); //FAIL
					}
				}
			}

			//last item on the stack is the result
			Number num = operation.pop();
			String answer = "";
			if(num.equals(1)) answer = "WIN";
			else if(num.equals(0)) answer = "FAIL";
			
			//set the value of the varident to the result
			for(Symbol s:symbols) {
				if(dataHolder.equals(s.getSymbol())) {					
					s.setValue(answer);
					break;
				}
			}
			
			return answer;
			//return null;
		}
	
	//converts troof to its corresponding boolean equivalent
	private boolean convertTroofToBoolean(String literal) {
		if(literal.equals(Token.WIN_TROOF_LITERAL)) return true;
		else return false;
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
			Token.OTHER_BOOLEAN_EXPRESSIONS.contains(classification) ||
			Token.COMPARISON_OPERATORS.contains(classification)) 
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

				if(currPos < line.length()) {				
					while(isASpace(line.charAt(currPos))) currPos++;
	
					currChar = line.charAt(currPos);
					currPos++;
				}
			}

			//concatenate the current character to the current lexeme
			currentLexeme += currChar;
					
			
			//if the end of the line is reached or the next char is a space, check if the current lexeme is a token
			if(currPos==line.length() || isASpace(line.charAt(currPos)) || line.charAt(currPos-1) == '\"') {
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
		if(!currentLexeme.equals("") && isAVariable() && status!=2) {
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
			if(t.getKey().contains(s)) return true;
		}
		//if the current lexeme is not a substring of any keyword, return false
		return false;
	}
	
	//check if the lexeme is a possible keyword used as a variable identifier
	public boolean isAVariable() {
		String tkn;
		
		if(tokens.size()!=0) {
			for(int i=tokens.size()-1;i!=0;i--) {
				tkn = tokens.get(i).getLexeme();
				if(tkn.equals(Token.I_HAS_A) || tkn.equals(Token.VISIBLE)) return true;
				else if(!tkn.equals(Token.BTW) || !tkn.equals(Token.TLDR)) return false;
			}			
		}

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
		String fileWithLines = "";
		
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
			
			for(int i=0;i<lines.length;i++)
				fileWithLines += String.format("%2d", i+1) + " " + lines[i] + "\n";
				
			//add to text area the content of file read
			this.codeDisplay.setText(fileWithLines); 
			System.out.println(fileString);
		} catch(Exception e){
			outputDisplay.setText("[!] File not found ");
			System.out.println("File not found!");
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
        	
        	//select attribute to show in the column
        	lexemefirstDataColumn.setCellValueFactory(new PropertyValueFactory<>("lexeme"));
        	lexemesecondDataColumn.setCellValueFactory(new PropertyValueFactory<>("classification"));
        	
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
    		//column header naming
        	symbolfirstDataColumn = new TableColumn<>("Symbol"); 
        	symbolsecondDataColumn = new TableColumn<>("Value"); 
        	
        	//select attribute to show in the column
        	symbolfirstDataColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        	symbolsecondDataColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        	
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
    	//populate table
    	for(Token token: tokens) lexemeTableView.getItems().add(token);
    	for(Symbol symbol: symbols) symbolTableView.getItems().add(symbol);
    }
    
    private void showError() {  	
    	//update GUI to show fail
    	passIndicator.setImage(cryingImg);
		outputDisplay.setText("[!] Error detected in line " + lineCheck);
    	
    	if(!validLexeme) lexicalIndicator.setImage(lexicalFailImg);
    	else lexicalIndicator.setImage(lexicalPassImg);
    		
		if(!validSyntax) syntaxIndicator.setImage(syntaxFailImg);
    	else syntaxIndicator.setImage(syntaxPassImg);
    		
    	if(!validSemantics) semanticIndicator.setImage(semanticFailImg);
    	else semanticIndicator.setImage(semanticPassImg);

		//prompt error dialog
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText("[!] Errors found in your code.");
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
