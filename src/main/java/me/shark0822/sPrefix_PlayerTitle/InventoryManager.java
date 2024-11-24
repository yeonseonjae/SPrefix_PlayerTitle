package me.shark0822.sPrefix_PlayerTitle;

import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.UUID;

public class InventoryManager {

    private final HashMap<UUID, Inventory> prefixSetSelf = new HashMap<>();
    private final HashMap<UUID, Inventory> prefixSetOther = new HashMap<>();
    private final HashMap<Inventory, UUID> managedPlayers = new HashMap<>();
    private final HashMap<UUID, Inventory> prefixManagement = new HashMap<>();
    private final HashMap<UUID, Inventory> prefixManagementPlayerList = new HashMap<>();
    private final HashMap<UUID, Inventory> prefixManagementDelete = new HashMap<>();
    private final HashMap<UUID, Inventory> prefixManagementDeleteConfirm = new HashMap<>();
    private final HashMap<UUID, Inventory> prefixManagementIssued = new HashMap<>();

    public boolean containAnyInv(UUID playerUUID) {
        return hasPrefixSetSelfInv(playerUUID) || hasPrefixSetOtherInv(playerUUID) || hasManagementInv(playerUUID) ||
                hasManagementPlayerList(playerUUID) || hasManagementDelete(playerUUID) || hasManagementDeleteConfirm(playerUUID) ||
                hasManagementIssued(playerUUID);
    }

    // ---------------------------------------------------------------------

    public Inventory getPrefixSetSelfInv(UUID playerUUID) {
        return prefixSetSelf.get(playerUUID);
    }

    public void setPrefixSetSelfInv(UUID playerUUID, Inventory inventory) {
        prefixSetSelf.put(playerUUID, inventory);
    }

    public boolean hasPrefixSetSelfInv(UUID playerUUID) {
        return prefixSetSelf.containsKey(playerUUID);
    }

    public void removePrefixSetSelfInv(UUID playerUUID) {
        prefixSetSelf.remove(playerUUID);
    }

    // ---------------------------------------------------------------------

    public Inventory getPrefixSetOtherInv(UUID playerUUID) {
        return prefixSetOther.get(playerUUID);
    }

    public void setPrefixSetOtherInv(UUID playerUUID, Inventory inventory) {
        prefixSetOther.put(playerUUID, inventory);
    }

    public boolean hasPrefixSetOtherInv(UUID playerUUID) {
        return prefixSetOther.containsKey(playerUUID);
    }

    public void removePrefixSetOtherInv(UUID playerUUID) {
        prefixSetOther.remove(playerUUID);
    }

    // ---------------------------------------------------------------------

    public UUID getManagedPlayer(Inventory inventory) {
        return managedPlayers.get(inventory);
    }

    public void setManagedPlayer(Inventory inventory, UUID playerUUID) {
        managedPlayers.put(inventory, playerUUID);
    }

    public boolean hasManagedPlayer(Inventory inventory) {
        return managedPlayers.containsKey(inventory);
    }

    public void removeManagedPlayer(Inventory inventory) {
        managedPlayers.remove(inventory);
    }

    // ---------------------------------------------------------------------


    public Inventory getManagementInv(UUID playerUUID) {
        return prefixManagement.get(playerUUID);
    }

    public void setManagementInv(UUID playerUUID, Inventory inventory) {
        prefixManagement.put(playerUUID, inventory);
    }

    public boolean hasManagementInv(UUID playerUUID) {
        return prefixManagement.containsKey(playerUUID);
    }

    public void removeManagementInv(UUID playerUUID) {
        prefixManagement.remove(playerUUID);
    }

    // ---------------------------------------------------------------------


    public Inventory getManagementPlayerList(UUID playerUUID) {
        return prefixManagementPlayerList.get(playerUUID);
    }

    public void setManagementPlayerList(UUID playerUUID, Inventory inventory) {
        prefixManagementPlayerList.put(playerUUID, inventory);
    }

    public boolean hasManagementPlayerList(UUID playerUUID) {
        return prefixManagementPlayerList.containsKey(playerUUID);
    }

    public void removeManagementPlayerList(UUID playerUUID) {
        prefixManagementPlayerList.remove(playerUUID);
    }

    // ---------------------------------------------------------------------


    public Inventory getManagementDelete(UUID playerUUID) {
        return prefixManagementDelete.get(playerUUID);
    }

    public void setManagementDelete(UUID playerUUID, Inventory inventory) {
        prefixManagementDelete.put(playerUUID, inventory);
    }

    public boolean hasManagementDelete(UUID playerUUID) {
        return prefixManagementDelete.containsKey(playerUUID);
    }

    public void removeManagementDelete(UUID playerUUID) {
        prefixManagementDelete.remove(playerUUID);
    }

    // ---------------------------------------------------------------------


    public Inventory getManagementDeleteConfirm(UUID playerUUID) {
        return prefixManagementDeleteConfirm.get(playerUUID);
    }

    public void setManagementDeleteConfirm(UUID playerUUID, Inventory inventory) {
        prefixManagementDeleteConfirm.put(playerUUID, inventory);
    }

    public boolean hasManagementDeleteConfirm(UUID playerUUID) {
        return prefixManagementDeleteConfirm.containsKey(playerUUID);
    }

    public void removeManagementDeleteConfirm(UUID playerUUID) {
        prefixManagementDeleteConfirm.remove(playerUUID);
    }

    // ---------------------------------------------------------------------


    public Inventory getManagementIssued(UUID playerUUID) {
        return prefixManagementIssued.get(playerUUID);
    }

    public void setManagementIssued(UUID playerUUID, Inventory inventory) {
        prefixManagementIssued.put(playerUUID, inventory);
    }

    public boolean hasManagementIssued(UUID playerUUID) {
        return prefixManagementIssued.containsKey(playerUUID);
    }

    public void removeManagementIssued(UUID playerUUID) {
        prefixManagementIssued.remove(playerUUID);
    }
}
