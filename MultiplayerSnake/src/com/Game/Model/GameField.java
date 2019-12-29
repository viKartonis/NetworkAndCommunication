package com.game.model;

import java.util.LinkedList;
import java.util.Random;
import java.util.function.BiFunction;

public class GameField
{
    private int width;//40
    private int height;//30
    private GameState[][] field;
    private boolean lost;
    private boolean pause;
    private Snake snake;
    private BiFunction<Integer, Integer, Coord> coordProducer =
            (x, y) -> new Coord((x + width) % width, (y + height) % height);
    private Coord food;

    public GameField(int height, int width)
    {
        this.width = width;
        this.height = height;
        field = new GameState[height][width];
        lost = false;
        pause = false;
        snake = new Snake();
        initField();
    }

    private void initField()
    {
        generateFood();
        for(int i = 0; i < height; ++i)
        {
            for (int j = 0; j < width; ++j)
            {
                if (i == height / 2 && j == width / 2)
                {
                    System.out.println(i +  " " + j);
                    field[i][j] = GameState.SNAKE;
                    snake.appendTail(new Coord(j, i));
                }
                else
                {
                    field[i][j] = GameState.EMPTY;
                }
            }
        }
    }

    private void generateFood()
    {
        Random randomX = new Random();
        int j = randomX.nextInt(width);
        Random randomY = new Random();
        int i = randomY.nextInt(height);
        field[i][j] = GameState.FOOD;
        food = new Coord(j, i);
    }


    public void updateField()
    {
        if (snake.getDirection() == null)
            return;

        int endX = snake.getTail().getX();
        int endY = snake.getTail().getY();
        field[endY][endX] = GameState.EMPTY;

        snake.move(coordProducer);

        int headX = snake.getHead().getX();
        int headY = snake.getHead().getY();

        if (field[headY][headX] == GameState.SNAKE)
        {
            lost = true;
            return;
        }

        if (snake.getHead().equals(food))
        {
            snake.incrementScore();
            field[endY][endX] = GameState.SNAKE;
            snake.appendTail(new Coord(endX, endY));
            generateFood();
        }
        field[headY][snake.getHead().getX()] = GameState.SNAKE;
    }

    public void setDirection(Direction direction)
    {
        snake.setDirection(direction);
    }

    public boolean getLost()
    {
        return lost;
    }
    public void setPause(){
        pause = !pause;
    }

    public boolean isPaused()
    {
        return pause;
    }

    public Coord currentCoord()
    {
        return new Coord(snake.getTail().getX(), snake.getTail().getY());
    }

    public LinkedList<Coord> getSnakeCoord()
    {
        return snake.getSnake();
    }

    public Coord getFood()
    {
        return food;
    }

    public Snake getSnake()
    {
        return snake;
    }
}

