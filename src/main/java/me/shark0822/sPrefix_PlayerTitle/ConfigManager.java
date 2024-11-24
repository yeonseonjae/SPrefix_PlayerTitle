package me.shark0822.sPrefix_PlayerTitle;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {

    private Main plugin;
    private File configFile;
    private YamlConfiguration config;
    private final String prefix;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);
        prefix = colorize(translateHexColorCodes(getString("prefix") + " "));

        setDefaultValues();
    }

    private void setDefaultValues() {
        // 기본값 설정을 키-값 형태로 정의
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("prefix", "&8[ &#4b4ab2칭호 &8]");
        defaults.put("gui.ui_title.prefix_setting_self", "칭호 설정");
        defaults.put("gui.ui_title.prefix_setting_other", "칭호 설정: {player}");
        defaults.put("gui.has_prefix_item.type", "NAME_TAG");
        defaults.put("gui.has_prefix_item.name", "{prefix}");
        defaults.put("gui.has_prefix_item.lores", Arrays.asList("", "&8▐ &c우클릭&f하여 칭호를 장착할 수 있습니다 ", ""));
        defaults.put("gui.has_prefix_item.custom_model_data", 0);
        defaults.put("gui.profile_item.type", "PLAYER_HEAD");
        defaults.put("gui.profile_item.name", "&6칭호 정보");
        defaults.put("gui.profile_item.lores", Arrays.asList("", "&8▐ &f닉네임 : &e{name}", "&8▐ &f장착중인 칭호 : {prefix}", ""));
        defaults.put("gui.profile_item.custom_model_data", 0);
        defaults.put("item.type", "ENCHANTED_BOOK");
        defaults.put("item.name", "&f[ &#4b4ab2칭호 &f] {title}");
        defaults.put("item.lores", Arrays.asList("", "&8▐ &f우클릭으로 칭호를 등록할 수 있습니다.", ""));
        defaults.put("item.custom_model_data", 0);
        defaults.put("chat_format", "{prefix}{displayname}: {message}");

        // 설정에 키가 없으면 기본값 추가
        defaults.forEach((key, value) -> {
            if (!config.contains(key)) {
                config.set(key, value);
            }
        });

        save(); // 변경 사항 저장
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    // 변경 사항 저장
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "설정 파일 저장 중 오류가 발생했습니다 : " + e.getMessage());
        }
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
        setDefaultValues();
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
