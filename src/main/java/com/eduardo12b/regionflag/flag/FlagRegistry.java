package com.eduardo12b.regionflag.flag;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.model.Region;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro central de flags. Permite adicionar novas flags de forma modular.
 */
public class FlagRegistry {

    private final RegionFlagPlugin plugin;
    private final Map<String, RegionFlag> flags = new ConcurrentHashMap<>();

    public FlagRegistry(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registra uma nova flag no sistema.
     */
    public void registerFlag(RegionFlag flag) {
        flags.put(flag.getId().toLowerCase(), flag);
        plugin.getMessageUtil().log("Flag registrada: " + flag.getId());
    }

    /**
     * Obtém uma flag pelo ID.
     */
    public RegionFlag getFlag(String id) {
        return flags.get(id.toLowerCase());
    }

    /**
     * Retorna todas as flags registradas.
     */
    public Collection<RegionFlag> getAllFlags() {
        return Collections.unmodifiableCollection(flags.values());
    }

    /**
     * Retorna os IDs de todas as flags registradas.
     */
    public Set<String> getFlagIds() {
        return Collections.unmodifiableSet(flags.keySet());
    }

    /**
     * Verifica se uma flag está registrada.
     */
    public boolean hasFlag(String id) {
        return flags.containsKey(id.toLowerCase());
    }

    /**
     * Dispara onEnter para todas as flags ativas na região.
     */
    public void fireEnter(Player player, Region region) {
        for (RegionFlag flag : flags.values()) {
            try {
                if (flag.isActive(region)) {
                    flag.onEnter(player, region);
                }
            } catch (Exception e) {
                plugin.getMessageUtil().error(
                    "Erro ao executar flag '" + flag.getId() + "' onEnter na região '" + region.getName() + "'", e
                );
            }
        }
    }

    /**
     * Dispara onExit para todas as flags ativas na região.
     */
    public void fireExit(Player player, Region region) {
        for (RegionFlag flag : flags.values()) {
            try {
                if (flag.isActive(region)) {
                    flag.onExit(player, region);
                }
            } catch (Exception e) {
                plugin.getMessageUtil().error(
                    "Erro ao executar flag '" + flag.getId() + "' onExit na região '" + region.getName() + "'", e
                );
            }
        }
    }
}
