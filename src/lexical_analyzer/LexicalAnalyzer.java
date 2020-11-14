package lexical_analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
	
//	public final static int WINDOW_WIDTH = 1500;
//	public final static int WINDOW_HEIGHT = 900;
	
	public final static int WINDOW_WIDTH = 100;
	public final static int WINDOW_HEIGHT = 100;
	
	public LexicalAnalyzer() {
		root = new Group();
		scene = new Scene(this.root,WINDOW_WIDTH,WINDOW_HEIGHT);
		canvas = new Canvas(WINDOW_HEIGHT,WINDOW_HEIGHT);
		canvas.getGraphicsContext2D();
	}
	
	public void setStage(Stage stage) {
		openFile();
		getLexemes();
		
		root.getChildren().add(canvas);

		this.stage = stage;
		this.stage.setTitle("LOLCode Interpreter");
		this.stage.setScene(this.scene);
		this.stage.show();
	}
	
	private void openFile() {
		file = file_chooser.showOpenDialog(stage);
		
		try {
			scanner = new Scanner(file);
			
			//save file to a string
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				file_string += line += '\n';
			} 
			
			System.out.println(file_string);
		} catch(Exception e){
			System.out.println("file not found!");
		}		
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
			
			// System.out.println(curr_lexeme + " " + curr_pos);
			
			//check if the current lexeme is valid
			if(checkLexeme(curr_lexeme)) {
				accepted_lexeme = true;
				lexemes.add(curr_lexeme);
			}
		}
		
		System.out.println("\nLIST OF LEXEMES");
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
}
