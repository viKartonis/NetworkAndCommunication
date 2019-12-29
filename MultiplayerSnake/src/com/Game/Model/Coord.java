package com.game.model;

public class Coord
{
    private int x;
    private int y;

    public Coord(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getY()
    {
        return y;
    }

    public int getX()
    {
        return x;
    }

    @Override
    public boolean equals(Object ob)
    {
        if(ob == null)
        {
            return false;
        }
        if (!(ob instanceof Coord))
        {
            return false;
        }
        return  (this.x == ((Coord) ob).x && this.y == ((Coord) ob).y);
    }
}
