package lexical_analyzer;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage) {
		LexicalAnalyzer lexical_analyzer_stage = new LexicalAnalyzer();
		lexical_analyzer_stage.setStage(stage);
	}
}