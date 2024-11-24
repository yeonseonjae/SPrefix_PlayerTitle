package me.shark0822.sPrefix_PlayerTitle;

import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandListener implements CommandExecutor, TabCompleter {

    public LuckPerms luckPerms;
    private final Main plugin;
    private final ConfigManager configManager;
    private final SaveDataManager saveDataManager;
    private final InventoryManager inventoryManager;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final String prefix;

    public CommandListener(Main plugin) {
        this.plugin = plugin;
        this.luckPerms = plugin.luckPerms;
        this.configManager = plugin.configManager;
        this.saveDataManager = plugin.saveDataManager;
        this.inventoryManager = plugin.inventoryManager;

        prefix = colorize(translateHexColorCodes(configManager.getString("prefix") + " "));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equals("칭호")) {
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(prefix + ChatColor.WHITE + "이 명령어는 플레이어만 사용할 수 있습니다");
                return true;
            }
            openPrefixSetSelf(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("도움말")) {
            sender.sendMessage(prefix + ChatColor.YELLOW + "/칭호 : 자신의 칭호 설정 GUI를 엽니다");
            if (sender.isOp()) {
                sender.sendMessage(prefix + ChatColor.YELLOW + "/칭호 관리 : 관리자 GUI를 엽니다");
                sender.sendMessage(prefix + ChatColor.YELLOW + "/칭호 관리 발급 [칭호 이름] : 등록된 칭호를 아이템화 합니다");
                sender.sendMessage(prefix + ChatColor.YELLOW + "/칭호 관리 플레이어 [이름] : 플레이어의 칭호 관리 GUI를 엽니다");
                sender.sendMessage(prefix + ChatColor.YELLOW + "/칭호 관리 생성 [칭호 이름] : 칭호를 등록합니다");
                sender.sendMessage(prefix + ChatColor.YELLOW + "/칭호 관리 삭제 [칭호 이름] : 등록된 칭호를 삭제합니다");
                sender.sendMessage(prefix + ChatColor.YELLOW + "/칭호 리로드 : config.yml 및 save.yml을(를) 다시 불러옵니다");
            }

            return true;
        }

        if (sender.isOp()) {
            if (args[0].equalsIgnoreCase("관리")) {

                if (args.length == 1) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(prefix + ChatColor.WHITE + "이 명령어는 플레이어만 사용할 수 있습니다");
                        return true;
                    }
                    openManagementGui(player);
                    return true;
                }

                String subCommand = args[1].toLowerCase();

                switch (subCommand) {
                    case "발급" -> {
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage(prefix + ChatColor.WHITE + "이 명령어는 플레이어만 사용할 수 있습니다");
                            return true;
                        }

                        if (args.length < 3) {
                            sender.sendMessage(prefix + ChatColor.RED + "/칭호 관리 발급 [칭호 이름]");
                            return false;
                        }

                        List<String> titles = saveDataManager.getTitles();
                        String title = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                        if (!titles.contains(title)) {
                            sender.sendMessage(prefix + ChatColor.RED + "존재하지 않는 칭호입니다");
                            return true;
                        }

                        giveTitleBook(player, title);
                        return true;
                    }
                    case "생성" -> {
                        if (args.length < 3) {
                            sender.sendMessage(prefix + ChatColor.RED + "/칭호 관리 생성 [칭호 이름]");
                            return false;
                        }

                        List<String> titles = saveDataManager.getTitles();
                        String title = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                        if (titles.contains(title)) {
                            sender.sendMessage(prefix + ChatColor.RED + "이미 존재하는 칭호입니다");
                            return true;
                        }

                        if (titles.size() >= 45) {
                            sender.sendMessage(prefix + ChatColor.RED + "더 이상 칭호를 생성할 수 없습니다, 생성된 칭호 수가 45개 이상입니다");
                            return true;
                        }

                        saveDataManager.addTitle(title);
                        sender.sendMessage(prefix + ChatColor.WHITE + colorize(translateHexColorCodes("칭호 " + title + "을(를) 생성했습니다")));
                        return true;

                    }
                    case "삭제" -> {
                        if (args.length < 3) {
                            sender.sendMessage(prefix + ChatColor.RED + "/칭호 관리 삭제 [칭호 이름]");
                            return false;
                        }

                        List<String> titles = saveDataManager.getTitles();
                        String title = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                        if (!titles.contains(title)) {
                            sender.sendMessage(prefix + ChatColor.RED + "존재하지 않는 칭호입니다");
                            return true;
                        }

                        saveDataManager.removeTitle(title);
                        sender.sendMessage(prefix + ChatColor.WHITE + colorize(translateHexColorCodes("칭호 " + title + "을(를) 삭제했습니다")));
                        return true;

                    }
                    case "플레이어" -> {
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage(prefix + ChatColor.WHITE + "이 명령어는 플레이어만 사용할 수 있습니다");
                            return true;
                        }

                        if (args.length < 3) {
                            sender.sendMessage(prefix + ChatColor.RED + "/칭호 관리 플레이어 [플레이어 이름]");
                            return false;
                        }

                        String playerName = args[2];
                        Player targetPlayer = Bukkit.getPlayerExact(playerName);

                        if (targetPlayer == null) {
                            sender.sendMessage(prefix + ChatColor.RED + "해당 플레이어를 찾을 수 없습니다");
                            return true;
                        }

                        handlePrefixSetOther(player, playerName);
                        return true;
                    }
                    default -> {
                        sender.sendMessage(prefix + ChatColor.RED + "/칭호 관리 [플레이어 | 생성 | 삭제] <플레이어 이름 | 칭호 이름>");
                        return false;
                    }
                }
            }

            if (args[0].equalsIgnoreCase("리로드")) {
                reloadAll();
                sender.sendMessage(prefix + ChatColor.WHITE + "성공적으로 리로드 되었습니다");
                return true;
            }

            sender.sendMessage(prefix + ChatColor.RED + "잘못된 명령어입니다");
        } else {
            sender.sendMessage(prefix + ChatColor.RED + "이 명령어를 사용할 권한이 없습니다");
        }
        return false;
    }


    private void reloadAll() {
        configManager.reload();
        saveDataManager.reload();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (sender.isOp()) {
            if (args.length == 1) {
                suggestions.add("관리");
                suggestions.add("리로드");
                suggestions.add("도움말");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("관리")) {
                suggestions.add("발급");
                suggestions.add("플레이어");
                suggestions.add("생성");
                suggestions.add("삭제");
            } else if (args.length == 3 && args[1].equalsIgnoreCase("발급")) {
                suggestions.addAll(saveDataManager.getTitles());
            } else if (args.length == 3 && args[1].equalsIgnoreCase("플레이어")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            } else if (args.length == 3 && args[1].equalsIgnoreCase("삭제")) {
                suggestions.addAll(saveDataManager.getTitles());
            }
        } else {
            suggestions.add("도움말");
        }
        return suggestions;
    }

    public void giveTitleBook(Player player, String title) {

        ItemStack titleItem = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = titleItem.getItemMeta();

        if (meta != null) {
            meta.getPersistentDataContainer().set(plugin.key, PersistentDataType.STRING, title);
            title = colorize(translateHexColorCodes(ChatColor.WHITE + title));
            meta.setDisplayName(colorize(translateHexColorCodes(title)));
            meta.setLore(Collections.singletonList(colorize(translateHexColorCodes("&7&l| &7우클릭하여 칭호를 획득할 수 있습니다 "))));

            titleItem.setItemMeta(meta);
        }
        player.getInventory().addItem(titleItem);
        player.sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + " " + ChatColor.WHITE + "칭호북을 발급했습니다")));
    }

    public void openPrefixSetSelf(Player player) {
        Inventory GUI = Bukkit.createInventory(null, 54, "칭호 설정");

        List<String> ownedTitles = saveDataManager.getPlayerOwnedTitles(player.getUniqueId());

        for (String ownedTitle : ownedTitles) {

            ItemStack titleItem = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = titleItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RESET + colorize(ownedTitle));
                meta.getPersistentDataContainer().set(plugin.key, PersistentDataType.STRING, ownedTitle);
                meta.setLore(Collections.singletonList(colorize(translateHexColorCodes("&7&l| &7우클릭하여 칭호를 장착할 수 있습니다 "))));

                titleItem.setItemMeta(meta);
            }
            GUI.addItem(titleItem);
        }

        for (int i = 45; i < 54; i++) {
            if (i == 49) {
                ItemStack profile = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = profile.getItemMeta();

                if (meta != null) {
                    if (meta instanceof SkullMeta skullMeta) {
                        skullMeta.setOwnerProfile(player.getPlayerProfile());
                    }

                    meta.setDisplayName(colorize(translateHexColorCodes("&6칭호 정보")));
                    meta.setLore(Arrays.asList("", colorize(translateHexColorCodes("&7&l| &f닉네임 : &e" + player.getName())),
                            colorize(translateHexColorCodes("&7&l| &f장착중인 칭호 : " + saveDataManager.getPlayerEquippedTitle(player.getUniqueId()))), ""));

                    profile.setItemMeta(meta);
                }

                GUI.setItem(i, profile);
                continue;
            }
            ItemStack nullSlot = createFillItem();
            GUI.setItem(i, nullSlot);
        }

        player.openInventory(GUI);
        inventoryManager.setPrefixSetSelfInv(player.getUniqueId(), GUI);
    }

    private void handlePrefixSetOther(Player player, String targetName) {
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            player.sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + " ")) + ChatColor.RED + "해당 플레이어를 찾을 수 없습니다");
            return;
        }

        openPrefixSetOther(player, targetPlayer);
    }

    public void openPrefixSetOther(Player player, Player targetPlayer) {
        Inventory managementGUI = Bukkit.createInventory(null, 54,
                colorize(translateHexColorCodes("칭호 설정: " + targetPlayer.getName())));

        List<String> ownedTitles = saveDataManager.getPlayerOwnedTitles(targetPlayer.getUniqueId());

        for (String ownedTitle : ownedTitles) {
            ItemStack titleItem = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = titleItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RESET + colorize(ownedTitle));
                meta.getPersistentDataContainer().set(plugin.key, PersistentDataType.STRING, ownedTitle);

                meta.setLore(Arrays.asList("", colorize(translateHexColorCodes("&7&l| &9좌클릭&7하여 보유한 칭호 목록에서 삭제합니다 ")),
                        colorize(translateHexColorCodes("&7&l| &c우클릭&7하여 칭호를 장착합니다 ")), ""));
                titleItem.setItemMeta(meta);
            }
            managementGUI.addItem(titleItem);
        }

        for (int i = 45; i < 54; i++) {
            if (i == 49) {
                ItemStack profile = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta meta = profile.getItemMeta();

                if (meta != null) {
                    if (meta instanceof SkullMeta) {
                        ((SkullMeta) meta).setOwnerProfile(targetPlayer.getPlayerProfile());
                    }

                    meta.setDisplayName(colorize(translateHexColorCodes("&6칭호 정보")));
                    meta.setLore(Arrays.asList("", colorize(translateHexColorCodes("&7&l| &f닉네임 : &e" + targetPlayer.getName())),
                            colorize(translateHexColorCodes("&7&l| &f장착중인 칭호 : " + saveDataManager.getPlayerEquippedTitle(targetPlayer.getUniqueId()))), ""));

                    profile.setItemMeta(meta);
                }
                managementGUI.setItem(i, profile);
                continue;
            } else if (i == 53) {
                ItemStack back = new ItemStack(Material.BARRIER);
                ItemMeta meta = back.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(ChatColor.RED + "뒤로가기");
                    meta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, "뒤로가기");
                    back.setItemMeta(meta);
                }
                managementGUI.setItem(i, back);
                continue;
            }
            ItemStack nullSlot = createFillItem();
            managementGUI.setItem(i, nullSlot);
        }

        player.openInventory(managementGUI);

        inventoryManager.setPrefixSetOtherInv(player.getUniqueId(), managementGUI);
        inventoryManager.setManagedPlayer(managementGUI, targetPlayer.getUniqueId());
    }

    public void openManagementGui(Player player) {
        Inventory managementGUI = Bukkit.createInventory(null, 27,
                colorize(translateHexColorCodes("칭호")));
        managementGUI.setItem(10, createButton(Material.NAME_TAG,
                "&a발급",
                Collections.singletonList("&7&l| &7등록된 칭호를 아이템화 합니다 ")));

        managementGUI.setItem(12, createButton(Material.PLAYER_HEAD,
                "&a플레이어",
                Collections.singletonList("&7&l| &7플레이어의 칭호를 관리합니다 ")));

        managementGUI.setItem(14, createButton(Material.OAK_SIGN,
                "&a생성",
                Collections.singletonList("&7&l| &7새로운 칭호를 생성합니다 ")));

        managementGUI.setItem(16, createButton(Material.BARRIER,
                "&a삭제",
                Collections.singletonList("&7&l| &7등록된 칭호를 삭제합니다 ")));


        fillWithPlaceholder(managementGUI);

        player.openInventory(managementGUI);

        startProfileUpdate(managementGUI, player);

        inventoryManager.removeManagementInv(player.getUniqueId());
        inventoryManager.setManagementInv(player.getUniqueId(), managementGUI);
    }

    private void startProfileUpdate(Inventory managementGUI, Player player) {
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int playerIndex = 0;

            @Override
            public void run() {
                if (!player.getOpenInventory().getTopInventory().equals(managementGUI)) {
                    Bukkit.getScheduler().cancelTasks(plugin);
                    return;
                }

                Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

                if (onlinePlayers.length > 0) {
                    if (playerIndex >= onlinePlayers.length) {
                        playerIndex = 0;
                    }

                    Player targetPlayer = onlinePlayers[playerIndex];

                    ItemStack headItem = managementGUI.getItem(12);

                    if (headItem != null && headItem.getType() == Material.PLAYER_HEAD) {
                        SkullMeta skullMeta = (SkullMeta) headItem.getItemMeta();
                        if (skullMeta != null) {
                            skullMeta.setOwnerProfile(targetPlayer.getPlayerProfile());
                            headItem.setItemMeta(skullMeta);
                        }
                        managementGUI.setItem(12, headItem);
                    }

                    playerIndex = (playerIndex + 1) % onlinePlayers.length;
                }
            }
        }, 0L, 40L);
    }

    private ItemStack createButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorize(translateHexColorCodes(ChatColor.RESET + name)));
            if (lore != null) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(colorize(translateHexColorCodes(line)));
                }
                meta.setLore(coloredLore);
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFillItem() {
        ItemStack nullSlot = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = nullSlot.getItemMeta();
        if (meta != null) meta.setDisplayName(" ");
        nullSlot.setItemMeta(meta);

        return nullSlot;
    }

    private void fillWithPlaceholder(Inventory inventory) {
        ItemStack placeholder = createFillItem();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, placeholder);
            }
        }
    }

    public void openPlayerListGui(Player admin) {
        Inventory playerListGUI = Bukkit.createInventory(null, 54,
                colorize(translateHexColorCodes("플레이어 목록")));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setDisplayName(ChatColor.YELLOW + onlinePlayer.getName());
                skullMeta.setOwnerProfile(onlinePlayer.getPlayerProfile());
                skullMeta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, onlinePlayer.getName());
            }
            playerHead.setItemMeta(skullMeta);
            playerListGUI.addItem(playerHead);
        }

        for (int i = 45; i < 54; i++) {
            if (i == 53) {
                ItemStack back = new ItemStack(Material.BARRIER);
                ItemMeta meta = back.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(ChatColor.RED + "뒤로가기");
                    meta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, "뒤로가기");
                    back.setItemMeta(meta);
                }
                playerListGUI.setItem(i, back);
                continue;
            }
            ItemStack nullSlot = createFillItem();
            playerListGUI.setItem(i, nullSlot);
        }

        admin.openInventory(playerListGUI);
        inventoryManager.setManagementPlayerList(admin.getUniqueId(), playerListGUI);
    }

    public void openDeleteTitleGui(Player player) {
        List<String> titles = saveDataManager.getTitles();
        Inventory deleteGUI = Bukkit.createInventory(null, 54,
                colorize(translateHexColorCodes("삭제")));

        for (String title : titles) {
            ItemStack titleItem = createButton(Material.NAME_TAG, title, Collections.singletonList("&7&l| &9좌클릭&7하여 칭호를 삭제합니다 "));
            ItemMeta meta = titleItem.getItemMeta();
            meta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, title);
            titleItem.setItemMeta(meta);
            deleteGUI.addItem(titleItem);
        }

        for (int i = 45; i < 54; i++) {
            if (i == 53) {
                ItemStack back = new ItemStack(Material.BARRIER);
                ItemMeta meta = back.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(ChatColor.RED + "뒤로가기");
                    meta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, "뒤로가기");
                    back.setItemMeta(meta);
                }
                deleteGUI.setItem(i, back);
                continue;
            }
            ItemStack nullSlot = createFillItem();
            deleteGUI.setItem(i, nullSlot);
        }

        player.openInventory(deleteGUI);
        inventoryManager.setManagementDelete(player.getUniqueId(), deleteGUI);
    }

    public void openIssuedTitleGui(Player player) {
        List<String> titles = saveDataManager.getTitles();
        Inventory issuedGUI = Bukkit.createInventory(null, 54,
                colorize(translateHexColorCodes("발급")));

        for (String title : titles) {
            ItemStack titleItem = createButton(Material.NAME_TAG, title, Collections.singletonList("&7&l| &9좌클릭&7하여 칭호를 발급받습니다 "));
            ItemMeta meta = titleItem.getItemMeta();
            meta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, title);
            titleItem.setItemMeta(meta);
            issuedGUI.addItem(titleItem);
        }

        for (int i = 45; i < 54; i++) {
            if (i == 53) {
                ItemStack back = new ItemStack(Material.BARRIER);
                ItemMeta meta = back.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(ChatColor.RED + "뒤로가기");
                    meta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, "뒤로가기");
                    back.setItemMeta(meta);
                }
                issuedGUI.setItem(i, back);
                continue;
            }
            ItemStack nullSlot = createFillItem();
            issuedGUI.setItem(i, nullSlot);
        }

        player.openInventory(issuedGUI);
        inventoryManager.setManagementIssued(player.getUniqueId(), issuedGUI);
    }

    public void openDeleteConfirm(Player player, String title) {
        Inventory confirmGUI = Bukkit.createInventory(null, 9,
                colorize(translateHexColorCodes("삭제")));

        ItemStack btnConfirm = createButton(Material.LIME_WOOL, "&a삭제", List.of("&7클릭하여 확정"));
        ItemStack btnCancel = createButton(Material.RED_WOOL, "&c취소", List.of("&7클릭하여 취소"));

        ItemMeta confirmMeta = btnConfirm.getItemMeta();
        confirmMeta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, title);
        btnConfirm.setItemMeta(confirmMeta);

        ItemMeta cancelMeta = btnCancel.getItemMeta();
        cancelMeta.getPersistentDataContainer().set(plugin.btn, PersistentDataType.STRING, "취소");
        btnCancel.setItemMeta(cancelMeta);

        confirmGUI.setItem(2, btnConfirm);
        confirmGUI.setItem(6, btnCancel);

        fillWithPlaceholder(confirmGUI);

        player.openInventory(confirmGUI);
        inventoryManager.setManagementDeleteConfirm(player.getUniqueId(), confirmGUI);
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