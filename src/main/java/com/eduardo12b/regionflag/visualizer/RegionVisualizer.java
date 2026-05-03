package com.eduardo12b.regionflag.visualizer;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.model.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Visualiza as bordas de uma região com partículas.
 * Usa Region Scheduler para thread-safety no Folia.
 */
public class RegionVisualizer {

    private final RegionFlagPlugin plugin;

    public RegionVisualizer(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Exibe partículas nas bordas de uma região por um determinado tempo.
     *
     * @param player O jogador que verá as partículas
     * @param region A região a visualizar
     */
    public void visualize(Player player, Region region) {
        World world = Bukkit.getWorld(region.getWorldName());
        if (world == null) {
            plugin.getMessageUtil().sendDirect(player, "&cMundo '" + region.getWorldName() + "' não encontrado.");
            return;
        }

        int duration = plugin.getConfig().getInt("visualizer.duration", 5);
        double density = plugin.getConfig().getDouble("visualizer.density", 0.5);
        String particleName = plugin.getConfig().getString("visualizer.particle", "FLAME");

        Particle particle;
        try {
            particle = Particle.valueOf(particleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            particle = Particle.FLAME;
        }

        int totalTicks = duration * 20;
        int intervalTicks = 5; // Atualizar a cada 5 ticks (0.25s)

        final Particle finalParticle = particle;
        final double step = density;

        // Usar Location central da região para o Region Scheduler
        Location centerLoc = new Location(world,
                (region.getMinX() + region.getMaxX()) / 2.0,
                (region.getMinY() + region.getMaxY()) / 2.0,
                (region.getMinZ() + region.getMaxZ()) / 2.0
        );

        // Agendar tarefa repetida
        final int[] ticksElapsed = {0};

        Bukkit.getRegionScheduler().runAtFixedRate(plugin, centerLoc, task -> {
            ticksElapsed[0] += intervalTicks;
            if (ticksElapsed[0] > totalTicks) {
                task.cancel();
                return;
            }

            spawnEdgeParticles(world, region, finalParticle, step, player);
        }, 1L, intervalTicks);

        plugin.getMessageUtil().send(player, "visualize-start",
                "%name%", region.getName(),
                "%duration%", String.valueOf(duration));
    }

    /**
     * Spawna partículas nas arestas (edges) da região cuboidal.
     */
    private void spawnEdgeParticles(World world, Region region, Particle particle, double step, Player player) {
        int minX = region.getMinX();
        int minY = region.getMinY();
        int minZ = region.getMinZ();
        int maxX = region.getMaxX() + 1;
        int maxY = region.getMaxY() + 1;
        int maxZ = region.getMaxZ() + 1;

        // 4 arestas no eixo X (bottom)
        for (double x = minX; x <= maxX; x += step) {
            spawnParticle(player, world, particle, x, minY, minZ);
            spawnParticle(player, world, particle, x, minY, maxZ);
            spawnParticle(player, world, particle, x, maxY, minZ);
            spawnParticle(player, world, particle, x, maxY, maxZ);
        }

        // 4 arestas no eixo Y (vertical)
        for (double y = minY; y <= maxY; y += step) {
            spawnParticle(player, world, particle, minX, y, minZ);
            spawnParticle(player, world, particle, minX, y, maxZ);
            spawnParticle(player, world, particle, maxX, y, minZ);
            spawnParticle(player, world, particle, maxX, y, maxZ);
        }

        // 4 arestas no eixo Z
        for (double z = minZ; z <= maxZ; z += step) {
            spawnParticle(player, world, particle, minX, minY, z);
            spawnParticle(player, world, particle, maxX, minY, z);
            spawnParticle(player, world, particle, minX, maxY, z);
            spawnParticle(player, world, particle, maxX, maxY, z);
        }
    }

    /**
     * Spawna uma partícula visível apenas para o jogador alvo.
     */
    private void spawnParticle(Player player, World world, Particle particle, double x, double y, double z) {
        Location loc = new Location(world, x, y, z);
        // Só envia se o jogador está perto o suficiente (128 blocos)
        if (player.getLocation().distanceSquared(loc) <= 16384) {
            player.spawnParticle(particle, loc, 1, 0, 0, 0, 0);
        }
    }
}
