package com.eduardo12b.regionflag.manager;

import com.eduardo12b.regionflag.RegionFlagPlugin;
import com.eduardo12b.regionflag.model.Region;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Gerencia o CRUD de regiões e persistência em YAML.
 * As regiões são salvas em arquivos separados por mundo: regions/<world>.yml
 */
public class RegionManager {

    private final RegionFlagPlugin plugin;
    private final RegionCache cache;
    private final File regionsFolder;

    public RegionManager(RegionFlagPlugin plugin) {
        this.plugin = plugin;
        this.cache = new RegionCache();
        this.regionsFolder = new File(plugin.getDataFolder(), "regions");
        if (!regionsFolder.exists()) {
            regionsFolder.mkdirs();
        }
    }

    /**
     * Carrega todas as regiões de todos os arquivos YAML.
     */
    public void loadAllRegions() {
        cache.clear();
        int total = 0;

        File[] files = regionsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            plugin.getMessageUtil().log("Nenhum arquivo de região encontrado.");
            return;
        }

        for (File file : files) {
            try {
                int count = loadRegionsFromFile(file);
                total += count;
            } catch (Exception e) {
                plugin.getMessageUtil().error("Erro ao carregar regiões de " + file.getName(), e);
            }
        }

        plugin.getMessageUtil().log("Carregadas " + total + " regiões de " + files.length + " mundo(s).");
    }

    /**
     * Carrega regiões de um arquivo YAML específico.
     */
    @SuppressWarnings("unchecked")
    private int loadRegionsFromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection regionsSection = config.getConfigurationSection("regions");
        if (regionsSection == null) return 0;

        int count = 0;
        for (String name : regionsSection.getKeys(false)) {
            try {
                ConfigurationSection regionSection = regionsSection.getConfigurationSection(name);
                if (regionSection == null) continue;

                Map<String, Object> data = regionSection.getValues(true);
                // Converter sub-seções para mapas
                data = flattenSection(regionSection);

                Region region = Region.deserialize(name, data);
                cache.addRegion(region);
                count++;

                plugin.getMessageUtil().debug("Região carregada: " + name + " (" + region.getWorldName() + ")");
            } catch (Exception e) {
                plugin.getMessageUtil().error("Erro ao carregar região '" + name + "' de " + file.getName(), e);
            }
        }

        return count;
    }

    /**
     * Converte uma ConfigurationSection para um Map recursivo.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> flattenSection(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection sub) {
                result.put(key, flattenSection(sub));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Salva todas as regiões de um mundo no arquivo YAML correspondente.
     */
    public void saveRegions(String worldName) {
        File file = new File(regionsFolder, worldName + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        List<Region> regions = cache.getRegionsByWorld(worldName);
        for (Region region : regions) {
            Map<String, Object> data = region.serialize();
            String path = "regions." + region.getName();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                config.set(path + "." + entry.getKey(), entry.getValue());
            }
        }

        try {
            config.save(file);
            plugin.getMessageUtil().debug("Regiões do mundo '" + worldName + "' salvas (" + regions.size() + " regiões).");
        } catch (IOException e) {
            plugin.getMessageUtil().error("Erro ao salvar regiões do mundo '" + worldName + "'", e);
        }
    }

    /**
     * Salva todas as regiões de todos os mundos.
     */
    public void saveAllRegions() {
        Set<String> worlds = new HashSet<>();
        for (Region region : cache.getAllRegions()) {
            worlds.add(region.getWorldName());
        }
        for (String world : worlds) {
            saveRegions(world);
        }
    }

    /**
     * Cria uma nova região.
     */
    public boolean createRegion(String name, String worldName,
                                int x1, int y1, int z1,
                                int x2, int y2, int z2, int priority) {
        if (cache.hasRegion(name)) {
            return false;
        }

        Region region = new Region(name, worldName, x1, y1, z1, x2, y2, z2, priority);
        cache.addRegion(region);
        saveRegions(worldName);

        plugin.getMessageUtil().log("Região criada: " + name + " em " + worldName);
        return true;
    }

    /**
     * Deleta uma região.
     */
    public boolean deleteRegion(String name) {
        Region region = cache.getRegionByName(name);
        if (region == null) return false;

        String worldName = region.getWorldName();
        cache.removeRegion(region);
        saveRegions(worldName);

        plugin.getMessageUtil().log("Região deletada: " + name);
        return true;
    }

    /**
     * Obtém uma região pelo nome.
     */
    public Region getRegion(String name) {
        return cache.getRegionByName(name);
    }

    /**
     * Obtém todas as regiões em uma posição (ordenadas por prioridade).
     */
    public List<Region> getRegionsAt(String worldName, int blockX, int blockY, int blockZ) {
        return cache.getRegionsAt(worldName, blockX, blockY, blockZ);
    }

    /**
     * Obtém todas as regiões de um mundo.
     */
    public List<Region> getRegionsByWorld(String worldName) {
        return cache.getRegionsByWorld(worldName);
    }

    /**
     * Obtém todas as regiões.
     */
    public Collection<Region> getAllRegions() {
        return cache.getAllRegions();
    }

    /**
     * Retorna os nomes de todas as regiões.
     */
    public List<String> getRegionNames() {
        List<String> names = new ArrayList<>();
        for (Region region : cache.getAllRegions()) {
            names.add(region.getName());
        }
        return names;
    }

    /**
     * Recarrega todas as regiões dos arquivos YAML.
     */
    public void reload() {
        loadAllRegions();
    }

    /**
     * Faz backup dos arquivos de região.
     */
    public void backup() {
        File backupFolder = new File(plugin.getDataFolder(), "backups");
        if (!backupFolder.exists()) backupFolder.mkdirs();

        String timestamp = String.valueOf(System.currentTimeMillis());
        File backupDir = new File(backupFolder, timestamp);
        backupDir.mkdirs();

        File[] files = regionsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                java.nio.file.Files.copy(file.toPath(),
                        new File(backupDir, file.getName()).toPath());
            } catch (IOException e) {
                plugin.getMessageUtil().error("Erro ao fazer backup de " + file.getName(), e);
            }
        }

        plugin.getMessageUtil().debug("Backup criado em: " + backupDir.getPath());
    }

    public RegionCache getCache() { return cache; }
}
