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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LexicalAnalyzer {
	private Stage stage;
	private Scene scene;
	private Group root;
	private Canvas canvas;
	public final static int WINDOW_WIDTH = 1500;
	public final static int WINDOW_HEIGHT = 900;
	
	//FOR FILE READING
	private FileChooser fileChooser = new FileChooser();
	private File file = new File("sample.lol");
	private String fileString="";
	private Scanner scanner;

	//FOR UI
	private Button fileButton = new Button("Select LOLCODE file");
	private Button executeButton = new Button("EXECUTE");
	private TextArea textArea = new TextArea();
    private TableColumn<Token, String> lexemefirstDataColumn, lexemesecondDataColumn;
    private TableColumn<Symbol, Symbol> symbolfirstDataColumn, symbolsecondDataColumn;
    private TableView<Token> lexemeTableView = new TableView<Token>();
    private TableView<Symbol> symbolTableView = new TableView<Symbol>(); 
	private int flag = 0; //flag checker if there is an invalid syntax
	ArrayList<Token> tokens = new ArrayList<Token>();
	Pattern possibleKeyword = Pattern.compile("SUM|DIFF|PRODUCKT|QUOSHUNT|MOD|BIGGR|SMALLR|BOTH|EITHER|WON|ANY|ALL"
									  +"|I|I HAS|BOTH|IS|IS NOW|O|YA|NO|IM|IM IN| IM OUTTA");
	
	
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
        
        //set preferences for displaying text area
        this.textArea.setLayoutX(0);
        this.textArea.setLayoutY(80);
        this.textArea.setPrefWidth(500);
        this.textArea.setPrefHeight(470);
        this.textArea.setEditable(false);

        //call to functions
		openFile();	
		generateLexemes();
		createTable("lexemes");
		createTable("symbols");
		
		root.getChildren().addAll(canvas, textArea, fileButton, executeButton);
		this.stage = stage;
		this.stage.setTitle("LOLCode Interpreter");
		this.stage.setScene(this.scene);
		this.stage.show();
	}
	
	
	//FUNCTIONS FOR EXECUTING LEXICAL ANALYZER
	
	private void getLexemes() {
		String classification, currentLexeme ="";

		//split file into lines
		String[] lines = fileString.split("\n");
			
		//process every line
		for(int i=0;i<lines.length;i++) {
			//split line into lexemes
			String[] lexemes = lines[i].split("\t| ");
			
			//process every lexeme
			for(int j=0;j<lexemes.length;j++) {
				//skip if empty string
				if(!lexemes[j].isEmpty()) {
					currentLexeme += lexemes[j];

					//check if the current lexeme is a token
					classification = checkLexeme(currentLexeme);				
					
					//if it is, then add it to the list of tokens
					if(classification != null) {
						//if string is detected, extract the contents inside the dbl quote
						if(classification.equals(Token.YARN_LITERAL_CLASSIFIER)) {
							
							//matcher to capture group
							Matcher m = Token.YARN_LITERAL.matcher(currentLexeme);
							
							//string buffer to get contents of captured group
							StringBuffer lexeme = new StringBuffer();
							
							//append all contents of captured group
							while (m.find()) {
							  lexeme.append(m.group(1).replace("\"", ""));
							}
							
							//add the start, string literal, and end quotes individually
							tokens.add(new Token(Token.STRING_DELIMITER, Token.STRING_DELIMITER_CLASSIFIER));
							tokens.add(new Token(lexeme.toString(), classification));
							tokens.add(new Token(Token.STRING_DELIMITER, Token.STRING_DELIMITER_CLASSIFIER));
							
						}else tokens.add(new Token(currentLexeme,classification));	//if not a string, add as is
						currentLexeme ="";	
						
					} else {
						currentLexeme += " ";
					}
					
					//ERROR DETECTION
					if(j==lexemes.length-1 && currentLexeme!="") {
						//prompt error dialog
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Error Dialog");
						alert.setHeaderText(null);
						alert.setContentText("[!] Error. There is an invalid syntax in the lolcode file.");
						alert.showAndWait();
						flag = 1; //set flag to 1 because there is an invalid syntax
						break;
					}
				}
			}	
			//stop iteration for checking lexemes
			if(flag==1) break; 
		}
		

		System.out.println("\nLEXEMES");
		for(int i=0;i<tokens.size();i++) {
			System.out.println(i+1 + ". " + tokens.get(i).getLexeme());
			System.out.println("   -" + tokens.get(i).getClassification() + "\n");
		}
	}
	
	//return classification if the current lexeme is a token
	public String checkLexeme(String currentLexeme) {
		if(Token.TOKEN_CLASSIFIER1.containsKey(currentLexeme)) return Token.TOKEN_CLASSIFIER1.get(currentLexeme);
		else if(!possibleKeyword(currentLexeme)) {
			if(Token.VARIABLE_IDENTIFIER.matcher(currentLexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.VARIABLE_IDENTIFIER);
			if(Token.FUNCTION_LOOP_IDENTIFIER.matcher(currentLexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.FUNCTION_LOOP_IDENTIFIER);
		}
		if(Token.NUMBR_LITERAL.matcher(currentLexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.NUMBR_LITERAL);
		if(Token.NUMBAR_LITERAL.matcher(currentLexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.NUMBAR_LITERAL);
		if(Token.YARN_LITERAL.matcher(currentLexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.YARN_LITERAL);
		if(tokens.get(tokens.size()-1).getClassification().equals(Token.I_HAS_A_CLASSFIER))	return Token.TOKEN_CLASSIFIER2.get(Token.VARIABLE_IDENTIFIER);
		
		return null;
	} 

	//check if the current lexeme is a possible keyword
	public boolean possibleKeyword(String currentLexeme) {
		//iterate through keys in the hashmap of classifiers
		for(Entry<String, String> token: Token.TOKEN_CLASSIFIER1.entrySet()) {
			//if the current lexeme is a substring of a keyword, return true
			if(token.getKey().contains(currentLexeme)) {
				System.out.println(currentLexeme);
				return true;
			}
		}
		
		//if the current lexeme is not a substring of any keyword, return false
		return false;
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
            	if(file.getAbsolutePath().matches(".*.lol$")) readFile();
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
			this.textArea.setText(fileString); 
			System.out.println(fileString);
		} catch(Exception a){
			System.out.println("file not found!");
		}
		
		//execute lexical analyzer after selecting lolcode file
		getLexemes();
	}
	
	private void resetAnalyzer() {
		//clear all values
		fileString = "";
		tokens.clear();
		lexemeTableView.getItems().clear();
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
    
	private void generateLexemes() {
		executeButton.setOnAction(e -> {
			readFile();
			if(flag==0) populateTable();
        });
	}
}
