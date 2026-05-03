package com.eduardo12b.regionflag.flag.impl;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.flag.RegionFlag;
import com.eduardo12b.regionflag.model.Region;
import com.eduardo12b.regionflag.util.ColorUtil;
import com.eduardo12b.regionflag.util.CooldownManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Flag que exibe title/subtitle ao entrar em uma região.
 * Suporta cores HEX (&#RRGGBB) e legacy (&a, &b etc).
 */
public class TitleFlag extends RegionFlag {

    public TitleFlag(RegionFlagPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "title"; }

    @Override
    public String getPermission() { return "regionflag.flag.title"; }

    @Override
    public String getDescription() { return "Exibe title/subtitle ao entrar na região"; }

    @Override
    public void onEnter(Player player, Region region) {
        String titleText = region.getTitleText();
        if (titleText == null) return;

        // Verificar cooldown
        int cooldown = region.getTitleCooldown();
        if (cooldown < 0) {
            cooldown = plugin.getConfig().getInt("cooldowns.title", 15);
        }

        String cdKey = CooldownManager.buildKey("title", region.getName());
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), cdKey, cooldown)) {
            return;
        }
        plugin.getCooldownManager().setCooldown(player.getUniqueId(), cdKey);

        // Processar placeholders
        String processedTitle = plugin.processPlaceholders(player, titleText);
        String processedSubtitle = region.getSubtitleText() != null
                ? plugin.processPlaceholders(player, region.getSubtitleText())
                : "";

        // Converter cores
        Component titleComponent = ColorUtil.parse(processedTitle);
        Component subtitleComponent = ColorUtil.parse(processedSubtitle);

        // Timing (em ticks → convertido para milissegundos: 1 tick = 50ms)
        int fadeIn = region.getTitleFadeIn();
        int stay = region.getTitleStay();
        int fadeOut = region.getTitleFadeOut();

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );

        Title title = Title.title(titleComponent, subtitleComponent, times);

        // Enviar via Entity Scheduler (Folia-safe)
        player.getScheduler().run(plugin, task -> {
            player.showTitle(title);
            plugin.getMessageUtil().debug("Title enviado para " + player.getName() + " na região " + region.getName());
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
            if (region.getTitleText() == null) {
                plugin.getMessageUtil().sendDirect(sender, "&7Nenhum title configurado para &e" + region.getName() + "&7.");
            } else {
                plugin.getMessageUtil().sendDirect(sender, "&6Title da região &e" + region.getName() + "&6:");
                plugin.getMessageUtil().sendDirect(sender, "&7Title: &f" + region.getTitleText());
                if (region.getSubtitleText() != null) {
                    plugin.getMessageUtil().sendDirect(sender, "&7Subtitle: &f" + region.getSubtitleText());
                }
                plugin.getMessageUtil().sendDirect(sender, "&7FadeIn: &f" + region.getTitleFadeIn()
                        + " &7Stay: &f" + region.getTitleStay()
                        + " &7FadeOut: &f" + region.getTitleFadeOut());
            }
            return true;
        }

        String subCmd = args[0].toLowerCase();

        if (subCmd.equals("remover") || subCmd.equals("remove")) {
            region.clearTitle();
            plugin.getRegionManager().saveRegions(region.getWorldName());
            plugin.getMessageUtil().send(sender, "flag-title-removed",
                    "%region%", region.getName());
            return true;
        }

        if (subCmd.equals("tempo") || subCmd.equals("timing")) {
            if (args.length < 4) {
                plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao <nome> title tempo <fadeIn> <stay> <fadeOut>");
                return true;
            }
            try {
                int fadeIn = Integer.parseInt(args[1]);
                int stay = Integer.parseInt(args[2]);
                int fadeOut = Integer.parseInt(args[3]);
                region.setTitleTiming(fadeIn, stay, fadeOut);
                plugin.getRegionManager().saveRegions(region.getWorldName());
                plugin.getMessageUtil().sendDirect(sender, "&aTiming atualizado: fadeIn=" + fadeIn
                        + " stay=" + stay + " fadeOut=" + fadeOut);
            } catch (NumberFormatException e) {
                plugin.getMessageUtil().sendDirect(sender, "&cValores devem ser números inteiros (em ticks).");
            }
            return true;
        }

        // Definir title e subtitle
        // Formato: /regiao <nome> title <title> ; <subtitle>
        String fullText = String.join(" ", args);
        String titleText;
        String subtitleText = null;

        if (fullText.contains(" ; ")) {
            String[] parts = fullText.split(" ; ", 2);
            titleText = parts[0].trim();
            subtitleText = parts[1].trim();
        } else if (fullText.contains(";")) {
            String[] parts = fullText.split(";", 2);
            titleText = parts[0].trim();
            subtitleText = parts[1].trim();
        } else {
            titleText = fullText.trim();
        }

        region.setTitle(titleText, subtitleText);

        // Definir timing padrão do config se não estiver definido
        int defFadeIn = plugin.getConfig().getInt("title.fadeIn", 10);
        int defStay = plugin.getConfig().getInt("title.stay", 70);
        int defFadeOut = plugin.getConfig().getInt("title.fadeOut", 20);
        region.setTitleTiming(defFadeIn, defStay, defFadeOut);

        plugin.getRegionManager().saveRegions(region.getWorldName());
        plugin.getMessageUtil().send(sender, "flag-title-set",
                "%region%", region.getName());
        plugin.getMessageUtil().sendDirect(sender, "&7Title: &f" + titleText);
        if (subtitleText != null) {
            plugin.getMessageUtil().sendDirect(sender, "&7Subtitle: &f" + subtitleText);
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("remover");
            completions.add("tempo");
            completions.add("<titulo>");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("tempo") || args[0].equalsIgnoreCase("timing"))) {
            completions.add("<fadeIn>");
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("tempo") || args[0].equalsIgnoreCase("timing"))) {
            completions.add("<stay>");
        }
        if (args.length == 4 && (args[0].equalsIgnoreCase("tempo") || args[0].equalsIgnoreCase("timing"))) {
            completions.add("<fadeOut>");
        }
        return completions;
    }

    @Override
    public boolean isActive(Region region) {
        return region.getTitleText() != null;
    }
}
