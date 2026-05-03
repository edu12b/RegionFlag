package com.eduardo12b.regionflag;

import com.eduardo12b.regionflag.command.RegionCommand;
import com.eduardo12b.regionflag.command.RegionTabCompleter;
import com.eduardo12b.regionflag.flag.FlagRegistry;
import com.eduardo12b.regionflag.flag.impl.ConsoleCommandFlag;
import com.eduardo12b.regionflag.flag.impl.PlaySoundFlag;
import com.eduardo12b.regionflag.flag.impl.TeleportFlag;
import com.eduardo12b.regionflag.flag.impl.TitleFlag;
import com.eduardo12b.regionflag.listener.PlayerConnectionListener;
import com.eduardo12b.regionflag.listener.PlayerMoveListener;
import com.eduardo12b.regionflag.manager.PlayerTracker;
import com.eduardo12b.regionflag.manager.RegionManager;
import com.eduardo12b.regionflag.model.PlayerSelection;
import com.eduardo12b.regionflag.util.CooldownManager;
import com.eduardo12b.regionflag.util.MessageUtil;
import com.eduardo12b.regionflag.visualizer.RegionVisualizer;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

/**
 * Classe principal do plugin RegionFlag.
 * Compatível com Paper e Folia (thread-safe).
 *
 * @author Eduardo12B
 */
public class RegionFlagPlugin extends JavaPlugin {

    private MessageUtil messageUtil;
    private CooldownManager cooldownManager;
    private PlayerSelection playerSelection;
    private RegionManager regionManager;
    private PlayerTracker playerTracker;
    private FlagRegistry flagRegistry;
    private RegionVisualizer regionVisualizer;

    private boolean debugEnabled = false;
    private boolean placeholderApiEnabled = false;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        // Salvar config padrão
        saveDefaultConfig();

        // Inicializar utilitários
        this.messageUtil = new MessageUtil(this);
        this.cooldownManager = new CooldownManager();
        this.playerSelection = new PlayerSelection();
        this.debugEnabled = getConfig().getBoolean("debug", false);

        // Inicializar managers
        this.regionManager = new RegionManager(this);
        this.playerTracker = new PlayerTracker(this);
        this.regionVisualizer = new RegionVisualizer(this);

        // Inicializar sistema de flags
        this.flagRegistry = new FlagRegistry(this);
        registerDefaultFlags();

        // Carregar regiões
        regionManager.loadAllRegions();

        // Registrar comandos
        registerCommands();

        // Registrar listeners
        registerListeners();

        // Verificar PlaceholderAPI
        checkPlaceholderAPI();

        // Agendar backup automático
        scheduleBackup();

        long elapsed = System.currentTimeMillis() - start;
        messageUtil.log("=================================");
        messageUtil.log("  RegionFlag v" + getDescription().getVersion());
        messageUtil.log("  Autor: Eduardo12B");
        messageUtil.log("  Regiões: " + regionManager.getAllRegions().size());
        messageUtil.log("  Flags: " + flagRegistry.getFlagIds().size());
        messageUtil.log("  PlaceholderAPI: " + (placeholderApiEnabled ? "Sim" : "Não"));
        messageUtil.log("  Folia: " + (isFolia() ? "Sim" : "Não"));
        messageUtil.log("  Carregado em " + elapsed + "ms");
        messageUtil.log("=================================");
    }

    @Override
    public void onDisable() {
        // Salvar todas as regiões
        if (regionManager != null) {
            regionManager.saveAllRegions();
            messageUtil.log("Regiões salvas com sucesso.");
        }

        // Limpar dados
        if (playerTracker != null) playerTracker.clearAll();
        if (cooldownManager != null) cooldownManager.clearAll();
        if (playerSelection != null) playerSelection.clearAll();

        messageUtil.log("RegionFlag desativado.");
    }

    // ==================== Inicialização ====================

    private void registerDefaultFlags() {
        flagRegistry.registerFlag(new PlaySoundFlag(this));
        flagRegistry.registerFlag(new ConsoleCommandFlag(this));
        flagRegistry.registerFlag(new TitleFlag(this));
        flagRegistry.registerFlag(new TeleportFlag(this));
    }

    private void registerCommands() {
        PluginCommand cmd = getCommand("regiao");
        if (cmd != null) {
            RegionCommand executor = new RegionCommand(this);
            RegionTabCompleter tabCompleter = new RegionTabCompleter(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(tabCompleter);
        } else {
            messageUtil.error("Falha ao registrar comando /regiao!");
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
    }

    private void checkPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderApiEnabled = true;
            messageUtil.log("PlaceholderAPI detectado! Suporte a placeholders ativado.");
        }
    }

    private void scheduleBackup() {
        if (!getConfig().getBoolean("backup.enabled", true)) return;

        long intervalSeconds = getConfig().getLong("backup.interval", 300);

        Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> {
            regionManager.backup();
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);

        messageUtil.debug("Backup automático agendado a cada " + intervalSeconds + "s.");
    }

    // ==================== Reload ====================

    public void reloadPlugin() {
        reloadConfig();
        debugEnabled = getConfig().getBoolean("debug", false);
        regionManager.reload();
        cooldownManager.clearAll();
        playerTracker.clearAll();

        // Re-verificar posições de jogadores online
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getScheduler().run(this, task -> {
                playerTracker.checkInitialPosition(player);
            }, null);
        }

        messageUtil.log("Plugin recarregado. " + regionManager.getAllRegions().size() + " regiões carregadas.");
    }

    // ==================== Utilitários ====================

    /**
     * Processa placeholders do PlaceholderAPI em uma string.
     */
    public String processPlaceholders(Player player, String text) {
        if (text == null) return "";
        if (placeholderApiEnabled) {
            try {
                return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                messageUtil.debug("Erro ao processar placeholder: " + e.getMessage());
            }
        }
        // Fallback: substituições básicas
        text = text.replace("%player_name%", player.getName());
        text = text.replace("%player_uuid%", player.getUniqueId().toString());
        text = text.replace("%player_world%", player.getWorld().getName());
        return text;
    }

    /**
     * Verifica se o servidor está rodando Folia.
     */
    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ==================== Getters ====================

    public MessageUtil getMessageUtil() { return messageUtil; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public PlayerSelection getPlayerSelection() { return playerSelection; }
    public RegionManager getRegionManager() { return regionManager; }
    public PlayerTracker getPlayerTracker() { return playerTracker; }
    public FlagRegistry getFlagRegistry() { return flagRegistry; }
    public RegionVisualizer getRegionVisualizer() { return regionVisualizer; }

    public boolean isDebugEnabled() { return debugEnabled; }
    public void setDebugEnabled(boolean enabled) { this.debugEnabled = enabled; }
    public boolean isPlaceholderApiEnabled() { return placeholderApiEnabled; }
}
