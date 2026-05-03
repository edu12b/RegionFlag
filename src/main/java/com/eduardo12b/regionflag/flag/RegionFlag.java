package com.eduardo12b.regionflag.flag;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.model.Region;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Classe abstrata para flags de região.
 * Cada flag representa uma ação executada ao entrar/sair de uma região.
 *
 * Para adicionar uma nova flag:
 * 1. Estenda esta classe
 * 2. Implemente todos os métodos abstratos
 * 3. Registre no FlagRegistry em RegionFlagPlugin.onEnable()
 */
public abstract class RegionFlag {

    protected final RegionFlagPlugin plugin;

    protected RegionFlag(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    /** Identificador único da flag (ex: "playsound", "console_command", "title") */
    public abstract String getId();

    /** Permissão necessária para configurar esta flag */
    public abstract String getPermission();

    /** Descrição curta da flag para ajuda */
    public abstract String getDescription();

    /** Executado quando um jogador ENTRA em uma região com esta flag */
    public abstract void onEnter(Player player, Region region);

    /** Executado quando um jogador SAI de uma região com esta flag */
    public abstract void onExit(Player player, Region region);

    /**
     * Processa o subcomando da flag.
     * Ex: /regiao <nome> playsound <som>
     *
     * @param sender O executor do comando
     * @param region A região alvo
     * @param args   Argumentos após o ID da flag
     * @return true se o comando foi processado com sucesso
     */
    public abstract boolean handleCommand(CommandSender sender, Region region, String[] args);

    /**
     * Retorna sugestões de tab completion.
     *
     * @param sender O executor
     * @param args   Argumentos atuais
     * @return Lista de sugestões
     */
    public abstract List<String> tabComplete(CommandSender sender, String[] args);

    /**
     * Verifica se a flag está ativa (configurada) em uma região.
     */
    public abstract boolean isActive(Region region);
}
