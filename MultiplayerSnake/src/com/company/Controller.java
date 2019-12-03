package com.company;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Controller
{
    private GameField field;

    public Controller(int height, int width)
    {
        field = new GameField(height, width);
    }

    public void handler(Stage stage)
    {
        EventHandler<KeyEvent> eventHandler = (KeyEvent e) ->
        {
            switch (e.getCode())
            {
                case UP:
                    field.setDirection(Direction.UP);
                    break;

                case DOWN:
                    field.setDirection(Direction.DOWN);
                    break;

                case LEFT:
                    field.setDirection(Direction.LEFT);
                    break;

                case RIGHT:
                    field.setDirection(Direction.RIGHT);
                    break;
            }
        };
        stage.addEventFilter(KeyEvent.KEY_PRESSED, eventHandler);
    }
    public GameField getField()
    {
        return field;
    }

}
