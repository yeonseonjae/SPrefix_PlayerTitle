package me.shark0822.sPrefix_PlayerTitle;

import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin {

    public LuckPerms luckPerms;
    public ConfigManager configManager;
    public SaveDataManager saveDataManager;
    public CommandListener commandListener;
    public TitleEventListener titleEventListener;
    public InventoryManager inventoryManager;

    public NamespacedKey key = new NamespacedKey(this, "title");
    public NamespacedKey btn = new NamespacedKey(this, "button");
    public boolean enableLP = false;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    @Override
    public void onEnable() {
        this.luckPerms = Bukkit.getServicesManager().load(LuckPerms.class);

        configManager = new ConfigManager(this);
        saveDataManager = new SaveDataManager(this);
        inventoryManager = new InventoryManager();
        commandListener = new CommandListener(this);
        titleEventListener = new TitleEventListener(this);

        if (luckPerms != null) {
            Bukkit.getConsoleSender().sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + ChatColor.GREEN + " " + "LuckPerms을(를) 찾아 성공적으로 연결했습니다.")));
            enableLP = true;
        }

        getServer().getPluginManager().registerEvents(titleEventListener, this);

        getCommand("칭호").setExecutor(commandListener);
    }

    @Override
    public void onDisable() {

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
