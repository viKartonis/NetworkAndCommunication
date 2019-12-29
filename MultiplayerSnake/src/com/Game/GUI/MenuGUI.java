package com.game.gui;

import com.game.controller.Controller;
import com.game.gui.*;
import com.game.network.IPEndPoint;
import com.game.network.MessageHandler;
import com.game.network.UDPUnicast;
import com.google.protobuf.MapEntry;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MenuGUI extends Application
{

    private int width = 300;
    private int height = 400;
    private int nodeNumber = 6;
    private Stage mainStage;
    private  Button play;
    private Stage settings;
    private TextField widthText;
    private TextField heightText;
    private GridPane gridPane;
    private UDPUnicast udpUnicast;
    private TextField nameText;
    private TextField foodText;
    private TextField timeText;
    private TextField probabilityText;
    private  long seq;
    private Condition cond;
    private Lock lock;
    private  InetAddress address = null;
    private int id;

    private void setOnlyNumbers(TextField textField)
    {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
                    textField.setText(oldValue);
                }
            }
        });
    }


    public void newGameHandler()
    {
        mainStage.close();

        settings = new Stage();
        gridPane = new GridPane();

        Label widthLabel = new Label("Width");
        Label heightLabel = new Label("Height");
        Label foodLabel = new Label("Food");
        Label timeLabel = new Label("Time");
        Label probabilityLabel = new Label("Food drop rate");
        Label gameName = new Label("Game name");


        widthText = new TextField();
        heightText = new TextField();
        foodText = new TextField();
        timeText = new TextField();
        probabilityText = new TextField();
        nameText = new TextField();

        play = new Button("PLAY!");

        for (int i = 0; i < nodeNumber; ++i)
        {
            gridPane.getRowConstraints().add(new RowConstraints(80));
        }

        GridPane.setMargin(widthLabel, new Insets(10, 10, 10, 10));
        GridPane.setMargin(heightLabel, new Insets(10, 10, 10, 10));
        GridPane.setMargin(foodLabel, new Insets(10, 10, 10, 10));
        GridPane.setMargin(timeLabel, new Insets(10, 10, 10, 10));
        GridPane.setMargin(probabilityLabel, new Insets(10, 10, 10, 10));
        GridPane.setMargin(gameName, new Insets(10, 10, 10, 10));
        GridPane.setMargin(play, new Insets(10,10,10,10));




        gridPane.add(widthLabel, 0, 0);
        gridPane.add(heightLabel, 0, 1);
        gridPane.add(foodLabel, 0, 2);
        gridPane.add(timeLabel, 0, 3);
        gridPane.add(probabilityLabel, 0, 4);
        gridPane.add(gameName, 0, 5);
        gridPane.add(widthText, 1, 0);
        gridPane.add(heightText, 1, 1);
        gridPane.add(foodText, 1, 2);
        gridPane.add(timeText, 1, 3);
        gridPane.add(probabilityText, 1, 4);
        gridPane.add(nameText, 1, 5);
        gridPane.add(play, 2,5);


        setOnlyNumbers(widthText);
        setOnlyNumbers(heightText);
        setOnlyNumbers(foodText);
        setOnlyNumbers(timeText);
        setOnlyNumbers(probabilityText);

    }


    public void playHandler()
    {
        settings.close();


        Controller controller;

        Group root = new Group();
        Stage primaryStage = new Stage();
        Scene scene = new Scene(root, Integer.valueOf(widthText.getText()),
                Integer.valueOf(heightText.getText()));
        Canvas canvas = new Canvas(Integer.valueOf(widthText.getText()),
                Integer.valueOf(heightText.getText()));


        SnakesProto.GamePlayer masterPlayer = SnakesProto.GamePlayer.newBuilder().setId(0)
                .setPort(8080).setRole(SnakesProto.NodeRole.MASTER)
                .setType(SnakesProto.PlayerType.HUMAN).setIpAddress("127.0.0.1").setScore(0)
                .setName(nameText.getText()).build();

        SnakesProto.GamePlayers allPlayers = SnakesProto.GamePlayers.newBuilder().addPlayers(masterPlayer)
                .build();

        SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder().setWidth(Integer.valueOf(widthText.getText()))
                .setHeight(Integer.valueOf(heightText.getText())).setFoodStatic(Integer.valueOf(foodText.getText()))
                .setStateDelayMs(Integer.valueOf(timeText.getText())).setDeadFoodProb(Integer.valueOf(probabilityText.getText()))
                .build();

        SnakesProto.GameMessage.AnnouncementMsg announcementMsg =
                SnakesProto.GameMessage.AnnouncementMsg.newBuilder().setPlayers(allPlayers).setConfig(gameConfig).build();

        seq = 0;
        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder().setAnnouncement(announcementMsg)
                .setMsgSeq(seq)
                .build();
        seq++;

        Runnable task = () -> {
            try
            {
                while(true)
                {
                    udpUnicast.sendAnnouncement(gameMessage);
                    try
                    {
                        Thread.sleep(3000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();

//        SnakesProto.GameState.Snake = SnakesProto.GameState.Snake.newBuilder().setState(SnakesProto.GameState
//                .Snake.SnakeState.ALIVE).setPlayerId(id).set
//        SnakesProto.GameState = SnakesProto.GameState.newBuilder().addSnakes();
//
//        SnakesProto.GameMessage gameStateMessage = SnakesProto.GameMessage.newBuilder().setState().build()
//        udpUnicast.sendGameState();

        GameGUI gui = new GameGUI(canvas, primaryStage, root);
        gui.init(Integer.valueOf(heightText.getText()), Integer.valueOf(widthText.getText()));
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

    ConcurrentHashMap<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> announcments;

    private List<Button> buttons = new LinkedList<>();


    public void joinHandler()
    {
        for(Button button : buttons)
        {
            button.setOnAction(event ->
            {
                try
                {
                    address = InetAddress.getLocalHost();
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                if(null != address)
                {
                    udpUnicast.sendJoin(new IPEndPoint(address, 8081),
                            SnakesProto.PlayerType.HUMAN, false, nameText.getText());
                    seq++;
                }
            });

        }
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        mainStage = stage;

        udpUnicast = new UDPUnicast();
        announcments = new ConcurrentHashMap<>();

        Thread t = new Thread(() ->
        {
            while (true)
            {
                UDPUnicast.Pair<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> announcementMsg
                        = udpUnicast.receiveAnnouncement();
                if (null != announcementMsg)
                {
                    id = announcementMsg.u.getPlayers().getPlayersCount() - 1;
                    announcments.put(announcementMsg.t, announcementMsg.u);
                }
            }
        });
        t.start();


        Button newGame = new Button("New game");           // кнопка

        VBox root = new VBox();


        root.getChildren().add(newGame);
        root.setSpacing(20);
        VBox.setMargin(newGame, new Insets(75, 100, 50, 100));


        newGame.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
               newGameHandler();
                play.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        playHandler();
                    }
                });

                Scene scene = new Scene(gridPane);
                settings.setTitle("New game"); // установка заголовка
                settings.setWidth(400);            // установка ширины
                settings.setHeight(500);           // установка длины
                settings.setScene(scene);          // установка Scene для Stage
                settings.show();                   // отображение окна на экране
            }
        });

        Button refresh = new Button("Refresh");
        refresh.setOnAction((ActionEvent event) ->
        {
            root.getChildren().removeAll(buttons);
            buttons.clear();
            for (Map.Entry<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> pair : announcments.entrySet())
            {
                IPEndPoint masterIp = pair.getKey();
                Button button = new Button(masterIp.getAddress().toString());
                buttons.add(button);
                root.getChildren().add(button);
                VBox.setMargin(newGame, new Insets(75, 100, 50, 100));
                System.out.println("add button");
            }
        });

        joinHandler();


//        UDPUnicast.Pair<IPEndPoint, SnakesProto.GameMessage.JoinMsg> joinMsg
//                = udpUnicast.receiveJoin();
//
//        if (null != joinMsg)
//        {
//            id++;
//        }


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
