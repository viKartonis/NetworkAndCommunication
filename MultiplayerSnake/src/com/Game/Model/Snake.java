package com.game.model;

import java.util.LinkedList;
import java.util.function.BiFunction;

public class Snake
{
    private LinkedList<Coord> listCoord;
    private Direction direction;
    private int score = 0;

    public Snake()
    {
        listCoord = new LinkedList<>();
        direction = Direction.UP;
    }

    public Snake(LinkedList<Coord> coords, Direction direction)
    {
        listCoord = coords;
        this.direction = direction;
    }

    public void appendTail(Coord coord)
    {
        listCoord.add(coord);
    }

    public Direction getDirection()
    {
        return direction;
    }

    public Coord getHead()
    {
        return listCoord.getFirst();
    }
    public Coord getTail()
    {
        return listCoord.getLast();
    }
    public void remove(int index)
    {
        listCoord.remove(index);
    }
    public int size()
    {
        return listCoord.size();
    }
    public void setDirection(Direction direction)
    {
        this.direction = direction;
    }

    public LinkedList<Coord> getSnake()
    {
        return listCoord;
    }

    public void move(BiFunction<Integer, Integer, Coord> coordProducer)
    {
        int beginX = listCoord.getFirst().getX(),
                beginY = listCoord.getFirst().getY();
        listCoord.removeLast();

        switch (direction)
        {
            case UP:
            {
                listCoord.push(coordProducer.apply(beginX, beginY - 1));
                break;
            }
            case DOWN:
            {
                listCoord.push(coordProducer.apply(beginX, beginY + 1));
                break;
            }
            case LEFT:
            {
                listCoord.push(coordProducer.apply(beginX - 1, beginY));
                break;
            }
            case RIGHT:
            {
                listCoord.push(coordProducer.apply(beginX + 1, beginY));
                break;
            }
        }
    }
    public void incrementScore()
    {
        score++;
    }
}
