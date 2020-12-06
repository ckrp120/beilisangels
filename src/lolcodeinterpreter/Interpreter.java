package lolcodeinterpreter;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
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
	private ImageView passIndicator = new ImageView(new Image("imgs/neutral.gif", 150, 150, true,true));
	private ImageView lexicalIndicator = new ImageView();
	private ImageView syntaxIndicator = new ImageView();
	private ImageView semanticIndicator = new ImageView();
	private ImageView titleImage = new ImageView(new Image("imgs/title.png", 1000, 90, true,true));
	private Image titleImg = new Image("imgs/title.png", 1000, 90, true,true);
	private Image happyImg = new Image("imgs/laughing.gif", 150, 150, true,true);
	private Image neutralImg = new Image("imgs/neutral.gif", 150, 150, true,true);
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
	
    //FOR LEXICAL/SYNTAX/SEMANTIC ANALYSIS
    private String[] lines;
    private String currentLexeme,dialogText;
    private int wordCheck,lineCheck,status;
    private boolean validLexeme,validSyntax,validSemantics,readBack,conditionalStatement,switchStatement;
    private ArrayList<Token> tokens = new ArrayList<Token>();
    private ArrayList<Token> tokensPerLine = new ArrayList<Token>();
    private ArrayList<Symbol> symbols = new ArrayList<Symbol>();
    private ArrayList<Token> opTokens = new ArrayList<Token>();

    //process queue for switch
    private Queue<ArrayList<Token>> pQueue = new LinkedList<>();
    private boolean checkingSwitchStatement = false;
    private boolean executingSwitchStatement = false;
    
    //process queue for if-then
    private Queue<ArrayList<Token>> ifQueue = new LinkedList<>();
    private boolean checkingIfStatement = false;
    private boolean executingIfStatement = false;
	
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
        
        //set preferences for imageview of lexical analysis indicator
        this.lexicalIndicator.setLayoutX(1270);
        this.lexicalIndicator.setLayoutY(810);
        
        //set preferences for imageview of syntax analysis indicator
        this.syntaxIndicator.setLayoutX(1270);
        this.syntaxIndicator.setLayoutY(850);
        
        //set preferences for imageview of semantic analysis indicator
        this.semanticIndicator.setLayoutX(1270);
        this.semanticIndicator.setLayoutY(890);
        
        //set preferences for imageview of title
        this.titleImage.setLayoutX(530);
        this.titleImage.setLayoutY(10);
        
        //call to functions
		openFile();	
		generateLexemes();
		createTable("lexemes");
		createTable("symbols");
		
		root.getChildren().addAll(canvas, codeDisplay, fileButton, executeButton, outputDisplay, passIndicator, lexicalIndicator, syntaxIndicator, semanticIndicator, titleImage);
		root.getStylesheets().add(getClass().getResource("lolcodeinterpreter.css").toString());
		this.stage = stage;
		this.stage.getIcons().add(new Image(("imgs/title.png")));
		this.stage.setTitle("LOLCODE INTERPRETER");
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

			if(status == 1) {	//LEXICAL ERROR
				validSyntax = false;
				validSemantics = false;
				break;
			}	
			
			//if the current line has a comment, ignore the BTW token
			if(!tokensPerLine.isEmpty() && tokensPerLine.get(tokensPerLine.size()-1).getLexeme().equals(Token.BTW))
				tokensPerLine.remove(tokensPerLine.size()-1);
			
			//if(validLexeme) System.out.println("Line "+lineCheck+": passed lexical");
			if(!tokensPerLine.isEmpty()) {
				checkSyntaxAndSemantics();

				//if(validSyntax) System.out.println("Line "+lineCheck+": passed syntax");
				//if(validSemantics) System.out.println("Line "+lineCheck+": passed semantics");

				if(!validSyntax || !validSemantics) {
		    		if(!validSyntax) validSemantics = false; //SYNTAX ERROR
		    		break;
		    	}
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
				if(tokensPerLine.get(0).getClassification().equals(Token.OMG_CLASSIFIER) && tokensPerLine.size() == 2) {
					if(Token.LITERALS.contains(tokensPerLine.get(1).getClassification()))
						storeTokensToQueue(Token.WTF);
					else validSyntax = false;
				}			
				else validSyntax = false;
			}
			
			//OMG
			else if(tokensPerLine.get(0).getLexeme().equals(Token.OMG)) {
				//check if the line next to OMG is a literal
				if(Token.LITERALS.contains(tokensPerLine.get(1).getClassification()) && tokensPerLine.size() == 2)
					storeTokensToQueue(Token.WTF);
				else validSyntax = false;
			}
			
			//PRINT = VISIBLE
			else if(tokensPerLine.get(0).getLexeme().equals(Token.VISIBLE)) {
				if(printSyntax()) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else printExecute();
				}
				else validSyntax = false;
			}
			
			//ACCEPT = GIMMEH
			else if(tokensPerLine.get(0).getLexeme().equals(Token.GIMMEH)) {
				if(acceptSyntax()) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else acceptExecute();
					
				}
				else validSyntax = false;
			}
			
			//VARIABLE DECLARATION = I HAS A
			else if(tokensPerLine.get(0).getLexeme().equals(Token.I_HAS_A)) {
				String literalClassification = varDeclarationSyntax();
				if(literalClassification != null) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else varDeclarationExecute(literalClassification);
				}
				else validSyntax = false;				
			}
	
			//ASSIGNMENT STATEMENT = R
			else if(tokensPerLine.get(1).getLexeme().equals(Token.R)) {
				String literalClassification = varAssignmentSyntax();
				if(literalClassification != null) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else varAssignmentExecute(literalClassification);
				}
				else validSyntax = false;
			}
			
			//ARITHMETIC OPERATIONS
			else if(Token.ARITHMETIC_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification())) {
				if(arithmeticSyntax(tokensPerLine)) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else arithmeticExecute(Token.IT,tokensPerLine);
				}
				else validSyntax = false;
			}	
			
			//COMPARISON OPERATORS
			else if(Token.COMPARISON_OPERATORS.contains(tokensPerLine.get(0).getClassification()) ) {
				if(comparisonSyntax(tokensPerLine)) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else comparisonExecute(Token.IT,tokensPerLine);
				}
				else validSyntax = false;
			}
	
			//BOOLEAN OPERATIONS
			else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification()) || 
					Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tokensPerLine.get(0).getClassification()) || Token.COMPARISON_OPERATORS.contains(tokensPerLine.get(0).getClassification())) {
				if(combiSyntax(tokensPerLine)) {
					//System.out.println("Passed!");
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else combiExecute(Token.IT, tokensPerLine);
				}
				else validSyntax = false;
			}
			
		} else {
			switch(tokensPerLine.get(0).getClassification()) {
				case Token.HAI_CLASSIFIER:
					break;
				case Token.KTHXBYE_CLASSIFIER:
					if(conditionalStatement==true) {
						for(int i=0; i<tokens.size(); i++) {
							if(!tokens.get(i).getLexeme().equals(Token.OIC)) {
								validSyntax = false;
							} else {
								validSyntax = true;
								break;
							}
						}
					} else if(switchStatement==true) {
						for(int i=0; i<tokens.size(); i++) {
							if(!tokens.get(i).getLexeme().equals(Token.OIC)) {
								validSyntax = false;
							} else {
								validSyntax = true;
								break;
							}
						}
					} else {
						validSyntax = true;
					}
					break;
				case Token.OBTW_CLASSIFIER:
					break;	
				case Token.TLDR_CLASSIFIER:
					break;
				case Token.WTF_CLASSIFIER:
					checkingSwitchStatement = true;
					switchStatement = true;
					storeTokensToQueue(Token.WTF);
					break;
				case Token.OIC_CLASSIFIER:
					//check if WTF and OMGs are already in the switch statement
					if((inProcessQueue(Token.WTF, Token.WTF) && inProcessQueue(Token.OMG, Token.WTF) && checkingSwitchStatement) || executingSwitchStatement) {
						storeTokensToQueue(Token.WTF);
						switchCaseExecute();	
					}
					//check if ORLY, YA RLY and NO WAI are already in the if-then statement
					else if((inProcessQueue(Token.O_RLY, Token.O_RLY) && inProcessQueue(Token.YA_RLY, Token.O_RLY) && inProcessQueue(Token.NO_WAI, Token.O_RLY) && checkingIfStatement) || executingIfStatement) {
						storeTokensToQueue(Token.O_RLY);
						ifElseExecute();	
					} else validSyntax = false;
					break;
				case Token.GTFO_CLASSIFIER:
					storeTokensToQueue(Token.WTF);
					break;
				case Token.OMGWTF_CLASSIFIER:
					storeTokensToQueue(Token.WTF);
					break;
				case Token.O_RLY_CLASSIFIER:
					checkingIfStatement = true;
					conditionalStatement = true;
					storeTokensToQueue(Token.O_RLY);
					break;
				case Token.YA_RLY_CLASSIFIER:
					if(checkingIfStatement && ifQueue.size() == 1) storeTokensToQueue(Token.O_RLY);
					else validSyntax=false;
					break;
				case Token.NO_WAI_CLASSIFIER:
					if(checkingIfStatement && ifQueue.size() > 2) {
						Iterator<ArrayList<Token>> iterator = ifQueue.iterator(); 
						if(iterator.next().get(0).getLexeme().equals(Token.O_RLY)) {
							if(iterator.next().get(0).getLexeme().equals(Token.YA_RLY)) storeTokensToQueue(Token.O_RLY);
							else validSyntax=false;
						} else validSyntax=false;
					} else validSyntax=false;
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
	
	//SEMANTICS FOR PRINT = VISIBLE
	private void printExecute() {
		Token tkn;
		int i=1,operation;
		boolean appendNewLine=true;
		String itValue = symbols.get(0).getValue();
		String visibleValue = "";
		
		while(i<tokensPerLine.size()) {
			tkn = tokensPerLine.get(i);
			
			//case 1: varident/it
			if(isAVarident(tkn.getClassification())) {
				
				//check if the varident is in the symbols
				Symbol s = isASymbol(tkn.getLexeme());
				
				if(s != null && !s.getDataType().equals(Symbol.UNINITIALIZED)) {
					visibleValue += s.getValue();	
				} else {
					validSemantics = false;
					break;
				}
			} 
			
			//case 2: expr
			else if((operation = isAnExpr(tkn.getClassification())) != 0) {
				opTokens.clear();
				
				//copy the tokens starting from the operation until the end of the expression
				String currToken,nextToken;
				int string=0;
				boolean stop = false;
				
				do {
					System.out.print("i: "+i);
					currToken = tokensPerLine.get(i).getClassification();
					System.out.println(" "+tokensPerLine.get(i).getLexeme());
					opTokens.add(tokensPerLine.get(i));
					if(currToken.equals(Token.STRING_DELIMITER_CLASSIFIER)) string++;
					
					if(isAVarident(currToken) || Token.LITERALS.contains(currToken) || string==2) {
						string = 0;
						if(i+1 != tokensPerLine.size()) {
							nextToken = tokensPerLine.get(i+1).getLexeme();
							if(!nextToken.equals(Token.AN)) stop = true;							
						}
					}

					i++;
				} while(i<tokensPerLine.size() && !stop);
				i--;

				//case 2.1: arith op
				if(operation == 1) {
					//check if the arithop has a valid syntax
					if(arithmeticSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else {
							arithmeticExecute(Token.IT,opTokens);
							visibleValue += symbols.get(0).getValue();
							symbols.get(0).setValue(itValue);
						}
					}
					else {
						validSyntax = false;
						break;
					}
				}
				
				//case 2.2: bool op
				else if(operation == 2 || operation == 3) {	
					//check if the boolop has a valid syntax
					if(booleanSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else {
							booleanExecute(Token.IT,opTokens);
							visibleValue += symbols.get(0).getValue();
							symbols.get(0).setValue(itValue);
						}
					}
					else {
						validSyntax = false;
						break;
					}
				}	
				
				//case 2.3: comp op
				else {	
					//check if the compop has a valid syntax
					if(comparisonSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else {
							comparisonExecute(Token.IT,opTokens);
							visibleValue += symbols.get(0).getValue();
							symbols.get(0).setValue(itValue);
						}
					}
					else {
						validSyntax = false;
						break;
					}
				}
			}
			
			//case 3: literals
			else if(Token.LITERALS.contains(tkn.getClassification())) {
				visibleValue += tkn.getLexeme();
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
		
		outputDisplayText += visibleValue;
		symbols.get(0).setValue(visibleValue);

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
		Symbol s;
		
		//check if the varident is in the symbols
		if((s = isASymbol(tokensPerLine.get(1).getLexeme())) != null) {
			outputDisplay.setText(outputDisplayText);
			//get user input
	        getInput(s,dialogText);
		} else validSemantics = false;
	}
	
	private void getInput(Symbol s,String dialogText) {
        TextInputDialog inputDialog = new TextInputDialog();

        //set the title,header text, and context text of input dialog
        inputDialog.setTitle("USER INPUT");
        inputDialog.setHeaderText(null);
        inputDialog.setContentText(dialogText);
        
        Optional<String> input = inputDialog.showAndWait();
               
        //if user entered an input, set the varident's value to the input
        if(input.isPresent()) {
            input.ifPresent(value -> {
            	s.setValue(value);
            	
            	//automatically typecast based on input
            	s.setDataType(getDataType(value));
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
		int operation;
		
		//check if the varident is already declared before
		if(isASymbol(identifier) != null) validSemantics = false;
		//case 1: I HAS A var
		else if(tokensPerLine.size() == 2) {
			symbols.add(new Symbol(identifier,Token.NOOB_TYPE_LITERAL, Symbol.UNINITIALIZED));	
		//case 2: I HAS A var ITZ var/lit/expr
		} else if(tokensPerLine.get(2).getClassification().equals(Token.ITZ_CLASSIFIER)) {
			//case 2.1: varident
			if(isAVarident(litClass)) {	
				Symbol s = isASymbol(tokensPerLine.get(3).getLexeme());
				
				if(s != null && !s.getDataType().equals(Symbol.UNINITIALIZED))
					symbols.add(new Symbol(identifier,s.getValue(), s.getDataType()));
				else validSemantics = false;
			}
			
			//case 2.2: expr
			else if((operation = isAnExpr(litClass)) != 0) {
				symbols.add(new Symbol(identifier,"", Symbol.UNINITIALIZED));

				ArrayList<Token> opToken = new ArrayList<Token>();
				
				opTokens.clear();

				//copy the tokens starting from the operation
				for(int i=3;i<tokensPerLine.size();i++)
					opToken.add(tokensPerLine.get(i));

				//case 2.2.1: arith op
				if(operation == 1) {
					//check if the arithop has a valid syntax
					if(arithmeticSyntax(opToken)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else arithmeticExecute(identifier,opToken);
					}
					else validSyntax = false;
					
				}

				//case 2.2.2: bool op
				else if(operation == 2 || operation == 3) {	
					//check if the boolop has a valid syntax
					if(booleanSyntax(opToken)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else booleanExecute(identifier,opToken);
					}
					else validSyntax = false;
				}	

				//case 2.2.3: comp op
				else {	
					//check if the compop has a valid syntax
					if(comparisonSyntax(opToken)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else comparisonExecute(identifier,opToken);
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
				symbols.add(new Symbol(identifier, tokensPerLine.get(3).getLexeme(), Symbol.FLOAT));
			else if(litClass.equals(Token.NUMBR_LITERAL_CLASSIFIER))
				symbols.add(new Symbol(identifier, tokensPerLine.get(3).getLexeme(), Symbol.INTEGER));
			else if(litClass.equals(Token.TROOF_LITERAL_CLASSIFIER))
				symbols.add(new Symbol(identifier, tokensPerLine.get(3).getLexeme(), Symbol.BOOLEAN));
		}
	}
		
	//SYNTAX FOR ASSIGNMENT STATEMENT = R
	private String varAssignmentSyntax() {
		//check if it assigns to a varident
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
		Symbol s = isASymbol(tokensPerLine.get(0).getLexeme());
		Symbol sv = isASymbol(tokensPerLine.get(2).getLexeme());
		int operation;
		
		//get the symbol, then set the value
		if(s != null) {							
			//case 1: varident
			if(isAVarident(litClass)) {							
				if(sv != null && !sv.getDataType().equals(Symbol.UNINITIALIZED)) {
					s.setValue(sv.getValue());
					s.setDataType(sv.getDataType());
				} else validSemantics = false;
			}
			
			//case 2.2: expr
			if((operation = isAnExpr(litClass)) != 0) {
				opTokens.clear();
				
				//copy the tokens starting from the operation
				for(int i=2;i<tokensPerLine.size();i++)
					opTokens.add(tokensPerLine.get(i));
				
				//case 2.2.1: arith op
				if(operation == 1) {
					//check if the arithop has a valid syntax
					if(arithmeticSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else arithmeticExecute(tokensPerLine.get(0).getLexeme(),opTokens);
					}
					else validSyntax = false;
				}
				
				//case 2.2.2: bool op
				else if(operation == 2 || operation == 3) {	
					//check if the boolop has a valid syntax
					if(booleanSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else booleanExecute(tokensPerLine.get(0).getLexeme(),opTokens);
					}
					else validSyntax = false;
				}	
				
				//case 2.2.3: comp op
				else {	
					//check if the compop has a valid syntax
					if(comparisonSyntax(opTokens)) {
						if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
						else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
						else comparisonExecute(tokensPerLine.get(0).getLexeme(),opTokens);
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
				s.setValue(tokensPerLine.get(2).getLexeme());
				s.setDataType(Symbol.FLOAT);
			}
			
			else if(litClass.equals(Token.NUMBR_LITERAL_CLASSIFIER)) {
				s.setValue(tokensPerLine.get(2).getLexeme());
				s.setDataType(Symbol.INTEGER);
			}
			
			else if(litClass.equals(Token.TROOF_LITERAL_CLASSIFIER)) {
				s.setValue(tokensPerLine.get(2).getLexeme());
				s.setDataType(Symbol.BOOLEAN);
			}
		} else validSemantics = false;
	}
	
	//SYNTAX FOR ARITHMETIC OPERATIONS
	private boolean arithmeticSyntax(ArrayList<Token> opTokens) {
		Stack<Token> checker = new Stack<Token>();
		int exprCount = 0, opCount = 0, anCount = 0;
		boolean startingPopped = false;
		
		for(int i=0; i<opTokens.size(); i++) {
			//implies that another operation has started in the same line
			if(startingPopped) {
				if(i+1==opTokens.size() && opTokens.get(i).getLexeme().equals(Token.STRING_DELIMITER)) return true;
				else return false; 
			}
			
			//add keywords to stack
			if(Token.ARITHMETIC_EXPRESSIONS.contains(opTokens.get(i).getClassification())) {
				checker.add(opTokens.get(i));
				
				//if not starting arithmetic expression, increment exprCount (meaning it is a nested expression)
				if(i > 0) exprCount++;
			} else if(opTokens.get(i).getLexeme().equals(Token.AN)) {
			//if an is encountered, add to an count
				anCount++;
			
			//if a varident or literal is detected, add to an operand count
			} else if(isADigit(opTokens.get(i).getClassification()) || isAVarident(opTokens.get(i).getClassification()) ||
						Token.LITERALS.contains(opTokens.get(i).getClassification())) {
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
	private Number arithmeticExecute(String dataHolder,ArrayList<Token> opTokens) {
		Stack<Number> operation = new Stack<Number>();
			
		//since operations are in prefix, reverse the tokens 
		Collections.reverse(opTokens);
		
		for(Token tkn: opTokens) {
			//case 1: numbar
			if(tkn.getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER)) {
				operation.push(parseFloat(tkn));
			//case 2: numbr
			} else if(tkn.getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER)) {
				operation.push(parseInt(tkn));
			//case 3: varident
			} else if(tkn.getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {				
				Symbol s = isASymbol(tkn.getLexeme());
				
				if(s != null && !s.getDataType().equals(Symbol.UNINITIALIZED)) {
					System.out.println(s.getSymbol()+" Data Type: "+s.getDataType());
									
					//integer detected
					if(s.getDataType().equals(Symbol.INTEGER)) operation.push(parseInt(symbols.indexOf(s)));
					
					//float detected
					else if(s.getDataType().equals(Symbol.FLOAT)) operation.push(parseFloat(symbols.indexOf(s)));
					
					//string detected
					else if(s.getDataType().equals(Symbol.STRING)) {
						//check its value's data type
						String classification = isAValidLexeme(symbols.get(symbols.indexOf(s)).getValue());
						
						//string is a numbar
						if(classification.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) operation.push(parseFloat(symbols.indexOf(s)));
						
						//string is a numbr
						else if(classification.equals(Token.NUMBR_LITERAL_CLASSIFIER)) operation.push(parseInt(symbols.indexOf(s)));
					}
				} else {
					validSemantics = false;
					return null;
				}
				
			
			//case 4: IT
			}else if(tkn.getLexeme().equals(Token.IT)) {
				Symbol it = getIT();
				//integer detected
				if(it.getDataType().equals(Symbol.INTEGER)) operation.push(Integer.parseInt(it.getValue()));
				
				//float detected
				else if(it.getDataType().equals(Symbol.FLOAT)) operation.push(Float.parseFloat(it.getValue()));
				
				//string detected
				else if(it.getDataType().equals(Symbol.STRING)) {
					//check its value's data type
					String classification = isAValidLexeme(it.getValue());
					
					//string is a numbar
					if(classification.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) operation.push(Float.parseFloat(it.getValue()));
					
					//string is a numbr
					else if(classification.equals(Token.NUMBR_LITERAL_CLASSIFIER)) operation.push(Integer.parseInt(it.getValue()));
					
					else {
						validSemantics = false;
						return null;
					}

				}else {
					
					//invalid dataType
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
			} else if(!tkn.getLexeme().equals(Token.AN)){
				validSemantics = false;
				return null;
			}
		}

		//last item on the stack is the result
		Number num = operation.pop();
		
		//set the value of the varident to the result
		Symbol s = isASymbol(dataHolder);
		if(s != null) {
			s.setValue(num.toString());
			
			if(num instanceof Float) s.setDataType(Symbol.FLOAT);
			else if(num instanceof Integer) s.setDataType(Symbol.INTEGER);
			return num;
		}	
		else {
			validSemantics = false;
			return null;
		} 
	}
	
	//SYNTAX FOR BOOLEAN OPERATIONS
	private boolean booleanSyntax(ArrayList<Token> booleanTokens) {
		Stack<Token> checker = new Stack<Token>();
		Token currentToken;
		int anCount = 0, popCount = 0;
		
		//since prefix, read the line in reverse
		Collections.reverse(booleanTokens);
		
		for(int i=0; i <booleanTokens.size(); i++) {
			currentToken = booleanTokens.get(i);
			
			//if AN is detected, it must not be the last or starting token, and must not be followed by an AN
			if(currentToken.getLexeme().equals(Token.AN)) {
				
				//AN is starting/last token
				if(i == 0 || i == (booleanTokens.size()-1)) 
					return false;
				
				//followed by AN
				else if(booleanTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				else anCount++;
			}else if(currentToken.getLexeme().equals(Token.NOT)) {
							
				//NOT is last token
				if(i == 0) return false;
				
				//followed by AN
				else if(booleanTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				else continue;
			}else if(currentToken.getLexeme().equals(Token.ALL_OF) || currentToken.getLexeme().equals(Token.ANY_OF)) {
				
				//if it starts with ANY OF/ALL OF then num of stack is ignored since these are infinite arity operations
				if(i == booleanTokens.size()-1 && !booleanTokens.get(i-1).getLexeme().equals(Token.AN))
					return true; 
				
				//operation cannot be nested
				else return false;
			}else if(currentToken.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER) | isAVarident(currentToken.getClassification())) {
				//if last token, it must be preceeded with an AN or NOT
				if(i == 0) {
					if(!(booleanTokens.get(i+1).getLexeme().equals(Token.AN) || booleanTokens.get(i+1).getLexeme().equals(Token.NOT)))
						return false;
				} else {
					//if not last token, it must be followed with an AN
					if(!booleanTokens.get(i-1).getLexeme().equals(Token.AN))
						return false;
				}
				
				//push to stack
				checker.push(currentToken);
			} else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(currentToken.getClassification())) {
				//make sure it is not followed by an 'AN'
				if(booleanTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				//make sure it is not the last token
				if(i == 0) return false;
				
				//pop one operand
				if(checker.size() > 1) {
					checker.pop();
					popCount++;
				}
				
				//insufficient amount of operands
				else return false;
			} else return false; //lexeme does not belong in the expression			
		}
		
		//there should only be 1 operand left and the number of ANs must match the number of operands
		if((checker.size() == 1) && (anCount == popCount)) return true;
		else return false;
	}
	
	//SEMANTICS FOR BOOLEAN OPERATIONS
	private boolean booleanExecute(String dataHolder, ArrayList<Token> booleanTokens) {
		Stack<Boolean> operation = new Stack<Boolean>();
		System.out.println("Data holder"+ dataHolder);
		
		for(Token tkn: booleanTokens) {	
			//System.out.print("TOS: ");
			if(!operation.empty()) {
				//System.out.println(operation.peek());
			}
			
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
				} else {
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
					} else {
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
		System.out.println("result: "+result);
		
		//set the value of the varident to the result
		Symbol s = isASymbol(dataHolder);
		if(s != null) {				
			if(result == true) s.setValue(Token.WIN_TROOF_LITERAL);
			else s.setValue(Token.FAIL_TROOF_LITERAL);
			
			s.setDataType(Symbol.BOOLEAN);
			return result;

		}
		
		validSemantics = false;
		return false;
	}
	
	//SYNTAX FOR COMPARISON OPERATIONS
	private boolean comparisonSyntax(ArrayList<Token> comparisonTokens) {
		Stack<Token> checker = new Stack<Token>();
		int exprCount = 0, opCount = 0, anCount = 0;
		boolean startingPopped = false;
		
		
		for(int i=0; i<comparisonTokens.size(); i++) {
			//implies that another operation has started in the same line
			if(startingPopped) return false; 
		
			//add keywords to stack
			if(Token.COMPARISON_OPERATORS.contains(comparisonTokens.get(i).getClassification()) || Token.ARITHMETIC_EXPRESSIONS.contains(comparisonTokens.get(i).getClassification())) {
				checker.add(comparisonTokens.get(i));
				//if not starting comparison operator, inc exprCount (meaning it is a nested expression)
				if(i > 0) exprCount++;
			} else if(comparisonTokens.get(i).getLexeme().equals(Token.AN)) {
				//if an is encountered, add to an count
				anCount++;
			} else if(comparisonTokens.get(i).getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER) || comparisonTokens.get(i).getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER) || comparisonTokens.get(i).getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
				//if num/var is encountered, add to an operand count
				opCount++;
			} else {
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
	
	//SEMANTICS FOR COMPARISON OPERATIONS
	private String comparisonExecute(String dataHolder,ArrayList<Token> compToken) {
		Stack<Number> operation = new Stack<Number>();
		Boolean isNumber = true;
		//since operations are in prefix, reverse the tokens 
		Collections.reverse(compToken);
		
		for(Token tkn: compToken) {
			//System.out.println(tkn.getLexeme());
			//case 1: numbar - floating points
			if(tkn.getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER)) {
				operation.push(parseFloat(tkn));
			//case 2: numbr - integer
			}else if(tkn.getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER)) {
				operation.push(parseInt(tkn));
			//case 3: varident
			}else if(tkn.getClassification().equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
				
				Symbol s = isASymbol(tkn.getLexeme());
				if(s != null && !s.getDataType().equals(Symbol.UNINITIALIZED)) {	
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
						else if(classification.equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) isNumber = false;
						else isNumber = false;
					}
				else {
					validSemantics = false;
					break;
				}
				
			//if operation is detected, pop 2 operands and perform the operation
			}else if(Token.COMPARISON_OPERATORS.contains(tkn.getClassification()) || Token.ARITHMETIC_EXPRESSIONS.contains(tkn.getClassification())){
				
				boolean resultIsNumbar = false;
				boolean resultIsNumbr = false;
				if(isNumber) {
					Number op1 = operation.pop();
					Number op2 = operation.pop();
					//check if one of the operands is numbar
					if(op1 instanceof Float && op2 instanceof Float) resultIsNumbar = true;
					if(op1 instanceof Integer && op2 instanceof Integer) resultIsNumbr = true;
					
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
		}

		//last item on the stack is the result
		String answer = "";
		if(isNumber) {
			Number num = operation.pop();
			if(num.equals(1)) answer = "WIN";
			else if(num.equals(0)) answer = "FAIL";
		} else answer = "FAIL";

		//if(isNumber == false) answer = "FAIL";
		//set the value of the varident to the result
		Symbol s = isASymbol(dataHolder);
		if(s != null) {	
			s.setValue(answer);
			s.setDataType(Symbol.BOOLEAN);
		} else validSemantics = false;
		
		return answer;
	}
	
	
	private boolean combiSyntax(ArrayList<Token> combiTokens) {
		Stack<String> checker = new Stack<String>();
		Token currentToken;
		int anCount = 0, popCount = 0;
		
		//since prefix, read the line in reverse
		Collections.reverse(combiTokens);
		
		for(int i=0; i < combiTokens.size(); i++) {
			currentToken = combiTokens.get(i);
			
			//if AN is detected, it must not be the last or starting token, and must not be followed by an AN
			if(currentToken.getLexeme().equals(Token.AN)) {
				
				//AN is starting/last token
				if(i == 0 || i == (combiTokens.size()-1)) 
					return false;
				
				//followed by AN
				else if(combiTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				else anCount++;
			}else if(currentToken.getClassification().equals(Token.NOT_CLASSIFIER)) {
			
				//NOT is last token
				if(i == 0) {
					System.out.println("last");
					return false;
				}
				
				//followed by AN
				else if(combiTokens.get(i-1).getLexeme().equals(Token.AN)) {
					System.out.println("AN");
					return false;
				}
					
				
				else {
					if(checker.size() > 0) {
						String op1 = checker.pop();
						if(!(op1.equals("TROOF") || op1.equals("VARIDENT"))) {
							System.out.println("here");
							return false;
						}
						
						checker.push("TROOF");
					}
				}
			}else if(currentToken.getLexeme().equals(Token.ALL_OF) || currentToken.getLexeme().equals(Token.ANY_OF)) {
				
				//if it starts with ANY OF/ALL OF then num of stack is ignored since these are infinite arity operations
				if(i == combiTokens.size()-1 && !combiTokens.get(i-1).getLexeme().equals(Token.AN)) {
					int stackSize = checker.size();
					String op1;
					String op2;
					
					for(int k = 0; k < stackSize; k++) {
						if(checker.size() > 1) {
							op1 = checker.pop();
							op2 = checker.pop();
							
							System.out.println("op1: "+op1);
							System.out.println("op2: "+op2);
							if(!(op1.equals("TROOF") || op1.equals("VARIDENT")) || !(op2.equals("TROOF") || op2.equals("VARIDENT"))) {
								return false;
							}
							else{
								popCount++;
								checker.push("TROOF");
							}
						}
					}
				}
					 
				
				//operation cannot be nested
				else return false;
			}else if(currentToken.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER)) {
				//if last token, it must be preceeded with an AN or NOT
				if(i == 0) {
					if(!(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT)))
						return false;
				} else {
					//if not last token, it must be followed with an AN
					if(!combiTokens.get(i-1).getLexeme().equals(Token.AN))
						return false;
				}
				
				//push to stack
				 checker.push("TROOF");
				
			}else if(isAVarident(currentToken.getClassification())) {
				
				if(i == 0) {
					if(!(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT)))
						return false;
				} else {
					//if not last token, it must be followed with an AN
					if(!combiTokens.get(i-1).getLexeme().equals(Token.AN))
						return false;
				}
				
				checker.push("VARIDENT");
				
			}else if(isADigit(currentToken.getClassification())) {
				if(i == 0) {
					if(!(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT)))
						return false;
				} else {
					//if not last token, it must be followed with an AN
					if(!combiTokens.get(i-1).getLexeme().equals(Token.AN))
						return false;
				}
				
				checker.push("DIGIT");
			}else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(currentToken.getClassification())) {
				//make sure it is not followed by an 'AN'
				if(combiTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				//make sure it is not the last token
				if(i == 0) return false;
				
				
				
				//pop one operand
				if(checker.size() > 1) {
					String op1 = checker.pop();
					String op2 = checker.pop();
					
					//if popped is not troof, then operand is not valid
					
					if(!(op1.equals("TROOF") || op1.equals("VARIDENT")) || !(op2.equals("TROOF") || op2.equals("VARIDENT"))) return false;
					else{
						popCount++;
						checker.push("TROOF");
					}
				}
				
				//insufficient amount of operands
				else return false;
			} else if(Token.ARITHMETIC_EXPRESSIONS.contains(currentToken.getClassification())){
				//make sure it is not followed by an 'AN'
				if(combiTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				//make sure it is not the last token
				if(i == 0) return false;
				
				//pop one operand
				if(checker.size() > 1) {
					String op1 = checker.pop();
					String op2 = checker.pop();
					
					//if popped is not troof, then operand is not valid
					
					if(!(op1.equals("DIGIT") || op1.equals("VARIDENT")) || !(op2.equals("DIGIT") || op2.equals("VARIDENT"))) return false;
					else{
						popCount++;
						checker.push("DIGIT");
					}
				}
			}else if(Token.COMPARISON_OPERATORS.contains(currentToken.getClassification())){
				//make sure it is not followed by an 'AN'
				if(combiTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				//make sure it is not the last token
				if(i == 0) return false;
				
				//pop one operand
				if(checker.size() > 1) {
					String op1 = checker.pop();
					String op2 = checker.pop();
					
					//if popped is not troof, then operand is not valid
					
					if(!(op1.equals("DIGIT") || op1.equals("VARIDENT") || op1.equals("TROOF")) || !(op2.equals("DIGIT") || op2.equals("VARIDENT") || op2.equals("TROOF"))) return false;
					else{
						popCount++;
						checker.push("TROOF");
					}
				}
			}else return false; //lexeme does not belong in the expression			
		}
		
		//there should only be 1 operand left and the number of ANs must match the number of operands
		if((checker.size() == 1) && (anCount == popCount) && (checker.peek().equals("TROOF"))) return true;
		else return false;
	}
	
	private String combiExecute(String dataHolder, ArrayList<Token> combiTokens) {
		Stack<String> operation = new Stack<String>();
		
		for(Token tkn: combiTokens) {
			System.out.println("tkn: " + tkn.getLexeme());
			if(tkn.getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER) || tkn.getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER) || tkn.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER)) {
				operation.push(tkn.getLexeme());
			}else if(isAVarident(tkn.getClassification())) {
				Symbol var = isASymbol(tkn.getLexeme());
				
				if(var != null && !var.getDataType().equals(Symbol.UNINITIALIZED)) {
					operation.push(var.getValue());
				}
				else {
					validSemantics = false;
					return null;
				}
			}else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tkn.getClassification())) {
				String op1 = operation.pop();
				String op2 = operation.pop();
				
				if(!((op1.equals(Token.WIN_TROOF_LITERAL) || op1.equals(Token.FAIL_TROOF_LITERAL))
						&& (op2.equals(Token.WIN_TROOF_LITERAL) || op2.equals(Token.FAIL_TROOF_LITERAL)))) {
					validSemantics = false;
					return null;
				}
				
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
				
			}else if(Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tkn.getClassification())) {
				String op1;
			
				
				if(tkn.getLexeme().equals(Token.NOT)) {
					op1 = operation.pop();
					if(!(op1.equals(Token.WIN_TROOF_LITERAL) || op1.equals(Token.FAIL_TROOF_LITERAL))) {
						validSemantics = false;
						break;
					}
					
					operation.push(notOperator(op1));
				}else {
					String op2;
					
					int currentStackSize = operation.size();
					if(tkn.getLexeme().equals(Token.ANY_OF)) {
						for(int j = 0; j < currentStackSize-1; j++) {
							op1 = operation.pop();
							op2 = operation.pop();
							if(!((op1.equals(Token.WIN_TROOF_LITERAL) || op1.equals(Token.FAIL_TROOF_LITERAL))
									&& (op2.equals(Token.WIN_TROOF_LITERAL) || op2.equals(Token.FAIL_TROOF_LITERAL)))) {
								validSemantics = false;
								return null;
							}
							
							operation.push(orOperator(op1, op2));
						}
					}else {
						for(int j = 0; j < currentStackSize-1; j++) {
							op1 = operation.pop();
							op2 = operation.pop();
							if(!((op1.equals(Token.WIN_TROOF_LITERAL) || op1.equals(Token.FAIL_TROOF_LITERAL))
									&& (op2.equals(Token.WIN_TROOF_LITERAL) || op2.equals(Token.FAIL_TROOF_LITERAL)))) {
								validSemantics = false;
								return null;
							}
							
							operation.push(andOperator(op1, op2));
						}
						
					}
				}
			}else if(Token.ARITHMETIC_EXPRESSIONS.contains(tkn.getClassification())) {
				//System.out.println("Line check: "+lineCheck);
				viewStack(operation);
				boolean resultIsNumbar = false;
				String op1 = operation.pop();
				String op2 = operation.pop();
				
				String classificationOp1 = isAValidLexeme(op1);
				String classificationOp2 = isAValidLexeme(op2);
				
				System.out.println("class op1: "+classificationOp1+" vs class op2: "+classificationOp2);
				
				if(!(isADigit(classificationOp1) || isADigit(classificationOp2))) {
					validSemantics = false;
					return null;
				}
				
				//check if one of the operands is numbar
				if(classificationOp1.equals(Token.NUMBAR_LITERAL_CLASSIFIER) || classificationOp2.equals(Token.NUMBAR_LITERAL_CLASSIFIER)) resultIsNumbar = true;
				
				
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
						answer = o1 / o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.MOD_OF_CLASSIFIER:
						answer = o1 % o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.BIGGR_OF_CLASSIFIER:
						System.out.println(o1+" vs "+o2);
						if(o1 > o2) {
							System.out.println("BIGGR: "+o1);
							operation.push(String.valueOf(o1));
						}
						else{
							System.out.println("BIGGR: "+o2);
							operation.push(String.valueOf(o2));
						}
						
						viewStack(operation);
						break;
					case Token.SMALLR_OF_CLASSIFIER:
						System.out.println(o1+" vs "+o2);
						if(o1 < o2) {
							System.out.println("SMALLR: "+o1);
							operation.push(String.valueOf(o1));
							viewStack(operation);
						}
						else{
							System.out.println("SMALLR: "+o2);
							operation.push(String.valueOf(o2));
						}
						
						viewStack(operation);
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
						answer = o1 / o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.MOD_OF_CLASSIFIER:
						answer = o1 % o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.BIGGR_OF_CLASSIFIER:
						System.out.println(o1+" vs "+o2);
						if(o1 > o2) {
							System.out.println("BIGGR: "+o1);
							operation.push(String.valueOf(o1));
							viewStack(operation);
						}
						else{
							System.out.println("BIGGR: "+o2);
							operation.push(String.valueOf(o2));
						}
						
						viewStack(operation);
						break;
					case Token.SMALLR_OF_CLASSIFIER:
	
						System.out.println(o1+" vs "+o2);
						if(o1 < o2) {
							System.out.println("SMALLR: "+o1);
							operation.push(String.valueOf(o1));
							viewStack(operation);
						}
						else{
							System.out.println("SMALLR: "+o2);
							operation.push(String.valueOf(o2));
						}
						
						viewStack(operation);
						break;
					}
				} 
			}else if(Token.COMPARISON_OPERATORS.contains(tkn.getClassification())) {
				System.out.println("Line: "+lineCheck);
				viewStack(operation);
				String op1 = operation.pop();
				String op2 = operation.pop();
				
				String classificationOp1 = isAValidLexeme(op1);
				String classificationOp2 = isAValidLexeme(op2);
				
				switch(tkn.getClassification()) {
					case Token.BOTH_SAEM_CLASSIFIER: // o1 == o2
						if(classificationOp1.equals(classificationOp2)) {
							if(op1.equals(op2)) operation.push(Token.WIN_TROOF_LITERAL);
							else operation.push(Token.FAIL_TROOF_LITERAL);
						}else operation.push(Token.FAIL_TROOF_LITERAL);
						break;
					case Token.DIFFRINT_CLASSIFIER: //o1 != o2
						if(classificationOp1.equals(classificationOp2)) {
							if(!op1.equals(op2)) {
								System.out.println("Result: WIN");
								operation.push(Token.WIN_TROOF_LITERAL);
							}
							else{
								System.out.println("Result: FAIL");
								operation.push(Token.FAIL_TROOF_LITERAL);
							}
						}else{
							System.out.println("Result: WIN");
							operation.push(Token.WIN_TROOF_LITERAL);
						}
						
						viewStack(operation);
						break;
				}
			}
		}
		
		
		String result = operation.pop();
		
		Symbol s = isASymbol(dataHolder);
	
		if(s != null) {	
			s.setValue(result);
			s.setDataType(Symbol.BOOLEAN);
		} else validSemantics = false;
		return result;
	}
	
	private void viewStack(Stack<String> op) {
		System.out.println("----------------------");
		System.out.println("STACK: ");
		for(String o: op) {
			System.out.println(o);
		}
		System.out.println("----------------------");
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
		
		//store original value of LineCheck
		int originalLineCheck = lineCheck;
		
		//change linecheck back to start of switch case
		lineCheck -= queueSize;
		
		
		//execute instructions in pQueue
		for(int i=0; i<queueSize; i++) {
			//dequeues the process queue
			tokensPerLine = pQueue.remove();
			
			lineCheck++;
			
			//skip WTF
			if(i == 0) continue; 
			
			//detects OMG
			else if(tokensPerLine.get(0).getLexeme().equals(Token.OMG)) {				
				//if has yet to enter a case, check condition
				if(!enteredCase) {
					
					/* compare IT and literal */
					
					Symbol it = getIT();
					//check if same datatype
					String classificationIT = isAValidLexeme(it.getValue());
					
					if(classificationIT.equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER)) {
						it = isASymbol(it.getValue());
						
						if(it != null) {
							classificationIT = isAValidLexeme(it.getValue());
						}else {
							validSemantics = false;
							return;
						}
						
					}
					String classificationCase = isAValidLexeme(tokensPerLine.get(1).getLexeme());
					
					if(classificationIT == null) classificationIT = Token.YARN_LITERAL_CLASSIFIER;
					//if classification is the same, check if value is the same
					if(classificationIT.equals(classificationCase)) {
						//if same, activate flag
						
						if(it.getValue().equals(tokensPerLine.get(1).getLexeme())) enteredCase = true;

					}
				} else continue;
			
			//if GTFO, clear the process queue and exit switch statement
			} else if(tokensPerLine.get(0).getLexeme().equals(Token.GTFO) && enteredCase) {
				pQueue.clear();
				executingSwitchStatement = false;
				break;
				
			//if OIC, clear the process queue and exit the switch statement	
			} else if(tokensPerLine.get(0).getLexeme().equals(Token.OIC)){
				pQueue.clear();
				executingSwitchStatement = false;
				break;
			
			//default case
			} else if(tokensPerLine.get(0).getLexeme().equals(Token.OMGWTF)){
				enteredCase = true;
				
			//execute instruction
			} else
				if(enteredCase) checkSyntaxAndSemantics();
		}
		
		lineCheck = originalLineCheck;
	}
	
	//SEMANTICS FOR IF ELSE STATEMENT
	private void ifElseExecute() {
		executingIfStatement = true;
		//checks if it has entered case
		boolean enteredCase = false;
		
		//set checking if then statement to false so that it would execute instructions
		checkingIfStatement = false;
		
		//get current queue size to get length of loop
		int queueSize = ifQueue.size();
		
		//store original value of LineCheck
		int originalLineCheck = lineCheck;
				
		//change linecheck back to start of switch case
		lineCheck -= queueSize;
		
		//execute instructions in ifQueue
		for(int i = 0; i < queueSize; i++) {
			//dequeues the process queue
			tokensPerLine = ifQueue.remove();
			lineCheck++;
			//skip O RLY?
			if(i == 0) continue;
			
			//detects YA RLY (if-statement)
			else if(tokensPerLine.get(0).getLexeme().equals(Token.YA_RLY)) {
				//if has yet to enter a case, check condition
				if(!enteredCase) {
					//compare IT and troof: if same, activate flag
					if(getIT().getValue() == Token.WIN_TROOF_LITERAL) enteredCase = true;
				} else continue; //skip if else
			}
			
			//detects NO WAI (else-statement)
			else if(tokensPerLine.get(0).getLexeme().equals(Token.NO_WAI)){
				//compare IT and troof: if same, activate flag
				if(!enteredCase) {
					if(getIT().getValue() == Token.FAIL_TROOF_LITERAL) enteredCase = true;
				} else { //emptry queue 
					ifQueue.clear();
					break;	
				}
				
			//execute instruction
			}else if(tokensPerLine.get(0).getLexeme().equals(Token.OIC)){
				ifQueue.clear();
				executingIfStatement = false;
				break;			
			} else if(enteredCase) checkSyntaxAndSemantics();
		}
		
		lineCheck = originalLineCheck;
	}
	
	//function to make deep copy of tokens per line
	private void storeTokensToQueue(String statement) {
		ArrayList<Token> lineTokens = new ArrayList<Token>();
		for(Token tkn: tokensPerLine) lineTokens.add(new Token(tkn.getLexeme(), tkn.getClassification()));
		
		if(statement == Token.WTF) pQueue.add(lineTokens);
		else if(statement == Token.O_RLY) ifQueue.add(lineTokens);
	}
	
	//check if instruction exists in pQueue
	private boolean inProcessQueue(String lexeme, String statement) {
		if(statement == Token.WTF) {
			for(ArrayList<Token> line: pQueue) {
				if(line.get(0).getLexeme().equals(lexeme)) return true;
			} 
		} else if (statement == Token.O_RLY) {
			for(ArrayList<Token> line: ifQueue) {
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
	
	//check if the classification of a token is a literal or an expression
	private boolean isALitOrExpr(String classification) {
		if(Token.LITERALS.contains(classification) || 
			isAnExpr(classification)!=0) return true;
		return false;
	}	
	
	private int isAnExpr(String classification) {
		if(Token.ARITHMETIC_EXPRESSIONS.contains(classification)) return 1;
		else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(classification)) return 2;
		else if(Token.OTHER_BOOLEAN_EXPRESSIONS.contains(classification)) return 3;
		else if(Token.COMPARISON_OPERATORS.contains(classification)) return 4;
		return 0;
	}	
	
	//return symbol if the varident is already added to or is part of the symbols
	private Symbol isASymbol(String var) {
		for(Symbol s: symbols) {
			if(s.getSymbol().equals(var))
				return s;
		}
		return null;
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
		if(!isEmpty(currentLexeme) && isAVariable() && status!=2) {
			readBack=true;
			return 2;
		//there's an invalid lexeme, stop iteration for getting lexemes
		} else if(!isEmpty(currentLexeme)) {
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
	
	private String getDataType(String value) {
		String classification = isAValidLexeme(value);
		
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
	public boolean execute() {
		String l;
		
		for(int i=0;i<tokens.size();i++) {
			l = tokens.get(i).getLexeme();
			
			if(isAComment(l)!=0 || l.equals(Token.TLDR)) continue;
			else {
				if(l.equals(Token.HAI)) break;
				else {
					lineCheck = 1;
					validSyntax = false;
					return false;
				}
			}
		}

		for(int i=tokens.size()-1;i>=0;i--) {
			l = tokens.get(i).getLexeme();
			if(isAComment(l)!=0 || l.equals(Token.TLDR)) continue;
			else {
				if(l.equals(Token.KTHXBYE)) return true;
				else {				
					validSyntax = false;
					return false;
				}
			}
		}
		
		if(validLexeme && validSyntax && validSemantics) return true;
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
            	System.out.println("[!] User cancelled input dialog");
            } else { //file chosen
            	//check if file extension ends with .lol
            	if(file.getAbsolutePath().matches(".*.lol$")) readFile();
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
		outputDisplayText = "";
		lineCheck = 0;		
		validLexeme = true;
		validSyntax = true;
		validSemantics = true;
		conditionalStatement = false;
		switchStatement = false;
		tokens.clear();
		tokensPerLine.clear();
		symbols.clear();
		opTokens.clear();
		pQueue.clear();
		ifQueue.clear();
		outputDisplay.clear();
		for(int i=0; i<lexemeTableView.getItems().size(); i++) lexemeTableView.getItems().clear();
		for(int i=0; i<symbolTableView.getItems().size(); i++) symbolTableView.getItems().clear();
		passIndicator.setImage(neutralImg);
		titleImage.setImage(titleImg);
		lexicalIndicator.setImage(null);
		syntaxIndicator.setImage(null);
		semanticIndicator.setImage(null);
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
    	for(Token token: tokens) lexemeTableView.getItems().add(token);
    	for(Symbol symbol: symbols) symbolTableView.getItems().add(symbol);
    }
    
    private void showError() {  	
    	//update GUI to show fail
    	passIndicator.setImage(cryingImg);
		outputDisplay.setText("[!] Error detected in line " + lineCheck);
    	
    	if(!validLexeme) {
    		lexicalIndicator.setImage(lexicalFailImg);
    		validSyntax = false;
    		validSemantics = false;
    	}
    	else lexicalIndicator.setImage(lexicalPassImg);
    		
		if(!validSyntax) {
			syntaxIndicator.setImage(syntaxFailImg);
    		validSemantics = false;
		}
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
			if(file!=null) {
				readFile();
				analyzeFile();
				if(execute()) showPass();
				else showError();
			} else {
				//prompt error dialog
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setContentText("[!] There is no lolcode file.");
				alert.setTitle("Error Dialog");
				alert.setHeaderText(null);
				alert.show();
			}
			
        });
	}

}
