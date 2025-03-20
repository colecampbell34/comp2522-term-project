package ca.bcit.comp2522.termproject.numbergame;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

/**
 * The main class for the number game.
 */
public class NumberGameMain extends Application
{
    /**
     * Overrides the javafx start method.
     * @param primaryStage the stage
     */
    @Override
    public void start(final Stage primaryStage)
    {
        new NumberGame(primaryStage);
    }

    /**
     * The entry point for the project.
     * @param args unused
     */
    public static void main(final String[] args)
    {
        launch(args);
    }
}
