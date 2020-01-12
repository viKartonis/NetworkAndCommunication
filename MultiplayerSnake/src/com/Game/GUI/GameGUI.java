package com.game.gui;

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

import java.util.List;
import java.util.Map;

public class GameGUI
{
    private GameField field;
    private final int increase = 20;
    private GraphicsContext graphicsContext;
    private Canvas canvas;
    private Stage stage;
    private Group root;
    private int lost = 0;
    private int height;
    private int width;
    private int updateTime;
    private Runnable onLostCallBack;
    private int playerId;

    public GameGUI(Runnable onLostCallBack, Canvas canvas, Stage stage, Group root, GameField field, int updateTime,
                   int playerId)
    {
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.canvas = canvas;
        this.stage = stage;
        this.root = root;
        this.field = field;
        this.updateTime = updateTime;
        this.onLostCallBack = onLostCallBack;
        this.playerId = playerId;
    }

    public void init(int height, int width, int foodNumber, float probability, float foodPerPlayer)
    {
        this.height = height;
        this.width = width;
        field.setField(height, width, foodNumber, probability, foodPerPlayer);
    }

    public void drawReceivedField(int height, int width)
    {
        this.height = height;
        this.width = width;
    }

    public void lostWindow()
    {
        Button exit = new Button("Exit");

        Group root = new Group();
        Stage primaryStage = new Stage();
        root.getChildren().add(exit);
        Scene scene = new Scene(root, 200, 100);

        primaryStage.setTitle("You lost :(");
        primaryStage.setScene(scene);
        primaryStage.show();

        exit.setOnAction((event ->
        {
            onLostCallBack.run();
            primaryStage.close();
            stage.close();
            System.exit(0);
        }));
    }

    public void draw()
    {
        synchronized (field)
        {
            field.updateField();

            //lost += (field.getLost() ? 1 : 0);
//            if (1 == lost)
//            {
//                lostWindow();
//            }
            graphicsContext.setFill(Color.BISQUE);
            graphicsContext.fillRect(0, 0, width * increase, height * increase);

            List<Coord> foods = field.getFood();
            graphicsContext.setFill(Color.RED);
            for (Coord food : foods)
            {
                graphicsContext.fillRect(food.getX() * increase, food.getY() * increase, increase, increase);
            }
            for (Map.Entry<Integer, Snake> snakeEntry : field.getSnakes().entrySet())
            {
                for (Coord coord : snakeEntry.getValue().getSnake())
                {
                    graphicsContext.setFill(Color.GREEN);
                    if(coord.equals(snakeEntry.getValue().getHead()))
                    {
                        graphicsContext.setFill(Color.DARKGREEN);
                    }
                    graphicsContext.fillRect(coord.getX() * increase, coord.getY() * increase, increase, increase);
                }
            }
        }

        if(0 == lost && null == field.getSnake(playerId))
        {
            lost++;
            lostWindow();
        }
    }

    public int getUpdateTime()
    {
        return updateTime;
    }

}
