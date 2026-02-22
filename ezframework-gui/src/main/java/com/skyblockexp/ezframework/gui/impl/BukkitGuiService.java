package com.skyblockexp.ezframework.gui.impl;

import com.skyblockexp.ezframework.gui.api.EzGUI;
import com.skyblockexp.ezframework.gui.api.GuiClickContext;
import com.skyblockexp.ezframework.gui.api.GuiItem;
import com.skyblockexp.ezframework.gui.api.GuiService;
import com.skyblockexp.ezframework.gui.api.MenuDefinition;
import com.skyblockexp.ezframework.gui.api.GuiPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import com.skyblockexp.ezframework.gui.api.GuiAction;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import com.skyblockexp.ezframework.message.api.Messaging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Minimal Bukkit-based GuiService implementation. Converts ``MenuDefinition``
 * into Bukkit inventories and dispatches click events to the menu's handler.
 */
public class BukkitGuiService implements GuiService, Listener {
    private JavaPlugin plugin;
    private final Map<Inventory, MenuDefinition> handlers = new ConcurrentHashMap<>();

    @Override
    public void init(Object plugin) {
        if (plugin instanceof JavaPlugin) {
            this.plugin = (JavaPlugin) plugin;
        } else {
            throw new IllegalArgumentException("BukkitGuiService requires a Bukkit JavaPlugin instance");
        }
        EzGUI.forPlugin(plugin).registerProvider(this);
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void openMenu(GuiPlayer player, MenuDefinition menu) {
        Player p = unwrap(player);
        if (!Bukkit.isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> openMenu(player, menu));
            return;
        }

        String title = Messaging.forPlugin(this.plugin).format(menu.getTitle() == null ? "" : menu.getTitle());
        Inventory inv = Bukkit.createInventory(null, menu.getSize(), title);
        for (Map.Entry<Integer, GuiItem> e : menu.getItems().entrySet()) {
            ItemStack is = ItemConverter.toItemStack(e.getValue(), menu, e.getKey(), this.plugin);
            inv.setItem(e.getKey(), is);
        }

        // register the full menu so we can dispatch per-slot actions or the menu-level handler
        handlers.put(inv, menu);

        p.openInventory(inv);
    }

    @Override
    public void closeMenu(GuiPlayer player) {
        Player p = unwrap(player);
        if (!Bukkit.isPrimaryThread()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> closeMenu(player));
            return;
        }
        p.closeInventory();
    }

    private ItemStack toItemStack(GuiItem item, MenuDefinition menu, int slot) {
        return ItemConverter.toItemStack(item, menu, slot, this.plugin);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        MenuDefinition m = handlers.get(inv);
        if (m == null) return;
        event.setCancelled(true);
        HumanEntity he = event.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;
        GuiPlayer gp = BukkitGuiAdapters.wrap(p);
        int slot = event.getRawSlot();
        ItemStack is = event.getCurrentItem();
        GuiItem gi = null;
        if (is != null) {
            gi = ItemConverter.fromItemStack(is, this.plugin);
        }

        // dispatch per-slot action if present; prefer stored ez_action if available
        GuiAction action = null;
        try {
            if (is != null && is.hasItemMeta()) {
                ItemMeta im = is.getItemMeta();
                NamespacedKey actionKey = new NamespacedKey(this.plugin, "ez_action");
                if (im.getPersistentDataContainer().has(actionKey, PersistentDataType.STRING)) {
                    String stored = im.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
                    if (stored != null) {
                        try {
                            int storedSlot = Integer.parseInt(stored);
                            action = m.getActions().get(storedSlot);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        } catch (Throwable t) {
            // ignore PDC errors
        }

        if (action == null) action = m.getActions().get(slot);
        if (action != null) {
            action.execute(new GuiClickContext(gp, slot, gi));
            return;
        }

        // fallback to menu-level click handler
        Consumer<GuiClickContext> h = m.getClickHandler();
        if (h != null) h.accept(new GuiClickContext(gp, slot, gi));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        handlers.remove(inv);
    }

    private Player unwrap(GuiPlayer player) {
        if (player instanceof BukkitGuiAdapters.BukkitWrappedGuiPlayer) return ((BukkitGuiAdapters.BukkitWrappedGuiPlayer) player).getPlayer();
        throw new IllegalArgumentException("GuiPlayer is not a BukkitWrappedGuiPlayer");
    }
}
