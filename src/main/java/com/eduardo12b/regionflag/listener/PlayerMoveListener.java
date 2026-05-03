package com.eduardo12b.regionflag.listener;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listener otimizado para detecção de movimento.
 * Só processa quando o jogador muda de BLOCO (não apenas olha ao redor).
 * Isso reduz drasticamente o processamento em servidores grandes.
 */
public class PlayerMoveListener implements Listener {

    private final RegionFlagPlugin plugin;

    public PlayerMoveListener(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        // Otimização: só processa se mudou de bloco
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        // Delegar ao PlayerTracker
        plugin.getPlayerTracker().updatePlayer(event.getPlayer(), to);
    }
}
