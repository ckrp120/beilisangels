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
	private File file;
	private String file_string="";
	private Scanner scanner;
    
	int curr_pos = 0;
	char curr_char;
	String curr_lexeme = "";
	boolean accepted_lexeme = false;
	ArrayList<String> lexemes = new ArrayList<String>();
	
	public final static int WINDOW_WIDTH = 1500;
	public final static int WINDOW_HEIGHT = 900;
	
	private Button fileButton = new Button("Select LOLCODE file");
	private Button executeButton = new Button("EXECUTE");
	private TextArea textarea = new TextArea();
    private TableColumn<Lexemes, Lexemes> lexemefirstDataColumn, lexemesecondDataColumn;
    private TableColumn<Symbols, Symbols> symbolfirstDataColumn, symbolsecondDataColumn;
    private TableView<Lexemes> lexemetable_view = new TableView<Lexemes>();
    private TableView<Symbols> symboltable_view = new TableView<Symbols>(); 

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
        });
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
			
			System.out.println(curr_lexeme + " " + curr_pos);
			
			//check if the current lexeme is valid
			if(checkLexeme(curr_lexeme)) {
				accepted_lexeme = true;
				lexemes.add(curr_lexeme);
			}
		}
		
		System.out.println("\nLIST OF LEXEMES\n");
		for(int i=0;i<lexemes.size();i++) {
			System.out.println(i+1 + ". " + lexemes.get(i));
		}
	}
	
	
	//return true if the current lexeme is valid
	public boolean checkLexeme(String curr_lexeme) {
		return curr_lexeme.equals(Lexemes.HAI) ||
				curr_lexeme.equals(Lexemes.KTHXBYE) ||
				curr_lexeme.equals(Lexemes.BTW) ||
				curr_lexeme.equals(Lexemes.OBTW) ||
				curr_lexeme.equals(Lexemes.TLDR) ||
				curr_lexeme.equals(Lexemes.I_HAS_A) ||
				curr_lexeme.equals(Lexemes.ITZ) ||
				curr_lexeme.equals(Lexemes.R) ||
				curr_lexeme.equals(Lexemes.SUM_OF) ||
				curr_lexeme.equals(Lexemes.DIFF_OF) ||
				curr_lexeme.equals(Lexemes.PRODUKT_OF) ||
				curr_lexeme.equals(Lexemes.QUOSHUNT_OF) ||
				curr_lexeme.equals(Lexemes.MOD_OF) ||
				curr_lexeme.equals(Lexemes.BIGGR_OF) ||
				curr_lexeme.equals(Lexemes.SMALLR_OF) ||
				curr_lexeme.equals(Lexemes.BOTH_OF) ||
				curr_lexeme.equals(Lexemes.EITHER_OF) ||
				curr_lexeme.equals(Lexemes.WON_OF) ||
				curr_lexeme.equals(Lexemes.NOT) ||
				curr_lexeme.equals(Lexemes.ANY_OF) ||
				curr_lexeme.equals(Lexemes.ALL_OF) ||
				curr_lexeme.equals(Lexemes.BOTH_SAEM) ||
				curr_lexeme.equals(Lexemes.DIFFRINT) ||
				curr_lexeme.equals(Lexemes.SMOOSH) ||
				curr_lexeme.equals(Lexemes.MAEK) ||
				curr_lexeme.equals(Lexemes.A) ||
				curr_lexeme.equals(Lexemes.IS_NOW_A) ||
				curr_lexeme.equals(Lexemes.VISIBLE) ||
				curr_lexeme.equals(Lexemes.GIMMEH) ||
				curr_lexeme.equals(Lexemes.O_RLY) ||
				curr_lexeme.equals(Lexemes.YA_RLY) ||
				curr_lexeme.equals(Lexemes.MEBBE) ||
				curr_lexeme.equals(Lexemes.NO_WAI) ||
				curr_lexeme.equals(Lexemes.OIC) ||
				curr_lexeme.equals(Lexemes.WTF) ||
				curr_lexeme.equals(Lexemes.OMG) ||
				curr_lexeme.equals(Lexemes.OMGWTF) ||
				curr_lexeme.equals(Lexemes.IM_IN_YR) ||
				curr_lexeme.equals(Lexemes.UPPIN) ||
				curr_lexeme.equals(Lexemes.NERFIN) ||
				curr_lexeme.equals(Lexemes.YR) ||
				curr_lexeme.equals(Lexemes.TIL) ||
				curr_lexeme.equals(Lexemes.WILE) ||
				curr_lexeme.equals(Lexemes.IM_OUTTA_YR);			
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
    
	private void generateLexemes() {
		executeButton.setOnAction(e -> {
			System.out.println("Insert functionalities here for symbols");
        });
	
	}
}
