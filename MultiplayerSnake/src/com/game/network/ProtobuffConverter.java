package com.game.network;

import com.game.model.*;
import me.ippolitov.fit.snakes.SnakesProto;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class ProtobuffConverter
{
    public static SnakesProto.GameState.Coord modelToProto(Coord coord)
    {
        return SnakesProto.GameState.Coord.newBuilder().setX(coord.getX()).setY(coord.getY()).build();
    }

    public static SnakesProto.Direction modelToProto(Direction direction)
    {
        if (null == direction)
        {
            return SnakesProto.Direction.UP;
        }
        switch (direction)
        {
            case DOWN:
                return SnakesProto.Direction.DOWN;
            case RIGHT:
                return SnakesProto.Direction.RIGHT;
            case LEFT:
                return SnakesProto.Direction.LEFT;
            default:
                return SnakesProto.Direction.UP;
        }
    }

    public static SnakesProto.GameState.Snake modelToProto(int id, Snake modelSnake)
    {
        ArrayList<SnakesProto.GameState.Coord> coordArrayList = new ArrayList<>();
        for (Coord coord : modelSnake.getSnake())
        {
            coordArrayList.add(modelToProto(coord));
        }
        return SnakesProto.GameState.Snake.newBuilder()
                .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE).setPlayerId(id)
                .addAllPoints(coordArrayList).setHeadDirection(modelToProto(modelSnake.getDirection())).build();
    }

    public static SnakesProto.GameState modelToProto(GameField field, Map<IPEndPoint, GamePlayer> gamePlayers)
    {
        LinkedList<SnakesProto.GameState.Snake> snakeLinkedList = new LinkedList<>();
        ArrayList<SnakesProto.GameState.Coord> coordFood = new ArrayList<>();
        synchronized (field)
        {
            Map<Integer, Snake> snakeMap = field.getSnakes();
            for (Map.Entry<Integer, Snake> snakeMapEntry : snakeMap.entrySet())
            {
                snakeLinkedList.add(modelToProto(snakeMapEntry.getKey(), snakeMapEntry.getValue()));
            }

            for (Coord food : field.getFood())
            {
                coordFood.add(modelToProto(food));
            }
        }
        SnakesProto.GamePlayers.Builder messageBuilder = SnakesProto.GamePlayers.newBuilder();

        //gamePlayers.clear();

        for (Map.Entry<IPEndPoint, GamePlayer> player : gamePlayers.entrySet())
        {
            if(!field.getNeedDelete().contains(player.getValue().getPlayerId()))
            {
                messageBuilder.addPlayers(modelToProto(player.getValue()));
            }
        }
        SnakesProto.GamePlayers gamePlayersMessage = messageBuilder.build();

//        SnakesProto.GamePlayers gamePlayersMessage = SnakesProto.GamePlayers.newBuilder()
//                .addAllPlayers(modelToProto(gamePlayers))
//                .build();


        SnakesProto.GameConfig gameConfig = SnakesProto.GameConfig.newBuilder().setWidth(field.getWidth())
                .setHeight(field.getHeight()).setFoodStatic(field.getFoodStatic()).build();

        return SnakesProto.GameState.newBuilder().addAllSnakes(snakeLinkedList)
                .setStateOrder(0).addAllFoods(coordFood)
                .setPlayers(gamePlayersMessage)
                .setConfig(gameConfig).build();
    }


    private static SnakesProto.GamePlayer modelToProto(GamePlayer player)
    {
        return SnakesProto.GamePlayer.newBuilder()
                .setIpAddress(player.getIpEndPoint().getAddress().toString().substring(1))
                .setPort(player.getIpEndPoint().getPort())
                .setScore(player.getScore())
                .setId(player.getPlayerId())
                .setRole(player.getRole())
                .setName(player.getPlayerName())
                .build();
    }

    private static List<SnakesProto.GamePlayer> modelToProto(Map<IPEndPoint, GamePlayer> players)
    {
        List<SnakesProto.GamePlayer> protoPlayers = new LinkedList<>();
        SnakesProto.GamePlayer gamePlayer;
        GamePlayer player;
        for (Map.Entry<IPEndPoint, GamePlayer> mapEntry: players.entrySet())
        {
            player = mapEntry.getValue();
            gamePlayer = ProtobuffConverter.modelToProto(player);
            protoPlayers.add(gamePlayer);
        }
        return protoPlayers;
    }

    public static Coord protoToModel(SnakesProto.GameState.Coord coord)
    {
        return new Coord(coord.getX(), coord.getY());
    }

    public static Direction protoToModel(SnakesProto.Direction direction)
    {
        switch (direction)
        {
            case DOWN:
                return Direction.DOWN;
            case RIGHT:
                return Direction.RIGHT;
            case LEFT:
                return Direction.LEFT;
            default:
                return Direction.UP;
        }
    }

    public static Snake protoToModel(SnakesProto.GameState.Snake snake)
    {
        LinkedList<Coord> coords = new LinkedList<>();
        for (SnakesProto.GameState.Coord coord : snake.getPointsList())
        {
            coords.add(protoToModel(coord));
        }

        return new Snake(coords, protoToModel(snake.getHeadDirection()));
    }

    public static void protoToModel(SnakesProto.GameState state, GameField field, Map<IPEndPoint, GamePlayer> gamePlayers)
    {
        int width = state.getConfig().getWidth();
        int height = state.getConfig().getHeight();
        float probability = state.getConfig().getDeadFoodProb();

        if (field.getWidth() != width || field.getHeight() != height)
        {
            field.initReceivedField(height, width, probability);
        }

        for (SnakesProto.GamePlayer player : state.getPlayers().getPlayersList())
        {
            IPEndPoint ipEndPoint;
            try
            {
               ipEndPoint = new IPEndPoint(InetAddress.getByName(player.getIpAddress()), player.getPort());
                gamePlayers.put(ipEndPoint, protoToModel(player));
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
        }
        Map<Integer, Snake> snakeMap = new HashMap<>();
        for(SnakesProto.GameState.Snake snake : state.getSnakesList())
        {
            snakeMap.put(snake.getPlayerId(), protoToModel(snake));
        }

        List<Coord> food = new LinkedList<>();
        for (SnakesProto.GameState.Coord coord : state.getFoodsList())
        {
            food.add(protoToModel(coord));
        }

        synchronized (field)
        {
            field.setSnakes(snakeMap);
            field.setFood(food);

            field.apply();
        }
    }

    private static GamePlayer protoToModel(SnakesProto.GamePlayer player)
    {
        try
        {
            return new GamePlayer(player.getIpAddress(), player.getPort(), player.getScore(), player.getId(),
                    player.getRole(), player.getName());
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
