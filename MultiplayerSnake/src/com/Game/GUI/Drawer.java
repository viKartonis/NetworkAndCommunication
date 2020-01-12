package com.game.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;

public class Drawer extends AnimationTimer
{
    private GameGUI gui;
    private int time;// = 150 sec
    private Runnable updater;

    public Drawer(GameGUI gui)
    {
        this.gui = gui;
        updater = () -> gui.draw();
        time = gui.getUpdateTime();
    }

//    @Override
//    public void run()
//    {
//        Runnable updater = new Runnable()
//        {
//
//            @Override
//            public void run()
//            {
//
//                gui.draw();
//            }
//    };
//
//
//        AnimationTimer at = new AnimationTimer()
//        {
//            private long lastUpd = 0;
//            @Override
//            public void handle(long now)
//            {
//
//            }
//        };
//
//        at.start();
//    }
    private long lastUpd = 0;

    @Override
    public void handle(long now)
    {
        if (now - lastUpd >= time * 1_000_000)
        {
            Platform.runLater(updater);
            lastUpd = now;
        }
    }
}
