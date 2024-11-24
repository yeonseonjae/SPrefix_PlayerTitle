package me.shark0822.sPrefix_PlayerTitle;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveDataManager {

    public LuckPerms luckPerms;
    private Main plugin;
    private final ConfigManager configManager;
    private File saveFile;
    private YamlConfiguration saveConfig;
    private final String prefix;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public SaveDataManager(Main plugin) {
        this.plugin = plugin;
        this.luckPerms = plugin.luckPerms;
        this.configManager = plugin.configManager;
        this.saveFile = new File(plugin.getDataFolder(), "save.yml");
        prefix = colorize(translateHexColorCodes(configManager.getString("prefix") + " "));

        if (!saveFile.exists()) {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.luckPerms == null) {
            return;
        }

        this.saveConfig = YamlConfiguration.loadConfiguration(saveFile);
    }

    public List<String> getTitles() {
        return saveConfig.getStringList("titles");
    }

    public void addTitle(String title) {
        List<String> titles = getTitles();
        if (!titles.contains(title)) {
            titles.add(title);
            saveConfig.set("titles", titles);
            save();
        }
    }

    public void setPlayerOwnedTitles(UUID playerUUID, List<String> titles) {
        saveConfig.set("players_title." + playerUUID + ".owned_titles", titles);
        save();
    }

    public List<String> getPlayerOwnedTitles(UUID playerUUID) {
        return saveConfig.getStringList("players_title." + playerUUID + ".owned_titles");
    }

    public void addPlayerOwnedTitle(UUID playerUUID, String title) {
        List<String> ownedTitles = saveConfig.getStringList("players_title." + playerUUID + ".owned_titles");

        if (!getTitles().contains(title)) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "칭호 " + title + "가 칭호 목록에 없으므로 플레이어 " + playerUUID + "에게 저장되지 않습니다");
            return;
        }

        if (ownedTitles.contains(title)) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "플레이어 " + playerUUID + "는 이미 칭호 " + title + "를 보유하고 있습니다");
            return;
        }

        ownedTitles.add(title);
        saveConfig.set("players_title." + playerUUID + ".owned_titles", ownedTitles);
        save();
    }

    public void setPlayerEquippedTitle(UUID playerUUID, String title) {
        if (!getTitles().contains(title)) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "칭호 " + title + "가 칭호 목록에 없으므로 플레이어 " + playerUUID + "의 장착된 칭호로 설정되지 않았습니다");
            return;
        }

        saveConfig.set("players_title." + playerUUID + ".equipped_title", title);

        if (plugin.enableLP) {
            User user = luckPerms.getUserManager().getUser(playerUUID);
            if (user != null) {
                user.data().clear(Node.builder("prefix.100.").build().getContexts());
                Node prefixNode = Node.builder("prefix.100." + title).build();
                user.data().add(prefixNode);
                luckPerms.getUserManager().saveUser(user);
            }
        }
        save();
    }

    public String getPlayerEquippedTitle(UUID playerUUID) {
        return saveConfig.getString("players_title." + playerUUID + ".equipped_title", "");
    }

    public void removePlayerTitle(UUID playerUUID, String title) {
        List<String> ownedTitles = getPlayerOwnedTitles(playerUUID);

        if (ownedTitles.contains(title)) {
            ownedTitles.remove(title);
            saveConfig.set("players_title." + playerUUID + ".equipped_title", null);
            saveConfig.set("players_title." + playerUUID + ".owned_titles", ownedTitles);
            save();

            if (plugin.enableLP) {
                User user = luckPerms.getUserManager().getUser(playerUUID);
                if (user != null) {
                    user.data().clear(Node.builder("prefix.100.").build().getContexts());
                    luckPerms.getUserManager().saveUser(user);
                }
            }
        }
    }

    public void removeTitle(String title) {
        List<String> titles = getTitles();

        if (titles.contains(title)) {
            titles.remove(title);
            saveConfig.set("titles", titles);

            for (UUID playerUUID : getAllPlayers()) {
                List<String> ownedTitles = getPlayerOwnedTitles(playerUUID);
                if (ownedTitles.contains(title)) {
                    ownedTitles.remove(title);
                    saveConfig.set("players_title." + playerUUID + ".owned_titles", ownedTitles);
                }

                if (getPlayerEquippedTitle(playerUUID).equals(title)) {
                    saveConfig.set("players_title." + playerUUID + ".equipped_title", null);

                    if (plugin.enableLP) {
                        User user = luckPerms.getUserManager().getUser(playerUUID);
                        if (user != null) {
                            user.data().clear(Node.builder("prefix.100.").build().getContexts());
                            luckPerms.getUserManager().saveUser(user);
                        }
                    }
                }
            }
            save();
        }
    }

    public List<UUID> getAllPlayers() {
        List<UUID> players = new ArrayList<>();
        if (saveConfig.contains("players_title")) {
            for (String playerUUIDStr : saveConfig.getConfigurationSection("players_title").getKeys(false)) {
                try {
                    players.add(UUID.fromString(playerUUIDStr));
                } catch (IllegalArgumentException e) {
                    Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.YELLOW + "잘못된 UUID 형식: " + playerUUIDStr);
                }
            }
        }
        return players;
    }


    public void save() {
        try {
            saveConfig.save(saveFile);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "세이브 파일 저장 중 오류가 발생했습니다 : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void reload() {
        saveConfig = YamlConfiguration.loadConfiguration(saveFile);

        List<String> validTitles = getTitles();

        for (UUID playerUUID : getAllPlayers()) {
            List<String> ownedTitles = getPlayerOwnedTitles(playerUUID);
            List<String> removedTitles = new ArrayList<>();

            ownedTitles.removeIf(title -> {
                if (!validTitles.contains(title)) {
                    removedTitles.add(title);
                    return true;
                }
                return false;
            });

            setPlayerOwnedTitles(playerUUID, ownedTitles);

            for (String removedTitle : removedTitles) {
                String playerName = (Bukkit.getPlayer(playerUUID) != null) ? Bukkit.getPlayer(playerUUID).getName() : "알 수 없는 플레이어";
                Bukkit.getConsoleSender().sendMessage(prefix + colorize(translateHexColorCodes(ChatColor.YELLOW + "플레이어 " + playerName + "이(가) 보유한 칭호 " + ChatColor.RESET + removedTitle + ChatColor.YELLOW + "이(가) 칭호 목록에 존재하지 않아 삭제 되었습니다")));
            }

            String equippedTitle = getPlayerEquippedTitle(playerUUID);
            if (equippedTitle != null && !equippedTitle.isEmpty() && !validTitles.contains(equippedTitle)) {
                String playerName = (Bukkit.getPlayer(playerUUID) != null) ? Bukkit.getPlayer(playerUUID).getName() : "알 수 없는 플레이어";
                Bukkit.getConsoleSender().sendMessage(prefix + colorize(translateHexColorCodes(ChatColor.YELLOW + "플레이어 " + playerName + "이(가) 장착중인 칭호 " + ChatColor.RESET + equippedTitle + ChatColor.YELLOW + "이(가) 칭호 목록에 존재하지 않아 삭제 되었습니다")));
                saveConfig.set("players_title." + playerUUID + ".equipped_title", null);
            }
        }
        save();
    }

    private String colorize(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String translateHexColorCodes(final String message) {
        final char colorChar = ChatColor.COLOR_CHAR;

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

        while (matcher.find()) {
            final String group = matcher.group(1);

            matcher.appendReplacement(buffer, colorChar + "x"
                    + colorChar + group.charAt(0) + colorChar + group.charAt(1)
                    + colorChar + group.charAt(2) + colorChar + group.charAt(3)
                    + colorChar + group.charAt(4) + colorChar + group.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }
}
