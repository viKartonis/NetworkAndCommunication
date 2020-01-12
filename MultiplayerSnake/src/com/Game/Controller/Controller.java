package com.game.controller;

import com.game.model.GameField;
import com.game.network.UDPUnicast;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import me.ippolitov.fit.snakes.SnakesProto;


public class Controller implements EventHandler<KeyEvent>
{
    private GameField field;
    private UDPUnicast udpUnicast;

    public Controller(GameField field, UDPUnicast udpUnicast)
    {
        this.field = field;
        this.udpUnicast = udpUnicast;
    }

    public GameField getField()
    {
        return field;
    }


    @Override
    public void handle(KeyEvent e)
    {
        switch (e.getCode())
        {
            case UP:
                udpUnicast.sendSteer(SnakesProto.Direction.UP);
                break;

            case DOWN:
                udpUnicast.sendSteer(SnakesProto.Direction.DOWN);
                break;

            case LEFT:
                udpUnicast.sendSteer(SnakesProto.Direction.LEFT);
                break;

            case RIGHT:
                udpUnicast.sendSteer(SnakesProto.Direction.RIGHT);
                break;
        }
    }
}
