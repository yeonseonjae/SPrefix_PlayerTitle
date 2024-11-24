package me.shark0822.sPrefix_PlayerTitle;

import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleEventListener implements Listener {

    public LuckPerms luckPerms;
    private final Main plugin;
    private final ConfigManager configManager;
    private final SaveDataManager saveDataManager;
    private final CommandListener commandListener;
    private final InventoryManager inventoryManager;

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private final String prefix;
    private Map<UUID, Boolean> createChat = new HashMap<>();

    public TitleEventListener(Main plugin) {
        this.plugin = plugin;
        this.luckPerms = plugin.luckPerms;
        this.configManager = plugin.configManager;
        this.saveDataManager = plugin.saveDataManager;
        this.commandListener = plugin.commandListener;
        this.inventoryManager = plugin.inventoryManager;
        prefix = colorize(translateHexColorCodes(configManager.getString("prefix") + " "));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (configManager.getBoolean("enable_chat_formatting", true)) {
            String playerTitle = saveDataManager.getPlayerEquippedTitle(player.getUniqueId());
            if (playerTitle == null) {
                playerTitle = "";
            }

            String format = configManager.getString("chat_format")
                    .replace("{prefix}", playerTitle)
                    .replace("{name}", player.getName())
                    .replace("{displayname}", player.getDisplayName())
                    .replace("{message}", message);

            format = colorize(translateHexColorCodes(format));
            event.setFormat(format.replace("{message}", message));
        }

        boolean isCreatingChat = createChat.getOrDefault(player.getUniqueId(), false);
        if (!isCreatingChat) {
            return;
        }

        if (message.startsWith("/")) {
            return;
        }

        event.setCancelled(true);

        List<String> titles = saveDataManager.getTitles();

        if (titles.contains(message)) {
            player.sendMessage(prefix + ChatColor.RED + "이미 존재하는 칭호입니다");
            return;
        }

        if (titles.size() >= 45) {
            player.sendMessage(prefix + ChatColor.RED + "더 이상 칭호를 생성할 수 없습니다. 생성된 칭호 수가 45개 이상입니다");
            Bukkit.getScheduler().runTask(plugin, () -> {
                commandListener.openManagementGui(player);
            });
            createChat.put(player.getUniqueId(), false);
            return;
        }

        if (message.equalsIgnoreCase("취소")) {
            player.sendMessage(prefix + ChatColor.WHITE + "칭호 생성을 취소했습니다");
            Bukkit.getScheduler().runTask(plugin, () -> {
                commandListener.openManagementGui(player);
            });
            createChat.put(player.getUniqueId(), false);
            return;
        }

        saveDataManager.addTitle(message);
        player.sendMessage(prefix + ChatColor.WHITE + colorize(translateHexColorCodes("칭호 " + message + "을(를) 생성했습니다")));

        Bukkit.getScheduler().runTask(plugin, () -> {
            commandListener.openManagementGui(player);
        });
        createChat.put(player.getUniqueId(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTitleBookUse(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT")) return;

        if (event.getItem() == null) return;

        if (event.getItem().getItemMeta() == null) return;

        if (!event.getItem().getItemMeta().getPersistentDataContainer().has(plugin.key, PersistentDataType.STRING))
            return;

        Player player = event.getPlayer();
        ItemMeta meta = event.getItem().getItemMeta();

        String title = meta.getPersistentDataContainer().get(plugin.key, PersistentDataType.STRING);

        if (!saveDataManager.getTitles().contains(title)) {
            player.sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + ChatColor.RED + " " + "존재하지 않는 칭호입니다")));
            return;
        }

        List<String> ownedTitles = saveDataManager.getPlayerOwnedTitles(player.getUniqueId());
        if (ownedTitles.contains(title)) {
            player.sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + ChatColor.YELLOW + " " + "이미 이 칭호를 가지고 있습니다")));
            return;
        }

        saveDataManager.addPlayerOwnedTitle(player.getUniqueId(), title);
        player.sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + ChatColor.WHITE + " " + "칭호 " + title + "이(가) 등록되었습니다")));
        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        if (inventoryManager.hasPrefixSetSelfInv(player.getUniqueId())) {
            inventoryManager.removePrefixSetSelfInv(player.getUniqueId());
        }

        if (inventoryManager.hasPrefixSetOtherInv(player.getUniqueId())) {
            inventoryManager.removePrefixSetOtherInv(player.getUniqueId());

            if (inventoryManager.getPrefixSetOtherInv(player.getUniqueId()) != null) {
                inventoryManager.removeManagedPlayer(inventoryManager.getPrefixSetOtherInv(player.getUniqueId()));
            }
        }

        if (inventoryManager.hasManagementInv(player.getUniqueId())) {
            inventoryManager.removeManagementInv(player.getUniqueId());
        }

        if (inventoryManager.hasManagementPlayerList(player.getUniqueId())) {
            inventoryManager.removeManagementPlayerList(player.getUniqueId());
        }

        if (inventoryManager.hasManagementDelete(player.getUniqueId())) {
            inventoryManager.removeManagementDelete(player.getUniqueId());
        }

        if (inventoryManager.hasManagementDeleteConfirm(player.getUniqueId())) {
            inventoryManager.removeManagementDeleteConfirm(player.getUniqueId());
        }

        if (inventoryManager.hasManagementIssued(player.getUniqueId())) {
            inventoryManager.removeManagementIssued(player.getUniqueId());
        }
    }

    @EventHandler
    public void onTitleInvClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (inventoryManager.containAnyInv(player.getUniqueId())) {
            event.setCancelled(true);
        }

        if (inventoryManager.hasPrefixSetSelfInv(player.getUniqueId())) {

            if (event.getInventory() != inventoryManager.getPrefixSetSelfInv(player.getUniqueId())) return;

            if (event.isRightClick()) {
                ItemStack title = event.getCurrentItem();
                if (title == null || title.getItemMeta() == null) return;
                if (!title.getType().equals(Material.NAME_TAG)) return;

                ItemMeta meta = title.getItemMeta();
                if (meta == null) return;

                player.sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + ChatColor.WHITE + " " + "칭호 " + meta.getDisplayName() + "을(를) 장착하였습니다")));
                saveDataManager.setPlayerEquippedTitle(player.getUniqueId(), meta.getPersistentDataContainer().get(plugin.key, PersistentDataType.STRING));
                player.closeInventory();
            }
            commandListener.openPrefixSetSelf(player);
        } else if (inventoryManager.hasPrefixSetOtherInv(player.getUniqueId()) && inventoryManager.hasManagedPlayer(inventoryManager.getPrefixSetOtherInv(player.getUniqueId()))) {

            if (event.getInventory() != inventoryManager.getPrefixSetOtherInv(player.getUniqueId())) return;

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.btn, PersistentDataType.STRING)) {
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING) != null) {
                    if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING).equals("뒤로가기")) {
                        commandListener.openPlayerListGui(player);
                    }
                }
            }

            if (event.isLeftClick() || event.isRightClick()) {
                ItemStack title = event.getCurrentItem();
                if (title == null || title.getItemMeta() == null) return;
                if (!title.getType().equals(Material.NAME_TAG))
                    return;

                ItemMeta meta = title.getItemMeta();
                if (meta == null) return;

                UUID managedPlayerUUID = inventoryManager.getManagedPlayer(inventoryManager.getPrefixSetOtherInv(player.getUniqueId()));
                Player targetPlayer = Bukkit.getPlayer(managedPlayerUUID);
                if (event.isLeftClick()) {
                    player.sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + ChatColor.WHITE + " " + "플레이어 "
                            + targetPlayer.getName() + "에게서 칭호 " + meta.getDisplayName() + "을(를) 삭제하였습니다")));
                    saveDataManager.removePlayerTitle(managedPlayerUUID, meta.getPersistentDataContainer().get(plugin.key, PersistentDataType.STRING));
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    player.sendMessage(colorize(translateHexColorCodes(configManager.getString("prefix") + ChatColor.WHITE + " " + "플레이어 "
                            + targetPlayer.getName() + "에게 칭호 " + meta.getDisplayName() + "을(를) 착용시켰습니다")));
                    saveDataManager.setPlayerEquippedTitle(managedPlayerUUID, meta.getPersistentDataContainer().get(plugin.key, PersistentDataType.STRING));
                    player.closeInventory();
                }

                commandListener.openPrefixSetOther(player, Bukkit.getPlayer(managedPlayerUUID));
            }
        } else if (inventoryManager.hasManagementInv(player.getUniqueId())) {

            if (event.getInventory() != inventoryManager.getManagementInv(player.getUniqueId())) return;

            if (event.getSlot() == 10) {
                commandListener.openIssuedTitleGui(player);
            } else if (event.getSlot() == 12) {
                commandListener.openPlayerListGui(player);
            } else if (event.getSlot() == 14) {
                List<String> titles = saveDataManager.getTitles();
                if (titles.size() >= 45) {
                    player.sendMessage(prefix + ChatColor.RED + "더 이상 칭호를 생성할 수 없습니다. 생성된 칭호 수가 45개 이상입니다");
                    return;
                }

                player.closeInventory();
                createChat.put(player.getUniqueId(), true);
                player.sendMessage(prefix + ChatColor.WHITE + "채팅으로 생성할 칭호를 입력하세요 (뒤로가기 : '취소' 입력");
            } else if (event.getSlot() == 16) {
                commandListener.openDeleteTitleGui(player);
            }

        } else if (inventoryManager.hasManagementPlayerList(player.getUniqueId())) {

            if (event.getInventory() != inventoryManager.getManagementPlayerList(player.getUniqueId())) return;

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.btn, PersistentDataType.STRING)) {
                    if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING) != null) {
                        String targetPlayerName = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING);
                        commandListener.openPrefixSetOther(player, Bukkit.getPlayer(targetPlayerName));
                    }
                }
            }

            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.btn, PersistentDataType.STRING)) {
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING) != null) {
                    if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING).equals("뒤로가기")) {
                        commandListener.openManagementGui(player);
                    }
                }
            }

        } else if (inventoryManager.hasManagementDelete(player.getUniqueId())) {

            if (event.getInventory() != inventoryManager.getManagementDelete(player.getUniqueId())) return;

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.btn, PersistentDataType.STRING)) {
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING) != null) {
                    if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING).equals("뒤로가기")) {
                        commandListener.openManagementGui(player);
                    }
                }
            }

            if (event.getCurrentItem().getType().equals(Material.NAME_TAG)) {
                commandListener.openDeleteConfirm(player, event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING));
            }

        } else if (inventoryManager.hasManagementDeleteConfirm(player.getUniqueId())) {

            if (event.getInventory() != inventoryManager.getManagementDeleteConfirm(player.getUniqueId())) return;

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.btn, PersistentDataType.STRING)) {
                String title = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING);
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING).equals("취소")) {
                    commandListener.openDeleteTitleGui(player);
                } else {
                    saveDataManager.removeTitle(title);
                    player.sendMessage(prefix + ChatColor.WHITE + colorize(translateHexColorCodes("칭호 " + title + "을(를) 삭제했습니다")));
                    commandListener.openDeleteTitleGui(player);
                }
            }

        } else if (inventoryManager.hasManagementIssued(player.getUniqueId())) {

            if (event.getInventory() != inventoryManager.getManagementIssued(player.getUniqueId())) return;

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.btn, PersistentDataType.STRING)) {
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING) != null) {
                    if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING).equals("뒤로가기")) {
                        commandListener.openManagementGui(player);
                    }
                }
            }

            if (event.getCurrentItem().getType().equals(Material.NAME_TAG)) {
                String title = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(plugin.btn, PersistentDataType.STRING);
                commandListener.giveTitleBook(player, title);
            }

        } else if (event.getInventory().getType() == InventoryType.ANVIL) {

            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(plugin.key, PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    player.setLevel(player.getLevel());
                    player.updateInventory();
                }
            }
        }
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
