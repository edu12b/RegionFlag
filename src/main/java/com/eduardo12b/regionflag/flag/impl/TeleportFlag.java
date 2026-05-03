package com.eduardo12b.regionflag.flag.impl;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.flag.RegionFlag;
import com.eduardo12b.regionflag.model.Region;
import com.eduardo12b.regionflag.util.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Flag que teleporta o jogador para coordenadas específicas ao entrar em uma região.
 *
 * Comando: /regiao <nome> teleport <x> <y> <z> [yaw] [pitch] [mundo]
 * Exemplo: /regiao spawn teleport 100 64 200 0 0 world
 */
public class TeleportFlag extends RegionFlag {

    public TeleportFlag(RegionFlagPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "teleport"; }

    @Override
    public String getPermission() { return "regionflag.flag.teleport"; }

    @Override
    public String getDescription() { return "Teleporta o jogador ao entrar na região"; }

    @Override
    public void onEnter(Player player, Region region) {
        if (!region.hasTeleport()) return;

        // Verificar cooldown
        int cooldown = region.getTeleportCooldown();
        if (cooldown < 0) {
            cooldown = plugin.getConfig().getInt("cooldowns.teleport", 10);
        }

        String cdKey = CooldownManager.buildKey("teleport", region.getName());
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), cdKey, cooldown)) {
            return;
        }
        plugin.getCooldownManager().setCooldown(player.getUniqueId(), cdKey);

        // Resolver mundo de destino
        String targetWorldName = region.getTeleportWorld();
        if (targetWorldName == null || targetWorldName.isEmpty()) {
            targetWorldName = region.getWorldName();
        }

        World targetWorld = Bukkit.getWorld(targetWorldName);
        if (targetWorld == null) {
            plugin.getMessageUtil().error("Mundo de teleport '" + targetWorldName
                    + "' não encontrado para região '" + region.getName() + "'");
            return;
        }

        Location destination = new Location(
                targetWorld,
                region.getTeleportX(),
                region.getTeleportY(),
                region.getTeleportZ(),
                region.getTeleportYaw(),
                region.getTeleportPitch()
        );

        // Teleportar via Entity Scheduler (Folia-safe)
        player.getScheduler().run(plugin, task -> {
            player.teleportAsync(destination).thenAccept(success -> {
                if (success) {
                    plugin.getMessageUtil().debug("Jogador " + player.getName()
                            + " teleportado para " + formatLocation(destination)
                            + " pela região " + region.getName());
                } else {
                    plugin.getMessageUtil().debug("Falha ao teleportar " + player.getName()
                            + " pela região " + region.getName());
                }
            });
        }, null);
    }

    @Override
    public void onExit(Player player, Region region) {
        // Nenhuma ação na saída
    }

    @Override
    public boolean handleCommand(CommandSender sender, Region region, String[] args) {
        if (args.length < 1) {
            // Mostrar configuração atual
            if (!region.hasTeleport()) {
                plugin.getMessageUtil().sendDirect(sender,
                        "&7Nenhum teleport configurado para &e" + region.getName() + "&7.");
            } else {
                plugin.getMessageUtil().sendDirect(sender,
                        "&6Teleport da região &e" + region.getName() + "&6:");
                plugin.getMessageUtil().sendDirect(sender,
                        "&7Destino: &f" + region.getTeleportX() + ", "
                                + region.getTeleportY() + ", " + region.getTeleportZ());
                plugin.getMessageUtil().sendDirect(sender,
                        "&7Yaw: &f" + region.getTeleportYaw()
                                + " &7Pitch: &f" + region.getTeleportPitch());
                plugin.getMessageUtil().sendDirect(sender,
                        "&7Mundo: &f" + region.getTeleportWorld());
            }
            return true;
        }

        String subCmd = args[0].toLowerCase();

        // Remover teleport
        if (subCmd.equals("remover") || subCmd.equals("remove")) {
            region.clearTeleport();
            plugin.getRegionManager().saveRegions(region.getWorldName());
            plugin.getMessageUtil().sendDirect(sender,
                    "&aTeleport removido da região &e" + region.getName() + "&a.");
            return true;
        }

        // Definir teleport: <x> <y> <z> [yaw] [pitch] [mundo]
        if (args.length < 3) {
            plugin.getMessageUtil().sendDirect(sender,
                    "&cUso: /regiao <nome> teleport <x> <y> <z> [yaw] [pitch] [mundo]");
            return true;
        }

        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            float yaw = args.length > 3 ? Float.parseFloat(args[3]) : 0f;
            float pitch = args.length > 4 ? Float.parseFloat(args[4]) : 0f;

            String world;
            if (args.length > 5) {
                world = args[5];
            } else if (sender instanceof Player player) {
                world = player.getWorld().getName();
            } else {
                world = region.getWorldName();
            }

            // Validar que o mundo existe
            if (Bukkit.getWorld(world) == null) {
                plugin.getMessageUtil().sendDirect(sender,
                        "&cMundo &e" + world + " &cnão encontrado.");
                return true;
            }

            region.setTeleport(x, y, z, yaw, pitch, world);
            plugin.getRegionManager().saveRegions(region.getWorldName());

            plugin.getMessageUtil().sendDirect(sender,
                    "&aTeleport configurado para a região &e" + region.getName() + "&a!");
            plugin.getMessageUtil().sendDirect(sender,
                    "&7Destino: &f" + x + ", " + y + ", " + z
                            + " &7(yaw=" + yaw + ", pitch=" + pitch + ")");
            plugin.getMessageUtil().sendDirect(sender, "&7Mundo: &f" + world);

        } catch (NumberFormatException e) {
            plugin.getMessageUtil().sendDirect(sender,
                    "&cCoordenadas inválidas. Use números válidos.");
            plugin.getMessageUtil().sendDirect(sender,
                    "&cUso: /regiao <nome> teleport <x> <y> <z> [yaw] [pitch] [mundo]");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        switch (args.length) {
            case 1 -> {
                completions.add("remover");
                if (sender instanceof Player player) {
                    // Sugerir coordenadas atuais do jogador
                    Location loc = player.getLocation();
                    completions.add(String.valueOf(loc.getBlockX()));
                }
            }
            case 2 -> {
                if (sender instanceof Player player) {
                    completions.add(String.valueOf(player.getLocation().getBlockY()));
                }
            }
            case 3 -> {
                if (sender instanceof Player player) {
                    completions.add(String.valueOf(player.getLocation().getBlockZ()));
                }
            }
            case 4 -> {
                completions.add("0");
                if (sender instanceof Player player) {
                    completions.add(String.valueOf(Math.round(player.getLocation().getYaw())));
                }
            }
            case 5 -> {
                completions.add("0");
                if (sender instanceof Player player) {
                    completions.add(String.valueOf(Math.round(player.getLocation().getPitch())));
                }
            }
            case 6 -> {
                // Sugerir mundos disponíveis
                for (World world : Bukkit.getWorlds()) {
                    completions.add(world.getName());
                }
            }
        }

        return completions;
    }

    @Override
    public boolean isActive(Region region) {
        return region.hasTeleport();
    }

    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f (%s)",
                loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }
}
