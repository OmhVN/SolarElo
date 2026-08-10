package dev.solar.solarelo.gui;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import dev.solar.solarelo.hooks.SkinsRestorerHook;
import dev.solar.solarelo.listeners.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BountyMenu {

    public static void open(SolarElo plugin, Player player) {
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        open(plugin, player, 1, "HIGH_TO_LOW");
    }

    public static void open(SolarElo plugin, Player player, int page, String filter) {
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-bounty", "&#ff3c3cTính năng Săn tiền thưởng hiện đang bị tắt.");
            player.sendMessage(EloGui.colorize(msg));
            return;
        }
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;
        String activeFilter = (filter == null || filter.isEmpty()) ? "HIGH_TO_LOW" : filter;
        org.bukkit.configuration.file.FileConfiguration bountyConfig = plugin.getGuiConfigManager().getBountyConfig();
        GuiLayoutHelper.LayoutInfo layout = GuiLayoutHelper.getLayoutInfo(bountyConfig, "gui-disposition", page);
        int rows = layout.rows;
        int limit = layout.limit;
        int offset = layout.offset;

        plugin.runAsync(() -> {
            PlayerData cached = plugin.getEloManager().getCachedData(player.getUniqueId());
            PlayerData selfData = cached != null ? cached : plugin.getDatabaseManager().loadPlayer(player.getUniqueId(), player.getName());

            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            onlinePlayers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));

            int minTargetElo = bountyConfig.getInt("minimum-target-elo", 0);

            List<PlayerData> targetDataList = new ArrayList<>();
            for (Player p : onlinePlayers) {
                if (plugin.getEloManager().isIpBlocked(p)) {
                    continue;
                }
                PlayerData data = plugin.getEloManager().getCachedData(p.getUniqueId());
                if (data == null) {
                    data = plugin.getEloManager().getData(p.getUniqueId(), p.getName());
                }
                if (data != null && data.getElo() >= minTargetElo) {
                    targetDataList.add(data);
                }
            }

            if (activeFilter.equalsIgnoreCase("LOW_TO_HIGH")) {
                targetDataList.sort((d1, d2) -> Integer.compare(d1.getElo(), d2.getElo()));
            } else {
                targetDataList.sort((d1, d2) -> Integer.compare(d2.getElo(), d1.getElo()));
            }

            int totalTargets = targetDataList.size();
            List<PlayerData> paginatedTargets = new ArrayList<>();
            int safeOffset = Math.max(0, offset);
            if (safeOffset < totalTargets) {
                for (int i = safeOffset; i < Math.min(totalTargets, safeOffset + limit); i++) {
                    paginatedTargets.add(targetDataList.get(i));
                }
            }
            boolean hasNextPage = totalTargets > (page * limit);

            plugin.runForEntity(player, () -> {
                if (!player.isOnline()) return;

                String titleTemplate = bountyConfig.getString("title", "#ff3c3cTạo Truy Nã - Chọn Mục Tiêu");
                String title = EloGui.colorize(titleTemplate.replace("{page}", String.valueOf(page)));

                EloGui.BountyHolder holder = new EloGui.BountyHolder(page, activeFilter);
                Inventory inv = EloGui.createInventory(holder, rows * 9, title);
                holder.setInventory(inv);

                setupBountyLayout(inv, plugin, bountyConfig, rows, limit, selfData, paginatedTargets);
                addBountyControls(inv, plugin, bountyConfig, page, activeFilter, rows, hasNextPage, selfData);

                player.openInventory(inv);
            });
        });
    }

    private static void setupBountyLayout(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration bountyConfig, int rows, int limit, PlayerData selfData, List<PlayerData> paginatedTargets) {
        boolean fillerEnabled = bountyConfig.getBoolean("filler.enabled", true);
        if (fillerEnabled) {
            Material paneMat = EloGui.getMaterial(bountyConfig.getString("filler.material"), Material.GRAY_STAINED_GLASS_PANE);
            ItemStack pane = new ItemStack(paneMat);
            ItemMeta paneMeta = pane.getItemMeta();
            if (paneMeta != null) {
                paneMeta.setDisplayName(" ");
                pane.setItemMeta(paneMeta);
            }
            for (int i = limit; i < rows * 9; i++) {
                inv.setItem(i, pane);
            }
        }

        boolean isLocked = selfData.getElo() < bountyConfig.getInt("minimum-unlock-elo", 1200);

        if (isLocked) {
            int slot = EloGui.getSlotFromLayout(bountyConfig, 'l', 22);
            if (slot >= 0 && slot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("locked-item.material"), Material.BARRIER);
                ItemStack lockedItem = new ItemStack(mat);
                ItemMeta meta = lockedItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(EloGui.colorize(bountyConfig.getString("locked-item.name", "#ff3c3c🔒 Bounty Locked")));
                    List<String> lore = new ArrayList<>();
                    int reqElo = bountyConfig.getInt("minimum-unlock-elo", 1200);
                    for (String l : bountyConfig.getStringList("locked-item.lore")) {
                        lore.add(EloGui.colorize(l.replace("{required}", String.valueOf(reqElo))
                                           .replace("{elo}", String.valueOf(selfData.getElo()))));
                    }
                    meta.setLore(lore);
                    lockedItem.setItemMeta(meta);
                }
                inv.setItem(slot, lockedItem);
            }
        } else {
            if (paginatedTargets.isEmpty()) {
                int slot = EloGui.getSlotFromLayout(bountyConfig, 'i', 22);
                if (slot >= 0 && slot < rows * 9) {
                    Material mat = EloGui.getMaterial(bountyConfig.getString("no-targets-item.material"), Material.BARRIER);
                    ItemStack noTargetsItem = new ItemStack(mat);
                    ItemMeta meta = noTargetsItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(EloGui.colorize(bountyConfig.getString("no-targets-item.name", "#ff3c3cKhông có người chơi hợp lệ")));
                        List<String> lore = new ArrayList<>();
                        for (String l : bountyConfig.getStringList("no-targets-item.lore")) {
                            lore.add(EloGui.colorize(l));
                        }
                        meta.setLore(lore);
                        noTargetsItem.setItemMeta(meta);
                    }
                    inv.setItem(slot, noTargetsItem);
                }
            } else {
                addBountyTargets(inv, plugin, bountyConfig, paginatedTargets, limit);
            }
        }
    }

    private static void addBountyTargets(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration bountyConfig, List<PlayerData> paginatedTargets, int limit) {
        int targetSlot = 0;
        String headNameTemplate = bountyConfig.getString("target-player-head.name", "#ffaa00{player}");
        List<String> headLoreTemplate = bountyConfig.getStringList("target-player-head.lore");
        if (headLoreTemplate == null || headLoreTemplate.isEmpty()) {
            headLoreTemplate = Arrays.asList(
                    "#aaaaaaElo: #ffaa00{elo}",
                    "#aaaaaaRank: &r{rank}",
                    "#aaaaaaTiền thưởng hiện tại: #00ff3c{bounty} Elo",
                    "",
                    "#00ff3cClick để đặt mức thưởng truy nã!"
            );
        }

        for (PlayerData tData : paginatedTargets) {
            if (targetSlot >= limit) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            if (skullMeta != null) {
                SkinsRestorerHook.applySkin(skullMeta, tData.getUuid(), tData.getName());

                NamespacedKey uuidKey = new NamespacedKey(plugin, "target_uuid");
                skullMeta.getPersistentDataContainer().set(uuidKey, PersistentDataType.STRING, tData.getUuid().toString());

                String rankKey = plugin.getRankManager().getRank(tData.getElo());
                String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);

                skullMeta.setDisplayName(EloGui.colorize(headNameTemplate.replace("{player}", tData.getName())));
                List<String> lore = new ArrayList<>();
                for (String l : headLoreTemplate) {
                    lore.add(EloGui.colorize(l.replace("{elo}", EloGui.formatNumber(tData.getElo()))
                                       .replace("{rank}", rankDisplay)
                                       .replace("{bounty}", EloGui.formatNumber(tData.getBounty()))));
                }
                skullMeta.setLore(lore);
                head.setItemMeta(skullMeta);
            }
            inv.setItem(targetSlot++, head);
        }
    }

    private static void addBountyControls(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration bountyConfig, int page, String activeFilter, int rows, boolean hasNextPage, PlayerData selfData) {
        boolean isLocked = selfData.getElo() < bountyConfig.getInt("minimum-unlock-elo", 1200);

        int prevSlot = EloGui.getSlotFromLayout(bountyConfig, 'b', 45);
        if (prevSlot >= 0 && prevSlot < rows * 9 && page > 1) {
            Material mat = EloGui.getMaterial(bountyConfig.getString("back.material"), Material.ARROW);
            ItemStack prev = new ItemStack(mat);
            ItemMeta prevMeta = prev.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("back.name", "#00BFFFTrang trước")));
                List<String> prevLore = new ArrayList<>();
                for (String l : bountyConfig.getStringList("back.lore")) {
                    prevLore.add(EloGui.colorize(l.replace("{page}", String.valueOf(page - 1))));
                }
                prevMeta.setLore(prevLore);
                prev.setItemMeta(prevMeta);
            }
            inv.setItem(prevSlot, prev);
        }

        if (!isLocked) {
            int nextSlot = EloGui.getSlotFromLayout(bountyConfig, 'n', 53);
            if (hasNextPage && nextSlot >= 0 && nextSlot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("next.material"), Material.ARROW);
                ItemStack nextItem = new ItemStack(mat);
                ItemMeta nextMeta = nextItem.getItemMeta();
                if (nextMeta != null) {
                    nextMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("next.name", "#00BFFFTrang sau")));
                    List<String> nextLore = new ArrayList<>();
                    for (String l : bountyConfig.getStringList("next.lore")) {
                        nextLore.add(EloGui.colorize(l.replace("{page}", String.valueOf(page + 1))));
                    }
                    nextMeta.setLore(nextLore);
                    nextItem.setItemMeta(nextMeta);
                }
                inv.setItem(nextSlot, nextItem);
            }

            int refSlot = EloGui.getSlotFromLayout(bountyConfig, 'r', 49);
            if (refSlot >= 0 && refSlot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("refresh.material"), Material.ANVIL);
                ItemStack refreshItem = new ItemStack(mat);
                ItemMeta refMeta = refreshItem.getItemMeta();
                if (refMeta != null) {
                    refMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("refresh.name", "#00BFFFLàm mới")));
                    List<String> refLore = new ArrayList<>();
                    for (String l : bountyConfig.getStringList("refresh.lore")) {
                        refLore.add(EloGui.colorize(l));
                    }
                    refMeta.setLore(refLore);
                    refreshItem.setItemMeta(refMeta);
                }
                inv.setItem(refSlot, refreshItem);
            }

            int filSlot = EloGui.getSlotFromLayout(bountyConfig, 'f', 50);
            if (filSlot >= 0 && filSlot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("filter.material"), Material.HOPPER);
                ItemStack filterItem = new ItemStack(mat);
                ItemMeta filMeta = filterItem.getItemMeta();
                if (filMeta != null) {
                    filMeta.setDisplayName(EloGui.colorize(bountyConfig.getString("filter.name", "#00BFFFLọc")));
                    List<String> filLore = new ArrayList<>();
                    List<String> options = bountyConfig.getStringList("filter.options");
                    if (options == null || options.isEmpty()) {
                        options = Arrays.asList("HIGH_TO_LOW", "LOW_TO_HIGH");
                    }
                    String selectedColor = bountyConfig.getString("filter.selected_color", "#00BFFF");
                    String unselectedColor = bountyConfig.getString("filter.unselected_color", "&f");
                    String bulletIcon = bountyConfig.getString("filter.bullet_icon", "▪ ");

                    List<String> optionLines = new ArrayList<>();
                    for (String opt : options) {
                        boolean isCurrent = opt.equalsIgnoreCase(activeFilter);
                        String optName = opt.equalsIgnoreCase("HIGH_TO_LOW") ? "Elo cao -> thấp" : "Elo thấp -> cao";
                        String color = isCurrent ? selectedColor : unselectedColor;
                        optionLines.add(EloGui.colorize(color + bulletIcon + optName));
                    }

                    for (String l : bountyConfig.getStringList("filter.lore")) {
                        if (l.contains("{options}")) {
                            filLore.addAll(optionLines);
                        } else {
                            filLore.add(EloGui.colorize(l));
                        }
                    }
                    filMeta.setLore(filLore);
                    filterItem.setItemMeta(filMeta);
                }
                inv.setItem(filSlot, filterItem);
            }
        }
    }

    public static void openBountyCreate(SolarElo plugin, Player player, UUID targetUuid, String targetName, int selectedAmount) {
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;

        plugin.runAsync(() -> {
            PlayerData creatorData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
            PlayerData targetData = plugin.getEloManager().getData(targetUuid, targetName);
            if (creatorData == null || targetData == null) return;

            plugin.runForEntity(player, () -> {
                if (!player.isOnline()) return;

                EloGui.BountyCreateHolder holder = new EloGui.BountyCreateHolder(targetUuid, targetName, selectedAmount);
                String title = EloGui.colorize("&cThiết Lập Tiền Thưởng Truy Nã");
                Inventory inv = EloGui.createInventory(holder, 27, title);
                holder.setInventory(inv);

                ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                ItemMeta pMeta = pane.getItemMeta();
                if (pMeta != null) { pMeta.setDisplayName(" "); pane.setItemMeta(pMeta); }
                for (int i = 0; i < 27; i++) {
                    inv.setItem(i, pane);
                }

                ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta headMeta = (SkullMeta) targetHead.getItemMeta();
                if (headMeta != null) {
                    SkinsRestorerHook.applySkin(headMeta, targetUuid, targetName);
                    headMeta.setDisplayName(EloGui.colorize("&cMục Tiêu: &f" + targetName));
                    List<String> lore = new ArrayList<>();
                    lore.add(EloGui.colorize("&7Elo mục tiêu: &e" + EloGui.formatNumber(targetData.getElo())));
                    lore.add(EloGui.colorize("&7Tiền thưởng hiện tại: &a" + EloGui.formatNumber(targetData.getBounty()) + " Elo"));
                    lore.add(EloGui.colorize("&7Elo của bạn hiện tại: &b" + EloGui.formatNumber(creatorData.getElo()) + " Elo"));
                    lore.add("");
                    lore.add(EloGui.colorize("&fMức thưởng đang chọn: &a+" + EloGui.formatNumber(holder.getSelectedAmount()) + " Elo"));
                    headMeta.setLore(lore);
                    targetHead.setItemMeta(headMeta);
                }
                inv.setItem(13, targetHead);

                inv.setItem(10, createAmountButton(Material.RED_STAINED_GLASS_PANE, "&c-100 Elo", "&7Bấm để giảm 100 Elo"));
                inv.setItem(11, createAmountButton(Material.RED_STAINED_GLASS_PANE, "&c-50 Elo", "&7Bấm để giảm 50 Elo"));
                inv.setItem(12, createAmountButton(Material.RED_STAINED_GLASS_PANE, "&c-10 Elo", "&7Bấm để giảm 10 Elo"));

                inv.setItem(14, createAmountButton(Material.LIME_STAINED_GLASS_PANE, "&a+10 Elo", "&7Bấm để tăng 10 Elo"));
                inv.setItem(15, createAmountButton(Material.LIME_STAINED_GLASS_PANE, "&a+50 Elo", "&7Bấm để tăng 50 Elo"));
                inv.setItem(16, createAmountButton(Material.LIME_STAINED_GLASS_PANE, "&a+100 Elo", "&7Bấm để tăng 100 Elo"));

                inv.setItem(19, createAmountButton(Material.RED_CONCRETE, "&c-1000 Elo", "&7Bấm để giảm 1000 Elo"));
                inv.setItem(20, createAmountButton(Material.RED_CONCRETE, "&c-500 Elo", "&7Bấm để giảm 500 Elo"));

                inv.setItem(21, createAmountButton(Material.ANVIL, "&eNhập số tiền tùy chỉnh", "&fBấm để nhập số Elo tùy chỉnh vào chat"));

                inv.setItem(22, createAmountButton(Material.EMERALD_BLOCK, "&a✔ XÁC NHẬN TẠO TRUY NÃ", "&fTreo &a+" + holder.getSelectedAmount() + " Elo &flên đầu &c" + targetName));

                inv.setItem(23, createAmountButton(Material.BARRIER, "&cReset về 0", "&fĐặt lại mức thưởng chọn thành 0"));

                inv.setItem(24, createAmountButton(Material.LIME_CONCRETE, "&a+500 Elo", "&7Bấm để tăng 500 Elo"));
                inv.setItem(25, createAmountButton(Material.LIME_CONCRETE, "&a+1000 Elo", "&7Bấm để tăng 1000 Elo"));

                player.openInventory(inv);
            });
        });
    }

    private static ItemStack createAmountButton(Material mat, String name, String loreLine) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(EloGui.colorize(name));
            List<String> lore = new ArrayList<>();
            lore.add(EloGui.colorize(loreLine));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void handleInventoryClick(InventoryClickEvent event, EloGui.BountyHolder bountyHolder, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration bountyConfig = plugin.getGuiConfigManager().getBountyConfig();

        int page = bountyHolder.getPage();
        String filter = bountyHolder.getFilter();

        int prevSlot = EloGui.getSlotFromLayout(bountyConfig, 'b', 45);
        int nextSlot = EloGui.getSlotFromLayout(bountyConfig, 'n', 53);
        int refreshSlot = EloGui.getSlotFromLayout(bountyConfig, 'r', 49);
        int filSlot = EloGui.getSlotFromLayout(bountyConfig, 'f', 50);

        PlayerData selfData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
        if (selfData != null && selfData.isLocked()) {
            plugin.getEffectManager().playGuiSound(player, "error");
            String msg = plugin.getMessageManager().get("bounty-locked-error", "&cElo của bạn đang bị khóa, không thể sử dụng tính năng săn thưởng!");
            player.sendMessage(EloGui.colorize(msg));
            player.closeInventory();
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if (slot == prevSlot || slot == nextSlot || slot == refreshSlot || slot == filSlot) {
            if (slot == prevSlot && page > 1) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openBounty(plugin, player, page - 1, filter);
            } else if (slot == nextSlot && currentItem != null && currentItem.getType() != Material.AIR && !currentItem.getType().name().endsWith("_GLASS_PANE")) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openBounty(plugin, player, page + 1, filter);
            } else if (slot == refreshSlot) {
                plugin.getEffectManager().playGuiSound(player, "click");
                EloGui.openBounty(plugin, player, page, filter);
            } else if (slot == filSlot) {
                plugin.getEffectManager().playGuiSound(player, "click");
                List<String> options = bountyConfig.getStringList("filter.options");
                if (options == null || options.isEmpty()) {
                    options = Arrays.asList("HIGH_TO_LOW", "LOW_TO_HIGH");
                }
                int idx = options.indexOf(filter.toUpperCase());
                int nextIdx = (idx + 1) % options.size();
                String nextFilter = options.get(nextIdx);
                EloGui.openBounty(plugin, player, 1, nextFilter);
            }
        } else if (currentItem != null && currentItem.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) currentItem.getItemMeta();
            if (skullMeta == null) return;

            NamespacedKey uuidKey = new NamespacedKey(plugin, "target_uuid");
            UUID targetUuid = null;
            if (skullMeta.getPersistentDataContainer().has(uuidKey, PersistentDataType.STRING)) {
                String str = skullMeta.getPersistentDataContainer().get(uuidKey, PersistentDataType.STRING);
                if (str != null) targetUuid = UUID.fromString(str);
            }
            if (targetUuid == null && skullMeta.getOwningPlayer() != null) {
                targetUuid = skullMeta.getOwningPlayer().getUniqueId();
            }
            if (targetUuid != null) {
                if (targetUuid.equals(player.getUniqueId())) {
                    plugin.getEffectManager().playGuiSound(player, "error");
                    player.sendMessage(EloGui.colorize("&cBạn không thể tự treo thưởng lên chính mình!"));
                    return;
                }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
                plugin.getEffectManager().playGuiSound(player, "click");
                openBountyCreate(plugin, player, target.getUniqueId(), target.getName() != null ? target.getName() : "Unknown", 0);
            }
        }
    }

    public static void handleBountyCreateClick(InventoryClickEvent event, EloGui.BountyCreateHolder holder, Player player, int slot, SolarElo plugin) {
        switch (slot) {
            case 10 -> updateBountyCreateAmount(plugin, player, holder, -100);
            case 11 -> updateBountyCreateAmount(plugin, player, holder, -50);
            case 12 -> updateBountyCreateAmount(plugin, player, holder, -10);
            case 14 -> updateBountyCreateAmount(plugin, player, holder, +10);
            case 15 -> updateBountyCreateAmount(plugin, player, holder, +50);
            case 16 -> updateBountyCreateAmount(plugin, player, holder, +100);
            case 19 -> updateBountyCreateAmount(plugin, player, holder, -1000);
            case 20 -> updateBountyCreateAmount(plugin, player, holder, -500);
            case 24 -> updateBountyCreateAmount(plugin, player, holder, +500);
            case 25 -> updateBountyCreateAmount(plugin, player, holder, +1000);
            case 23 -> updateBountyCreateAmount(plugin, player, holder, -holder.getSelectedAmount()); // Reset 0
            case 21 -> { // Custom chat input
                plugin.getEffectManager().playGuiSound(player, "click");
                player.closeInventory();
                GuiListener.chatPrompts.put(player.getUniqueId(), new GuiListener.ChatPromptData(holder.getTargetUuid(), holder.getTargetName(), "bounty_custom"));
                player.sendMessage(EloGui.colorize("&#00ff3c[Truy Nã] &fVui lòng nhập số Elo bạn muốn treo thưởng lên &c" + holder.getTargetName() + " &fvào khung chat (hoặc gõ &#ff3c3ccancel&f để hủy):"));
            }
            case 22 -> { // Confirm create
                if (holder.getSelectedAmount() <= 0) {
                    plugin.getEffectManager().playGuiSound(player, "error");
                    player.sendMessage(EloGui.colorize("&cVui lòng chọn mức tiền thưởng lớn hơn 0!"));
                    return;
                }
                player.closeInventory();
                plugin.getEloManager().placeBounty(player, holder.getTargetUuid(), holder.getTargetName(), holder.getSelectedAmount());
            }
        }
    }

    private static void updateBountyCreateAmount(SolarElo plugin, Player player, EloGui.BountyCreateHolder holder, int delta) {
        plugin.getEffectManager().playGuiSound(player, "click");
        holder.addSelectedAmount(delta);
        openBountyCreate(plugin, player, holder.getTargetUuid(), holder.getTargetName(), holder.getSelectedAmount());
    }

    public static void handleActiveQuestClick(InventoryClickEvent event, Player player, int slot, SolarElo plugin) {
        EloGui.openBounty(plugin, player);
    }

    public static void handleBountyConfirmClick(InventoryClickEvent event, EloGui.BountyConfirmHolder confirmHolder, Player player, int slot, SolarElo plugin) {
        EloGui.openBounty(plugin, player);
    }
}
