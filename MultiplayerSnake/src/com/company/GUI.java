package com.company;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.LinkedList;

public class GUI
{
    private GameField field;
    private final int increase = 20;
    private Controller controller;
    private LinkedList<Coord> snake;
    private GraphicsContext graphicsContext;
    private Canvas canvas;
    private Stage stage;
    private Coord food;
    private boolean lost = false;

    public GUI(Canvas canvas, Stage stage)
    {
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.canvas = canvas;
        this.stage = stage;
    }
    public void init()
    {
        controller = new Controller(30, 40);
        field = controller.getField();
    }

    public void draw()
    {
        field.updateField();
        lost = field.getLost();
        if(lost)
        {
            stage.close();
        }
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, 800, 600);

        food = field.getFood();
        graphicsContext.setFill(Color.BLUE);
        graphicsContext.fillRect(food.getX() * increase, food.getY() * increase, increase, increase);

        snake = field.getSnake();
        graphicsContext.setFill(Color.BISQUE);
        for (Coord i : snake)
        {
            graphicsContext.fillRect(i.getX() * increase, i.getY() * increase, increase, increase);
        }
    }

    public Controller getController()
    {
        return controller;
    }

}
