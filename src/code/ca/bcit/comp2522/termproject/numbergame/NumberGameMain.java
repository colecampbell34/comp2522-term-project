package ca.bcit.comp2522.termproject.numbergame;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Provides a static method to launch the Number Game UI on the JavaFX Application Thread.
 * Manages the lifecycle of the game's Stage.
 *
 * @author colecampbell
 * @version 1.0
 */
public final class NumberGameMain
{

    // Keep track of the currently open stage for this specific game
    // This helps prevent accidental multiple openings if desired, or manage focus.
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
        // Ensure all UI creation and manipulation happens on the JavaFX Application Thread.
        Platform.runLater(() ->
                          {
                              try
                              {
                                  if (currentNumberGameStage != null && currentNumberGameStage.isShowing())
                                  {
                                      currentNumberGameStage.toFront();
                                      return; // Don't create a new one if already showing
                                  }

                                  final Stage stage = new Stage();
                                  currentNumberGameStage = stage;

                                  // Set the title for the window
                                  stage.setTitle("Number Sorting Game");

                                  stage.setOnHidden((WindowEvent event) ->
                                                    {
                                                        if (currentNumberGameStage == stage)
                                                        {
                                                            currentNumberGameStage = null;
                                                        }
                                                        // Execute the callback provided by the caller (e.g., latch.countDown())
                                                        if (onClose != null)
                                                        {
                                                            onClose.run();
                                                        }
                                                    });

                                  // Instantiate the actual game UI, passing the stage it should use.
                                  // The NumberGame constructor should set up the scene and potentially show the stage.
                                  new NumberGame(stage);

                                  stage.toFront(); // Bring the new window to the front

                              } catch (final Exception e)
                              {
                                  System.err.println("CRITICAL: Failed to launch Number Game on JavaFX Thread!");
                                  e.printStackTrace();

                                  if (onClose != null)
                                  {
                                      System.err.println("Executing onClose callback due to launch failure.");
                                      // Ensure callback runs on FX thread if it modifies FX state, otherwise direct call is fine for latch.
                                      onClose.run();
                                  }
                                  // Clean up stage if partially created
                                  if (currentNumberGameStage != null && !currentNumberGameStage.isShowing())
                                  {
                                      currentNumberGameStage = null;
                                  }
                              }
                          });
    }

    // Private constructor to prevent instantiation of this utility class
    private NumberGameMain()
    {
    }
}
