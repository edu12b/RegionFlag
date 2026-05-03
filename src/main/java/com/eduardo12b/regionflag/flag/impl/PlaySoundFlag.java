package com.eduardo12b.regionflag.flag.impl;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.flag.RegionFlag;
import com.eduardo12b.regionflag.model.Region;
import com.eduardo12b.regionflag.util.CooldownManager;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Flag que toca sons ao entrar em uma região.
 */
public class PlaySoundFlag extends RegionFlag {

    public PlaySoundFlag(RegionFlagPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() { return "playsound"; }

    @Override
    public String getPermission() { return "regionflag.flag.playsound"; }

    @Override
    public String getDescription() { return "Toca sons ao entrar na região"; }

    @Override
    public void onEnter(Player player, Region region) {
        List<String> sounds = region.getSounds();
        if (sounds.isEmpty()) return;

        // Verificar cooldown
        int cooldown = region.getPlaysoundCooldown();
        if (cooldown < 0) {
            cooldown = plugin.getConfig().getInt("cooldowns.playsound", 5);
        }

        String cdKey = CooldownManager.buildKey("playsound", region.getName());
        if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), cdKey, cooldown)) {
            return;
        }
        plugin.getCooldownManager().setCooldown(player.getUniqueId(), cdKey);

        // Tocar sons usando Entity Scheduler (Folia-safe)
        player.getScheduler().run(plugin, task -> {
            for (String soundName : sounds) {
                try {
                    // Processar PlaceholderAPI
                    String processedSound = plugin.processPlaceholders(player, soundName);
                    player.playSound(player.getLocation(), processedSound, 1.0f, 1.0f);
                    plugin.getMessageUtil().debug("Som tocado para " + player.getName() + ": " + processedSound);
                } catch (Exception e) {
                    plugin.getMessageUtil().error("Erro ao tocar som '" + soundName + "' para " + player.getName(), e);
                }
            }
        }, null);
    }

    @Override
    public void onExit(Player player, Region region) {
        // Nenhuma ação na saída por padrão
    }

    @Override
    public boolean handleCommand(CommandSender sender, Region region, String[] args) {
        if (args.length < 1) {
            plugin.getMessageUtil().send(sender, "flag-playsound-list",
                    "%sounds%", String.join(", ", region.getSounds()));
            return true;
        }

        String subCmd = args[0].toLowerCase();

        if (subCmd.equals("remover") || subCmd.equals("remove")) {
            if (args.length < 2) {
                plugin.getMessageUtil().sendDirect(sender, "&cUso: /regiao <nome> playsound remover <som>");
                return true;
            }
            region.removeSound(args[1]);
            plugin.getRegionManager().saveRegions(region.getWorldName());
            plugin.getMessageUtil().send(sender, "flag-playsound-removed",
                    "%region%", region.getName());
            return true;
        }

        if (subCmd.equals("listar") || subCmd.equals("list")) {
            List<String> sounds = region.getSounds();
            if (sounds.isEmpty()) {
                plugin.getMessageUtil().sendDirect(sender, "&7Nenhum som configurado.");
            } else {
                plugin.getMessageUtil().sendDirect(sender, "&6Sons da região &e" + region.getName() + "&6:");
                for (int i = 0; i < sounds.size(); i++) {
                    plugin.getMessageUtil().sendRaw(sender, "region-list-entry",
                            "%name%", sounds.get(i), "%priority%", String.valueOf(i));
                }
            }
            return true;
        }

        // Adicionar som
        String sound = args[0];
        region.addSound(sound);
        plugin.getRegionManager().saveRegions(region.getWorldName());
        plugin.getMessageUtil().send(sender, "flag-playsound-added",
                "%sound%", sound, "%region%", region.getName());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("remover");
            completions.add("listar");
            // Sugerir sons comuns do Minecraft
            String partial = args[0].toLowerCase();
            for (Sound sound : Sound.values()) {
                String name = sound.key().asString();
                if (name.toLowerCase().contains(partial)) {
                    completions.add(name);
                    if (completions.size() > 30) break;
                }
            }
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("remover") || args[0].equalsIgnoreCase("remove"))) {
            // Nota: sem acesso à região aqui, sugerir sons genéricos
            completions.add("<som>");
        }
        return completions;
    }

    @Override
    public boolean isActive(Region region) {
        return !region.getSounds().isEmpty();
    }
}
