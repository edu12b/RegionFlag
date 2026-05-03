package com.eduardo12b.regionflag.listener;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Listener para conexão/desconexão e teleporte de jogadores.
 */
public class PlayerConnectionListener implements Listener {

    private final RegionFlagPlugin plugin;

    public PlayerConnectionListener(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Verificar a posição inicial com um pequeno delay para garantir que o jogador já carregou
        event.getPlayer().getScheduler().runDelayed(plugin, task -> {
            plugin.getPlayerTracker().checkInitialPosition(event.getPlayer());
        }, null, 10L); // 10 ticks de delay
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerTracker().removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Atualizar posição após teleporte (pode mudar de região)
        event.getPlayer().getScheduler().runDelayed(plugin, task -> {
            plugin.getPlayerTracker().updatePlayer(event.getPlayer(), event.getPlayer().getLocation());
        }, null, 1L);
    }
}
