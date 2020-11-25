package lexical_analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private File file = new File("lolcode/arith.lol");
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
    private boolean invalidSyntax = false; //flag checker if there is an invalid syntax
	private int lineCheck;
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
	
	private void checkLexemes() {
	    int currPos;
	    char currChar;
	    boolean acceptedLexeme;
		String classification, currentLexeme;

		//split file into lines
		String[] lines = fileString.split("\n");
		
		//process every line
		for(String line:lines) {
			lineCheck++;

			//if the current line has no code, continue to the next line
			if(line.isEmpty()) continue;
			
			currPos=0;
			acceptedLexeme = false;
			currentLexeme = "";
		
			//ignore spaces/tabs at the beginning of the line
			do {
				currChar = line.charAt(currPos);
				currPos++;
			} while(isSpace(currChar));
			
			currPos--;
			
			//start checking the lexemes 
			while(currPos < line.length()) {
				//get current character and increment position
				currChar = line.charAt(currPos);
				currPos++;
	
				//if the previous formed lexeme is accepted, ignore the next white space/s
				if(acceptedLexeme) {
					acceptedLexeme = false;
	
					while(isSpace(currChar)) {
						currChar = line.charAt(currPos);
						currPos++;
					}
				}
	
				//concatenate the current character to the current lexeme
				currentLexeme += currChar;

				//if the end of the line is reached or a space is detected, check if the current lexeme is a token
				if(currPos==line.length() || isSpace(line.charAt(currPos))) {
					classification = checkLexeme(currentLexeme);
					
					//if it is, then add it to the list of tokens
					if(classification != null) {
						acceptedLexeme = true;
						
						//if string is detected, add the start quote, string literal, and end quote individually
						if(classification.equals(Token.YARN_LITERAL_CLASSIFIER)) {						
							//matcher to capture group
							Matcher m = Token.YARN_LITERAL.matcher(currentLexeme);
		
							if(m.find()) {
								tokens.add(new Token(m.group(1), Token.STRING_DELIMITER_CLASSIFIER));
								tokens.add(new Token(m.group(2), classification));
								tokens.add(new Token(m.group(3), Token.STRING_DELIMITER_CLASSIFIER));
							}
						}else tokens.add(new Token(currentLexeme,classification));	//if not a string, add as is
						
						currentLexeme ="";
					}
				}	
			}
			
			//ERROR DETECTION
			if(currentLexeme!="") {
				invalidSyntax = true; //set invalidSyntax to true because there is an invalid syntax
				break; //stop iteration for checking lexemes
			}
		}
	
		System.out.println("\nLEXEMES");
		for(int i=0;i<tokens.size();i++) {
			System.out.println(i+1 + ". " + tokens.get(i).getLexeme()+ ":" + tokens.get(i).getClassification() + "\n");
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
		if(!tokens.isEmpty()) {
			if(tokens.get(tokens.size()-1).getClassification().equals(Token.I_HAS_A_CLASSFIER))	return Token.VARIABLE_IDENTIFIER_CLASSIFIER;
		}
		return null;
	} 

	//check if the current lexeme is a possible keyword
	public boolean possibleKeyword(String currentLexeme) {
		//iterate through keys in the hashmap of classifiers
		for(Entry<String, String> token: Token.TOKEN_CLASSIFIER.entrySet()) {
			//if the current lexeme is a substring of a keyword, return true
			if(token.getKey().contains(currentLexeme)) return true;
		}
		//if the current lexeme is not a substring of any keyword, return false
		return false;
	}
	
	//check if the character is a space
	public boolean isSpace(char c) {
		return c == ' ' || c == '\t';                                 
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
			checkLexemes();
			if(!invalidSyntax) showPass();
			else showError();
        });
	}
}
