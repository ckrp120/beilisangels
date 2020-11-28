package lolcodeinterpreter;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage) {
		Interpreter interpreterStage = new Interpreter();
		interpreterStage.setStage(stage);
	}
}