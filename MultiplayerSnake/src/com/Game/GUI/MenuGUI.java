package com.game.gui;

import com.game.model.GameField;
import com.game.network.IPEndPoint;
import com.game.network.UDPUnicast;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.ippolitov.fit.snakes.SnakesProto;

import java.util.concurrent.ConcurrentHashMap;

public class MenuGUI extends Application
{

    private int width = 300;
    private int height = 400;
    private Stage mainStage;
    private  Button play;
    private Stage settings;
    private GridPane gridPane;
    private UDPUnicast udpUnicast;
    private int id;
    private ConcurrentHashMap<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> announcments;
    private ButtonHandler buttonHandler;
    private GameField field;

    @Override
    public void start(Stage stage) throws Exception
    {
        mainStage = stage;
        settings = new Stage();
        gridPane = new GridPane();
        announcments = new ConcurrentHashMap<>();
        play = new Button("PLAY!");
        VBox root = new VBox();
        field = new GameField();
        udpUnicast = new UDPUnicast(field);
        buttonHandler = new ButtonHandler(mainStage, play, settings, gridPane, id, announcments, udpUnicast, root, field);

        Thread t = new Thread(() ->
        {
            while (true)
            {
                UDPUnicast.Pair<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> announcementMsg
                        = udpUnicast.receiveAnnouncement();
                if (null != announcementMsg)
                {
                    id = announcementMsg.second.getPlayers().getPlayersCount() - 1;
                    announcments.put(announcementMsg.first, announcementMsg.second);
                }
            }
        });
        t.start();

        Button newGame = new Button("New game");           // кнопка

        root.getChildren().add(newGame);
        root.setSpacing(20);
        VBox.setMargin(newGame, new Insets(75, 100, 50, 100));

        newGame.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
               buttonHandler.newGameHandler();
                play.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        buttonHandler.playHandler();
                    }
                });

                Scene scene = new Scene(gridPane);
                settings.setTitle("New game"); // установка заголовка
                settings.setWidth(400);            // установка ширины
                settings.setHeight(700);           // установка длины
                settings.setScene(scene);          // установка Scene для Stage
                settings.show();                   // отображение окна на экране
            }
        });

        Button refresh = new Button("Refresh");
        refresh.setOnAction((ActionEvent event) ->
        {
            buttonHandler.refreshHandler();
        });

        root.getChildren().add(refresh);
        VBox.setMargin(refresh, new Insets(0, 0, 0, 10));
        Scene scene = new Scene(root);  // создание Scene
        stage.setTitle("Snake"); // установка заголовка
        stage.setWidth(width);            // установка ширины
        stage.setHeight(height);           // установка длины
        scene.setFill(Color.BLUE);
        stage.setScene(scene);          // установка Scene для Stage
        stage.show();                   // отображение окна на экране
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
