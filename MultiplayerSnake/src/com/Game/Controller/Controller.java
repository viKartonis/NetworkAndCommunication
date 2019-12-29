package com.game.controller;

import com.game.model.GameField;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import me.ippolitov.fit.snakes.SnakesProto;

public class Controller
{
    private GameField field;
    private NetworkController networkController;
    public Controller(int height, int width)
    {
        field = new GameField(height, width);
        networkController = new NetworkController();
    }

    public void handler(Stage stage)
    {
        EventHandler<KeyEvent> eventHandler = (KeyEvent e) ->
        {
            switch (e.getCode())
            {
                case UP:
                    //field.setDirection(Direction.UP);
                  networkController.sendDirection(SnakesProto.Direction.UP);

                    break;

                case DOWN:
                   // field.setDirection(Direction.DOWN);
                    networkController.sendDirection(SnakesProto.Direction.DOWN);

                    break;

                case LEFT:
                    //field.setDirection(Direction.LEFT);
                    networkController.sendDirection(SnakesProto.Direction.LEFT);

                    break;

                case RIGHT:
                    //field.setDirection(Direction.RIGHT);
                    networkController.sendDirection(SnakesProto.Direction.RIGHT);

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
