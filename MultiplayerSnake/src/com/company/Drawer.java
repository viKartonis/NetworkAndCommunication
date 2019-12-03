package com.company;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;

public class Drawer extends Thread
{
    private GUI gui;
    private long time = 150_000_000;

    public Drawer(GUI gui)
    {
        this.gui = gui;
    }

    @Override
    public void run()
    {
        Runnable updater = new Runnable()
        {

            @Override
            public void run()
            {
                gui.draw();
            }
        };


        AnimationTimer at = new AnimationTimer()
        {
            private long lastUpd = 0;
            @Override
            public void handle(long now)
            {
                if (now - lastUpd >= time)
                {
                    Platform.runLater(updater);
                    lastUpd = now;
                }
            }
        };

        at.start();
    }
}
