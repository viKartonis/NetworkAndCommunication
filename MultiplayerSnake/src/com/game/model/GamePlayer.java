package com.game.model;

import com.game.network.IPEndPoint;
import me.ippolitov.fit.snakes.SnakesProto;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GamePlayer
{
    private IPEndPoint ipEndPoint;
    private int score;
    private int playerId;
    private SnakesProto.NodeRole role;
    private String playerName;

    public IPEndPoint getIpEndPoint()
    {
        return ipEndPoint;
    }

    public void setIpEndPoint(IPEndPoint ipEndPoint)
    {
        this.ipEndPoint = ipEndPoint;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public int getPlayerId()
    {
        return playerId;
    }

    public void setPlayerId(int playerId)
    {
        this.playerId = playerId;
    }

    public SnakesProto.NodeRole getRole()
    {
        return role;
    }

    public void setRole(SnakesProto.NodeRole role)
    {
        this.role = role;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }

    public GamePlayer(IPEndPoint ipEndPoint, int score, int playerId, SnakesProto.NodeRole role, String playerName)
    {
        this.ipEndPoint = ipEndPoint;
        this.score = score;
        this.playerId = playerId;
        this.role = role;
        this.playerName = playerName;
    }

    public GamePlayer(String address, int port, int score, int playerId, SnakesProto.NodeRole role, String playerName) throws UnknownHostException
    {
        this(new IPEndPoint(InetAddress.getByName(address), port), score, playerId, role, playerName);
    }


}
