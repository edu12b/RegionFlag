package com.eduardo12b.regionflag.manager;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.model.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rastreia em quais regiões cada jogador está.
 * Detecta entrada e saída de regiões, disparando os eventos das flags.
 * Thread-safe para Folia.
 */
public class PlayerTracker {

    private final RegionFlagPlugin plugin;

    // UUID do jogador → Set de nomes das regiões em que está atualmente
    private final Map<UUID, Set<String>> playerRegions = new ConcurrentHashMap<>();

    public PlayerTracker(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Atualiza a posição do jogador e detecta entrada/saída de regiões.
     * Chamado quando o jogador muda de bloco.
     */
    public void updatePlayer(Player player, Location location) {
        if (location == null || location.getWorld() == null) return;

        String worldName = location.getWorld().getName();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();

        // Obter regiões na nova posição
        List<Region> currentRegions = plugin.getRegionManager()
                .getRegionsAt(worldName, blockX, blockY, blockZ);

        // Nomes das regiões atuais
        Set<String> currentNames = new HashSet<>();
        Map<String, Region> currentMap = new HashMap<>();
        for (Region region : currentRegions) {
            currentNames.add(region.getName());
            currentMap.put(region.getName(), region);
        }

        // Nomes das regiões anteriores
        Set<String> previousNames = playerRegions.getOrDefault(player.getUniqueId(), Collections.emptySet());

        // Detectar regiões que o jogador ENTROU (estão em current mas não em previous)
        for (String name : currentNames) {
            if (!previousNames.contains(name)) {
                Region region = currentMap.get(name);
                if (region != null) {
                    plugin.getMessageUtil().debug(player.getName() + " entrou na região: " + name);
                    plugin.getFlagRegistry().fireEnter(player, region);
                }
            }
        }

        // Detectar regiões que o jogador SAIU (estão em previous mas não em current)
        for (String name : previousNames) {
            if (!currentNames.contains(name)) {
                Region region = plugin.getRegionManager().getRegion(name);
                if (region != null) {
                    plugin.getMessageUtil().debug(player.getName() + " saiu da região: " + name);
                    plugin.getFlagRegistry().fireExit(player, region);
                }
            }
        }

        // Atualizar estado
        if (currentNames.isEmpty()) {
            playerRegions.remove(player.getUniqueId());
        } else {
            playerRegions.put(player.getUniqueId(), currentNames);
        }
    }

    /**
     * Verifica a posição inicial do jogador (ao entrar no servidor).
     */
    public void checkInitialPosition(Player player) {
        updatePlayer(player, player.getLocation());
    }

    /**
     * Limpa os dados do jogador (ao sair do servidor).
     */
    public void removePlayer(UUID playerId) {
        playerRegions.remove(playerId);
        plugin.getCooldownManager().clearPlayer(playerId);
    }

    /**
     * Retorna os nomes das regiões em que o jogador está.
     */
    public Set<String> getPlayerRegions(UUID playerId) {
        return playerRegions.getOrDefault(playerId, Collections.emptySet());
    }

    /**
     * Limpa todos os dados de rastreamento.
     */
    public void clearAll() {
        playerRegions.clear();
    }
}
