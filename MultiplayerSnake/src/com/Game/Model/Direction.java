package com.game.model;

public enum Direction
{
    UP,
    DOWN,
    RIGHT,
    LEFT;

    public static Direction getOpposite(Direction direction)
    {
        switch (direction)
        {
            case UP:
            {
                return DOWN;
            }
            case DOWN:
            {
                return UP;
            }
            case RIGHT:
            {
                return LEFT;
            }
            default:
            {
                return RIGHT;
            }
        }
    }
}
