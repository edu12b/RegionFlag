package com.eduardo12b.regionflag.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia cooldowns por jogador e por ação (flag:região).
 * Thread-safe com ConcurrentHashMap para Folia.
 */
public final class CooldownManager {

    // Chave: "flagType:regionName" → Map<UUID, timestamp>
    private final Map<String, Map<UUID, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Verifica se o jogador está em cooldown para uma ação.
     *
     * @param playerId UUID do jogador
     * @param key      Chave do cooldown (ex: "playsound:spawn")
     * @param seconds  Duração do cooldown em segundos
     * @return true se ainda está em cooldown
     */
    public boolean isOnCooldown(UUID playerId, String key, int seconds) {
        if (seconds <= 0) return false;

        Map<UUID, Long> playerCooldowns = cooldowns.get(key);
        if (playerCooldowns == null) return false;

        Long lastUse = playerCooldowns.get(playerId);
        if (lastUse == null) return false;

        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        return elapsed < seconds;
    }

    /**
     * Retorna o tempo restante do cooldown em segundos.
     */
    public int getRemainingCooldown(UUID playerId, String key, int seconds) {
        if (seconds <= 0) return 0;

        Map<UUID, Long> playerCooldowns = cooldowns.get(key);
        if (playerCooldowns == null) return 0;

        Long lastUse = playerCooldowns.get(playerId);
        if (lastUse == null) return 0;

        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        return Math.max(0, seconds - (int) elapsed);
    }

    /**
     * Define o cooldown para o jogador.
     */
    public void setCooldown(UUID playerId, String key) {
        cooldowns.computeIfAbsent(key, k -> new ConcurrentHashMap<>())
                .put(playerId, System.currentTimeMillis());
    }

    /**
     * Remove o cooldown de um jogador para uma ação.
     */
    public void removeCooldown(UUID playerId, String key) {
        Map<UUID, Long> playerCooldowns = cooldowns.get(key);
        if (playerCooldowns != null) {
            playerCooldowns.remove(playerId);
        }
    }

    /**
     * Limpa todos os cooldowns de um jogador.
     */
    public void clearPlayer(UUID playerId) {
        for (Map<UUID, Long> playerCooldowns : cooldowns.values()) {
            playerCooldowns.remove(playerId);
        }
    }

    /**
     * Limpa todos os cooldowns.
     */
    public void clearAll() {
        cooldowns.clear();
    }

    /**
     * Gera a chave de cooldown para uma flag e região.
     */
    public static String buildKey(String flagType, String regionName) {
        return flagType + ":" + regionName;
    }
}
