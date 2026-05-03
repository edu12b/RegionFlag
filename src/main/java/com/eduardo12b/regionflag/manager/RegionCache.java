package com.eduardo12b.regionflag.manager;

import com.eduardo12b.regionflag.model.Region;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache de regiões baseado em chunks para lookup O(1).
 * Cada chunk key mapeia para o conjunto de regiões que o interceptam.
 * Thread-safe com ConcurrentHashMap para Folia.
 */
public class RegionCache {

    // chunkKey(cx, cz) → Set de regiões que interceptam esse chunk
    private final Map<Long, Set<Region>> chunkToRegions = new ConcurrentHashMap<>();

    // Nome da região → Region (para lookup direto por nome)
    private final Map<String, Region> nameToRegion = new ConcurrentHashMap<>();

    /**
     * Adiciona uma região ao cache.
     */
    public void addRegion(Region region) {
        nameToRegion.put(region.getName().toLowerCase(), region);

        for (long key : region.getChunkKeys()) {
            chunkToRegions.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                    .add(region);
        }
    }

    /**
     * Remove uma região do cache.
     */
    public void removeRegion(Region region) {
        nameToRegion.remove(region.getName().toLowerCase());

        for (long key : region.getChunkKeys()) {
            Set<Region> regions = chunkToRegions.get(key);
            if (regions != null) {
                regions.remove(region);
                if (regions.isEmpty()) {
                    chunkToRegions.remove(key);
                }
            }
        }
    }

    /**
     * Obtém todas as regiões que contêm uma coordenada de bloco específica,
     * ordenadas por prioridade (maior primeiro).
     */
    public List<Region> getRegionsAt(String worldName, int blockX, int blockY, int blockZ) {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        long key = Region.chunkKey(chunkX, chunkZ);

        Set<Region> candidates = chunkToRegions.get(key);
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Region> matching = new ArrayList<>();
        for (Region region : candidates) {
            if (region.getWorldName().equals(worldName)
                    && blockX >= region.getMinX() && blockX <= region.getMaxX()
                    && blockY >= region.getMinY() && blockY <= region.getMaxY()
                    && blockZ >= region.getMinZ() && blockZ <= region.getMaxZ()) {
                matching.add(region);
            }
        }

        // Ordenar por prioridade (maior primeiro)
        matching.sort(Comparator.comparingInt(Region::getPriority).reversed());
        return matching;
    }

    /**
     * Obtém uma região pelo nome.
     */
    public Region getRegionByName(String name) {
        return nameToRegion.get(name.toLowerCase());
    }

    /**
     * Retorna todas as regiões no cache.
     */
    public Collection<Region> getAllRegions() {
        return Collections.unmodifiableCollection(nameToRegion.values());
    }

    /**
     * Retorna todas as regiões de um mundo específico.
     */
    public List<Region> getRegionsByWorld(String worldName) {
        List<Region> result = new ArrayList<>();
        for (Region region : nameToRegion.values()) {
            if (region.getWorldName().equals(worldName)) {
                result.add(region);
            }
        }
        result.sort(Comparator.comparingInt(Region::getPriority).reversed());
        return result;
    }

    /**
     * Verifica se uma região com o nome existe.
     */
    public boolean hasRegion(String name) {
        return nameToRegion.containsKey(name.toLowerCase());
    }

    /**
     * Limpa todo o cache.
     */
    public void clear() {
        chunkToRegions.clear();
        nameToRegion.clear();
    }

    /**
     * Reconstrói o cache a partir de uma coleção de regiões.
     */
    public void rebuild(Collection<Region> regions) {
        clear();
        for (Region region : regions) {
            addRegion(region);
        }
    }

    /**
     * Retorna o número total de regiões no cache.
     */
    public int size() {
        return nameToRegion.size();
    }
}
