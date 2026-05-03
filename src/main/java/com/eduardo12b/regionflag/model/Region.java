package com.eduardo12b.regionflag.model;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

/**
 * Representa uma região cuboidal definida por dois pontos (pos1 e pos2).
 * Armazena dados de flags e prioridade para resolução de overlaps.
 */
public class Region implements ConfigurationSerializable, Comparable<Region> {

    private final String name;
    private final String worldName;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private int priority;

    // Dados das flags: cada flag armazena seus dados em uma sub-seção
    private List<String> sounds = new ArrayList<>();
    private List<String> consoleCommands = new ArrayList<>();
    private String titleText = null;
    private String subtitleText = null;
    private int titleFadeIn = 10;
    private int titleStay = 70;
    private int titleFadeOut = 20;

    // Teleport
    private Double teleportX = null;
    private Double teleportY = null;
    private Double teleportZ = null;
    private float teleportYaw = 0f;
    private float teleportPitch = 0f;
    private String teleportWorld = null;

    // Cooldowns personalizados por flag (em segundos); -1 = usar padrão do config
    private int playsoundCooldown = -1;
    private int consoleCommandCooldown = -1;
    private int titleCooldown = -1;
    private int teleportCooldown = -1;

    public Region(String name, String worldName, int x1, int y1, int z1, int x2, int y2, int z2, int priority) {
        this.name = name;
        this.worldName = worldName;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        this.priority = priority;
    }

    /**
     * Verifica se uma localização está dentro desta região.
     */
    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null) return false;
        if (!location.getWorld().getName().equals(worldName)) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    /**
     * Verifica se esta região intercepta um chunk específico.
     */
    public boolean intersectsChunk(int chunkX, int chunkZ) {
        int chunkMinX = chunkX << 4;
        int chunkMaxX = chunkMinX + 15;
        int chunkMinZ = chunkZ << 4;
        int chunkMaxZ = chunkMinZ + 15;

        return maxX >= chunkMinX && minX <= chunkMaxX
                && maxZ >= chunkMinZ && minZ <= chunkMaxZ;
    }

    /**
     * Retorna todas as chunk keys que esta região ocupa.
     */
    public Set<Long> getChunkKeys() {
        Set<Long> keys = new HashSet<>();
        int startCX = minX >> 4;
        int endCX = maxX >> 4;
        int startCZ = minZ >> 4;
        int endCZ = maxZ >> 4;

        for (int cx = startCX; cx <= endCX; cx++) {
            for (int cz = startCZ; cz <= endCZ; cz++) {
                keys.add(chunkKey(cx, cz));
            }
        }
        return keys;
    }

    /**
     * Gera uma chave única para um chunk (combina X e Z em um long).
     */
    public static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    // ==================== Getters ====================

    public String getName() { return name; }
    public String getWorldName() { return worldName; }
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }
    public int getPriority() { return priority; }

    public List<String> getSounds() { return sounds; }
    public List<String> getConsoleCommands() { return consoleCommands; }
    public String getTitleText() { return titleText; }
    public String getSubtitleText() { return subtitleText; }
    public int getTitleFadeIn() { return titleFadeIn; }
    public int getTitleStay() { return titleStay; }
    public int getTitleFadeOut() { return titleFadeOut; }
    public int getPlaysoundCooldown() { return playsoundCooldown; }
    public int getConsoleCommandCooldown() { return consoleCommandCooldown; }
    public int getTitleCooldown() { return titleCooldown; }
    public int getTeleportCooldown() { return teleportCooldown; }

    public Double getTeleportX() { return teleportX; }
    public Double getTeleportY() { return teleportY; }
    public Double getTeleportZ() { return teleportZ; }
    public float getTeleportYaw() { return teleportYaw; }
    public float getTeleportPitch() { return teleportPitch; }
    public String getTeleportWorld() { return teleportWorld; }
    public boolean hasTeleport() { return teleportX != null; }

    // ==================== Setters ====================

    public void setPriority(int priority) { this.priority = priority; }
    public void setSounds(List<String> sounds) { this.sounds = new ArrayList<>(sounds); }
    public void setConsoleCommands(List<String> commands) { this.consoleCommands = new ArrayList<>(commands); }

    public void setTitle(String title, String subtitle) {
        this.titleText = title;
        this.subtitleText = subtitle;
    }

    public void setTitleTiming(int fadeIn, int stay, int fadeOut) {
        this.titleFadeIn = fadeIn;
        this.titleStay = stay;
        this.titleFadeOut = fadeOut;
    }

    public void setPlaysoundCooldown(int cooldown) { this.playsoundCooldown = cooldown; }
    public void setConsoleCommandCooldown(int cooldown) { this.consoleCommandCooldown = cooldown; }
    public void setTitleCooldown(int cooldown) { this.titleCooldown = cooldown; }
    public void setTeleportCooldown(int cooldown) { this.teleportCooldown = cooldown; }

    public void setTeleport(double x, double y, double z, float yaw, float pitch, String world) {
        this.teleportX = x;
        this.teleportY = y;
        this.teleportZ = z;
        this.teleportYaw = yaw;
        this.teleportPitch = pitch;
        this.teleportWorld = world;
    }

    public void clearTeleport() {
        this.teleportX = null;
        this.teleportY = null;
        this.teleportZ = null;
        this.teleportYaw = 0f;
        this.teleportPitch = 0f;
        this.teleportWorld = null;
    }

    public void addSound(String sound) {
        if (!sounds.contains(sound)) {
            sounds.add(sound);
        }
    }

    public void removeSound(String sound) {
        sounds.remove(sound);
    }

    public void addConsoleCommand(String command) {
        consoleCommands.add(command);
    }

    public void removeConsoleCommand(int index) {
        if (index >= 0 && index < consoleCommands.size()) {
            consoleCommands.remove(index);
        }
    }

    public void clearTitle() {
        this.titleText = null;
        this.subtitleText = null;
    }

    /**
     * Retorna lista de tipos de flag ativos nesta região.
     */
    public List<String> getActiveFlags() {
        List<String> flags = new ArrayList<>();
        if (!sounds.isEmpty()) flags.add("playsound");
        if (!consoleCommands.isEmpty()) flags.add("console_command");
        if (titleText != null) flags.add("title");
        if (teleportX != null) flags.add("teleport");
        return flags;
    }

    // ==================== Serialization ====================

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", worldName);
        map.put("priority", priority);

        Map<String, Object> pos1 = new LinkedHashMap<>();
        pos1.put("x", minX);
        pos1.put("y", minY);
        pos1.put("z", minZ);
        map.put("pos1", pos1);

        Map<String, Object> pos2 = new LinkedHashMap<>();
        pos2.put("x", maxX);
        pos2.put("y", maxY);
        pos2.put("z", maxZ);
        map.put("pos2", pos2);

        // Flags
        Map<String, Object> flags = new LinkedHashMap<>();

        if (!sounds.isEmpty()) {
            Map<String, Object> soundData = new LinkedHashMap<>();
            soundData.put("sounds", new ArrayList<>(sounds));
            if (playsoundCooldown >= 0) soundData.put("cooldown", playsoundCooldown);
            flags.put("playsound", soundData);
        }

        if (!consoleCommands.isEmpty()) {
            Map<String, Object> cmdData = new LinkedHashMap<>();
            cmdData.put("commands", new ArrayList<>(consoleCommands));
            if (consoleCommandCooldown >= 0) cmdData.put("cooldown", consoleCommandCooldown);
            flags.put("console_command", cmdData);
        }

        if (titleText != null) {
            Map<String, Object> titleData = new LinkedHashMap<>();
            titleData.put("title", titleText);
            if (subtitleText != null) titleData.put("subtitle", subtitleText);
            titleData.put("fadeIn", titleFadeIn);
            titleData.put("stay", titleStay);
            titleData.put("fadeOut", titleFadeOut);
            if (titleCooldown >= 0) titleData.put("cooldown", titleCooldown);
            flags.put("title", titleData);
        }

        if (teleportX != null) {
            Map<String, Object> tpData = new LinkedHashMap<>();
            tpData.put("x", teleportX);
            tpData.put("y", teleportY);
            tpData.put("z", teleportZ);
            tpData.put("yaw", teleportYaw);
            tpData.put("pitch", teleportPitch);
            tpData.put("world", teleportWorld);
            if (teleportCooldown >= 0) tpData.put("cooldown", teleportCooldown);
            flags.put("teleport", tpData);
        }

        if (!flags.isEmpty()) {
            map.put("flags", flags);
        }

        return map;
    }

    /**
     * Desserializa uma região a partir de um mapa de configuração.
     */
    @SuppressWarnings("unchecked")
    public static Region deserialize(String name, Map<String, Object> map) {
        String world = (String) map.get("world");
        int priority = map.containsKey("priority") ? ((Number) map.get("priority")).intValue() : 0;

        Map<String, Object> pos1 = (Map<String, Object>) map.get("pos1");
        Map<String, Object> pos2 = (Map<String, Object>) map.get("pos2");

        int x1 = ((Number) pos1.get("x")).intValue();
        int y1 = ((Number) pos1.get("y")).intValue();
        int z1 = ((Number) pos1.get("z")).intValue();
        int x2 = ((Number) pos2.get("x")).intValue();
        int y2 = ((Number) pos2.get("y")).intValue();
        int z2 = ((Number) pos2.get("z")).intValue();

        Region region = new Region(name, world, x1, y1, z1, x2, y2, z2, priority);

        // Carregar flags
        if (map.containsKey("flags")) {
            Map<String, Object> flags = (Map<String, Object>) map.get("flags");

            if (flags.containsKey("playsound")) {
                Map<String, Object> soundData = (Map<String, Object>) flags.get("playsound");
                if (soundData.containsKey("sounds")) {
                    region.sounds = new ArrayList<>((List<String>) soundData.get("sounds"));
                }
                if (soundData.containsKey("cooldown")) {
                    region.playsoundCooldown = ((Number) soundData.get("cooldown")).intValue();
                }
            }

            if (flags.containsKey("console_command")) {
                Map<String, Object> cmdData = (Map<String, Object>) flags.get("console_command");
                if (cmdData.containsKey("commands")) {
                    region.consoleCommands = new ArrayList<>((List<String>) cmdData.get("commands"));
                }
                if (cmdData.containsKey("cooldown")) {
                    region.consoleCommandCooldown = ((Number) cmdData.get("cooldown")).intValue();
                }
            }

            if (flags.containsKey("title")) {
                Map<String, Object> titleData = (Map<String, Object>) flags.get("title");
                region.titleText = (String) titleData.get("title");
                region.subtitleText = titleData.containsKey("subtitle") ? (String) titleData.get("subtitle") : null;
                region.titleFadeIn = titleData.containsKey("fadeIn") ? ((Number) titleData.get("fadeIn")).intValue() : 10;
                region.titleStay = titleData.containsKey("stay") ? ((Number) titleData.get("stay")).intValue() : 70;
                region.titleFadeOut = titleData.containsKey("fadeOut") ? ((Number) titleData.get("fadeOut")).intValue() : 20;
                if (titleData.containsKey("cooldown")) {
                    region.titleCooldown = ((Number) titleData.get("cooldown")).intValue();
                }
            }

            if (flags.containsKey("teleport")) {
                Map<String, Object> tpData = (Map<String, Object>) flags.get("teleport");
                region.teleportX = ((Number) tpData.get("x")).doubleValue();
                region.teleportY = ((Number) tpData.get("y")).doubleValue();
                region.teleportZ = ((Number) tpData.get("z")).doubleValue();
                region.teleportYaw = tpData.containsKey("yaw") ? ((Number) tpData.get("yaw")).floatValue() : 0f;
                region.teleportPitch = tpData.containsKey("pitch") ? ((Number) tpData.get("pitch")).floatValue() : 0f;
                region.teleportWorld = tpData.containsKey("world") ? (String) tpData.get("world") : region.worldName;
                if (tpData.containsKey("cooldown")) {
                    region.teleportCooldown = ((Number) tpData.get("cooldown")).intValue();
                }
            }
        }

        return region;
    }

    @Override
    public int compareTo(Region other) {
        // Maior prioridade primeiro
        return Integer.compare(other.priority, this.priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return name.equalsIgnoreCase(region.name) && worldName.equals(region.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), worldName);
    }

    @Override
    public String toString() {
        return "Region{name='" + name + "', world='" + worldName + "', priority=" + priority + "}";
    }
}
