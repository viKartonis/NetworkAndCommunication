package com.game.gui;

import com.game.controller.Controller;
import com.game.model.GameField;
import com.game.network.IPEndPoint;
import com.game.network.UDPUnicast;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.ippolitov.fit.snakes.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ButtonHandler
{
    private final int increase = 20;
    private VBox root;
    private List<Button> buttons = new LinkedList<>();
    private ConcurrentHashMap<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> announcments;
    private IPEndPoint masterIp;
    private InetAddress address = null;
    private UDPUnicast udpUnicast;
    private Button newGame = new Button("New game");           // кнопка
    private Stage mainStage;
    private Stage settings;
    private GridPane gridPane;
    private TextField widthText;
    private TextField heightText;
    private TextField nameText;
    private TextField foodText;
    private TextField timeText;
    private TextField probabilityText;
    private TextField foodPerPlayerText;
    private  Button play;
    private int id;
    private int nodeNumber = 15;
    private GameField field;

    public ButtonHandler(Stage stage, Button play, Stage settings, GridPane gridPane, int id,
                         ConcurrentHashMap<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> announcments,
                         UDPUnicast udpUnicast, VBox root, GameField field)
    {
        mainStage = stage;
        this.play = play;
        this.settings = settings;
        this.gridPane = gridPane;
        this.id = id;
        this.announcments = announcments;
        this.udpUnicast = udpUnicast;
        this.root = root;
        this.field = field;
    }

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

    public void joinHandler(Button button, SnakesProto.GameMessage.AnnouncementMsg announcement)
    {
        try
        {
            address = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        if (null != address)
        {
            UDPUnicast.Pair<IPEndPoint, SnakesProto.GameMessage> pair = null;

            boolean error = false;
            synchronized (udpUnicast)
            {
                udpUnicast.sendJoin(new IPEndPoint(masterIp.getAddress(), masterIp.getPort()),
                        SnakesProto.PlayerType.HUMAN, false, button.getText());

                do
                {
                    pair = udpUnicast.receive();
                    if(null !=pair && SnakesProto.GameMessage.TypeCase.STATE == pair.second.getTypeCase())
                    {
                        break;
                    }
                    if(null != pair && SnakesProto.GameMessage.TypeCase.ERROR == pair.second.getTypeCase())
                    {
                        error = true;
                        break;
                    }
                } while (true);
            }

            if(error)
            {
                Stage stage = new Stage();
                VBox root = new VBox();
                Scene scene = new Scene(root, 200, 300);
                root.getChildren().add(new Label("Cannot to join to this game"));
                stage.setScene(scene);
                stage.show();
                return;
            }
            mainStage.close();

            int width = pair.second.getState().getState().getConfig().getWidth();
            int height = pair.second.getState().getState().getConfig().getHeight();


            udpUnicast.setId(pair.second.getReceiverId());

            createGameStage(width * increase, height * increase,
                    announcement.getConfig().getStateDelayMs(), pair.second.getReceiverId())
                    .drawReceivedField(height, width);
            System.out.println();
        }
    }

    public void refreshHandler()
    {
        root.getChildren().removeAll(buttons);
        buttons.clear();
        for (Map.Entry<IPEndPoint, SnakesProto.GameMessage.AnnouncementMsg> pair : announcments.entrySet())
        {
            masterIp = pair.getKey();
            Button button = new Button(masterIp.getAddress().toString());
            button.setOnAction(clickEvent ->
            {
                SnakesProto.GameMessage.AnnouncementMsg announcement = pair.getValue();
                joinHandler(button, announcement);
            });
            buttons.add(button);
            root.getChildren().add(button);
            VBox.setMargin(newGame, new Insets(75, 100, 50, 100));
        }
    }

    public void newGameHandler()
    {
        mainStage.close();

        Label widthLabel = new Label("Width");
        Label heightLabel = new Label("Height");
        Label foodLabel = new Label("Food");
        Label timeLabel = new Label("Update time");
        Label probabilityLabel = new Label("Food drop rate");
        Label gameName = new Label("Game name");
        Label foodPerPlayer = new Label("Food per player");

        widthText = new TextField("40");
        heightText = new TextField("40");
        foodText = new TextField("20");
        timeText = new TextField("150");
        probabilityText = new TextField("0.5");
        nameText = new TextField("fafaf");
        foodPerPlayerText = new TextField("0.5");

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
        GridPane.setMargin(foodPerPlayer, new Insets(10,10,10,10));


        gridPane.add(widthLabel, 0, 0);
        gridPane.add(heightLabel, 0, 1);
        gridPane.add(foodLabel, 0, 2);
        gridPane.add(timeLabel, 0, 3);
        gridPane.add(probabilityLabel, 0, 4);
        gridPane.add(gameName, 0, 5);
        gridPane.add(foodPerPlayer, 0, 6);

        gridPane.add(widthText, 1, 0);
        gridPane.add(heightText, 1, 1);
        gridPane.add(foodText, 1, 2);
        gridPane.add(timeText, 1, 3);
        gridPane.add(probabilityText, 1, 4);
        gridPane.add(nameText, 1, 5);
        gridPane.add(foodPerPlayerText, 1, 6);
        gridPane.add(play, 2,7);

        setOnlyNumbers(widthText);
        setOnlyNumbers(heightText);
        setOnlyNumbers(foodText);
        setOnlyNumbers(timeText);
        setOnlyNumbers(foodPerPlayerText);
        setOnlyNumbers(probabilityText);
    }

    public void playHandler()
    {
        settings.close();

        int width = Integer.valueOf(widthText.getText());
        int height = Integer.valueOf(heightText.getText());
        int foodNumber = Integer.valueOf(foodText.getText());
        int updateTime = Integer.valueOf(timeText.getText());
        float probability = Float.valueOf(probabilityText.getText());
        float foodPerPlayerNumber = Float.valueOf(foodPerPlayerText.getText());

        try
        {
            address = InetAddress.getByName("127.0.0.1");
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        synchronized (udpUnicast)
        {
            udpUnicast.setMaster(true);
            udpUnicast.setAnnouncment(nameText.getText(), width, height, foodNumber,updateTime, probability);
        }
        createGameStage(width * increase, height * increase, updateTime, 0)
                .init(height, width, foodNumber, probability, foodPerPlayerNumber);
    }

    private GameGUI createGameStage(int width, int height, int updateTime, int playerId)
    {
        Thread ping = new Thread(()->
        {
            while(true)
            {
                synchronized (udpUnicast)
                {
                    udpUnicast.sendPing();
                }

                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        });
        Thread senderAndReceiver = new Thread(() -> {
            while(true)
            {
                synchronized (udpUnicast)
                {
                    udpUnicast.receive();
                    try
                    {
                        udpUnicast.sendAnnouncement();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    udpUnicast.sendState();
                }

                try
                {
                    Thread.sleep(20);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        });


        Group root = new Group();
        Stage primaryStage = new Stage();
        Scene scene = new Scene(root, width, height);
        Canvas canvas = new Canvas(width, height);
        GameGUI gui = new GameGUI(()->
        {
                senderAndReceiver.interrupt();
                ping.interrupt();
        },canvas, primaryStage, root, field, updateTime, playerId);


        Controller controller = new Controller(field, udpUnicast);
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, controller);

        Drawer drawer = new Drawer(gui);
        drawer.start();

        canvas.setFocusTraversable(true);
        root.getChildren().add(canvas);
        primaryStage.setTitle("Snake");
        primaryStage.setScene(scene);
        primaryStage.show();

        senderAndReceiver.start();
        ping.start();

        return gui;
    }

}
