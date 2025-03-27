package ca.bcit.comp2522.termproject.numbergame;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Launches the UI for the number game.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class NumberGameMain extends Application
{
    private static Stage    currentStage;
    private static Runnable onCloseCallback;

    /**
     * Launches the game.
     * @param callback the runnable object
     */
    public static void launchGame(final Runnable callback)
    {
        onCloseCallback = callback;
        Platform.runLater(() ->
                          {
                              try
                              {
                                  if (currentStage != null)
                                  {
                                      currentStage.close();
                                  }
                                  currentStage = new Stage();
                                  new NumberGameMain().start(currentStage);
                              } catch (final Exception e)
                              {
                                  e.printStackTrace();
                              }
                          });
    }

    /**
     * Starts the UI.
     * @param primaryStage the stage
     */
    @Override
    public void start(final Stage primaryStage)
    {
        final NumberGame game;
        game = new NumberGame(primaryStage);
        primaryStage.setOnHidden(e ->
                                 {
                                     if (onCloseCallback != null)
                                     {
                                         Platform.runLater(onCloseCallback);
                                     }
                                 });
    }
}