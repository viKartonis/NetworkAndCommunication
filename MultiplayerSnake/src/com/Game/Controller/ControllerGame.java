package com.game.controller;

import me.ippolitov.fit.snakes.SnakesProto;

interface ControllerGame
{
    void sendDirection(SnakesProto.Direction direction);
}
