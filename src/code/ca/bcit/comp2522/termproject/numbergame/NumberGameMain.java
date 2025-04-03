package ca.bcit.comp2522.termproject.numbergame;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Objects;

/**
 * Provides a static method to launch the Number Game UI on the JavaFX Application Thread.
 * Manages the lifecycle of the game's Stage.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class NumberGameMain
{
    private static Stage currentNumberGameStage;

    /**
     * Launches a new Number Game window safely on the JavaFX Application Thread.
     * If an instance is already open, it brings it to the front.
     *
     * @param onClose A Runnable to execute *after* the launched game Stage has been closed/hidden.
     *                This is crucial for signaling the waiting main thread.
     */
    public static void launchGame(final Runnable onClose)
    {
        Objects.requireNonNull(onClose,
                               "Runnable cannot be null");

        Platform.runLater(() ->
                          {
                              try
                              {
                                  if (currentNumberGameStage != null &&
                                      currentNumberGameStage.isShowing())
                                  {
                                      currentNumberGameStage.toFront();
                                      return;
                                  }

                                  final Stage stage;
                                  stage                  = new Stage();
                                  currentNumberGameStage = stage;

                                  // Set the title for the window
                                  stage.setTitle("Number Game");

                                  stage.setOnHidden((final WindowEvent event) ->
                                                    {
                                                        if (currentNumberGameStage == stage)
                                                        {
                                                            currentNumberGameStage = null;
                                                        }

                                                        Objects.requireNonNull(onClose,
                                                                               "Runnable cannot be null");
                                                        onClose.run();
                                                    });

                                  // Instantiate the actual game UI, passing the stage it should use.
                                  // The NumberGame constructor should set up the scene and potentially show the stage.
                                  new NumberGame(stage);

                                  stage.toFront(); // Bring the new window to the front

                              } catch (final Exception e)
                              {
                                  e.printStackTrace();
                              }
                          });
    }
}
