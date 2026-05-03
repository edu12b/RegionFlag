package com.eduardo12b.regionflag.command;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.flag.RegionFlag;
import com.eduardo12b.regionflag.model.Region;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Executor principal do comando /regiao.
 * Delega subcomandos para os handlers apropriados.
 */
public class RegionCommand implements CommandExecutor {

    private final RegionFlagPlugin plugin;

    public RegionCommand(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "pos1" -> handlePos(sender, 1);
            case "pos2" -> handlePos(sender, 2);
            case "criar", "create" -> handleCreate(sender, args);
            case "deletar", "delete" -> handleDelete(sender, args);
            case "listar", "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "prioridade", "priority" -> handlePriority(sender, args);
            case "visualizar", "visualize" -> handleVisualize(sender, args);
            case "reload" -> handleReload(sender);
            case "debug" -> handleDebug(sender);
            case "ajuda", "help" -> showHelp(sender);
            default -> handleRegionFlag(sender, args);
        }

        return true;
    }

    // ==================== Subcomandos ====================

    private void handlePos(CommandSender sender, int pos) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        if (!player.hasPermission("regionflag.create")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }

        Location loc = player.getLocation();
        if (pos == 1) {
            plugin.getPlayerSelection().setPos1(player.getUniqueId(), loc);
            plugin.getMessageUtil().send(sender, "pos1-set",
                    "%x%", String.valueOf(loc.getBlockX()),
                    "%y%", String.valueOf(loc.getBlockY()),
                    "%z%", String.valueOf(loc.getBlockZ()));
        } else {
            plugin.getPlayerSelection().setPos2(player.getUniqueId(), loc);
            plugin.getMessageUtil().send(sender, "pos2-set",
                    "%x%", String.valueOf(loc.getBlockX()),
                    "%y%", String.valueOf(loc.getBlockY()),
                    "%z%", String.valueOf(loc.getBlockZ()));
        }
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        if (!player.hasPermission("regionflag.create")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao criar <nome>");
            return;
        }

        String name = args[1];

        if (!plugin.getPlayerSelection().hasCompleteSelection(player.getUniqueId())) {
            plugin.getMessageUtil().send(sender, "pos-incomplete");
            return;
        }

        if (!plugin.getPlayerSelection().isSameWorld(player.getUniqueId())) {
            plugin.getMessageUtil().sendDirect(sender, "&cAs posições devem estar no mesmo mundo.");
            return;
        }

        if (plugin.getRegionManager().getRegion(name) != null) {
            plugin.getMessageUtil().send(sender, "region-already-exists", "%name%", name);
            return;
        }

        Location pos1 = plugin.getPlayerSelection().getPos1(player.getUniqueId());
        Location pos2 = plugin.getPlayerSelection().getPos2(player.getUniqueId());

        boolean created = plugin.getRegionManager().createRegion(
                name,
                pos1.getWorld().getName(),
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ(),
                0 // prioridade padrão
        );

        if (created) {
            plugin.getMessageUtil().send(sender, "region-created", "%name%", name);
            plugin.getPlayerSelection().clear(player.getUniqueId());
        } else {
            plugin.getMessageUtil().send(sender, "region-already-exists", "%name%", name);
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("regionflag.admin")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao deletar <nome>");
            return;
        }

        String name = args[1];
        if (plugin.getRegionManager().deleteRegion(name)) {
            plugin.getMessageUtil().send(sender, "region-deleted", "%name%", name);
        } else {
            plugin.getMessageUtil().send(sender, "region-not-found", "%name%", name);
        }
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("regionflag.use")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }

        if (sender instanceof Player player) {
            String worldName = player.getWorld().getName();
            List<Region> regions = plugin.getRegionManager().getRegionsByWorld(worldName);

            plugin.getMessageUtil().send(sender, "region-list-header", "%world%", worldName);
            if (regions.isEmpty()) {
                plugin.getMessageUtil().sendRaw(sender, "region-list-empty");
            } else {
                for (Region region : regions) {
                    plugin.getMessageUtil().sendRaw(sender, "region-list-entry",
                            "%name%", region.getName(),
                            "%priority%", String.valueOf(region.getPriority()));
                }
            }
        } else {
            // Console: listar todas
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                plugin.getMessageUtil().sendDirect(sender,
                        "&e" + region.getName() + " &7(" + region.getWorldName() + ", p=" + region.getPriority() + ")");
            }
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("regionflag.use")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao info <nome>");
            return;
        }

        Region region = plugin.getRegionManager().getRegion(args[1]);
        if (region == null) {
            plugin.getMessageUtil().send(sender, "region-not-found", "%name%", args[1]);
            return;
        }

        plugin.getMessageUtil().sendRaw(sender, "region-info-header", "%name%", region.getName());
        plugin.getMessageUtil().sendRaw(sender, "region-info-world", "%world%", region.getWorldName());
        plugin.getMessageUtil().sendRaw(sender, "region-info-pos1",
                "%x1%", String.valueOf(region.getMinX()),
                "%y1%", String.valueOf(region.getMinY()),
                "%z1%", String.valueOf(region.getMinZ()));
        plugin.getMessageUtil().sendRaw(sender, "region-info-pos2",
                "%x2%", String.valueOf(region.getMaxX()),
                "%y2%", String.valueOf(region.getMaxY()),
                "%z2%", String.valueOf(region.getMaxZ()));
        plugin.getMessageUtil().sendRaw(sender, "region-info-priority",
                "%priority%", String.valueOf(region.getPriority()));

        List<String> activeFlags = region.getActiveFlags();
        String flagsStr = activeFlags.isEmpty() ? "nenhuma" : String.join(", ", activeFlags);
        plugin.getMessageUtil().sendRaw(sender, "region-info-flags", "%flags%", flagsStr);

        // Detalhes das flags ativas
        if (!region.getSounds().isEmpty()) {
            plugin.getMessageUtil().sendDirect(sender, "&7Sons: &f" + String.join(", ", region.getSounds()));
        }
        if (!region.getConsoleCommands().isEmpty()) {
            plugin.getMessageUtil().sendDirect(sender, "&7Comandos: &f" + region.getConsoleCommands().size() + " comando(s)");
        }
        if (region.getTitleText() != null) {
            plugin.getMessageUtil().sendDirect(sender, "&7Title: &f" + region.getTitleText());
            if (region.getSubtitleText() != null) {
                plugin.getMessageUtil().sendDirect(sender, "&7Subtitle: &f" + region.getSubtitleText());
            }
        }
        if (region.hasTeleport()) {
            plugin.getMessageUtil().sendDirect(sender, "&7Teleport: &f" 
                    + region.getTeleportX() + ", " + region.getTeleportY() + ", " + region.getTeleportZ()
                    + " &7(" + region.getTeleportWorld() + ")");
        }
    }

    private void handlePriority(CommandSender sender, String[] args) {
        if (!sender.hasPermission("regionflag.admin")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }
        // Formato: /regiao prioridade <nome> <valor>
        // OU:      /regiao <nome> prioridade <valor>
        if (args.length < 3) {
            plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao <nome> prioridade <valor>");
            return;
        }

        String regionName = args[1];
        int priority;
        try {
            priority = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            plugin.getMessageUtil().sendDirect(sender, "&cPrioridade deve ser um número inteiro.");
            return;
        }

        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            plugin.getMessageUtil().send(sender, "region-not-found", "%name%", regionName);
            return;
        }

        region.setPriority(priority);
        plugin.getRegionManager().saveRegions(region.getWorldName());
        // Rebuild cache para atualizar a ordem de prioridade
        plugin.getRegionManager().reload();

        plugin.getMessageUtil().send(sender, "priority-set",
                "%name%", region.getName(),
                "%priority%", String.valueOf(priority));
    }

    private void handleVisualize(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        if (!player.hasPermission("regionflag.admin")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao visualizar <nome>");
            return;
        }

        Region region = plugin.getRegionManager().getRegion(args[1]);
        if (region == null) {
            plugin.getMessageUtil().send(sender, "region-not-found", "%name%", args[1]);
            return;
        }

        plugin.getRegionVisualizer().visualize(player, region);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("regionflag.admin")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }

        plugin.reloadPlugin();
        plugin.getMessageUtil().send(sender, "reload-success");
    }

    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission("regionflag.admin")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }

        boolean newState = !plugin.isDebugEnabled();
        plugin.setDebugEnabled(newState);

        if (newState) {
            plugin.getMessageUtil().send(sender, "debug-enabled");
        } else {
            plugin.getMessageUtil().send(sender, "debug-disabled");
        }
    }

    /**
     * Tenta interpretar como: /regiao <nomeRegião> <flag> <args...>
     * Ex: /regiao spawn playsound minecraft:entity.experience_orb.pickup
     */
    private void handleRegionFlag(CommandSender sender, String[] args) {
        if (args.length < 2) {
            // Verificar se é uma região e mostrar info
            Region region = plugin.getRegionManager().getRegion(args[0]);
            if (region != null) {
                handleInfo(sender, new String[]{"info", args[0]});
            } else {
                plugin.getMessageUtil().send(sender, "unknown-command");
            }
            return;
        }

        String regionName = args[0];
        String flagId = args[1].toLowerCase();

        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            plugin.getMessageUtil().send(sender, "region-not-found", "%name%", regionName);
            return;
        }

        // Verificar se é "prioridade"
        if (flagId.equals("prioridade") || flagId.equals("priority")) {
            if (args.length < 3) {
                plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao <nome> prioridade <valor>");
                return;
            }
            handlePriority(sender, new String[]{"prioridade", regionName, args[2]});
            return;
        }

        // Buscar flag no registry
        RegionFlag flag = plugin.getFlagRegistry().getFlag(flagId);
        if (flag == null) {
            plugin.getMessageUtil().sendDirect(sender, "&cFlag desconhecida: &e" + flagId);
            plugin.getMessageUtil().sendDirect(sender, "&7Flags disponíveis: &f"
                    + String.join(", ", plugin.getFlagRegistry().getFlagIds()));
            return;
        }

        // Verificar permissão
        if (!sender.hasPermission(flag.getPermission())) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return;
        }

        // Delegar para a flag
        String[] flagArgs = args.length > 2 ? Arrays.copyOfRange(args, 2, args.length) : new String[0];
        flag.handleCommand(sender, region, flagArgs);
    }

    private void showHelp(CommandSender sender) {
        plugin.getMessageUtil().sendRaw(sender, "help-header");
        plugin.getMessageUtil().sendRaw(sender, "help-pos1");
        plugin.getMessageUtil().sendRaw(sender, "help-pos2");
        plugin.getMessageUtil().sendRaw(sender, "help-criar");
        plugin.getMessageUtil().sendRaw(sender, "help-deletar");
        plugin.getMessageUtil().sendRaw(sender, "help-listar");
        plugin.getMessageUtil().sendRaw(sender, "help-info");
        plugin.getMessageUtil().sendRaw(sender, "help-playsound");
        plugin.getMessageUtil().sendRaw(sender, "help-command");
        plugin.getMessageUtil().sendRaw(sender, "help-title");
        plugin.getMessageUtil().sendRaw(sender, "help-priority");
        plugin.getMessageUtil().sendRaw(sender, "help-visualizar");
        plugin.getMessageUtil().sendRaw(sender, "help-reload");
        plugin.getMessageUtil().sendRaw(sender, "help-debug");
        plugin.getMessageUtil().sendRaw(sender, "help-teleport");
    }
}
