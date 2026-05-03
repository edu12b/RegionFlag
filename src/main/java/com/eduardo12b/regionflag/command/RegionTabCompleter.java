package com.eduardo12b.regionflag.command;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.flag.RegionFlag;
import com.eduardo12b.regionflag.model.Region;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completer inteligente para o comando /regiao.
 */
public class RegionTabCompleter implements TabCompleter {

    private final RegionFlagPlugin plugin;

    public RegionTabCompleter(RegionFlagPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Primeiro argumento: subcomandos + nomes de regiões
            List<String> options = new ArrayList<>();
            options.add("pos1");
            options.add("pos2");
            options.add("criar");
            options.add("deletar");
            options.add("listar");
            options.add("info");
            options.add("visualizar");
            options.add("reload");
            options.add("debug");
            options.add("ajuda");
            // Adicionar nomes de regiões
            options.addAll(plugin.getRegionManager().getRegionNames());

            return filterCompletions(options, args[0]);
        }

        if (args.length == 2) {
            String first = args[0].toLowerCase();

            switch (first) {
                case "criar", "create" -> {
                    completions.add("<nome>");
                }
                case "deletar", "delete", "info", "visualizar", "visualize" -> {
                    return filterCompletions(plugin.getRegionManager().getRegionNames(), args[1]);
                }
                case "prioridade", "priority" -> {
                    return filterCompletions(plugin.getRegionManager().getRegionNames(), args[1]);
                }
                default -> {
                    // Verificar se o primeiro arg é nome de região
                    Region region = plugin.getRegionManager().getRegion(first);
                    if (region != null) {
                        // Sugerir flags + prioridade
                        List<String> options = new ArrayList<>(plugin.getFlagRegistry().getFlagIds());
                        options.add("prioridade");
                        return filterCompletions(options, args[1]);
                    }
                }
            }
        }

        if (args.length >= 3) {
            String regionName = args[0];
            String flagId = args[1].toLowerCase();

            Region region = plugin.getRegionManager().getRegion(regionName);
            if (region != null) {
                // Delegar tab complete para a flag
                RegionFlag flag = plugin.getFlagRegistry().getFlag(flagId);
                if (flag != null) {
                    String[] flagArgs = Arrays.copyOfRange(args, 2, args.length);
                    List<String> flagCompletions = flag.tabComplete(sender, flagArgs);
                    if (flagCompletions != null) {
                        return filterCompletions(flagCompletions, args[args.length - 1]);
                    }
                }

                // Prioridade
                if (flagId.equals("prioridade") || flagId.equals("priority")) {
                    if (args.length == 3) {
                        completions.add("0");
                        completions.add("1");
                        completions.add("5");
                        completions.add("10");
                        return filterCompletions(completions, args[2]);
                    }
                }
            }

            // /regiao prioridade <região> <valor>
            if (args[0].equalsIgnoreCase("prioridade") || args[0].equalsIgnoreCase("priority")) {
                if (args.length == 3) {
                    completions.add("0");
                    completions.add("1");
                    completions.add("5");
                    completions.add("10");
                    return filterCompletions(completions, args[2]);
                }
            }
        }

        return filterCompletions(completions, args.length > 0 ? args[args.length - 1] : "");
    }

    /**
     * Filtra as sugestões com base no texto parcial digitado.
     */
    private List<String> filterCompletions(List<String> options, String partial) {
        if (partial == null || partial.isEmpty()) {
            return options;
        }
        String lower = partial.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
