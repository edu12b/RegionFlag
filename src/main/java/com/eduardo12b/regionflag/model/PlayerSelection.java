package com.eduardo12b.regionflag.model;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Armazena as seleções de pos1/pos2 dos jogadores para criação de regiões.
 * Thread-safe para compatibilidade com Folia.
 */
public class PlayerSelection {

    private final Map<UUID, Location> pos1Map = new ConcurrentHashMap<>();
    private final Map<UUID, Location> pos2Map = new ConcurrentHashMap<>();

    /**
     * Define a posição 1 do jogador.
     */
    public void setPos1(UUID playerId, Location location) {
        pos1Map.put(playerId, location.clone());
    }

    /**
     * Define a posição 2 do jogador.
     */
    public void setPos2(UUID playerId, Location location) {
        pos2Map.put(playerId, location.clone());
    }

    /**
     * Obtém a posição 1 do jogador.
     */
    public Location getPos1(UUID playerId) {
        return pos1Map.get(playerId);
    }

    /**
     * Obtém a posição 2 do jogador.
     */
    public Location getPos2(UUID playerId) {
        return pos2Map.get(playerId);
    }

    /**
     * Verifica se o jogador tem ambas as posições definidas.
     */
    public boolean hasCompleteSelection(UUID playerId) {
        return pos1Map.containsKey(playerId) && pos2Map.containsKey(playerId);
    }

    /**
     * Verifica se ambas as posições estão no mesmo mundo.
     */
    public boolean isSameWorld(UUID playerId) {
        Location p1 = pos1Map.get(playerId);
        Location p2 = pos2Map.get(playerId);
        if (p1 == null || p2 == null) return false;
        if (p1.getWorld() == null || p2.getWorld() == null) return false;
        return p1.getWorld().getName().equals(p2.getWorld().getName());
    }

    /**
     * Limpa a seleção do jogador.
     */
    public void clear(UUID playerId) {
        pos1Map.remove(playerId);
        pos2Map.remove(playerId);
    }

    /**
     * Limpa todas as seleções.
     */
    public void clearAll() {
        pos1Map.clear();
        pos2Map.clear();
    }
}
