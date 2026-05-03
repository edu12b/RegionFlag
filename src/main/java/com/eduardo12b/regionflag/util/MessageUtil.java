package com.eduardo12b.regionflag.util;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import com.eduardo12b.regionflag.RegionFlagPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Utilitário para envio de mensagens configuráveis com suporte a cores e placeholders.
 */
public final class MessageUtil {

    private final RegionFlagPlugin plugin;

    public MessageUtil(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    public void send(CommandSender sender, String key, String... replacements) {
        String message = getMessage(key);
        if (message == null || message.isEmpty()) return;
        message = getPrefix() + message;
        if (replacements.length >= 2) {
            for (int i = 0; i < replacements.length - 1; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(ColorUtil.parse(message));
    }

    public void sendRaw(CommandSender sender, String key, String... replacements) {
        String message = getMessage(key);
        if (message == null || message.isEmpty()) return;
        if (replacements.length >= 2) {
            for (int i = 0; i < replacements.length - 1; i += 2) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        sender.sendMessage(ColorUtil.parse(message));
    }

    public void sendDirect(CommandSender sender, String message) {
        sender.sendMessage(ColorUtil.parse(getPrefix() + message));
    }

    public String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "&cMensagem não encontrada: " + key);
    }

    public String getPrefix() {
        return plugin.getConfig().getString("prefix", "&7[RegionFlag] ");
    }

    public void log(String message) { plugin.getLogger().info(message); }
    public void debug(String message) {
        if (plugin.isDebugEnabled()) plugin.getLogger().info("[DEBUG] " + message);
    }
    public void warn(String message) { plugin.getLogger().warning(message); }
    public void error(String message) { plugin.getLogger().severe(message); }
    public void error(String message, Throwable t) { plugin.getLogger().severe(message); t.printStackTrace(); }
}
