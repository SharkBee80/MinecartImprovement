package com.SharkBee80.MinecartImprovement;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class MinecartImprovementSignListener implements Listener {

    MinecartImprovement plugin;

    public MinecartImprovementSignListener(MinecartImprovement instance) {

        plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent e) {
        if (e.getLine(0).equalsIgnoreCase("[msp]")) {
            if (e.getLine(1).equalsIgnoreCase("fly") || e.getLine(1).equalsIgnoreCase("nofly")) {
//                if (!(e.getPlayer().hasPermission("msp.signs.fly"))) {
//                    e.setLine(0, "NO PERMS");
//                }
            } else {
                boolean error = false;
                double speed = -1;

                try {
                    speed = Double.parseDouble(e.getLine(1));
                } catch (Exception ex) {
                    error = true;
                }

                if (error || 4 < speed || speed < 0) {
                    e.setLine(1, "WRONG VALUE");
                    e.setLine(2, "0.0-4.0");
                }

//                if (!(e.getPlayer().hasPermission("msp.signs.speed"))) {
//                    e.setLine(0, "NO PERMS");
//                }

            }


        }

    }


}
