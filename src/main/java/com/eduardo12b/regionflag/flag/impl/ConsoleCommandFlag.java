package com.eduardo12b.regionflag.flag.impl;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.flag.RegionFlag;
import com.eduardo12b.regionflag.model.Region;
import com.eduardo12b.regionflag.util.CooldownManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Flag que executa comandos no console ao entrar em uma região.
 */
public class ConsoleCommandFlag extends RegionFlag {

    public ConsoleCommandFlag(RegionFlagPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "console_command"; }

    @Override
    public String getPermission() { return "regionflag.flag.console_command"; }

    @Override
    public String getDescription() { return "Executa comandos no console ao entrar na região"; }

    @Override
    public void onEnter(Player player, Region region) {
        List<String> commands = region.getConsoleCommands();
        if (commands.isEmpty()) return;

        // Verificar cooldown
        int cooldown = region.getConsoleCommandCooldown();
        if (cooldown < 0) {
            cooldown = plugin.getConfig().getInt("cooldowns.console_command", 10);
        }

        String cdKey = CooldownManager.buildKey("console_command", region.getName());
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), cdKey, cooldown)) {
            return;
        }
        plugin.getCooldownManager().setCooldown(player.getUniqueId(), cdKey);

        // Executar comandos no Global Region Scheduler (Folia-safe para comandos de console)
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            for (String command : commands) {
                try {
                    // Processar placeholders
                    String processedCmd = plugin.processPlaceholders(player, command);
                    // Substituir %player_name% manualmente caso PlaceholderAPI não esteja presente
                    processedCmd = processedCmd.replace("%player_name%", player.getName());
                    processedCmd = processedCmd.replace("%player_uuid%", player.getUniqueId().toString());

                    plugin.getMessageUtil().debug("Executando comando console para " + player.getName() + ": " + processedCmd);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd);
                } catch (Exception e) {
                    plugin.getMessageUtil().error("Erro ao executar comando '" + command + "' para " + player.getName(), e);
                }
            }
        });
    }

    @Override
    public void onExit(Player player, Region region) {
        // Nenhuma ação na saída
    }

    @Override
    public boolean handleCommand(CommandSender sender, Region region, String[] args) {
        if (args.length < 1) {
            // Listar comandos
            List<String> commands = region.getConsoleCommands();
            if (commands.isEmpty()) {
                plugin.getMessageUtil().sendDirect(sender, "&7Nenhum comando configurado.");
            } else {
                plugin.getMessageUtil().sendDirect(sender, "&6Comandos da região &e" + region.getName() + "&6:");
                for (int i = 0; i < commands.size(); i++) {
                    plugin.getMessageUtil().sendDirect(sender, "  &8[" + i + "] &f" + commands.get(i));
                }
            }
            return true;
        }

        String subCmd = args[0].toLowerCase();

        if (subCmd.equals("remover") || subCmd.equals("remove")) {
            if (args.length < 2) {
                plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao <nome> console_command remover <índice>");
                return true;
            }
            try {
                int index = Integer.parseInt(args[1]);
                region.removeConsoleCommand(index);
                plugin.getRegionManager().saveRegions(region.getWorldName());
                plugin.getMessageUtil().send(sender, "flag-command-removed",
                        "%region%", region.getName());
            } catch (NumberFormatException e) {
                plugin.getMessageUtil().sendDirect(sender, "&cÍndice inválido.");
            }
            return true;
        }

        if (subCmd.equals("listar") || subCmd.equals("list")) {
            return handleCommand(sender, region, new String[0]);
        }

        // Adicionar comando — junta todos os args como o comando
        String command = String.join(" ", args);
        region.addConsoleCommand(command);
        plugin.getRegionManager().saveRegions(region.getWorldName());
        plugin.getMessageUtil().send(sender, "flag-command-added",
                "%region%", region.getName());
        plugin.getMessageUtil().sendDirect(sender, "&7Comando: &f" + command);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("remover");
            completions.add("listar");
            completions.add("<comando>");
        }
        return completions;
    }

    @Override
    public boolean isActive(Region region) {
        return !region.getConsoleCommands().isEmpty();
    }
}
