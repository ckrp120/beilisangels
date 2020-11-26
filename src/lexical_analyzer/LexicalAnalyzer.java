package lexical_analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
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
	private File file = new File("lolcode/variables.lol");
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
    private boolean invalidSyntax,possibleKeywordDetected,readBack;
	ArrayList<Token> tokens = new ArrayList<Token>();
	
	
	public LexicalAnalyzer() {
		root = new Group();
		scene = new Scene(this.root,WINDOW_WIDTH,WINDOW_HEIGHT, Color.BISQUE);
		canvas = new Canvas(WINDOW_HEIGHT,WINDOW_HEIGHT);
		canvas.getGraphicsContext2D();
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
		this.stage.setTitle("LOLCode Interpreter");
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
			//0 - valid syntax
			//1 - invalid syntax
			//2 - invalid syntax, but process again because there's
			//	  a variable identifier detected as a possible keyword
			
			status = checkLexemes(lines[lineCheck]);
			
			//case 2
			if(status == 2) {
				lineCheck--;
				//process again starting from where an invalid syntax is detected
				status = checkLexemes(currentLexeme);
			}  
			//case 1 or case 2 and there's still an invalid syntax
			if(status == 1) break;
		}
	
		System.out.println("\nLEXEMES");
		for(int i=0;i<tokens.size();i++) {
			System.out.println(i+1 + ". " + tokens.get(i).getLexeme()+ ":" + tokens.get(i).getClassification() + "\n");
		}		
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
						}
					
					//if a comment is detected, ignore whatever comes after it
					//0 - not a comment
					//1 - one line comment (BTW)
					//2 - multiline comment (OBTW)
					} else if((commentDetected = isAComment(currentLexeme)) != 0) {
						//case 1: BTW (skip the current line)
						if(commentDetected == 1) {
							tokens.add(new Token(currentLexeme,classification));
							currentLexeme = "";
						//case 2: OBTW .. TLDR (must have their own lines)
						} else if(wordCheck == 0) {
							tokens.add(new Token(currentLexeme,classification));
							currentLexeme = "";
							String commentEnder;
							
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
					} else tokens.add(new Token(currentLexeme,classification));	
						
					currentLexeme ="";
					wordCheck++;
				}
			}	
		}
		
		//ERROR DETECTION
		
		//there's an invalid syntax, but process again because a variable identifier is detected as a possible keyword
		if(!currentLexeme.equals("") && possibleKeywordDetected && status!=2) {
			readBack=true;
			return 2;
		//there's an invalid syntax, stop iteration for getting lexemes
		} else if(currentLexeme!="") {
			invalidSyntax = true;
			return 1;
		}		
		
		return 0;
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
		invalidSyntax = false;
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
    	
    	//populate table
    	for(Token token: tokens) lexemeTableView.getItems().add(token);
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
		passIndicator.setImage(happyImg);
		lexicalIndicator.setImage(lexicalPassImg);
    }
    
	private void generateLexemes() {
		executeButton.setOnAction(e -> {
			readFile();
			getLexemes();
			if(!invalidSyntax) showPass();
			else showError();
        });
	}
}
