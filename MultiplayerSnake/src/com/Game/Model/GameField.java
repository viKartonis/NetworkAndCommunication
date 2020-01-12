package com.game.model;

import java.util.*;
import java.util.function.BiFunction;

public class GameField
{
    private int width;
    private int height;
    private GameState[][] field;
    private boolean lost;
    private boolean pause;
    private Map<Integer, Snake> snakes;
    private BiFunction<Integer, Integer, Coord> coordProducer =
            (x, y) -> new Coord((x + width) % width, (y + height) % height);
    private List<Coord> foods;
    private Random random = new Random();
    private int foodNumber;
    private float probability;
    private float probabilityPerPlayer;
    List<Integer> needDelete;


    public GameField()
    {
        snakes = new HashMap<>();
        foods = new LinkedList<>();
    }

    public void setField(int height, int width, int foodNumber, float probability, float probabilityPerPlayer)
    {
        this.width = width;
        this.height = height;
        field = new GameState[height][width];
        lost = false;
        pause = false;
        snakes.clear();
        foods.clear();
        snakes.put(0, new Snake());
        this.foodNumber = foodNumber;
        this.probability = probability;
        this.probabilityPerPlayer = probabilityPerPlayer;
        initField();
    }

    private void clearField()
    {
        for(int i = 0; i < height; ++i)
        {
            for (int j = 0; j < width; ++j)
            {
                field[i][j] = GameState.EMPTY;
            }
        }
    }

    private void initField()
    {
        clearField();
        for (int i = 0; i < foodNumber; ++i)
        {
            generateFood();
        }

        int i = height / 2;
        int j = width / 2;

        field[i][j] = GameState.SNAKE;
        field[i-1][j] = GameState.SNAKE;
        snakes.get(0).appendTail(new Coord(j, i));
        snakes.get(0).appendTail(new Coord(j, i-1));
    }

    public void initReceivedField(int height, int width, float probability)
    {
        this.width = width;
        this.height = height;
        this.probability = probability;
        field = new GameState[height][width];
    }

    private Coord generateCoord()
    {
        int j = random.nextInt(width);
        int i = random.nextInt(height);
        return new Coord(j, i);
    }

    private void generateFood()
    {
        Coord c = generateCoord();
        field[c.getY()][c.getX()] = GameState.FOOD;
        foods.add(c);
    }

    private Coord find5x5()
    {
        for (int y = 0; y < height - 5; ++y)
        {
            for (int x = 0; x < width - 5; ++x)
            {

                boolean isEmpty = true;
                for (int i = y; i < 5 && isEmpty; ++i)
                {
                    for (int j = x; j < 5 && isEmpty; ++j)
                    {
                        if (GameState.SNAKE == field[i][j])
                        {
                            isEmpty = false;
                        }
                    }
                }

                if (isEmpty)
                {
                    return new Coord(x, y);
                }

            }
        }
        return null;
    }

    private boolean generateSnake(int id)
    {
        Coord coord = find5x5();

        int j = random.nextInt(4);
        int i = random.nextInt(4);

        Coord head;
        if (null != coord)
        {
            head = new Coord(j + coord.getX(), i + coord.getY());
            field[head.getY()][head.getX()] = GameState.SNAKE;

            Coord tail;
            if (0 < head.getY() - 1)//height != head.getY() + 1 ||
            {
                tail = new Coord(head.getX(), head.getY() - 1);
            }
            else
            {
                tail = new Coord(head.getX(), head.getY() + 1);
            }
            snakes.put(id, new Snake());
            snakes.get(id).appendTail(head);
            snakes.get(id).appendTail(tail);
            return true;
        }
        return false;
    }

    public void updateField()
    {
        needDelete = new LinkedList<>();
        for (Map.Entry<Integer, Snake> snakeMapEntry : snakes.entrySet())
        {
            Snake snake = snakeMapEntry.getValue();
            if (snake.getDirection() == null)
                return;

            Coord last = snake.getTail();
            int endX = snake.getTail().getX();
            int endY = snake.getTail().getY();
            field[endY][endX] = GameState.EMPTY;

            snake.move(coordProducer);

            int headX = snake.getHead().getX();
            int headY = snake.getHead().getY();

            if (field[headY][headX] == GameState.SNAKE)
            {
                for(Coord coord : snake.getSnake())
                {
                    if(probability <= random.nextFloat())
                    {
                        field[coord.getY()][coord.getX()] = GameState.FOOD;
                    }
                    else
                    {
                        field[coord.getY()][coord.getX()] = GameState.EMPTY;
                    }
                }
                lost = true;
                needDelete.add(snakeMapEntry.getKey());
                break;
            }

            if (field[headY][headX] == GameState.FOOD)
            {
                snake.incrementScore();
                field[endY][endX] = GameState.SNAKE;
                snake.appendTail(last);
                foods.remove(snake.getHead());
                generateFood();
            }
            field[headY][headX] = GameState.SNAKE;
        }

        for (Integer integer : needDelete)
        {
            snakes.remove(integer);
        }
    }

    public void setDirection(Direction direction, int id)
    {
        Snake s = snakes.get(id);
        if (null != s)
        {
            if(direction != Direction.getOpposite(s.getDirection()))
            {
                s.setDirection(direction);
            }
        }
      //  else System.out.println(id + " is null");
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

    public LinkedList<Coord> getSnakeCoord(int index)
    {
        return snakes.get(index).getSnake();
    }

    public List<Coord> getFood()
    {
        return foods;
    }

    public Snake getSnake(int index)
    {
        return snakes.get(index);
    }

    public int getSnakeNumbers()
    {
        return snakes.size();
    }

    public boolean addSnake(int id)
    {
        if (generateSnake(id))
        {
            generateFood();
            return true;
        }
        return false;
    }

    public Map<Integer, Snake> getSnakes()
    {
        return snakes;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getFoodStatic()
    {
        return foods.size();
    }

    public void setSnakes(Map<Integer, Snake> snakes)
    {
        this.snakes = snakes;
    }

    public void setFood(List<Coord> food)
    {
        foods = food;
    }

    public void apply()
    {
        clearField();
        for (Map.Entry<Integer, Snake> snakeEntry : snakes.entrySet())
        {
            for (Coord coord : snakeEntry.getValue().getSnake())
            {
                field[coord.getY()][coord.getX()] = GameState.SNAKE;
            }
        }

        for(Coord coord : foods)
        {
            field[coord.getY()][coord.getX()] = GameState.FOOD;
        }
    }

    public List<Integer> getNeedDelete()
    {
        return needDelete;
    }

    public float getFoodDrop()
    {
        return probability;
    }
}

