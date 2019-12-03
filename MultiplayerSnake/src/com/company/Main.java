package com.company;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Controller controller;

        Group root = new Group();
        Scene scene = new Scene(root, 800, 600);
        Canvas canvas = new Canvas(800, 600);

        GUI gui = new GUI(canvas, primaryStage);
        gui.init();
        controller = gui.getController();
        controller.handler(primaryStage);

        Drawer drawer = new Drawer(gui);
        drawer.setDaemon(true);
        drawer.start();

        canvas.setFocusTraversable(true);
        root.getChildren().add(canvas);
        primaryStage.setTitle("Snake");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
