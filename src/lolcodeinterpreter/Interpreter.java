package lolcodeinterpreter;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
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
	private File file;
	private String currentPath = Paths.get("testcases").toAbsolutePath().normalize().toString();
	private String fileString="";
	private Scanner scanner;

	//FOR UI
	private Button fileButton = new Button("Select LOLCODE file");
	private Button executeButton = new Button("EXECUTE");
	private TextArea codeDisplay = new TextArea();
	private TextArea outputDisplay = new TextArea();
	private String outputDisplayText="";
	private Image titleImg = new Image("imgs/title.png", 1000, 90, true,true);
	private ImageView titleImage = new ImageView(titleImg);
	private Image happyImg = new Image("imgs/laughing.gif", 150, 150, true,true);
	private Image neutralImg = new Image("imgs/neutral.gif", 150, 150, true,true);
	private Image cryingImg = new Image("imgs/crying.gif", 150, 150, true,true);
	private ImageView passIndicator = new ImageView(neutralImg);
	private TableColumn<Token, String> lexemefirstDataColumn, lexemesecondDataColumn;
    private TableColumn<Symbol, Symbol> symbolfirstDataColumn, symbolsecondDataColumn;
    private TableView<Token> lexemeTableView = new TableView<Token>();
    private TableView<Symbol> symbolTableView = new TableView<Symbol>(); 
	
    //FOR LEXICAL/SYNTAX/SEMANTIC ANALYSIS
    private String[] lines;
    private String currentLexeme,dialogText;
    private int lineNumber,status,orlyCount;
    private boolean validFile,validLexical,validSyntax,validSemantics,readBack,conditionalStatement,switchStatement;
    private ArrayList<ArrayList<Token>> tokens = new ArrayList<ArrayList<Token>>();
    private ArrayList<Token> tokensPerLine = new ArrayList<Token>();
    private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
    private ArrayList<Token> opTokens = new ArrayList<Token>();
    private Stack<String> operation = new Stack<String>();
    
    //process queue for switch
    private LinkedList<ArrayList<Token>> pQueue = new LinkedList<>();
    private boolean checkingSwitchStatement = false;
    private boolean executingSwitchStatement = false;
    
    //process queue for if-then
    private ArrayList<ArrayList<Token>> ifArray = new ArrayList<ArrayList<Token>>();
    private boolean checkingIfStatement = false;
    private boolean executingIfStatement = false;
	
    /* ERROR PROMPTS */
    
    //MISSING KEYWORDS
    public final static String WTF_MISSING = "missing WTF keyword";
    public final static String OMG_MISSING = "missing OMG keyword";
    public final static String OMGWTF_MISSING = "missing OMGWTF keyword";
    public final static String OIC_MISSING = "missing OIC keyword";
    public final static String ORLY_MISSING = "missing ORLY keyword";
    public final static String YARLY_MISSING = "missing YARLY keyword";
    public final static String EXPR_MISSING = "missing expression";
    public final static String MKAY_MISSING = "missing MKAY keyword";
    public final static String VARIDENT_MISSING = "missing variable identifier";
    public final static String ITZ_MISSING = "missing ITZ keyword";
    public final static String OPERAND_MISSING = "missing operand";
    public final static String AN_MISSING = "missing AN keyword";

    //INCORRECT
    public final static String INCORRECT_TYPE = "incorrect type";
    public final static String INCORRECT_STATEMENT = "incorrect statement";
    public final static String INCORRECT_TOKEN_NUM = "incorrect number of tokens";
    
    //MISPLACED
    public final static String AN_MISPLACED = "misplaced AN keyword";
    public final static String NOT_MISPLACED = "misplaced NOT keyword";
    public final static String OPERATOR_MISPLACED = "misplaced operator";
    
    //TYPECASTING ERRORS
    public final static String PARSE_YARN = "cannot parse YARN to NUMBR/NUMBAR";
    public final static String PARSE_NOOB = "cannot parse NOOB to NUMBR/NUMBAR";
    
    //UNIQUE
    public final static String UNDECLARED = "undeclared variable identifier";
    public final static String DECLARED = "previously declared variable identifier";
    public final static String UNINITIALIZED = "uninitialized variable identifier";
    public final static String INSUFFICIENT_OP = "insufficient amount of operands"; 
    public final static String UNEXPECTED_FLOAT = "unexpected float";
    public final static String DIV_BY_ZERO = "division by zero";
    public final static String PRINT_NULL = "cannot print NOOB value";
    public final static String INVALID_DATA_TYPE = "invalid data type";
    public final static String INVALID_FORMAT = "invalid format";
    public final static String INVALID_LEXEME = "invalid lexeme";
    public final static String INVALID_STATEMENT = "invalid statement";
    public final static String TOO_MANY_OPERANDS = "too many operands";
    public final static String UNEXPECTED_END = "unexpected end of expression";
    public final static String DUP_MKAY = "duplicate MKAY/presence of nested infinite arity operation";
    public final static String UNNECESSARY_MKAY = "presence of MKAY without matching infinite arity operation";
	
    public Interpreter() {
		root = new Group();
		scene = new Scene(this.root,WINDOW_WIDTH,WINDOW_HEIGHT, Color.web("#315f72"));
		canvas = new Canvas(WINDOW_HEIGHT,WINDOW_HEIGHT);
		canvas.getGraphicsContext2D();
	}
	
	public void setStage(Stage stage) {
		//set preferences for "select LOLCODE file" button
        this.fileButton.setLayoutX(0);
        this.fileButton.setLayoutY(100);
        this.fileButton.setMinWidth(500);
        
        //set preferences for "EXECUTE" button
        this.executeButton.setLayoutX(0);
        this.executeButton.setLayoutY(600);
        this.executeButton.setMinWidth(1500);
        
        //set preferences for displaying code
        this.codeDisplay.setLayoutX(0);
        this.codeDisplay.setLayoutY(130);
        this.codeDisplay.setPrefWidth(500);
        this.codeDisplay.setPrefHeight(470);
        this.codeDisplay.setEditable(false);
        
        //set preferences for displaying output
        this.outputDisplay.setLayoutX(10);
        this.outputDisplay.setLayoutY(650);
        this.outputDisplay.setPrefWidth(1200);
        this.outputDisplay.setPrefHeight(270);
        this.outputDisplay.setEditable(false);
        
        //set preferences for imageview of pass indicator
        this.passIndicator.setLayoutX(1270);
        this.passIndicator.setLayoutY(650);
          
        //set preferences for imageview of title
        this.titleImage.setLayoutX(530);
        this.titleImage.setLayoutY(10);
        
        //call to functions
		openFile();	
		generateLexemes();
		createTable("lexemes");
		createTable("symbols");
		
		root.getChildren().addAll(canvas, codeDisplay, fileButton, executeButton, outputDisplay, passIndicator, titleImage);
		root.getStylesheets().add(getClass().getResource("lolcodeinterpreter.css").toString());
		this.stage = stage;
		this.stage.getIcons().add(new Image(("imgs/title.png")));
		this.stage.setTitle("LOLCODE INTERPRETER");
		this.stage.setMinWidth(WINDOW_WIDTH);
		this.stage.setMinHeight(WINDOW_HEIGHT);
		this.stage.setScene(this.scene);
		this.stage.show();
	}
	
	
	//FUNCTION FOR INTERPRETING LOLCODE FILE
	private void interpretFile() {		
		while(lineNumber<lines.length) { //process every line
			status = 0;
			readBack=false;

			
			//check status of the current line
			//0 - valid lexeme; 
			//1 - invalid lexeme; 
			//2 - invalid lexeme, but process again bc a varident is detected as a possible keyword
			status = checkLexeme(lines[lineNumber]);
			System.out.println("Line check: "+lineNumber);

			if(status == 2) { //case 2
				lineNumber--;
				status = checkLexeme(currentLexeme); //process again starting from where an invalid lexeme is detected
			}  
			if(status == 1) { //case 1 or case 2 and there's still an invalid lexeme
				createErrorPrompt(Interpreter.INVALID_LEXEME);
				validSyntax = false;
				break;
			}	
			
			System.out.println("passed lexical");

			if(!tokensPerLine.isEmpty()) {
				addToTokens();
			
				//if the current line has a comment, ignore the BTW token
				if(tplLexeme(tokensPerLine.size()-1).equals(Token.BTW))
					tokensPerLine.remove(tokensPerLine.size()-1);
			}
			
			if(!tokensPerLine.isEmpty()) {
				checkSyntaxAndSemantics();
				if(validSyntax) System.out.println("passed syntax");
				if(validSemantics) System.out.println("passed semantics");
				if(!validSyntax || !validSemantics) break;

			}
			tokensPerLine.clear();
		}
	}
	
	
	//FUNCTIONS FOR SYNTAX AND SEMANTIC ANALYSIS
	private void checkSyntaxAndSemantics() {
		if(tokensPerLine.size() > 1) {
			//SWITCH CASE = WTF? OMG OMGWTF OIC
			//IF WTF? was the previous operation, it must be followed by an OMG keyword
			if(checkingSwitchStatement && pQueue.size() == 1) {
				if(tplClass(0).equals(Token.OMG_CLASSIFIER) && tplSize(2)) {
					if(isALit(tplClass(1))) storeTokensToQueue(Token.WTF);
					else validSyntax = false;
				}else if(tokensPerLine.size() > 2 && tplClass(2).equals(Token.YARN_LITERAL_CLASSIFIER)) {			
					storeTokensToQueue(Token.WTF);
				}
				else{
					createErrorPrompt(Interpreter.INVALID_STATEMENT);
					validSyntax = false;
				}
			}
			
			//OMG
			else if(tplLexeme(0).equals(Token.OMG)) {
				if(!checkingSwitchStatement) validSyntax = false;
				//there is already a default case
				else if(inProcessQueue(Token.OMGWTF, Token.WTF)) validSyntax = false;
				//check if the line next to OMG is a literal
				else if(isALit(tplClass(1)) && tplSize(2))
					storeTokensToQueue(Token.WTF);
				else if(tokensPerLine.size() > 2 && tplClass(2).equals(Token.YARN_LITERAL_CLASSIFIER))
					storeTokensToQueue(Token.WTF);
				else {
					createErrorPrompt(Interpreter.INVALID_STATEMENT);
					validSyntax = false;
				}
			}
			
			//MEBBE
			else if(tplLexeme(0).equals(Token.MEBBE)) {
				if(checkingIfStatement) {
					if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tplClass(1)) || 
							Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tplClass(1)) || Token.COMPARISON_OPERATORS.contains(tplClass(1))) {
						storeTokensToQueue(Token.O_RLY);
					} else {
						createErrorPrompt(Interpreter.INVALID_STATEMENT);
						validSyntax = false;
					}
				} else {
					createErrorPrompt(Interpreter.INVALID_STATEMENT);
					validSyntax = false;
				}
			}
			
			//PRINT = VISIBLE
			else if(tplLexeme(0).equals(Token.VISIBLE)) {
				if(printSyntax()) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else printExecute();
				}
				else {
					validSyntax = false;
				}
			}
			
			//ACCEPT = GIMMEH
			else if(tplLexeme(0).equals(Token.GIMMEH)) {
				if(acceptSyntax()) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else acceptExecute();		
				}
				else validSyntax = false;
			}
			
			//VARIABLE DECLARATION = I HAS A
			else if(tplLexeme(0).equals(Token.I_HAS_A)) {
				String literalClassification = varDeclarationSyntax();
				if(literalClassification != null) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else varDeclarationExecute(literalClassification);
				}
				else validSyntax = false;				
			}
	
			//ASSIGNMENT STATEMENT = R
			else if(tplLexeme(1).equals(Token.R)) {
				String literalClassification = varAssignmentSyntax();
				if(literalClassification != null) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else varAssignmentExecute(literalClassification);
				}
				else validSyntax = false;
			}
			
			//ARITHMETIC OPERATIONS
			else if(Token.ARITHMETIC_EXPRESSIONS.contains(tplClass(0))) {
				if(combiSyntax(tokensPerLine)) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else combiExecute(Token.IT,tokensPerLine);
				}
				else validSyntax = false;
			}	
			
			//COMPARISON OPERATORS
			else if(Token.COMPARISON_OPERATORS.contains(tplClass(0)) ) {
				if(combiSyntax(tokensPerLine)) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else combiExecute(Token.IT,tokensPerLine);
				}
				else validSyntax = false;
			}
	
			//BOOLEAN OPERATIONS
			else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tplClass(0)) || 
					Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tplClass(0)) || Token.COMPARISON_OPERATORS.contains(tplClass(0))) {
				if(combiSyntax(tokensPerLine)) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else combiExecute(Token.IT, tokensPerLine);
				}
				else validSyntax = false;
			}
			
			else if(Token.SMOOSH_CLASSIFIER.equals(tplClass(0))) {
				if(smooshSyntax(tokensPerLine)) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else smooshExecute(Token.IT, tokensPerLine);
				}
				else validSyntax = false;
			}
			
			else if(Token.HAI_CLASSIFIER.equals(tplClass(0))) {
				if(!(tplSize(2) && tplLexeme(1).equals("1.2"))) {
					createErrorPrompt(Interpreter.UNEXPECTED_FLOAT);
					validSyntax = false;
				}
			} 
			
			else {
				createErrorPrompt(Interpreter.INVALID_STATEMENT);
				validSyntax = false;
			}
		} else {
			if(tplClass(0).equals(Token.HAI_CLASSIFIER) || 
				tplClass(0).equals(Token.OBTW_CLASSIFIER) || tplClass(0).equals(Token.TLDR_CLASSIFIER))
				validSyntax = true;
			else if(tplClass(0).equals(Token.KTHXBYE_CLASSIFIER)) {
				if(conditionalStatement==true || switchStatement==true) {
			    	for(ArrayList<Token> tokensPerLine: tokens) {
			        	for(Token token: tokensPerLine) {
							if(token.getLexeme().equals(Token.OIC)) {
								validSyntax = true;
								break;
							} else {
								validSyntax = false;
							} 
			        	} 
			        	if(validSyntax) break;
			    	}
			    	if(!validSyntax) createErrorPrompt(Interpreter.OIC_MISSING);
				} 
			} else if(tplClass(0).equals(Token.WTF_CLASSIFIER)) {
				checkingSwitchStatement = true;
				switchStatement = true;
				storeTokensToQueue(Token.WTF);
			} else if(tplClass(0).equals(Token.OIC_CLASSIFIER)) {
				//check if WTF and OMGs are already in the switch statement
				if((inProcessQueue(Token.WTF, Token.WTF) && inProcessQueue(Token.OMG, Token.WTF) && checkingSwitchStatement) || executingSwitchStatement) {
					storeTokensToQueue(Token.WTF);
					switchCaseExecute();	
				}
				
				//check if ORLY, YA RLY are already in the if-then statement
				else if((inProcessQueue(Token.O_RLY, Token.O_RLY) && inProcessQueue(Token.YA_RLY, Token.O_RLY) && checkingIfStatement) || executingIfStatement) {
					storeTokensToQueue(Token.O_RLY);
					orlyCount--;
					if(orlyCount==0) ifElseExecute();	
				} else {
					createErrorPrompt(Interpreter.INVALID_STATEMENT);
					validSyntax = false;
				}
			} else if(tplClass(0).equals(Token.GTFO_CLASSIFIER)) {
				storeTokensToQueue(Token.WTF);
			} else if(tplClass(0).equals(Token.OMGWTF_CLASSIFIER)) {
				//duplication of OMGWTF
				if(inProcessQueue(Token.OMGWTF, Token.WTF)) validSyntax = false;
				else storeTokensToQueue(Token.WTF);
			} else if(tplClass(0).equals(Token.O_RLY_CLASSIFIER)) {
				checkingIfStatement = true;
				conditionalStatement = true;
				orlyCount++;
				storeTokensToQueue(Token.O_RLY);
			} else if(tplClass(0).equals(Token.YA_RLY_CLASSIFIER)) {
				if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
				else {
					createErrorPrompt(Interpreter.INVALID_STATEMENT);
					validSyntax=false;
				}
			} else if(tplClass(0).equals(Token.NO_WAI_CLASSIFIER)) {
				if(checkingIfStatement) {
					for(ArrayList<Token> tokensPerLine: tokens) {
			        	for(Token token: tokensPerLine) {
							if(token.getLexeme().equals(Token.YA_RLY)) {
								storeTokensToQueue(Token.O_RLY);
								validSyntax = true;
								break;
							} else {
								validSyntax = false;
							} 
			        	} 
			        	if(validSyntax) break;
			    	}
			    	if(!validSyntax) createErrorPrompt(Interpreter.YARLY_MISSING);
				}
				else {
					createErrorPrompt(Interpreter.INVALID_STATEMENT);
					validSyntax=false;
				}
			} else if(tplLexeme(0).equals(Token.I_HAS_A) || tplLexeme(0).equals(Token.GIMMEH)){
				createErrorPrompt(Interpreter.VARIDENT_MISSING);				
				validSyntax = false;
			} else {
				createErrorPrompt(Interpreter.INVALID_STATEMENT);				
				validSyntax = false;				
			}
		}
	}	
	
	//SYNTAX FOR PRINT = VISIBLE
	private boolean printSyntax() {
		int i=1,operation;
		
		if(tplSize(1)) return true;
		
		while(i<tokensPerLine.size()) {			
			//case 1: varident or literal
			if(isALitOrDlmtrOrVar(tplClass(i))) i++;

			//case 2: expr
			else if((operation = isAnExpr(tplClass(i))) != 0) {
				opTokens.clear();				
				boolean stop = false;

				//copy the tokens starting from the operation until the end of the expression
				do {
					opTokens.add(tokensPerLine.get(i));
					if(isADlmtr(tplClass(i))) {
						i++;
						continue;
					}
					
					if(isALitOrVar(tplClass(i)) || Token.MKAY_CLASSIFIER.equals(tplClass(i))) {
						if(i+1 != tokensPerLine.size()) {
							if(tplClass(i).equals(Token.YARN_LITERAL_CLASSIFIER)) {
								if(i+2 < tokensPerLine.size()) {
									if(!(tplLexeme(i+2).equals(Token.AN) || tplLexeme(i+2).equals(Token.MKAY))) stop = true;	
								}
							}else {
								if(!(tplLexeme(i+1).equals(Token.AN) | tplLexeme(i+1).equals(Token.MKAY))) stop = true;
							}
														
						}
					}

					i++;
				} while(i<tokensPerLine.size() && !stop);

				if(operation == 4 && !smooshSyntax(opTokens)) {
					
					return false;
				}
				else if(!combiSyntax(opTokens)) return false;
			}
			
			//case where the visible ends with an exclamation
			else if(tplLexeme(i).equals(Token.EXCLAMATION_POINT)) {
				if(tplSize(i+1))  i++;
				else {
					createErrorPrompt(Interpreter.INVALID_FORMAT);
					return false;
				}
			}
		}	
		return true;
	}
	
	//SEMANTICS FOR PRINT = VISIBLE
	private void printExecute() {
		int i=1,operation;
		boolean appendNewLine=true;
		String itValue = symbols.get(0).getValue();
		String visibleValue = "";
		
		if(tplSize(1)) visibleValue = "\n";
		
		while(i<tokensPerLine.size()) {			
			//case 1: varident/it
			if(isAVar(tplClass(i))) {
				
				//check if the varident is in the symbols
				Symbol s = isASymbol(tplLexeme(i));
				
				if(s != null) {
					if(!s.getDataType().equals(Symbol.UNINITIALIZED))
						visibleValue += s.getValue();	
					else {
						createErrorPrompt(Interpreter.PRINT_NULL);
						validSemantics = false;
						break;
					}
				} else {
					createErrorPrompt(Interpreter.UNDECLARED);
					validSemantics = false;
					break;
				}
			} 
			
			//case 2: expr
			else if((operation = isAnExpr(tplClass(i))) != 0) {
				opTokens.clear();
				
				//copy the tokens starting from the operation until the end of the expression
				boolean stop = false;
				
				do {
					opTokens.add(tokensPerLine.get(i));
					if(tplClass(i).equals(Token.STRING_DELIMITER_CLASSIFIER)) {
						i++;
						continue;
					}
					
					if(isALitOrVar(tplClass(i)) || Token.MKAY_CLASSIFIER.equals(tplClass(i))) {
						if(i+1 != tokensPerLine.size()) {
							
							if(tplClass(i).equals(Token.YARN_LITERAL_CLASSIFIER)) {
								if(i+2 < tokensPerLine.size()) {
									if(!(tplLexeme(i+2).equals(Token.AN) || tplLexeme(i+2).equals(Token.MKAY))) 
										stop = true;	
								}
							}
							else if(!(tplLexeme(i+1).equals(Token.AN) || tplLexeme(i+1).equals(Token.MKAY))) 
								stop = true;								
						}
					}

					i++;
				} while(i<tokensPerLine.size() && !stop);
				i--;

				
				//case 2.1: concat op
				if(operation == 4) {
					//check if the boolop has a valid syntax
					if(smooshSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else {
							smooshExecute(Token.IT,opTokens);
							visibleValue += symbols.get(0).getValue();
							symbols.get(0).setValue(itValue);
						}
					}
					else {
						validSyntax = false;
						break;
					}
				}
				
				//case 2.2: other op
				else {	
					//check if the compop has a valid syntax
					if(combiSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else {
							combiExecute(Token.IT,opTokens);
							visibleValue += symbols.get(0).getValue();
							symbols.get(0).setValue(itValue);
						}
					} else {
						validSyntax = false;
						break;
					}
				}			
			} else if(isALit(tplClass(i))) { //case 3: literals
				visibleValue += tplLexeme(i);
				dialogText = tplLexeme(i);
			} else if(isADlmtr(tplClass(i))) { //skip string delimiter of a yarn literal
				i++;
				continue;
			} else if(tplLexeme(i).equals(Token.EXCLAMATION_POINT)) { //visible ends with an exclamation
				appendNewLine = false;
			} else {
				createErrorPrompt(Interpreter.INVALID_FORMAT);
				validSemantics = false;
				break;
			}
			
			i++;
		}	
		
		outputDisplayText += visibleValue;
		if(appendNewLine) outputDisplayText += "\n";						
	}
	
	//SYNTAX FOR ACCEPT = GIMMEH
	private boolean acceptSyntax() {
		if(tplSize(2)) {
			if(isAVar(tplClass(1))) return true; 
			createErrorPrompt(Interpreter.INCORRECT_TYPE);
			return false; //not a variable
		}
		createErrorPrompt(Interpreter.INCORRECT_TOKEN_NUM);
		return false; //GIMMEH contains anything less or more than the variable

	}
	
	//SEMANTICS FOR ACCEPT = GIMMEH
	private void acceptExecute() {
		Symbol s = isASymbol(tplLexeme(1));
		
		if(s != null) { //check if the variable is in the symbols
			clearTable();
			populateTable();
			outputDisplay.setText(outputDisplayText);
	        getInput(s,dialogText); //get user input
		} else validSemantics = false; //variable is undeclared
	}
	
	private void getInput(Symbol s,String dialogText) {
        TextInputDialog inputDialog = new TextInputDialog();

        //set the title,header text, and context text of input dialog
        inputDialog.setTitle("USER INPUT");
        inputDialog.setHeaderText(null);
        inputDialog.setContentText(dialogText);
        
        Optional<String> input = inputDialog.showAndWait();     
        
        if(input.isPresent()) { //if user entered an input, set the variable's value to the input
            input.ifPresent(value -> {
            	s.setValue(value);
            	s.setDataType(getDataType(value)); //automatically typecast based on input
            	outputDisplayText += value + "\n";
            });	
        } else validSemantics = false;  
	}
	
	
	
	//SYNTAX FOR VARIABLE DECLARATION = I HAS A
	private String varDeclarationSyntax() {
		if(tokensPerLine.size() > 1) {
			if(isAVar(tplClass(1))) {	
				if(tplSize(2)) return ""; //case 1: I HAS A var
				if(tplClass(2).equals(Token.ITZ_CLASSIFIER)) { //case 2: I HAS A var ITZ var/lit/expr
					if(tplSize(4)) {
						if(isALitOrVar(tplClass(3))) return tplClass(3);
						else {
							createErrorPrompt(Interpreter.INCORRECT_TYPE);
							return null;
						}
					}
					if(tplSize(6) && Token.YARN_LITERAL_CLASSIFIER.equals(tplClass(4))) return tplClass(4);	
					if(isAnExpr(tplClass(3)) != 0) {
						opTokens.clear();
						for(int i=3;i<tokensPerLine.size();i++)
							opTokens.add(tokensPerLine.get(i));
							
						if(!(smooshSyntax(opTokens) || combiSyntax(opTokens))) {
							validSyntax = false;
							return null;
						}
						return tplClass(3);	
					}
					createErrorPrompt(Interpreter.INVALID_FORMAT);
					return null;
				}
				else {
					createErrorPrompt(Interpreter.ITZ_MISSING);
					return null;
				}
			} else {
				createErrorPrompt(Interpreter.INCORRECT_TYPE);
				return null;
			}
		} else {
			//return null if no varident is declared
			createErrorPrompt(Interpreter.VARIDENT_MISSING);
			return null;			
		}
	}
	
	//SEMANTICS FOR VARIABLE DECLARATION = I HAS A
	public void varDeclarationExecute(String litClass) {
		String identifier = tplLexeme(1);
		int operation;
		
		//check if the varident is already declared before
		if(isASymbol(identifier) != null) {
			createErrorPrompt(Interpreter.DECLARED);
			validSemantics = false;
		}
		//case 1: I HAS A var
		else if(tplSize(2)) {
			symbols.add(new Symbol(identifier,Token.NOOB_TYPE_LITERAL, Symbol.UNINITIALIZED));	
		//case 2: I HAS A var ITZ var/lit/expr
		} else {
			//case 2.1: varident
			if(isAVar(litClass)) {	
				Symbol s = isASymbol(tplLexeme(3));
				
				if(s != null) {
					if(!s.getDataType().equals(Symbol.UNINITIALIZED))
						symbols.add(new Symbol(identifier,s.getValue(), s.getDataType()));
					else {
						createErrorPrompt(Interpreter.UNINITIALIZED);
						validSemantics = false;
					}
				}
				else {
					createErrorPrompt(Interpreter.UNDECLARED);
					validSemantics = false;
				}
			}
			
			//case 2.2: expr
			else if((operation = isAnExpr(litClass)) != 0) {
				symbols.add(new Symbol(identifier,"", Symbol.UNINITIALIZED));

				ArrayList<Token> opToken = new ArrayList<Token>();
				
				opTokens.clear();

				//copy the tokens starting from the operation
				for(int i=3;i<tokensPerLine.size();i++)
					opToken.add(tokensPerLine.get(i));
				
				//case: smoosh
				if(operation == 4) {
					//check if the concat op has a valid syntax
					if(smooshSyntax(opToken)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else smooshExecute(identifier,opToken);
					}
					else validSyntax = false;
				}

				//case: other op
				else {	
					if(combiSyntax(opToken)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else combiExecute(identifier,opToken);
					}
					else validSyntax = false;
				}
			}

			//case 2.3: literal
			//a yarn literal
			else if(litClass.equals(Token.YARN_LITERAL_CLASSIFIER))
				symbols.add(new Symbol(identifier, tokensPerLine.get(4).getLexeme(), Symbol.STRING));
			//or other type literals
			else if(litClass.equals(Token.NUMBAR_LITERAL_CLASSIFIER))
				symbols.add(new Symbol(identifier, tplLexeme(3), Symbol.FLOAT));
			else if(litClass.equals(Token.NUMBR_LITERAL_CLASSIFIER))
				symbols.add(new Symbol(identifier, tplLexeme(3), Symbol.INTEGER));
			else if(litClass.equals(Token.TROOF_LITERAL_CLASSIFIER))
				symbols.add(new Symbol(identifier, tplLexeme(3), Symbol.BOOLEAN));
			else {
				createErrorPrompt(Interpreter.INCORRECT_TYPE);
				validSemantics = false;
			}
		}
	}
			
	//SYNTAX FOR ASSIGNMENT STATEMENT = R
	private String varAssignmentSyntax() {
		if(tokensPerLine.size() > 2) {		
			//check if it assigns to a varident
			if(isAVar(tplClass(0))) {
				//return value if it is a varident/it, literal, or expr
				if(tplSize(3) && isALitOrVar(tplClass(2)))
					return tplClass(2);	
				if(tplSize(5) && Token.YARN_LITERAL_CLASSIFIER.equals(tplClass(3))) return tplClass(3);
				if(isAnExpr(tplClass(2))!=0) return tplClass(2);
				createErrorPrompt(Interpreter.INVALID_FORMAT);
				validSyntax = false;
				return null;
			} else {
				createErrorPrompt(Interpreter.INCORRECT_TYPE);
				validSyntax = false;
				return null;
			}
		}
		createErrorPrompt(Interpreter.INCORRECT_TOKEN_NUM);
		return null;
	}
	
	//SEMANTICS FOR ASSIGNMENT STATEMENT = R
	private void varAssignmentExecute(String litClass) {
		Symbol s = isASymbol(tplLexeme(0));
		Symbol sv = isASymbol(tplLexeme(2));
		int operation;
		
		//get the symbol, then set the value
		if(s != null) {							
			//case 1: varident
			if(isAVar(litClass)) {							
				if(sv != null) {
					if(!sv.getDataType().equals(Symbol.UNINITIALIZED)) {
						s.setValue(sv.getValue());
						s.setDataType(sv.getDataType());
					} else {
						createErrorPrompt(Interpreter.UNINITIALIZED);
						validSemantics = false;						
					}
				} else {
					createErrorPrompt(Interpreter.UNDECLARED);
					validSemantics = false;
				}
			}
			
			//case 2.2: expr
			if((operation = isAnExpr(litClass)) != 0) {
				opTokens.clear();
				
				//copy the tokens starting from the operation
				for(int i=2;i<tokensPerLine.size();i++)
					opTokens.add(tokensPerLine.get(i));
				
				//case: concat op
				if(operation == 4) {	
					if(smooshSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else smooshExecute(tplLexeme(0),opTokens);
					}
					else validSyntax = false;
				}
				
				//case: other op
				else {	
					if(combiSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else combiExecute(tplLexeme(0),opTokens);
					}
					else validSyntax = false;
				}
			}
							
			//case 3: literals
			//a yarn literal
			else if(litClass.equals(Token.YARN_LITERAL_CLASSIFIER)) {
				s.setValue(tokensPerLine.get(3).getLexeme());
				s.setDataType(Symbol.STRING);
			}
			//or other type literals
			else if(litClass.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) {
				s.setValue(tplLexeme(2));
				s.setDataType(Symbol.FLOAT);
			}
			
			else if(litClass.equals(Token.NUMBR_LITERAL_CLASSIFIER)) {
				s.setValue(tplLexeme(2));
				s.setDataType(Symbol.INTEGER);
			}
			
			else if(litClass.equals(Token.TROOF_LITERAL_CLASSIFIER)) {
				s.setValue(tplLexeme(2));
				s.setDataType(Symbol.BOOLEAN);
			}
		} else {
			createErrorPrompt(Interpreter.UNDECLARED);
			validSemantics = false;
		}
	}
			
	private boolean combiSyntax(ArrayList<Token> combiTokens) {
		Token currentToken;
		int infArityOpCount = 0;
		boolean mkayIsPresent = false;
		//since prefix, read the line in reverse
		Collections.reverse(combiTokens);
		
		for(int i=0; i < combiTokens.size(); i++) {
			currentToken = combiTokens.get(i);

			//if AN is detected, it must not be the last or starting token, and must not be followed by an AN
			if(currentToken.getLexeme().equals(Token.AN)) {
				
				//AN is starting/last token
				if(i == 0 || i == (combiTokens.size()-1)) { 
					createErrorPrompt(Interpreter.UNEXPECTED_END);
					return false;
				}
					
				
				//followed by AN/MKAY
				else if((combiTokens.get(i-1).getLexeme().equals(Token.AN) || combiTokens.get(i-1).getLexeme().equals(Token.MKAY))) {
					createErrorPrompt(Interpreter.AN_MISPLACED);
					return false;
				}
					
			}else if(currentToken.getClassification().equals(Token.NOT_CLASSIFIER)) {
			
				//NOT is last token
				if(i == 0) {
					createErrorPrompt(Interpreter.NOT_MISPLACED);
					return false;
				}
				
				//followed by AN/MKAY
				else if(combiTokens.get(i-1).getLexeme().equals(Token.AN) || combiTokens.get(i-1).getLexeme().equals(Token.MKAY)) {
					createErrorPrompt(Interpreter.AN_MISPLACED);
					return false;
				}
					
				
				else {
					
					//insufficient amount of operands
					if(operation.size() == 0) {
						createErrorPrompt(Interpreter.INSUFFICIENT_OP);
						return false;
					}
						
					
				}
			}else if(currentToken.getLexeme().equals(Token.ALL_OF) || currentToken.getLexeme().equals(Token.ANY_OF)) {
				
				//missing mkay
				if(!mkayIsPresent) {
					createErrorPrompt(Interpreter.MKAY_MISSING);
					return false;
				}
				
				//if it starts with ANY OF/ALL OF, pop depending on the amt of inf arity op cnt
				if(infArityOpCount > 1 && !combiTokens.get(i-1).getLexeme().equals(Token.AN)) {
					
					int popCnt = 0;	
					while(popCnt < infArityOpCount-1) {
						if(operation.size() > 1) {
							operation.pop();
							operation.pop();
							operation.push("TROOF");
							popCnt++;
						}else {
							createErrorPrompt(Interpreter.INSUFFICIENT_OP);
							validSyntax = false;
							return false;
						}
					}
										
					infArityOpCount = 0;
					mkayIsPresent = false;
				}
					 
				
				
				else {
					if(infArityOpCount <= 1) createErrorPrompt(Interpreter.INSUFFICIENT_OP);
					else createErrorPrompt(Interpreter.AN_MISPLACED);
					
					return false;
				}
			}else if(currentToken.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER) || currentToken.getLexeme().equals(Token.NOOB_TYPE_LITERAL)) {
				//if last token, it must be preceeded with an AN or NOT
				if(i == 0) {
					if(i+1 < combiTokens.size()-1 && !(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT))) {
						createErrorPrompt(Interpreter.AN_MISSING);
						return false;
					}
				} else {
					//if not last token, it must be followed with an AN/MKAY
					if(!(combiTokens.get(i-1).getLexeme().equals(Token.AN) || combiTokens.get(i-1).getLexeme().equals(Token.MKAY))) {
						createErrorPrompt(Interpreter.AN_MISPLACED);
						return false;
					}
						
				}
				
				//push to stack
				 operation.push("TROOFNOOB");
				
				 //update inf arity op count if mkay is present
				 if(mkayIsPresent) infArityOpCount++;
			}else if(Token.STRING_DELIMITER_CLASSIFIER.equals(currentToken.getClassification())) {
				continue;
			}else if(currentToken.getClassification().equals(Token.YARN_LITERAL_CLASSIFIER)){
				if(i == 0) {
					
					if(i+2 < combiTokens.size()-1 && !(combiTokens.get(i+2).getLexeme().equals(Token.AN) || combiTokens.get(i+2).getLexeme().equals(Token.NOT))) {
						createErrorPrompt(Interpreter.AN_MISSING);
						return false;
					}
						
				} else {
					//if not last token, it must be followed with an AN/MKAY
					if(i-2 > 0 && !(combiTokens.get(i-2).getLexeme().equals(Token.AN) || combiTokens.get(i-2).getLexeme().equals(Token.MKAY))) {
						createErrorPrompt(Interpreter.AN_MISSING);
						return false;
					}
						
				}
				
				//push to stack
				 operation.push("YARN");
				 if(mkayIsPresent) infArityOpCount++;
			}else if(isAVar(currentToken.getClassification())) {
				
				//if not last token
				if(i == 0) {
					if(i+1 < combiTokens.size() && !(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT))) {
						createErrorPrompt(Interpreter.AN_MISSING);
						return false;
					}
						
				} else {
					//if not last token, it must be followed with an AN/MKAY
					if(i-1 > 0 && !(combiTokens.get(i-1).getLexeme().equals(Token.AN) || !combiTokens.get(i-1).getLexeme().equals(Token.MKAY))) {
						createErrorPrompt(Interpreter.AN_MISSING);
						return false;
					}
						
				}
				
				operation.push("VARIDENT");
				if(mkayIsPresent) infArityOpCount++;
			}else if(isADigit(currentToken.getClassification())) {
				if(i == 0) {
					if(!(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT))) {
						createErrorPrompt(Interpreter.AN_MISSING);
						return false;
					}
						
				} else {
					//if not last token, it must be followed with an AN
					if(!(combiTokens.get(i-1).getLexeme().equals(Token.AN) | combiTokens.get(i-1).getLexeme().equals(Token.MKAY))) {
						createErrorPrompt(Interpreter.AN_MISSING);
						return false;
					}
						
				}
				
				operation.push("DIGIT");
				if(mkayIsPresent) infArityOpCount++;
			}else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(currentToken.getClassification())) {
				
				
				//make sure it is not the last token
				if(i == 0) {
					createErrorPrompt(Interpreter.UNEXPECTED_END);
					return false;
				}
				
				
				//make sure it is not followed by an 'AN'
				else if(combiTokens.get(i-1).getLexeme().equals(Token.AN)) {
					createErrorPrompt(Interpreter.AN_MISPLACED);
					return false;
				}
				
				
				
				//pop one operand
				if(operation.size() > 1) {
					operation.pop();
					operation.pop();
					operation.push("TROOF");
					if(mkayIsPresent) infArityOpCount--;
				}
				
				//insufficient amount of operands
				else{
					createErrorPrompt(Interpreter.INSUFFICIENT_OP);
					return false;
				}
			} else if(Token.ARITHMETIC_EXPRESSIONS.contains(currentToken.getClassification())){
				
				//make sure it is not the last token
				if(i == 0) {
					createErrorPrompt(Interpreter.OPERATOR_MISPLACED);
					return false;
				}
				
				//make sure it is not followed by an 'AN'
				else if(combiTokens.get(i-1).getLexeme().equals(Token.AN)) {
					createErrorPrompt(Interpreter.AN_MISPLACED);
					return false;
				}
				
				//pop one operand
				if(operation.size() > 1) {
					operation.pop();
					operation.pop();
					operation.push("DIGIT");
					if(mkayIsPresent) infArityOpCount--;
				}else{
					createErrorPrompt(Interpreter.INSUFFICIENT_OP);
					return false;
				}
			}else if(Token.COMPARISON_OPERATORS.contains(currentToken.getClassification())){
				
				
				//make sure it is not the last token
				if(i == 0) {
					createErrorPrompt(Interpreter.OPERATOR_MISPLACED);
					return false;
				}
				
				//make sure it is not followed by an 'AN'
				else if(combiTokens.get(i-1).getLexeme().equals(Token.AN)) {
					createErrorPrompt(Interpreter.AN_MISPLACED);
					return false;
				}
					
				
				//pop one operand
				if(operation.size() > 1) {
					operation.pop();
					operation.pop();
					operation.push("TROOF");
					if(mkayIsPresent) infArityOpCount--;
				}else{
					createErrorPrompt(Interpreter.INSUFFICIENT_OP);
					return false;
				}
			}else if(Token.MKAY_CLASSIFIER.equals(currentToken.getClassification())) {
				if(mkayIsPresent) {
					createErrorPrompt(Interpreter.DUP_MKAY);
					return false;
				}
				
				if(i == 0) {
					String nextToken = combiTokens.get(i+1).getClassification();
					if(nextToken.equals(Token.MKAY_CLASSIFIER)) {
						createErrorPrompt(Interpreter.DUP_MKAY);
						return false;
					}
					
					else if(!(isALit(nextToken) || isAVar(nextToken) || Token.STRING_DELIMITER_CLASSIFIER.equals(nextToken))) {
						createErrorPrompt(Interpreter.OPERAND_MISSING);
						return false;
					}
						
				} else {
					//if not last token, it must be followed with an AN/MKAY
					if(!(combiTokens.get(i-1).getLexeme().equals(Token.AN))) {
						createErrorPrompt(Interpreter.AN_MISSING);
						return false;
					}
						
				}
				
				mkayIsPresent = true;
			}else return false; //lexeme does not belong in the expression			
		
		}
		
		//there should only be 1 operand left
		if((operation.size() == 1) && infArityOpCount == 0) {
			//back to original state
			Collections.reverse(combiTokens);
			operation.pop();
			return true;
		}
		else {
			createErrorPrompt(Interpreter.TOO_MANY_OPERANDS);
			return false;
		
		}
		
		
	}
	
	private String combiExecute(String dataHolder, ArrayList<Token> combiTokens) {
		//since prefix, read the line in reverse
		Collections.reverse(combiTokens);
		int infArityOpCount = 0;
		boolean mkayIsPresent = false;
		
		//check if dataHolder exists
		Symbol s = isASymbol(dataHolder);
		if(s == null) {
			createErrorPrompt(Interpreter.UNDECLARED);
			validSemantics = false;
			return null;
		}
		
		
		for(Token tkn: combiTokens) {			
			if(tkn.getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER) || tkn.getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER) || tkn.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER) || tkn.getLexeme().equals(Token.NOOB_TYPE_LITERAL)) {
				operation.push(tkn.getLexeme());
				if(mkayIsPresent) infArityOpCount++;
			}else if(isAVar(tkn.getClassification())) {
				Symbol var = isASymbol(tkn.getLexeme());
				
				if(var != null) {
					if(var.getDataType().equals(Symbol.STRING)) {
						operation.push("\""+var.getValue()+"\"");
					}
					
					else operation.push(var.getValue());
					
					if(mkayIsPresent) infArityOpCount++;
				}
				else {
					createErrorPrompt(Interpreter.UNDECLARED);
					validSemantics = false;
					return null;
				}
			}  else if(tkn.getClassification().equals(Token.STRING_DELIMITER_CLASSIFIER)){
				continue;
			}else if(tkn.getClassification().equals(Token.YARN_LITERAL_CLASSIFIER)) {
				operation.push("\""+tkn.getLexeme()+"\"");
				if(mkayIsPresent) infArityOpCount++;
			}else if(tkn.getClassification().equals(Token.MKAY_CLASSIFIER)) {
				mkayIsPresent = true;
			}else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tkn.getClassification())) {
				String op1 = operation.pop();
				String classificationOp1 = getClass(op1);
				if(!classificationOp1.equals(Token.TROOF_LITERAL_CLASSIFIER)) op1 = boolTypeCast(op1);				
				
				String op2 = operation.pop();
				String classificationOp2 = getClass(op2);
				if(!classificationOp2.equals(Token.TROOF_LITERAL_CLASSIFIER)) op2 = boolTypeCast(op2);
				
				switch(tkn.getClassification()) {
					case Token.BOTH_OF_CLASSIFIER:
						operation.push(andOperator(op1, op2));
						break;
					case Token.EITHER_OF_CLASSIFIER:
						operation.push(orOperator(op1, op2));
						break;
					case Token.WON_OF_CLASSIFIER:
						operation.push(xorOperator(op1, op2));
						break;
				}
				
				if(mkayIsPresent) infArityOpCount--;
				
			}else if(Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tkn.getClassification())) {
				String op1;
				
				if(tkn.getLexeme().equals(Token.NOT)) {
					op1 = operation.pop();
					String classificationOp1 = getClass(op1);
					if(!classificationOp1.equals(Token.TROOF_LITERAL_CLASSIFIER)) op1 = boolTypeCast(op1);
					
					operation.push(notOperator(op1));
				}else {
					String op2;
					
					if(tkn.getLexeme().equals(Token.ANY_OF)) {
						
						int popCnt = 0;
						while(popCnt < infArityOpCount-1) {
							op1 = operation.pop();
							String classificationOp1 = getClass(op1);
							if(!classificationOp1.equals(Token.TROOF_LITERAL_CLASSIFIER)) op1 = boolTypeCast(op1);
							
							
							op2 = operation.pop();
							String classificationOp2 = getClass(op2);
							if(!classificationOp2.equals(Token.TROOF_LITERAL_CLASSIFIER)) op2 = boolTypeCast(op2);
							
							
							operation.push(orOperator(op1, op2));
							popCnt++;
						}
						infArityOpCount = 0;
						mkayIsPresent = false;
					}else {
						int popCnt = 0;
						while(popCnt < infArityOpCount-1) {
							op1 = operation.pop();
							
							String classificationOp1 = getClass(op1);
							if(!classificationOp1.equals(Token.TROOF_LITERAL_CLASSIFIER)) op1 = boolTypeCast(op1);
							
							
							op2 = operation.pop();
							String classificationOp2 = getClass(op2);
							if(!classificationOp2.equals(Token.TROOF_LITERAL_CLASSIFIER)) op2 = boolTypeCast(op2);
							
							operation.push(andOperator(op1, op2));
							popCnt++;
						}
						
						infArityOpCount = 0;
						mkayIsPresent = false;
					}
				}
			}else if(Token.ARITHMETIC_EXPRESSIONS.contains(tkn.getClassification())) {
				boolean resultIsNumbar = false;
				String op1 = operation.pop();
				String classificationOp1 = getClass(op1);
				
				if(!isADigit(classificationOp1)) op1 = arithTypeCast(op1);
				
				if(op1 == null) {
					validSemantics = false;
					return null;
				}
				
				String op2 = operation.pop();
				String classificationOp2 = getClass(op2);
				
				if(!isADigit(classificationOp2)) op2 = arithTypeCast(op2);
				
				if(op2 == null) {
					validSemantics = false;
					return null;
				}
				
				
				classificationOp1 = getClass(op1);
				classificationOp2 = getClass(op2);
				
				//check if one of the operands is numbar
				if(classificationOp1.equals(Token.NUMBAR_LITERAL_CLASSIFIER) || classificationOp2.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) resultIsNumbar = true;
				
				if(mkayIsPresent) infArityOpCount--;
				//if numbar, result must be float
				if(resultIsNumbar) {
					Float o1 = Float.parseFloat(op1);
					Float o2 = Float.parseFloat(op2);
					
					Float answer;
					//perform the operation then push to stack
					switch(tkn.getClassification()) {
					case Token.SUM_OF_CLASSIFIER:
						answer = o1 + o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.DIFF_OF_CLASSIFIER:
						answer = o1 - o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.PRODUKT_OF_CLASSIFIER:
						answer = o1 * o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.QUOSHUNT_OF_CLASSIFIER:
						if(o2 == 0) {
							
							createErrorPrompt(Interpreter.DIV_BY_ZERO);
							validSemantics = false;
							return null;
						}
						answer = o1 / o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.MOD_OF_CLASSIFIER:
						if(o2 == 0) {
							createErrorPrompt(Interpreter.DIV_BY_ZERO);
							validSemantics = false;
							return null;
						}
						answer = o1 % o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.BIGGR_OF_CLASSIFIER:
						if(o1 > o2) {
							operation.push(String.valueOf(o1));
						}
						else{
							operation.push(String.valueOf(o2));
						}
						
						break;
					case Token.SMALLR_OF_CLASSIFIER:
						if(o1 < o2) {
							operation.push(String.valueOf(o1));
						}
						else{
							operation.push(String.valueOf(o2));
						}
						break;
					}
				} else {
					//since no numbar val is detected, operands are assumed to be both numbr
					int o1 = Integer.parseInt(op1);
					int o2 = Integer.parseInt(op2);
					
					
					int answer;
					//perform the operation then push to stack
					switch(tkn.getClassification()) {
					case Token.SUM_OF_CLASSIFIER:
						answer = o1 + o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.DIFF_OF_CLASSIFIER:
						answer = o1 - o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.PRODUKT_OF_CLASSIFIER:
						answer = o1 * o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.QUOSHUNT_OF_CLASSIFIER:
						if(o2 == 0) {
							validSemantics = false;
							createErrorPrompt(Interpreter.DIV_BY_ZERO);
							return null;
						}
						answer = o1 / o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.MOD_OF_CLASSIFIER:
						if(o2 == 0) {
							createErrorPrompt(Interpreter.DIV_BY_ZERO);
							validSemantics = false;
							return null;
						}
						answer = o1 % o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.BIGGR_OF_CLASSIFIER:
						if(o1 > o2) {
							operation.push(String.valueOf(o1));
						}
						else{
							operation.push(String.valueOf(o2));
						}
						
						break;
					case Token.SMALLR_OF_CLASSIFIER:
	
						if(o1 < o2) {
							operation.push(String.valueOf(o1));
						}
						else{
							operation.push(String.valueOf(o2));
						}
						
						break;
					}
				} 
			}else if(Token.COMPARISON_OPERATORS.contains(tkn.getClassification())) {
				String op1 = operation.pop();
				String op2 = operation.pop();
				
				if(mkayIsPresent) infArityOpCount--;
				switch(tkn.getClassification()) {
					case Token.BOTH_SAEM_CLASSIFIER: // o1 == o2
						if(op1.equals(op2)) operation.push(Token.WIN_TROOF_LITERAL);
						else operation.push(Token.FAIL_TROOF_LITERAL);
						break;
					case Token.DIFFRINT_CLASSIFIER: //o1 != o2
						if(!op1.equals(op2)) {
							operation.push(Token.WIN_TROOF_LITERAL);
						}
						else{
							operation.push(Token.FAIL_TROOF_LITERAL);
						}						
						break;
				}
			}
		}
		
		
		String result = operation.pop();
		s.setValue(result);
		s.setDataType(Symbol.BOOLEAN);
		return result;
	}
		
	//TYPECASTS NON TROOF OPERANDS TO TROOF
	private String boolTypeCast(String op1) {
		String classificationOp1 = getClass(op1);
		
		//if string
		if(classificationOp1.equals(Token.YARN_LITERAL_CLASSIFIER)) {
			
			//if empty string, return fail
			if(op1.equals("\"\"")) return Token.FAIL_TROOF_LITERAL;
			
			//else return true
			else return Token.WIN_TROOF_LITERAL;
			
		//if digit
		}else if(isADigit(classificationOp1)){
			
			//if 0, return fail
			if(op1.equals("0")) return Token.FAIL_TROOF_LITERAL;
			
			//else return win
			else return Token.WIN_TROOF_LITERAL;
		
		//if NOOB, return fail
		}else if(classificationOp1.equals(Token.NOOB_TYPE_LITERAL)) {
			return Token.FAIL_TROOF_LITERAL;
			
		}else return op1;
		

	}
	
	//TYPECASTS NON NUMBAR/NUMBR OPERANDS TO NUMBR/NUMBAR
	private String arithTypeCast(String op1) {
		String classificationOp1 = getClass(op1);
		if(classificationOp1.equals(Token.YARN_LITERAL_CLASSIFIER)) {
			op1 = op1.replace("\"", "");
			
			classificationOp1 = getClass(op1);
			if(!isADigit(classificationOp1)) {
				
				createErrorPrompt(Interpreter.PARSE_YARN);
				return null;
			}
			
		}else if(classificationOp1.equals(Token.TROOF_LITERAL_CLASSIFIER)) {					
			if(op1.equals(Token.WIN_TROOF_LITERAL)) return "1";
			else return "0";
		}else if(Token.NOOB_TYPE_LITERAL.equals(op1)) {
			createErrorPrompt(Interpreter.PARSE_NOOB);
			return null;
		}
		
		return op1;
	}
		
	private String notOperator(String op1) {
		if(op1.equals(Token.WIN_TROOF_LITERAL)) return Token.FAIL_TROOF_LITERAL;
		else if(op1.equals(Token.FAIL_TROOF_LITERAL)) return Token.WIN_TROOF_LITERAL;
		else return null;
	}
	
	private String andOperator(String op1, String op2) {
		boolean operand1 = convertTroofToBoolean(op1);
		boolean operand2 = convertTroofToBoolean(op2);
	
		
		if(operand1 && operand2) return Token.WIN_TROOF_LITERAL;
		else return Token.FAIL_TROOF_LITERAL;
		
	}
	
	private String orOperator(String op1, String op2) {
		boolean operand1 = convertTroofToBoolean(op1);
		boolean operand2 = convertTroofToBoolean(op2);
	
		
		if(operand1 || operand2) return Token.WIN_TROOF_LITERAL;
		else return Token.FAIL_TROOF_LITERAL;
	}
	
	private String xorOperator(String op1, String op2) {
		boolean operand1 = convertTroofToBoolean(op1);
		boolean operand2 = convertTroofToBoolean(op2);
	
		
		if(operand1 ^ operand2) return Token.WIN_TROOF_LITERAL;
		else return Token.FAIL_TROOF_LITERAL;
	}
	
	//SEMANTICS FOR SWITCH CASE STATEMENT
	private void switchCaseExecute() {
		
		executingSwitchStatement = true;
		//checks if it has entered case
		boolean enteredCase = false;
		
		//set checking switch statement to false so that it would execute instructions
		checkingSwitchStatement = false;
		
		//get current queue size to get length of loop
		int queueSize = pQueue.size();
		
		//store original value of lineNumber
		int originalLineCheck = lineNumber;
		
		//change lineNumber back to start of switch case
		lineNumber -= queueSize;
		
		
		//execute instructions in pQueue
		for(int i=0; i<queueSize; i++) {
			//dequeues the process queue
			tokensPerLine = pQueue.remove();
			
			lineNumber++;
			
			//skip WTF
			if(i == 0) continue; 
			
			//detects OMG
			else if(tplLexeme(0).equals(Token.OMG)) {				
				//if has yet to enter a case, check condition
				if(!enteredCase) {
					
					/* compare IT and literal */
					
					Symbol it = getIT();
					
					if(tplLexeme(1).equals(Token.STRING_DELIMITER)) {
						if(it.getDataType().equals(Symbol.STRING)) {
							if(it.getValue().equals(tplLexeme(2))) enteredCase = true;
						}
					}else {
						if(it.getValue().equals(tplLexeme(1))) enteredCase = true;
					}					

				} else continue;
			
			//if GTFO, clear the process queue and exit switch statement
			} else if(tplLexeme(0).equals(Token.GTFO) && enteredCase) {
				pQueue.clear();
				executingSwitchStatement = false;
				break;
				
			//if OIC, clear the process queue and exit the switch statement	
			} else if(tplLexeme(0).equals(Token.OIC)){
				pQueue.clear();
				executingSwitchStatement = false;
				break;
			
			//default case
			} else if(tplLexeme(0).equals(Token.OMGWTF)){
				enteredCase = true;
				
			//execute instruction
			} else if(enteredCase) {
					checkSyntaxAndSemantics();
					if(!validSemantics) return;
				}
		}
		
		lineNumber = originalLineCheck;
	}
	
	private ArrayList <Integer> checkIfElse(Integer currLine) {
		int buffer = 0;
		int latestLine = 0;
		
		//checks if it has entered case
		boolean enteredCase = false;
		
		//checks if it entered mebbe
		boolean enteredMebbe = false;
		int mebbeCount = 0;
		ArrayList <Integer> skip = new ArrayList<Integer>();
		
		//store old IT value
		Symbol oldIT = getIT();
						
		//checks if IT value is WIN
		if(getIT().getValue().equals(Token.WIN_TROOF_LITERAL)) {
			
			//if WIN, look ahead of the code to check which lines to skip 
			for(int i = currLine+1; i<ifArray.size(); i++) {
				//store latest line
				latestLine = i;
				
				//if ORLY is encountered, increment buffer. 
				if(ifArray.get(i).get(0).getLexeme().equals(Token.O_RLY)) {
					//if mebbe has been previously encountered, increment count
					if(enteredMebbe) mebbeCount++;
					buffer++;
				}
				
				//if MEBBE is encountered, break to make sure next lines are skipped
				else if(ifArray.get(i).get(0).getLexeme().equals(Token.MEBBE)) break;
				
				//if NOWAI is encountered, decrement buff count meaning the pairs are still correct
				else if(ifArray.get(i).get(0).getLexeme().equals(Token.NO_WAI)) {
					if(enteredMebbe) mebbeCount++;
					if(!enteredMebbe && buffer==0) break;
					else buffer--;
				}
			}
			
			//look ahead of the code again starting from the line you stopped checking
			for(int i = latestLine+1; i<ifArray.size(); i++) {
				
				if(ifArray.get(i).get(0).getLexeme().equals(Token.O_RLY)) {
					buffer++;
				}
				
				else if(ifArray.get(i).get(0).getLexeme().equals(Token.OIC)) {
					if(buffer==0) break;
					else buffer--;
				}
				
				else if(ifArray.get(i).get(0).getLexeme().equals(Token.OIC)) break;
				
				//add lines to skip
				skip.add(i);
			}
			
		} 
		
		//checks if IT value is FAIL
		else {
			for(int i = currLine+1; i<ifArray.size(); i++) {
				
				//if ORLY is encountered, increment buffer
				if(ifArray.get(i).get(0).getLexeme().equals(Token.O_RLY)) {
					//if MEBBE previously encountered, increment mebbe count 
					//to make sure that the next mebbe will be skipped
					if(enteredMebbe) mebbeCount++;
					buffer++;
				} 
				
				//if MEBBE is encountered
				else if(ifArray.get(i).get(0).getLexeme().equals(Token.MEBBE)) {
						enteredMebbe = true;
						if(enteredMebbe) mebbeCount++;
						
						if(mebbeCount<2) { //if MEBBE count less than 2, meaning no other MEBBE entered a case
							tokensPerLine = ifArray.get(i);
							combiExecute(Token.IT, tokensPerLine);
							Symbol it = getIT();
							
							//if the evaluated expression equivalent to WIN
							if(it.getValue().equals(Token.WIN_TROOF_LITERAL)) {
								
								//if same, activate flag
								enteredCase = true;
								skip.add(i); //add line to lines to skip
								continue;
							} 
							
							//if the evaluate expression is FAIL
							else {
								
								//return back the old value of IT before evaluating MEBBE
								Symbol s = isASymbol(Token.IT);
								mebbeCount--;
								if(s != null) {	
									s.setValue(oldIT.getValue());
									s.setDataType(oldIT.getDataType());
								} else validSemantics = false;
							}
						} 
						
						//if MEBBE were previously encountered, skip the line
						else {
							skip.add(i);
							continue;
						}
						
				}
				
				//if NO WAI is encountered, buffer and no case should be entered in order to stop the execution of if else block
				else if(ifArray.get(i).get(0).getLexeme().equals(Token.NO_WAI)) {
					if(enteredMebbe) mebbeCount++;
					if(!enteredCase && buffer==0) break;
					else buffer--;
				}
				
				//if the line did not enter any case, skip the line
				if(!enteredCase) skip.add(i);
				
				//if there are more than one MEBBE, skip the line
				else if (mebbeCount > 1) skip.add(i);
				
			}
		}
		
		return skip;
	}
	
	//SEMANTICS FOR IF ELSE STATEMENT
	private void ifElseExecute() {
		ArrayList <Integer> skipList = new ArrayList<Integer>();
		ArrayList <Integer> newSkip = new ArrayList<Integer>();
		
		executingIfStatement = true;
		
		//set checking if then statement to false so that it would execute instructions
		checkingIfStatement = false;
		
		//get current queue size to get length of loop
		int arraySize = ifArray.size();
		
		//store original value of lineNumber
		int originalLineCheck = lineNumber;
		
		//change lineNumber back to start of switch case
		lineNumber -= arraySize;
		
		//check all lines to be executed inside the if-then statement
		for(int i = 0; i < arraySize; i++) {
			
			//if lines to be skipped are not empty, remove the current line and do not execute
			if(!skipList.isEmpty())
			if(skipList.contains(i)) {
				skipList.remove((Object) i);
				continue;
			}
			
			//get array index
			tokensPerLine = ifArray.get(i);
			lineNumber++;
			
			//if ORLY is encountered, enter function to look ahead of the code 
			if(tokensPerLine.get(0).getLexeme().equals(Token.O_RLY)) {
				newSkip = checkIfElse(i);
				//add new lines to skip to the previously stored list of skips
				newSkip.forEach(skipList::add);
			} 
			
			//when if-then keywords are encountered, just skip since they had been evaluated already
			 else if(tokensPerLine.get(0).getLexeme().equals(Token.MEBBE) ||
					 tokensPerLine.get(0).getLexeme().equals(Token.OIC) ||
					 tokensPerLine.get(0).getLexeme().equals(Token.NO_WAI) ||
					 tokensPerLine.get(0).getLexeme().equals(Token.YA_RLY)) {	
				 continue;
			} 
			 
			 //if lines of code are not keywords, execute its corresponding evaluation
			 else {
				checkSyntaxAndSemantics();
				if(!validSemantics) return;
			}
		}

		//set to false since the if statement is finished
		executingIfStatement = false;
		lineNumber = originalLineCheck;
	}
	
	//SYNTAX FOR CONCATENATION
		private boolean smooshSyntax(ArrayList<Token> smooshTokens) {
			Token currentToken;
			currentToken = smooshTokens.get(1);

			//token after SMOOSH must be a literal or a varident
			if(!(isALit(currentToken.getClassification()) || isAVar(currentToken.getClassification()) || Token.STRING_DELIMITER_CLASSIFIER.equals(currentToken.getClassification()))) {
				return false;
			}
			
			for(int i = 2; i < smooshTokens.size(); i++) {
				currentToken = smooshTokens.get(i);
				
				//if literal/varident
				if(isALit(currentToken.getClassification()) || isAVar(currentToken.getClassification())) {
					
					
					//if not last token
					if(i < smooshTokens.size()-1) {
						
						
						
						//if yarn, skip delimiter and must be followed by AN/MKAY keyword
						if(currentToken.getClassification().equals(Token.YARN_LITERAL_CLASSIFIER)) {
							if(i+2 < smooshTokens.size() && !(smooshTokens.get(i+2).getLexeme().equals(Token.AN) || smooshTokens.get(i+2).getLexeme().equals(Token.MKAY))) {
								return false;
							}
						}
						
						//next token must AN/MKAY
						else if(!(smooshTokens.get(i+1).getLexeme().equals(Token.AN) || smooshTokens.get(i+1).getLexeme().equals(Token.MKAY))) {
							return false;
						}
					}
				}else if(Token.STRING_DELIMITER_CLASSIFIER.equals(currentToken.getClassification())) {
					//skip if delimiter
					continue;
				}else if(currentToken.getClassification().equals(Token.AN_CLASSIFIER)){
					
					//must not be the last token
					if(i == smooshTokens.size()-1) return false;
					
					//must not be followed by MKAY/AN
					else if(smooshTokens.get(i+1).getClassification().equals(Token.AN_CLASSIFIER) || smooshTokens.get(i+1).getClassification().equals(Token.MKAY_CLASSIFIER)) return false;
				}else if(Token.MKAY_CLASSIFIER.equals(currentToken.getClassification())) {
					//must be last token
					if(i != smooshTokens.size()-1) return false;
				}else{
					return false;
				}
			}
			
			return true;
		}
		
		//SEMANTICS FOR SMOOSH
		private String smooshExecute(String dataHolder, ArrayList<Token> smooshTokens) {
			String concat = "";
			
			Symbol varHolder = isASymbol(dataHolder);
			if(varHolder == null) return null;
			
			for(Token tkn: smooshTokens) {
				if(isALit(tkn.getClassification())) {
					concat += tkn.getLexeme();
				}else if(isAVar(tkn.getClassification())) {
					Symbol var = isASymbol(tkn.getLexeme());
					if(var != null && !var.getDataType().equals(Symbol.UNINITIALIZED)) {
						concat += var.getValue();
					}else {
						validSemantics = false;
						return null;
					}
				}
			}
			
			
			
			varHolder.setValue(concat);
			varHolder.setDataType(Symbol.STRING);
			
			return concat;
		}
	
	//function to make deep copy of tokens per line
	private void storeTokensToQueue(String statement) {
		ArrayList<Token> lineTokens = new ArrayList<Token>();
		for(Token tkn: tokensPerLine) lineTokens.add(new Token(tkn.getLexeme(), tkn.getClassification()));
		
		if(statement == Token.WTF) pQueue.add(lineTokens);
		else if(statement == Token.O_RLY) ifArray.add(lineTokens);
	}
	
	//create error prompt
	public void createErrorPrompt(String errorPrompt) {
		outputDisplayText += "ERROR on Line Number "+lineNumber+": "+errorPrompt;
	}
	
	private void addToTokens() {
		ArrayList<Token> copyTokensPerLine = new ArrayList<Token>();
		for(Token tkn: tokensPerLine) 
			copyTokensPerLine.add(new Token(tkn.getLexeme(), tkn.getClassification()));
		
		tokens.add(copyTokensPerLine);
	}
	
	//check if instruction exists in pQueue
	private boolean inProcessQueue(String lexeme, String statement) {
		if(statement == Token.WTF) {
			for(ArrayList<Token> line: pQueue) {
				if(line.get(0).getLexeme().equals(lexeme)) return true;
			} 
		} else if (statement == Token.O_RLY) {
			for(ArrayList<Token> line: ifArray) {
				if(line.get(0).getLexeme().equals(lexeme)) return true;
			} 
		} 
		
		return false;
	}
		
	//function to get IT
	private Symbol getIT() {
		for(Symbol s: symbols) {
			if(s.getSymbol().equals(Token.IT)) {
				return s;
			}
		} return null;
	}
	
	//converts troof to its corresponding boolean equivalent
	private boolean convertTroofToBoolean(String literal) {
		if(literal.equals(Token.WIN_TROOF_LITERAL)) return true;
		else return false;
	}
	
	private boolean isADlmtr(String c) {
		return c.equals(Token.STRING_DELIMITER_CLASSIFIER);
	}
	
	private boolean isADigit(String c) {
		return c.equals(Token.NUMBAR_LITERAL_CLASSIFIER) || c.equals(Token.NUMBR_LITERAL_CLASSIFIER);
	}
	
	private boolean isALit(String c) {
		return Token.LITERALS.contains(c);
	}
	
	private boolean isAVar(String c) {
		return c.equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER) || c.equals(Token.IT_CLASSIFIER);
	}
	
	private int isAnExpr(String classification) {
		if(Token.ARITHMETIC_EXPRESSIONS.contains(classification)) return 1;
		else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(classification)) return 2;
		else if(Token.OTHER_BOOLEAN_EXPRESSIONS.contains(classification)) return 3;
		else if(Token.SMOOSH_CLASSIFIER.equals(classification)) return 4;
		else if(Token.COMPARISON_OPERATORS.contains(classification)) return 5;
		return 0;
	}

	private boolean isALitOrVar(String c) {
		return isALit(c) || isAVar(c);
	}	

	private boolean isALitOrDlmtrOrVar(String c) {
		return isALit(c) || isADlmtr(c) || isAVar(c);
	}
	
	private boolean isALitOrExpr(String c) {
		return isALit(c) || isAnExpr(c)!=0;
	}	
	
	private Symbol isASymbol(String var) {
		for(Symbol s: symbols) {
			if(s.getSymbol().equals(var))
				return s;
		}
		return null;
	}
	
	private String tplLexeme(int index) {
		return tokensPerLine.get(index).getLexeme();
	}
	
	private String tplClass(int index) {
		return tokensPerLine.get(index).getClassification();
	}
	
	private boolean tplSize(int size) {
		return tokensPerLine.size()==size;
	}
	
	//FUNCTIONS FOR THE LEXICAL ANALYSIS
	private int checkLexeme(String line) {		
		int currPos=0, commentDetected=0;
	    char currChar;
	    boolean acceptedLexeme=false;
		String classification;
		
		lineNumber++;
		
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

			System.out.println(currentLexeme+"-");
			//if the end of the line is reached or the next char is a space, check if the current lexeme is a token
			if(currPos==line.length() || isASpace(line.charAt(currPos))) {
				boolean endsWithExclamation=false;
				if(!currentLexeme.equals(Token.EXCLAMATION_POINT) && currentLexeme.endsWith("!") && currPos==line.length()) {
					endsWithExclamation=true;
					currentLexeme = currentLexeme.substring(0, currentLexeme.length()-1);
				}
				classification = isAValidLexeme(currentLexeme);
				//if it is, then add it to the list of tokens
				if(classification != null) {
					acceptedLexeme = true;
					
					//if a string is detected, add the start quote, string literal, and end quote individually
					if(classification.equals(Token.YARN_LITERAL_CLASSIFIER)) {
						if(!currentLexeme.endsWith(":\"") ||
							(currentLexeme.endsWith(":\"") && currentLexeme.charAt(currentLexeme.length()-3)==':')) {
							//matcher to capture group
							Matcher m = Token.YARN_LITERAL.matcher(currentLexeme);
							
							if(m.find()) {
								String finalString = finalString(m.group(2));
						
								if(finalString!=null) {
									tokensPerLine.add(new Token(m.group(1), Token.STRING_DELIMITER_CLASSIFIER));
									tokensPerLine.add(new Token(finalString, classification));
									tokensPerLine.add(new Token(m.group(3), Token.STRING_DELIMITER_CLASSIFIER));
									
									if(endsWithExclamation) {
										tokensPerLine.add(new Token(Token.EXCLAMATION_POINT, Token.EXCLAMATION_POINT_CLASSIFIER));
									}
									currentLexeme ="";
								} else {
									validLexical = false;
									break;
								}
							} 
						} else {
							acceptedLexeme = false;
						}
					
					//if a comment is detected, ignore whatever comes after it
					//0 - not a comment; 1 - one line comment (BTW); 2 - multiline comment (OBTW)
					} else if((commentDetected = isAComment(currentLexeme)) != 0) {
						//case 1: BTW (skip the current line)
						if(commentDetected == 1) {
							tokensPerLine.add(new Token(currentLexeme,classification));
							currentLexeme = "";
						//case 2: OBTW .. TLDR (must have their own lines)
						} else if(tokensPerLine.size() == 0) {
							tokensPerLine.add(new Token(currentLexeme,classification));
							currentLexeme = "";
							String commentEnder;
							
							//ignore lines until a TLDR is detected
							do {
								commentEnder="";
								lineNumber++;
								line = lines[lineNumber];
								String[] lexemes = line.split(" ");
								
								
								for(int i=0;i<lexemes.length;i++) {
									if(!lexemes[i].equals("")) commentEnder+=lexemes[i];
								}	
							} while(!commentEnder.equals(Token.TLDR));		
						}
						break;
					
					//if not a string or a comment, add as is
					} else{
						tokensPerLine.add(new Token(currentLexeme,classification));
						
						if(endsWithExclamation) {
							tokensPerLine.add(new Token(Token.EXCLAMATION_POINT, Token.EXCLAMATION_POINT_CLASSIFIER));
						}
						currentLexeme ="";
					}						
				}
			}	
		}
		
		//ERROR DETECTION
		//there's an invalid lexeme, but process again because a variable identifier is detected as a possible keyword
		if(!isEmpty(currentLexeme) && isAVariable() && status!=2) {
			readBack=true;
			return 2;
		//there's an invalid lexeme, stop iteration for getting lexemes
		} else if(!isEmpty(currentLexeme)) {
			validLexical = false;
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
	
	//get classification
	private String getClass(String currentLexeme) {
		
		if(Token.NUMBR_LITERAL.matcher(currentLexeme).matches()) return Token.NUMBR_LITERAL_CLASSIFIER;
		if(Token.NUMBAR_LITERAL.matcher(currentLexeme).matches()) return Token.NUMBAR_LITERAL_CLASSIFIER;
		if(Token.YARN_LITERAL.matcher(currentLexeme).matches()) return Token.YARN_LITERAL_CLASSIFIER;
		if(currentLexeme.equals(Token.WIN_TROOF_LITERAL) || currentLexeme.equals(Token.FAIL_TROOF_LITERAL)) return Token.TROOF_LITERAL_CLASSIFIER;
		if(currentLexeme.equals(Token.NOOB_TYPE_LITERAL)) return Token.NOOB_TYPE_LITERAL;
		return Token.YARN_LITERAL_CLASSIFIER;
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
		String[] tkn;

		System.out.println("1");
		if(tokensPerLine.size()>0) {
			if(tplLexeme(0).equals(Token.I_HAS_A)) {
				if(Character.isLetter(currentLexeme.charAt(0))) {
					return true;
				}
				else return false;
			} else if(tplLexeme(0).equals(Token.VISIBLE)) {
				tkn = currentLexeme.split(" ");
				if(isASymbol(tkn[0])!=null) return true;
				else return false;
			}
			else return false;		
		} 

		if(currentLexeme.contains(" R ")) return true;
	
		return false;		
	}
	
	private String getDataType(String value) {
		String classification = getClass(value);
		
		switch(classification) {
			case Token.NUMBR_LITERAL_CLASSIFIER:
				return Symbol.INTEGER;
			case Token.NUMBAR_LITERAL_CLASSIFIER:
				return Symbol.FLOAT;
			case Token.TROOF_LITERAL_CLASSIFIER:
				return Symbol.BOOLEAN;
			case Token.YARN_LITERAL_CLASSIFIER:
				return Symbol.STRING;
			default:
				return Symbol.UNINITIALIZED;
		}
	}
	
	public String finalString(String currLexeme) {
		char currChar;
		int i=0;
		
		while(i<currLexeme.length()-1) {
			currChar = currLexeme.charAt(i);
			i++;
			
			if(currChar==':') {
				currChar = currLexeme.charAt(i);
				if(i!=currLexeme.length() && correctEscapeCharacter(currChar)) i++;				
				else return null;
			}
		}
		
		if(currLexeme.contains(":)")) {
			Matcher newline = Token.NEWLINE.matcher(currLexeme);       
			if(newline.find()) currLexeme = currLexeme.replace(newline.group(),"\n");
		}
		
		if(currLexeme.contains(":>")) {
			Matcher tab = Token.TAB.matcher(currLexeme);       
			if(tab.find()) currLexeme = currLexeme.replace(tab.group(),"\t");
		}
		if(currLexeme.contains(":o")) {
			Matcher bell = Token.BELL.matcher(currLexeme);       
			if(bell.find()) currLexeme = currLexeme.replace(bell.group(),"g");
		}
		if(currLexeme.contains(":\"")) {
			Matcher double_quote = Token.DOUBLE_QUOTE.matcher(currLexeme);       
			if(double_quote.find()) currLexeme = currLexeme.replace(double_quote.group(),"\"");
		}
		if(currLexeme.contains("::")) {
			Matcher colon = Token.COLON.matcher(currLexeme);       
			if(colon.find()) currLexeme = currLexeme.replace(colon.group(),":");
		}
     
		return currLexeme;
	}
	
	public boolean correctEscapeCharacter(char c) {
		return c==')' || c=='>' ||  c=='o' || c=='\"' || c==':';                                 
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
	
	//check if there are no errors in the file
	public boolean endsWithKTHXBYE() {
		String l;
		
		for(int i=tokens.size()-1;i>=0;i--) {
			ArrayList<Token> tokensPerLine = tokens.get(i);
			
			for(int j=tokensPerLine.size()-1;j>=0;j--) {
				l = tokensPerLine.get(j).getLexeme();	

				if(isAComment(l)!=0 || l.equals(Token.TLDR)) continue;
				else {
					if(l.equals(Token.KTHXBYE)) return true;
					else {				
						validSyntax = false;
						return false;
					}
				}
			}
		}		
		return false;
	}
	
	public boolean startsWithHAI() {
		String l;

		for(ArrayList<Token> tokensPerLine: tokens) {
			for(Token token: tokensPerLine) {
				l = token.getLexeme();

				if(isAComment(l)!=0 || l.equals(Token.TLDR)) continue;
				else if(l.equals(Token.HAI)) return true;
				else {
					lineNumber = 1;
					validSyntax = false;
					return false;
				}
			}
		}
		return false;
	}
	
	//FUNCTIONS FOR FILE READING

	private void openFile() {
		//action for "select LOLCODE file" button
        fileButton.setOnAction(e -> {
        	resetAnalyzer();
        	codeDisplay.clear();
        	
        	fileChooser.setInitialDirectory(new File(currentPath));
        	file = fileChooser.showOpenDialog(stage);
        	
        	//no file chosen
            if(file == null) {
            	outputDisplay.setText("No file selected");
            	validFile = false;
            } else { //file chosen
            	//check if file extension ends with .lol
            	if(file.getAbsolutePath().matches(".*.lol$")) {
            		validFile = true;
            		readFile();
            	}
            	else {
                	outputDisplay.setText("Invalid file!");
            		validFile = false;
            	}
            }
        });
	}
	
	private void readFile() {
		String fileWithLines = "";
		
		if(validFile) {
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
				outputDisplay.setText("File not found!");
			}
		}
	}
	
	private void resetAnalyzer() {
		//clear all values
		fileString = "";
		outputDisplayText = "";
		lineNumber = 0;		
		orlyCount =0;
		validLexical = true;
		validSyntax = true;
		validSemantics = true;
		conditionalStatement = false;
		switchStatement = false;
		tokens.clear();
		tokensPerLine.clear();
		symbols.clear();
		opTokens.clear();
		operation.clear();
		pQueue.clear();
		ifArray.clear();
		outputDisplay.clear();
		clearTable();
		passIndicator.setImage(neutralImg);
		titleImage.setImage(titleImg);
		symbols.add(new Symbol(Token.IT,Token.NOOB_TYPE_LITERAL, Symbol.UNINITIALIZED));
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
        	lexemeTableView.setLayoutY(100);
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
        	symbolTableView.setLayoutY(100);
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
    	for(ArrayList<Token> tokensPerLine: tokens) {
        	for(Token token: tokensPerLine) {
        		lexemeTableView.getItems().add(token);        		
        	}
    	}
    	for(Symbol symbol: symbols) symbolTableView.getItems().add(symbol);
    }
    
    private void clearTable() {
    	//clear table
		for(int i=0; i<lexemeTableView.getItems().size(); i++) lexemeTableView.getItems().clear();
		for(int i=0; i<symbolTableView.getItems().size(); i++) symbolTableView.getItems().clear();
    }
    
    private void showError() {  	
    	//update GUI to show fail
    	passIndicator.setImage(cryingImg);
		outputDisplay.setText(outputDisplayText);
		
		//prompt error dialog
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText("[!] Errors found in your code.");
		alert.setTitle("Error Dialog");
		alert.setHeaderText(null);
		alert.show();
    }
    
    private void showPass() {
		clearTable();
		populateTable();		
		outputDisplay.setText(outputDisplayText);
		passIndicator.setImage(happyImg);
    }
    
	private void generateLexemes() {
		executeButton.setOnAction(e -> {
			if(file!=null) {
				if(validFile) {
					readFile();
					interpretFile();

					if(startsWithHAI() && endsWithKTHXBYE() && validLexical && validSyntax && validSemantics) showPass();
					else showError();
				}
			} else {
				//prompt error dialog
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setContentText("Please select a LOLCODE file");
				alert.setTitle("Error Dialog");
				alert.setHeaderText(null);
				alert.show();
			}
			
        });
	}

}
