package com.game.controller;

import me.ippolitov.fit.snakes.SnakesProto;

public class NetworkController implements ControllerGame
{
    private  SnakesProto.GameMessage.SteerMsg steerMessage;
    private  SnakesProto.GameMessage gameMessage;

    public void sendDirection(SnakesProto.Direction direction)
    {
        steerMessage = SnakesProto.GameMessage
                .SteerMsg.newBuilder()
                .setDirection(direction).build();

        gameMessage = SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMessage).build();
    }

    public SnakesProto.GameMessage getGameMessage()
    {
        return gameMessage;
    }
}
