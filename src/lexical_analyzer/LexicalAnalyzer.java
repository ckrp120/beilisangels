package lexical_analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
//import javafx.scene.control.cell.PropertyValueFactory; //wag muna idelete, baka importante sa table view??

public class LexicalAnalyzer {
	private Stage stage;
	private Scene scene;
	private Group root;
	private Canvas canvas;
	
	private FileChooser file_chooser = new FileChooser();
	private File file = new File("sample.lol"); //ginanto ko muna para execute na agad iciclick haha -tin
	private boolean isFileValid;
	private String file_string="";
	private String classification;
	private Scanner scanner;
    
	int curr_pos = 0;
	char curr_char;
	String curr_lexeme = "";
	boolean accepted_lexeme = false;
	ArrayList<Token> tokens = new ArrayList<Token>();
	
	public final static int WINDOW_WIDTH = 1500;
	public final static int WINDOW_HEIGHT = 900;
	
	private Button fileButton = new Button("Select LOLCODE file");
	private Button executeButton = new Button("EXECUTE");
	private TextArea textarea = new TextArea();
    private TableColumn<Token, String> lexemefirstDataColumn, lexemesecondDataColumn;
    private TableColumn<Symbol, Symbol> symbolfirstDataColumn, symbolsecondDataColumn;
    private TableView<Token> lexemetable_view = new TableView<Token>();
    private TableView<Symbol> symboltable_view = new TableView<Symbol>(); 

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
        this.textarea.setLayoutX(0);
        this.textarea.setLayoutY(80);
        this.textarea.setPrefWidth(500);
        this.textarea.setPrefHeight(470);
        this.textarea.setEditable(false);

        //call to functions
		openFile();	
		generateLexemes();
		createTable("lexemes");
		createTable("symbols");
		
		root.getChildren().addAll(canvas, textarea, fileButton, executeButton);
		
		this.stage = stage;
		this.stage.setTitle("LOLCode Interpreter");
		this.stage.setScene(this.scene);
		this.stage.show();
	}
	
	private void openFile() {
		//action for "select LOLCODE file" button
        fileButton.setOnAction(e -> { 
        	file = file_chooser.showOpenDialog(stage);
        	//no file chosen
            if(file == null) {
            	System.out.println("[!] User cancelled input dialog.");
            } else { //file chosen
            	//System.out.println(file.getAbsolutePath());
            	
            	//check if file extension ends with ,lol
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
				file_string += line += '\n';
			} 
			
			//add to text area the content of file read
			this.textarea.setText(file_string); 
			System.out.println(file_string);
		} catch(Exception a){
			System.out.println("file not found!");
		}
		
		//execute lexeme analyzer after selecting lolcode file
		getLexemes();
	}
	
	private void getLexemes() {
		while(curr_pos < file_string.length()-1) {
			//get current character and increment position
			curr_char = file_string.charAt(curr_pos);
			curr_pos++;
			
			//if the previous formed lexeme is accepted, make a new lexeme to be checked
			if(accepted_lexeme) {
				accepted_lexeme = false;
				curr_lexeme = "";
				
				//case that the accepted lexeme has operands, ignore white spaces
				while(isSpace(curr_char)) {
					curr_char = file_string.charAt(curr_pos);
					curr_pos++;
				}
			}else {
				
				
			}
			
			//if current characters are next line followed by spaces/tabs, ignore and increment position
			if(curr_char == '\n') {
				do {
					curr_char = file_string.charAt(curr_pos);
					curr_pos++;
				} while(isSpace(curr_char));
			}
			
			//concatenate the current character to the current lexeme
			curr_lexeme += curr_char;
			
			
			classification = checkLexeme(curr_lexeme);
			//check if the current lexeme is valid
			if(classification != null) {
				accepted_lexeme = true;
				tokens.add(new Token(curr_lexeme,classification));
			}
		}
		
		System.out.println("\nLIST OF LEXEMES");
		for(int i=0;i<tokens.size();i++) {
			System.out.println(i+1 + ". " + tokens.get(i).getLexeme() + " " + tokens.get(i).getClassification());
		}
	}
	
	
//	put(Token.VARIABLE_IDENTIFIER,Token.VARIABLE_IDENTIFIER_CLASSIFIER);   
//	put(Token.FUNCTION_LOOP_IDENTIFIER,FUNCTION_LOOP_IDENTIFIER_CLASSIFIER);   
//	put(Token.NUMBR_LITERAL,Token.NUMBR_LITERAL_CLASSIFIER); 
//	put(Token.NUMBAR_LITERAL,Token.NUMBAR_LITERAL_CLASSIFIER); 
//	put(Token.YARN_LITERAL,Token.YARN_LITERAL_CLASSIFIER); 
	
	//return true if the current lexeme is valid
	public String checkLexeme(String curr_lexeme) {
		
		//check if lexeme exists in the hashmap
		if(Token.TOKEN_CLASSIFIER1.containsKey(curr_lexeme)) return Token.TOKEN_CLASSIFIER1.get(curr_lexeme);
//		if(Token.VARIABLE_IDENTIFIER.matcher(curr_lexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.VARIABLE_IDENTIFIER);
//		if(Token.FUNCTION_LOOP_IDENTIFIER.matcher(curr_lexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.FUNCTION_LOOP_IDENTIFIER);
		if(Token.NUMBR_LITERAL.matcher(curr_lexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.NUMBR_LITERAL);
		if(Token.NUMBAR_LITERAL.matcher(curr_lexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.NUMBAR_LITERAL);
		if(Token.YARN_LITERAL.matcher(curr_lexeme).matches()) return Token.TOKEN_CLASSIFIER2.get(Token.YARN_LITERAL);
		
		return null;
	} 
	
	public boolean isSpace(char c) {
		return c == ' ' || c == '\t';                                 
	}
	
	//add this to remove warnings for table views
    @SuppressWarnings("unchecked")
	private void createTable(String type) {
    	if(type == "lexemes") {
    		//column header naming
        	lexemefirstDataColumn = new TableColumn<>("Lexeme");
        	lexemesecondDataColumn = new TableColumn<>("Classification"); 
        	


        
//        	lexemefirstDataColumn.setCellValueFactory(new PropertyValueFactory<>("lexemes"));
//        	lexemesecondDataColumn.setCellValueFactory(new PropertyValueFactory<>("classifications"));
        	
        	
        	//set table view column width preference
        	lexemefirstDataColumn.setMinWidth(250);
        	lexemesecondDataColumn.setMinWidth(250);
        	
        	//set table view size preference
        	lexemetable_view.setLayoutX(500);
        	lexemetable_view.setLayoutY(50);
        	lexemetable_view.setPrefHeight(500);
        	
        	//not editable, output should only based on analyzer
        	lexemetable_view.setEditable(false);
        	lexemetable_view.getSelectionModel().setCellSelectionEnabled(true);
        	lexemetable_view.getColumns().addAll(lexemefirstDataColumn, lexemesecondDataColumn);
            root.getChildren().add(lexemetable_view);
    	} else if(type == "symbols"){
        	symbolfirstDataColumn = new TableColumn<>("Identifier"); 
        	symbolsecondDataColumn = new TableColumn<>("Value"); 

//        	symbolfirstDataColumn.setCellValueFactory(new PropertyValueFactory<>("identifiers"));
//        	symbolsecondDataColumn.setCellValueFactory(new PropertyValueFactory<>("values"));
        	
        	//set table view column width preference
        	symbolfirstDataColumn.setMinWidth(250);
        	symbolsecondDataColumn.setMinWidth(250);
        	
        	//set table view size preference
        	symboltable_view.setLayoutX(1000);
        	symboltable_view.setLayoutY(50);
        	symboltable_view.setPrefHeight(500);

        	//not editable, output should only based on analyzer
        	symboltable_view.setEditable(false);
        	symboltable_view.getSelectionModel().setCellSelectionEnabled(true);
        	symboltable_view.getColumns().addAll(symbolfirstDataColumn, symbolsecondDataColumn);
            root.getChildren().add(symboltable_view);
    	} 
    }
    
    
    private void populateTable() {
    	
    	//select attribute to show in the column
    	lexemefirstDataColumn.setCellValueFactory(new PropertyValueFactory<>("lexeme"));
    	lexemesecondDataColumn.setCellValueFactory(new PropertyValueFactory<>("classification"));
    	
    	//populate table
    	for(Token token: tokens)
			lexemetable_view.getItems().add(token);
    }
    
	private void generateLexemes() {
		executeButton.setOnAction(e -> {
			readFile(); //ginanto ko muna para execute na agad iciclick haha -tin
			populateTable();
        });
	
	}
	
	private void resetAnalyzer() {
		
		//clear all values
		file_string = "";
		curr_pos = 0;
		curr_char = Character.MIN_VALUE;
		curr_lexeme = "";
		accepted_lexeme = false;
		tokens.clear();
		lexemetable_view.getItems().clear();
		
	}
}
