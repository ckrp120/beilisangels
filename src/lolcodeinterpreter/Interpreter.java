package lolcodeinterpreter;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
    private TableColumn<Token, String> lexemefirstDataColumn, lexemesecondDataColumn;
    private TableColumn<Symbol, Symbol> symbolfirstDataColumn, symbolsecondDataColumn;
    private TableView<Token> lexemeTableView = new TableView<Token>();
    private TableView<Symbol> symbolTableView = new TableView<Symbol>(); 
	
    //FOR LEXICAL/SYNTAX/SEMANTIC ANALYSIS
    private String[] lines;
    private String currentLexeme,dialogText;
    private int lineNumber,status,orlyCount;
    private boolean validLexeme,validSyntax,validSemantics,readBack,conditionalStatement,switchStatement;
    private ArrayList<ArrayList<Token>> tokens = new ArrayList<ArrayList<Token>>();
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
			
			if(status == 2) { //case 2
				lineNumber--;
				//process again starting from where an invalid lexeme is detected
				status = checkLexeme(currentLexeme);
			}  
			if(status == 1) { //case 1 or case 2 and there's still an invalid lexeme
				validSyntax = false;
				break;
			}	
			
			if(!tokensPerLine.isEmpty()) {
				addToTokens();
			
				//if the current line has a comment, ignore the BTW token
				if(tokensPerLine.get(tokensPerLine.size()-1).getLexeme().equals(Token.BTW))
					tokensPerLine.remove(tokensPerLine.size()-1);
			}
			
			if(!tokensPerLine.isEmpty()) {
				checkSyntaxAndSemantics();
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
				if(tplClass(0).equals(Token.OMG_CLASSIFIER) && tokensPerLine.size() == 2) {
					if(Token.LITERALS.contains(tplClass(1)))
						storeTokensToQueue(Token.WTF);
					else validSyntax = false;
				}			
				else validSyntax = false;
			}
			
			//OMG
			else if(tplLexeme(0).equals(Token.OMG)) {
				//check if the line next to OMG is a literal
				if(Token.LITERALS.contains(tplClass(1)) && tokensPerLine.size() == 2)
					storeTokensToQueue(Token.WTF);
				else validSyntax = false;
			}
			
			//MEBBE
			else if(tplLexeme(0).equals(Token.MEBBE)) {
				if(checkingIfStatement) {
					if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(tplClass(1)) || 
							Token.OTHER_BOOLEAN_EXPRESSIONS.contains(tplClass(1)) || Token.COMPARISON_OPERATORS.contains(tplClass(1))) {
						storeTokensToQueue(Token.O_RLY);
					} else validSyntax = false;
				} else validSyntax = false;
			}
			
			//PRINT = VISIBLE
			else if(tplLexeme(0).equals(Token.VISIBLE)) {
				if(printSyntax()) {
					if(checkingSwitchStatement) storeTokensToQueue(Token.WTF);
					else if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else printExecute();
				}
				else validSyntax = false;
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
				if(!(tokensPerLine.size()==2 && tplLexeme(1).equals("1.2"))) validSyntax = false;
			} 
			else validSyntax = false;
		} else {
			switch(tplClass(0)) {
				case Token.HAI_CLASSIFIER:
					break;
				case Token.KTHXBYE_CLASSIFIER:
//					if(conditionalStatement==true || switchStatement==true) {
//				    	for(ArrayList<Token> tokensPerLine: tokens) {
//				        	for(Token token: tokensPerLine) {
//								if(!token.getLexeme().equals(Token.OIC)) {
//									validSyntax = false;
//								} else {
//									validSyntax = true;
//									break;
//								}
//				        	}
//				    	}
//					} else {
//						validSyntax = true;
//					}
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
					//check if ORLY, YA RLY are already in the if-then statement
					else if((inProcessQueue(Token.O_RLY, Token.O_RLY) && inProcessQueue(Token.YA_RLY, Token.O_RLY) && checkingIfStatement) || executingIfStatement) {
						storeTokensToQueue(Token.O_RLY);
						orlyCount--;
						if(orlyCount==0) ifElseExecute();	
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
					orlyCount++;
					storeTokensToQueue(Token.O_RLY);
					break;
				case Token.YA_RLY_CLASSIFIER:
					if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					else validSyntax=false;
					break;
				case Token.NO_WAI_CLASSIFIER:
					if(checkingIfStatement) storeTokensToQueue(Token.O_RLY);
					break;
				default:
					validSyntax=false;
					break;
			}
		}
	}	
	
	private String tplLexeme(int index) {
		return tokensPerLine.get(index).getLexeme();
	}
	
	private String tplClass(int index) {
		return tokensPerLine.get(index).getClassification();
	}
	
	//SYNTAX FOR PRINT = VISIBLE
	private boolean printSyntax() {
		Token tkn;
		int i=1;
		
		if(tokensPerLine.size() == 1) return true;
		else {
			while(i<tokensPerLine.size()) {
				tkn = tokensPerLine.get(i);
				
				if(isAVarident(tkn.getClassification()) ||
					isALitOrExpr(tkn.getClassification()) ||
					tkn.getLexeme().equals(Token.STRING_DELIMITER) ||
					tkn.getLexeme().equals(Token.AN))
					i++;
				else return false;
			}
		}
		return true;
	}
	
	//SEMANTICS FOR PRINT = VISIBLE
	private void printExecute() {
		Token tkn;
		int i=1,operation;
		boolean appendNewLine=true;
		String itValue = symbols.get(0).getValue();
		String visibleValue = "";
		
		if(tokensPerLine.size()==1) visibleValue = "\n";
		
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
					currToken = tokensPerLine.get(i).getClassification();
					opTokens.add(tokensPerLine.get(i));
					if(currToken.equals(Token.STRING_DELIMITER_CLASSIFIER)) {
						i++;
						continue;
					}
					
					if(isAVarident(currToken) || Token.LITERALS.contains(currToken) || string==2 || Token.MKAY_CLASSIFIER.equals(currToken)) {
						string = 0;
						if(i+1 != tokensPerLine.size()) {
							
							if(currToken.equals(Token.YARN_LITERAL_CLASSIFIER)) {
								if(i+2 < tokensPerLine.size()) {
									nextToken = tokensPerLine.get(i+2).getLexeme();
									if(!(nextToken.equals(Token.AN) || nextToken.equals(Token.MKAY))) stop = true;	
								}
							}else {
								nextToken = tokensPerLine.get(i+1).getLexeme();
								if(!(nextToken.equals(Token.AN) | nextToken.equals(Token.MKAY))) stop = true;
							}
														
						}
					}else if(currToken.equals(Token.MKAY_CLASSIFIER)) {
						stop = true;
					}

					i++;
				} while(i<tokensPerLine.size() && !stop);
				i--;

				
				//case 2.3: concat op
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
				
				//case 2.4: comp op
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
		//symbols.get(0).setValue(visibleValue);
		//symbols.get(0).setDataType(Symbol.STRING);
		//System.out.println("Symbol: "+symbols.get(0).getSymbol() + " Data Type: " + symbols.get(0).getDataType());
		

		if(appendNewLine) outputDisplayText += "\n";						
	}
	
	//SYNTAX FOR ACCEPT = GIMMEH
	private boolean acceptSyntax() {
		if(tokensPerLine.size() == 2) {
			if(tplClass(1).equals(Token.VARIABLE_IDENTIFIER_CLASSIFIER))
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
		if((s = isASymbol(tplLexeme(1))) != null) {
			outputDisplay.setText(outputDisplayText);
			//get user input
			clearTable();
			populateTable();
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
            	outputDisplayText += value + "\n";
            });	
        //else, error
        } else {
        	validSemantics = false;
        }
        
	}
	
	
	
	//SYNTAX FOR VARIABLE DECLARATION = I HAS A
	
	
	private String varDeclarationSyntax() {
		if(tokensPerLine.size() > 1) {
			if(isAVarident(tplClass(1))) {	
				//case 1: I HAS A var
				if(tokensPerLine.size() == 2) return "";
				//case 2: I HAS A var ITZ var/lit/expr
				if(tplClass(2).equals(Token.ITZ_CLASSIFIER)) {
					if(tokensPerLine.size() == 4 && 
							(isAVarident(tokensPerLine.get(3).getClassification()) || Token.LITERALS.contains(tokensPerLine.get(3).getClassification())))
						return tokensPerLine.get(3).getClassification();	
					if(tokensPerLine.size() == 6 && Token.YARN_LITERAL_CLASSIFIER.equals(tokensPerLine.get(4).getClassification()))
						return tokensPerLine.get(4).getClassification();	
					if(isAnExpr(tokensPerLine.get(3).getClassification()) != 0)
						return tokensPerLine.get(3).getClassification();	
					return null;
				}
				
				return null;
			}
		}
		//return null if what's declared is not a varident/it
		//or the value given is not a varident/it, literal, or expr
		return null;
	}
	
	//SEMANTICS FOR VARIABLE DECLARATION = I HAS A
	public void varDeclarationExecute(String litClass) {
		String identifier = tplLexeme(1);
		int operation;
		
		//check if the varident is already declared before
		if(isASymbol(identifier) != null) validSemantics = false;
		//case 1: I HAS A var
		else if(tokensPerLine.size() == 2) {
			symbols.add(new Symbol(identifier,Token.NOOB_TYPE_LITERAL, Symbol.UNINITIALIZED));	
		//case 2: I HAS A var ITZ var/lit/expr
		} else {
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
		if(isAVarident(tplClass(0))) {
			//return value if it is a varident/it, literal, or expr
			if(tokensPerLine.size() == 3 &&
					(isAVarident(tplClass(2)) || Token.LITERALS.contains(tplClass(2))))
				return tplClass(2);	
			if(tokensPerLine.size() == 5 && Token.YARN_LITERAL_CLASSIFIER.equals(tokensPerLine.get(3).getClassification())) 
				return tokensPerLine.get(3).getClassification();
			if(isALitOrExpr(tplClass(2)))
				return tplClass(2);
			return null;
		} return null;
	}
	
	//SEMANTICS FOR ASSIGNMENT STATEMENT = R
	private void varAssignmentExecute(String litClass) {
		Symbol s = isASymbol(tplLexeme(0));
		Symbol sv = isASymbol(tplLexeme(2));
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
		} else validSemantics = false;
	}
			
	private boolean combiSyntax(ArrayList<Token> combiTokens) {
		Stack<String> checker = new Stack<String>();
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
				if(i == 0 || i == (combiTokens.size()-1)) 
					return false;
				
				//followed by AN/MKAY
				else if((combiTokens.get(i-1).getLexeme().equals(Token.AN) || combiTokens.get(i-1).getLexeme().equals(Token.MKAY)))
					return false;
			}else if(currentToken.getClassification().equals(Token.NOT_CLASSIFIER)) {
			
				//NOT is last token
				if(i == 0) {
					return false;
				}
				
				//followed by AN/MKAY
				else if(combiTokens.get(i-1).getLexeme().equals(Token.AN) || combiTokens.get(i-1).getLexeme().equals(Token.MKAY)) {
					return false;
				}
					
				
				else {
					
					//insufficient amount of operands
					if(checker.size() == 0) return false;
						
					
				}
			}else if(currentToken.getLexeme().equals(Token.ALL_OF) || currentToken.getLexeme().equals(Token.ANY_OF)) {
				if(!mkayIsPresent) return false;
				
				//if it starts with ANY OF/ALL OF then num of stack is ignored since these are infinite arity operations
				if(infArityOpCount > 1 && !combiTokens.get(i-1).getLexeme().equals(Token.AN)) {
					
					int popCnt = 0;	
					while(popCnt < infArityOpCount-1) {
						System.out.println(popCnt);
						if(checker.size() > 1) {
							checker.pop();
							checker.pop();
							checker.push("TROOF");
							popCnt++;
						}else {
							validSyntax = false;
							return false;
						}
					}
										
					infArityOpCount = 0;
					mkayIsPresent = false;
				}
					 
				
				//operation cannot be nested
				else return false;
			}else if(currentToken.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER) || currentToken.getLexeme().equals(Token.NOOB_TYPE_LITERAL)) {
				//if last token, it must be preceeded with an AN or NOT
				if(i == 0) {
					if(i+1 < combiTokens.size() && !(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT)))
						return false;
				} else {
					//if not last token, it must be followed with an AN/MKAY
					if(!(combiTokens.get(i-1).getLexeme().equals(Token.AN) || combiTokens.get(i-1).getLexeme().equals(Token.MKAY)))
						return false;
				}
				
				//push to stack
				 checker.push("TROOFNOOB");
				if(mkayIsPresent) infArityOpCount++;
			}else if(Token.STRING_DELIMITER_CLASSIFIER.equals(currentToken.getClassification())) {
				continue;
			}else if(currentToken.getClassification().equals(Token.YARN_LITERAL_CLASSIFIER)){
				if(i == 0) {
					
					if(i+2 < combiTokens.size() && !(combiTokens.get(i+2).getLexeme().equals(Token.AN) || combiTokens.get(i+2).getLexeme().equals(Token.NOT))) {
						return false;
					}
						
				} else {
					//if not last token, it must be followed with an AN/MKAY
					if(i-2 > 0 && !(combiTokens.get(i-2).getLexeme().equals(Token.AN) || combiTokens.get(i-2).getLexeme().equals(Token.MKAY))) {
						return false;
					}
						
				}
				
				//push to stack
				 checker.push("YARN");
				 if(mkayIsPresent) infArityOpCount++;
			}else if(isAVarident(currentToken.getClassification())) {
				
				if(i == 0) {
					if(i+1 < combiTokens.size() && !(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT)))
						return false;
				} else {
					//if not last token, it must be followed with an AN/MKAY
					if(i-1 > 0 && !(combiTokens.get(i-1).getLexeme().equals(Token.AN) || !combiTokens.get(i-1).getLexeme().equals(Token.MKAY)))
						return false;
				}
				
				checker.push("VARIDENT");
				if(mkayIsPresent) infArityOpCount++;
			}else if(isADigit(currentToken.getClassification())) {
				if(i == 0) {
					if(!(combiTokens.get(i+1).getLexeme().equals(Token.AN) || combiTokens.get(i+1).getLexeme().equals(Token.NOT)))
						return false;
				} else {
					//if not last token, it must be followed with an AN
					if(!(combiTokens.get(i-1).getLexeme().equals(Token.AN) | combiTokens.get(i-1).getLexeme().equals(Token.MKAY)))
						return false;
				}
				
				checker.push("DIGIT");
				if(mkayIsPresent) infArityOpCount++;
			}else if(Token.BINARY_BOOLEAN_EXPRESSIONS.contains(currentToken.getClassification())) {
				//make sure it is not followed by an 'AN'
				if(combiTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				//make sure it is not the last token
				if(i == 0) return false;
				
				
				
				//pop one operand
				if(checker.size() > 1) {
					checker.pop();
					checker.pop();
					checker.push("TROOF");
					if(mkayIsPresent) infArityOpCount--;
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
					checker.pop();
					checker.pop();
					checker.push("DIGIT");
					if(mkayIsPresent) infArityOpCount--;
				}else return false;
			}else if(Token.COMPARISON_OPERATORS.contains(currentToken.getClassification())){
				//make sure it is not followed by an 'AN'
				if(combiTokens.get(i-1).getLexeme().equals(Token.AN))
					return false;
				
				//make sure it is not the last token
				if(i == 0) return false;
				
				//pop one operand
				if(checker.size() > 1) {
					checker.pop();
					checker.pop();
					checker.push("TROOF");
					if(mkayIsPresent) infArityOpCount--;
				}else return false;
			}else if(Token.MKAY_CLASSIFIER.equals(currentToken.getClassification())) {
				if(mkayIsPresent) return false;
				
				if(i == 0) {
					String nextToken = combiTokens.get(i+1).getClassification();
					if(!(Token.LITERALS.contains(nextToken) || isAVarident(nextToken) || Token.STRING_DELIMITER_CLASSIFIER.equals(nextToken))) {
						return false;
					}
						
				} else {
					//if not last token, it must be followed with an AN/MKAY
					if(!(combiTokens.get(i-1).getLexeme().equals(Token.AN))) {
						return false;
					}
						
				}
				
				mkayIsPresent = true;
			}else return false; //lexeme does not belong in the expression			
		
		}
		
		//there should only be 1 operand left
		if((checker.size() == 1) && infArityOpCount == 0) {
			//back to original state
			Collections.reverse(combiTokens);
			return true;
		}
		else {
			return false;
		
		}
	}
	
	private String combiExecute(String dataHolder, ArrayList<Token> combiTokens) {
		Stack<String> operation = new Stack<String>();

		//since prefix, read the line in reverse
		Collections.reverse(combiTokens);
		int infArityOpCount = 0;
		boolean mkayIsPresent = false;
		
		
		for(Token tkn: combiTokens) {			
			if(tkn.getClassification().equals(Token.NUMBAR_LITERAL_CLASSIFIER) || tkn.getClassification().equals(Token.NUMBR_LITERAL_CLASSIFIER) || tkn.getClassification().equals(Token.TROOF_LITERAL_CLASSIFIER) || tkn.getLexeme().equals(Token.NOOB_TYPE_LITERAL)) {
				operation.push(tkn.getLexeme());
				if(mkayIsPresent) infArityOpCount++;
			}else if(isAVarident(tkn.getClassification())) {
				Symbol var = isASymbol(tkn.getLexeme());
				
				if(var != null) {
					if(var.getDataType().equals(Symbol.STRING)) {
						operation.push("\""+var.getValue()+"\"");
					}
					
					else operation.push(var.getValue());
					
					if(mkayIsPresent) infArityOpCount++;
				}
				else {
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
				System.out.println("here"+op1);
				
				
				String op2 = operation.pop();
				String classificationOp2 = getClass(op2);
				if(!classificationOp2.equals(Token.TROOF_LITERAL_CLASSIFIER)) op2 = boolTypeCast(op2);
				
				System.out.println("op2"+op2);
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
				//System.out.println("Line check: "+lineNumber);
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
							validSemantics = false;
							return null;
						}
						answer = o1 / o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.MOD_OF_CLASSIFIER:
						if(o2 == 0) {
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
							return null;
						}
						answer = o1 / o2;
						operation.push(String.valueOf(answer));
						break;
					case Token.MOD_OF_CLASSIFIER:
						if(o2 == 0) {
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
		
		Symbol s = isASymbol(dataHolder);
	
		if(s != null) {	
			s.setValue(result);
			s.setDataType(Symbol.BOOLEAN);
		} else validSemantics = false;
		return result;
	}
	
	//TYPECASTS NON TROOF OPERANDS TO TROOF
	private String boolTypeCast(String op1) {
		String classificationOp1 = getClass(op1);
		System.out.println("orig"+op1);
		
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
			if(!isADigit(classificationOp1)) return null;
			
		}else if(classificationOp1.equals(Token.TROOF_LITERAL_CLASSIFIER)) {					
			if(op1.equals(Token.WIN_TROOF_LITERAL)) return "1";
			else return "0";
		}else if(Token.NOOB_TYPE_LITERAL.equals(op1)) {
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
		
		//store original value of LineCheck
		int originalLineCheck = lineNumber;
		
		//change linecheck back to start of switch case
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
					//check if same datatype
					String classificationIT = getClass(it.getValue());

					String classificationCase = getClass(tplLexeme(1));
					
					//if classification is the same, check if value is the same
					if(classificationIT.equals(classificationCase)) {
						//if same, activate flag
						
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
	
	//SEMANTICS FOR IF ELSE STATEMENT
	private void ifElseExecute() {
		executingIfStatement = true;
		//checks if it has entered case
		boolean enteredCase = false;
		//checks if it entered mebbe
		boolean enteredMebbe = false;
		
		//set checking if then statement to false so that it would execute instructions
		checkingIfStatement = false;
		
		//get current queue size to get length of loop
		int queueSize = ifQueue.size();
		
		//store original value of LineCheck
		int originalLineCheck = lineNumber;
		
		//store old IT value
		Symbol oldIT = getIT();
				
		//change linecheck back to start of switch case
		lineNumber -= queueSize;
		
		//execute instructions in ifQueue
		for(int i = 0; i < queueSize; i++) {
			//dequeues the process queue
			tokensPerLine = ifQueue.remove();
			lineNumber++;
			//skip O RLY?
			if(tplLexeme(0).equals(Token.O_RLY)) {
				enteredCase = false;
				continue;
			}
			
			//detects YA RLY (if-statement)
			else if(tplLexeme(0).equals(Token.YA_RLY)) {
				//if has yet to enter a case, check condition
				if(!enteredCase) {
					//compare IT and troof: if same, activate flag
					if(getIT().getValue() == Token.WIN_TROOF_LITERAL) enteredCase = true;
				} else continue; //skip if else
			}
			
			//detects MEBBE
			else if(tplLexeme(0).equals(Token.MEBBE)) {	
				System.out.println("case? " +enteredCase);
				if(!enteredCase) {
					combiExecute(Token.IT, tokensPerLine);
					
					Symbol it = getIT();
					System.out.println("IT: " + it.getValue());

					//if classification is the same, check if value is the same
					if(it.getValue().equals(Token.WIN_TROOF_LITERAL)) {
						//if same, activate flag
						enteredCase = true;
					} else {
						Symbol s = isASymbol(Token.IT);
						
						if(s != null) {	
							s.setValue(oldIT.getValue());
							s.setDataType(oldIT.getDataType());
						} else validSemantics = false;
					}
				} else enteredMebbe = true;
				
				Symbol it = getIT();
				System.out.println("it.getValue(): " + it.getValue());
			}
			
			//detects NO WAI (else-statement)
			else if(tplLexeme(0).equals(Token.NO_WAI)){
				//compare IT and troof: if same, activate flag
				if(!enteredCase) {
					if(getIT().getValue() == Token.FAIL_TROOF_LITERAL) enteredCase = true;
				} else { //emptry queue 
					ifQueue.clear();
					break;	
				}
				
			//execute instruction
			}else if(tplLexeme(0).equals(Token.OIC)){
				ifQueue.clear();
				executingIfStatement = false;
				break;			
			} else if(enteredCase && !enteredMebbe) {
				checkSyntaxAndSemantics();
				if(!validSemantics) return;
			}
		}
		
		lineNumber = originalLineCheck;
	}
	
	//SYNTAX FOR CONCATENATION
		private boolean smooshSyntax(ArrayList<Token> smooshTokens) {
			Token currentToken;
			currentToken = smooshTokens.get(1);

			//token after SMOOSH must be a literal or a varident
			if(!(Token.LITERALS.contains(currentToken.getClassification()) || isAVarident(currentToken.getClassification()) || Token.STRING_DELIMITER_CLASSIFIER.equals(currentToken.getClassification()))) {
				return false;
			}
			
			for(int i = 2; i < smooshTokens.size(); i++) {
				currentToken = smooshTokens.get(i);
				
				//if literal/varident
				if(Token.LITERALS.contains(currentToken.getClassification()) || isAVarident(currentToken.getClassification())) {
					
					
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
				if(Token.LITERALS.contains(tkn.getClassification())) {
					concat += tkn.getLexeme();
				}else if(isAVarident(tkn.getClassification())) {
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
		else if(statement == Token.O_RLY) ifQueue.add(lineTokens);
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
		else if(Token.SMOOSH_CLASSIFIER.equals(classification)) return 4;
		else if(Token.COMPARISON_OPERATORS.contains(classification)) return 5;
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
									validLexeme = false;
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
		String tkn;

		if(tokensPerLine.size()>0) {
			tkn = tplLexeme(0);
			if(tkn.equals(Token.I_HAS_A) || tkn.equals(Token.VISIBLE)) {
				if(Character.isLetter(currentLexeme.charAt(0))) return true;
				else return false;
			}
			else return false;		
		} 

		if(currentLexeme.contains(" R ")) return true;

		if(tokens.size()>0) {
			ArrayList<Token> tokensPerLine = tokens.get(tokens.size()-1);
			tkn = tokensPerLine.get(tokensPerLine.size()-1).getLexeme();
			if(tkn.equals(Token.BTW) || tkn.equals(Token.TLDR)) return false;
			else return true;		
		}

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
	public boolean execute() {
		String l;
		
		if(startsWithHAI()) {
			for(int i=tokens.size()-1;i>=0;i--) {
				ArrayList<Token> tokensPerLine = tokens.get(i);
				
				for(int j=tokensPerLine.size()-1;j>=0;j--) {
					l = tokensPerLine.get(j).getLexeme();	
	
					if(isAComment(l)!=0 || l.equals(Token.TLDR)) continue;
					else {
						if(l.equals(Token.KTHXBYE)) {
							System.out.println("tama");
							return true;
						}
						else {				
							validSyntax = false;
							return false;
						}
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
		lineNumber = 0;		
		orlyCount =0;
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
		clearTable();
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
		outputDisplay.setText("[!] Error detected in line " + lineNumber);
		
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
				readFile();
				interpretFile();
				if(execute() && validLexeme && validSyntax && validSemantics) showPass();
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
