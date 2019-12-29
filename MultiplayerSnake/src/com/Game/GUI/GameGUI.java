package com.game.gui;

import com.game.controller.Controller;
import com.game.model.Coord;
import com.game.model.GameField;
import com.game.model.Snake;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.LinkedList;

public class GameGUI
{
    private GameField field;
    private final int increase = 20;
    private Controller controller;
    private LinkedList<Coord> snake;
    private GraphicsContext graphicsContext;
    private Canvas canvas;
    private Stage stage;
    private Group root;
    private Coord food;
    private boolean lost = false;
    private int height;
    private int width;

    public GameGUI(Canvas canvas, Stage stage, Group root)
    {
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.canvas = canvas;
        this.stage = stage;
        this.root = root;
    }
    public void init(int height, int width)
    {
        this.height = height;
        this.width = width;
        controller = new Controller(height, width);
        field = controller.getField();
    }

    public void lostWindow()
    {
        Scene lostScene = new Scene(root, 400, 300);
        Button tryAgain = new Button("Try again");
        Button exit = new Button("Exit");

        stage.setScene(lostScene);
    }

    public void draw()
    {
        field.updateField();
        lost = field.getLost();
        if(lost)
        {
            lostWindow();
            stage.close();
        }
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0,  width, height);

        food = field.getFood();
        graphicsContext.setFill(Color.BLUE);
        graphicsContext.fillRect(food.getX(), food.getY(), increase, increase);

        snake = field.getSnakeCoord();
        graphicsContext.setFill(Color.BISQUE);
        for (Coord i : snake)
        {
            graphicsContext.fillRect(i.getX(), i.getY(), increase , increase);
        }
    }

    public Controller getController()
    {
        return controller;
    }

    public Snake getSnake()
    {
        return field.getSnake();
    }
}
