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
import java.util.Collections;
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
                if (data != null && data.getBounty() > 0 && data.getElo() >= minTargetElo) {
                    targetDataList.add(data);
                }
            }

            for (PlayerData pd : plugin.getEloManager().getCachedPlayers()) {
                if (pd != null && pd.getBounty() > 0 && pd.getElo() >= minTargetElo && !targetDataList.contains(pd)) {
                    targetDataList.add(pd);
                }
            }

            if (activeFilter.equalsIgnoreCase("LOW_TO_HIGH")) {
                targetDataList.sort((d1, d2) -> Integer.compare(d1.getBounty(), d2.getBounty()));
            } else {
                targetDataList.sort((d1, d2) -> Integer.compare(d2.getBounty(), d1.getBounty()));
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

        boolean isLocked = selfData.getElo() < bountyConfig.getInt("minimum-unlock-elo", 0);

        if (isLocked) {
            int slot = EloGui.getSlotFromLayout(bountyConfig, 'l', 22);
            if (slot >= 0 && slot < rows * 9) {
                Material mat = EloGui.getMaterial(bountyConfig.getString("locked-item.material"), Material.BARRIER);
                ItemStack lockedItem = new ItemStack(mat);
                ItemMeta meta = lockedItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(EloGui.colorize(bountyConfig.getString("locked-item.name", "#ff3c3c🔒 Bounty Locked")));
                    List<String> lore = new ArrayList<>();
                    int reqElo = bountyConfig.getInt("minimum-unlock-elo", 0);
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
                                       .replace("{bounty}", EloGui.formatNumber(tData.getBounty()))
                                       .replace("{reward_elo}", EloGui.formatNumber(tData.getBounty()))
                                       .replace("{player}", tData.getName())));
                }
                skullMeta.setLore(lore);
                head.setItemMeta(skullMeta);
            }
            inv.setItem(targetSlot++, head);
        }
    }

    private static void addBountyControls(Inventory inv, SolarElo plugin, org.bukkit.configuration.file.FileConfiguration bountyConfig, int page, String activeFilter, int rows, boolean hasNextPage, PlayerData selfData) {
        boolean isLocked = selfData.getElo() < bountyConfig.getInt("minimum-unlock-elo", 0);

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

            int actSlot = EloGui.getSlotFromLayout(bountyConfig, 'a', 48);
            if (actSlot >= 0 && actSlot < rows * 9) {
                org.bukkit.configuration.ConfigurationSection actSec = bountyConfig.getConfigurationSection("active-quest");
                if (actSec == null) {
                    actSec = bountyConfig.getConfigurationSection("active-bounty-item");
                }
                String matStr = actSec != null ? actSec.getString("material", "BLUE_BANNER") : "BLUE_BANNER";
                Material mat = EloGui.getMaterial(matStr, Material.BLUE_BANNER);
                ItemStack actItem = new ItemStack(mat);
                ItemMeta actMeta = actItem.getItemMeta();
                if (actMeta != null) {
                    String name = actSec != null ? actSec.getString("name", "#00BFFFᴀᴄᴛɪᴠᴇ ǫᴜᴇsᴛ") : "#00BFFFᴀᴄᴛɪᴠᴇ ǫᴜᴇsᴛ";
                    actMeta.setDisplayName(EloGui.colorize(name));
                    List<String> actLore = new ArrayList<>();
                    List<String> rawLore = actSec != null ? actSec.getStringList("lore") : null;
                    if (rawLore != null) {
                        for (String l : rawLore) {
                            actLore.add(EloGui.colorize(l));
                        }
                    }
                    actMeta.setLore(actLore);
                    actItem.setItemMeta(actMeta);
                }
                inv.setItem(actSlot, actItem);
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

        org.bukkit.configuration.file.FileConfiguration config = plugin.getGuiConfigManager().getBountyCreateConfig();
        if (config == null || !config.getBoolean("enabled", true)) return;

        plugin.runAsync(() -> {
            PlayerData creatorData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
            PlayerData targetData = plugin.getEloManager().getData(targetUuid, targetName);
            if (creatorData == null || targetData == null) return;

            plugin.runForEntity(player, () -> {
                if (!player.isOnline()) return;

                EloGui.BountyCreateHolder holder = new EloGui.BountyCreateHolder(targetUuid, targetName, selectedAmount);
                String rawTitle = config.getString("title", "&cʙᴏᴜɴᴛʏ ᴀᴍᴏᴜɴᴛ sᴇᴛᴜᴘ");
                int rows = config.getInt("rows", 6);
                int size = rows * 9;
                Inventory inv = EloGui.createInventory(holder, size, EloGui.colorize(rawTitle));
                holder.setInventory(inv);

                if (config.getBoolean("filler.enabled", true)) {
                    Material fillMat = Material.matchMaterial(config.getString("filler.material", "GRAY_STAINED_GLASS_PANE"));
                    if (fillMat == null) fillMat = Material.GRAY_STAINED_GLASS_PANE;
                    ItemStack pane = new ItemStack(fillMat);
                    ItemMeta pMeta = pane.getItemMeta();
                    if (pMeta != null) { pMeta.setDisplayName(" "); pane.setItemMeta(pMeta); }
                    for (int i = 0; i < size; i++) {
                        inv.setItem(i, pane);
                    }
                }

                int headSlot = config.getInt("target-head.slot", 13);
                ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta headMeta = (SkullMeta) targetHead.getItemMeta();
                if (headMeta != null) {
                    SkinsRestorerHook.applySkin(headMeta, targetUuid, targetName);
                    String nameFmt = config.getString("target-head.name", "&cᴛᴀʀɢᴇᴛ: &f{target}");
                    nameFmt = nameFmt.replace("{target}", targetName);
                    headMeta.setDisplayName(EloGui.colorize(nameFmt));

                    List<String> rawLore = config.getStringList("target-head.lore");
                    List<String> lore = new ArrayList<>();
                    for (String line : rawLore) {
                        line = line.replace("{target}", targetName)
                                   .replace("{target_elo}", EloGui.formatNumber(targetData.getElo()))
                                   .replace("{target_bounty}", EloGui.formatNumber(targetData.getBounty()))
                                   .replace("{your_elo}", EloGui.formatNumber(creatorData.getElo()))
                                   .replace("{selected_amount}", EloGui.formatNumber(holder.getSelectedAmount()));
                        lore.add(EloGui.colorize(line));
                    }
                    headMeta.setLore(lore);
                    targetHead.setItemMeta(headMeta);
                }
                inv.setItem(headSlot, targetHead);

                org.bukkit.configuration.ConfigurationSection buttonsSec = config.getConfigurationSection("buttons");
                if (buttonsSec != null) {
                    for (String key : buttonsSec.getKeys(false)) {
                        org.bukkit.configuration.ConfigurationSection btn = buttonsSec.getConfigurationSection(key);
                        if (btn == null) continue;
                        int slot = btn.getInt("slot", -1);
                        if (slot < 0 || slot >= size) continue;
                        Material mat = Material.matchMaterial(btn.getString("material", "RED_STAINED_GLASS_PANE"));
                        if (mat == null) mat = Material.RED_STAINED_GLASS_PANE;
                        String name = btn.getString("name", "");
                        List<String> lore = btn.getStringList("lore");
                        inv.setItem(slot, createConfigItem(mat, name, lore, holder, targetName));
                    }
                }

                int customSlot = config.getInt("custom-amount.slot", 31);
                Material customMat = Material.matchMaterial(config.getString("custom-amount.material", "BOOK"));
                if (customMat == null) customMat = Material.BOOK;
                String customName = config.getString("custom-amount.name", "&eᴄᴜsᴛᴏᴍ ᴀᴍᴏᴜɴᴛ");
                List<String> customLore = config.getStringList("custom-amount.lore");
                inv.setItem(customSlot, createConfigItem(customMat, customName, customLore, holder, targetName));

                int backSlot = config.getInt("back.slot", 49);
                Material backMat = Material.matchMaterial(config.getString("back.material", "ARROW"));
                if (backMat == null) backMat = Material.ARROW;
                String backName = config.getString("back.name", "&cʙᴀᴄᴋ");
                List<String> backLore = config.getStringList("back.lore");
                inv.setItem(backSlot, createConfigItem(backMat, backName, backLore, holder, targetName));

                player.openInventory(inv);
            });
        });
    }

    private static ItemStack createConfigItem(Material mat, String name, List<String> rawLore, EloGui.BountyCreateHolder holder, String targetName) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(EloGui.colorize(name));
            if (rawLore != null && !rawLore.isEmpty()) {
                List<String> lore = new ArrayList<>();
                for (String line : rawLore) {
                    line = line.replace("{selected_amount}", EloGui.formatNumber(holder.getSelectedAmount()))
                               .replace("{target}", targetName);
                    lore.add(EloGui.colorize(line));
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void openBountySelectPlayer(SolarElo plugin, Player player, int page) {
        if (EloGui.checkIpBlockedRedirect(plugin, player, false)) return;

        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getGuiConfigManager().getBountySelectConfig();
        if (cfg != null && !cfg.getBoolean("enabled", true)) return;

        plugin.runAsync(() -> {
            List<PlayerData> playersList = new ArrayList<>(plugin.getEloManager().getCachedPlayers());
            playersList.removeIf(p -> p.getUuid().equals(player.getUniqueId()));

            if (playersList.isEmpty()) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getUniqueId().equals(player.getUniqueId())) {
                        PlayerData pd = plugin.getEloManager().getData(p.getUniqueId(), p.getName());
                        if (pd != null) playersList.add(pd);
                    }
                }
            }

            plugin.runForEntity(player, () -> {
                if (!player.isOnline()) return;

                EloGui.BountySelectHolder holder = new EloGui.BountySelectHolder(page);
                String title = cfg != null ? cfg.getString("title", "sᴇʟᴇᴄᴛ ᴛᴀʀɢᴇᴛ") : "sᴇʟᴇᴄᴛ ᴛᴀʀɢᴇᴛ";
                int rows = cfg != null ? cfg.getInt("rows", 6) : 6;
                int size = Math.min(54, Math.max(9, rows * 9));

                Inventory inv = EloGui.createInventory(holder, size, EloGui.colorize(title));
                holder.setInventory(inv);

                if (cfg != null && cfg.getBoolean("filler.enabled", false)) {
                    String matName = cfg.getString("filler.material", "GRAY_STAINED_GLASS_PANE");
                    Material mat = Material.matchMaterial(matName);
                    if (mat == null) mat = Material.GRAY_STAINED_GLASS_PANE;

                    ItemStack pane = new ItemStack(mat);
                    ItemMeta pMeta = pane.getItemMeta();
                    if (pMeta != null) { pMeta.setDisplayName(" "); pane.setItemMeta(pMeta); }
                    for (int i = 0; i < size; i++) inv.setItem(i, pane);
                }

                int backSlot = cfg != null ? cfg.getInt("buttons.back.slot", 45) : 45;
                int nextSlot = cfg != null ? cfg.getInt("buttons.next.slot", 53) : 53;

                int itemsPerPage = Math.max(1, size - 9);
                int totalPages = Math.max(1, (int) Math.ceil((double) playersList.size() / itemsPerPage));
                int currentPage = Math.min(Math.max(1, page), totalPages);
                int startIndex = (currentPage - 1) * itemsPerPage;
                int endIndex = Math.min(startIndex + itemsPerPage, playersList.size());

                String headNameFmt = cfg != null ? cfg.getString("player-head.name", "&c{target}") : "&c{target}";
                List<String> headLoreFmt = cfg != null && cfg.contains("player-head.lore") ? cfg.getStringList("player-head.lore") :
                        Arrays.asList("&7Elo: &e{target_elo}", "&7Current Bounty: &a{target_bounty}", "", "&e👉 Click to place bounty on this player!");

                for (int i = startIndex; i < endIndex; i++) {
                    PlayerData targetData = playersList.get(i);
                    int slot = i - startIndex;

                    ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    if (meta != null) {
                        SkinsRestorerHook.applySkin(meta, targetData.getUuid(), targetData.getName());
                        meta.setDisplayName(EloGui.colorize(headNameFmt.replace("{target}", targetData.getName())));

                        String bountyVal = plugin.getVaultHook().hasEconomy() ? plugin.getVaultHook().format(targetData.getBounty()) : (EloGui.formatNumber(targetData.getBounty()) + " Elo");

                        List<String> lore = new ArrayList<>();
                        for (String l : headLoreFmt) {
                            lore.add(EloGui.colorize(l.replace("{target}", targetData.getName())
                                    .replace("{target_elo}", EloGui.formatNumber(targetData.getElo()))
                                    .replace("{target_bounty}", bountyVal)));
                        }
                        meta.setLore(lore);

                        NamespacedKey uuidKey = new NamespacedKey(plugin, "target_uuid");
                        meta.getPersistentDataContainer().set(uuidKey, PersistentDataType.STRING, targetData.getUuid().toString());
                        head.setItemMeta(meta);
                    }
                    inv.setItem(slot, head);
                }

                // Back Button (Slot 45 - Bottom Left)
                String backMatName = cfg != null ? cfg.getString("buttons.back.material", "ARROW") : "ARROW";
                Material bMat = Material.matchMaterial(backMatName);
                if (bMat == null) bMat = Material.ARROW;
                ItemStack back = new ItemStack(bMat);
                ItemMeta bMeta = back.getItemMeta();
                if (bMeta != null) {
                    String bName = cfg != null ? cfg.getString("buttons.back.name", "&cʙᴀᴄᴋ") : "&cʙᴀᴄᴋ";
                    bMeta.setDisplayName(EloGui.colorize(bName));

                    List<String> rawLore = currentPage > 1 ?
                            (cfg != null && cfg.contains("buttons.back.lore-pages") ? cfg.getStringList("buttons.back.lore-pages") : Collections.singletonList("&7Go to page {prev_page}")) :
                            (cfg != null && cfg.contains("buttons.back.lore-page-1") ? cfg.getStringList("buttons.back.lore-page-1") : Collections.singletonList("&7Return to bounty menu"));

                    List<String> bLore = new ArrayList<>();
                    for (String l : rawLore) {
                        bLore.add(EloGui.colorize(l.replace("{prev_page}", String.valueOf(currentPage - 1))));
                    }
                    bMeta.setLore(bLore);
                    back.setItemMeta(bMeta);
                }
                inv.setItem(backSlot, back);

                // Next Button (Slot 53 - Bottom Right) if there's a next page
                if (currentPage < totalPages) {
                    String nextMatName = cfg != null ? cfg.getString("buttons.next.material", "ARROW") : "ARROW";
                    Material nMat = Material.matchMaterial(nextMatName);
                    if (nMat == null) nMat = Material.ARROW;
                    ItemStack next = new ItemStack(nMat);
                    ItemMeta nMeta = next.getItemMeta();
                    if (nMeta != null) {
                        String nName = cfg != null ? cfg.getString("buttons.next.name", "&aɴᴇxᴛ") : "&aɴᴇxᴛ";
                        nMeta.setDisplayName(EloGui.colorize(nName));

                        List<String> rawLore = cfg != null && cfg.contains("buttons.next.lore") ? cfg.getStringList("buttons.next.lore") : Collections.singletonList("&7Go to page {next_page}");
                        List<String> nLore = new ArrayList<>();
                        for (String l : rawLore) {
                            nLore.add(EloGui.colorize(l.replace("{next_page}", String.valueOf(currentPage + 1))));
                        }
                        nMeta.setLore(nLore);
                        next.setItemMeta(nMeta);
                    }
                    inv.setItem(nextSlot, next);
                }

                player.openInventory(inv);
            });
        });
    }

    public static void handleBountySelectClick(InventoryClickEvent event, EloGui.BountySelectHolder holder, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getGuiConfigManager().getBountySelectConfig();
        int backSlot = cfg != null ? cfg.getInt("buttons.back.slot", 45) : 45;
        int nextSlot = cfg != null ? cfg.getInt("buttons.next.slot", 53) : 53;

        int currentPage = holder.getPage();

        if (slot == backSlot) { // Bottom-left corner back button
            plugin.getEffectManager().playGuiSound(player, "click");
            if (currentPage > 1) {
                openBountySelectPlayer(plugin, player, currentPage - 1);
            } else {
                EloGui.openBounty(plugin, player, 1, "HIGH_TO_LOW");
            }
            return;
        }

        if (slot == nextSlot) { // Bottom-right corner next button
            plugin.getEffectManager().playGuiSound(player, "click");
            openBountySelectPlayer(plugin, player, currentPage + 1);
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item != null && item.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
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
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
                plugin.getEffectManager().playGuiSound(player, "click");
                openBountyCreate(plugin, player, target.getUniqueId(), target.getName() != null ? target.getName() : player.getName(), 0);
            }
        }
    }

    public static void handleInventoryClick(InventoryClickEvent event, EloGui.BountyHolder bountyHolder, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration bountyConfig = plugin.getGuiConfigManager().getBountyConfig();

        int page = bountyHolder.getPage();
        String filter = bountyHolder.getFilter();

        int prevSlot = EloGui.getSlotFromLayout(bountyConfig, 'b', 45);
        int nextSlot = EloGui.getSlotFromLayout(bountyConfig, 'n', 53);
        int refreshSlot = EloGui.getSlotFromLayout(bountyConfig, 'r', 49);
        int filSlot = EloGui.getSlotFromLayout(bountyConfig, 'f', 50);
        int actSlot = EloGui.getSlotFromLayout(bountyConfig, 'a', 48);

        PlayerData selfData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
        if (selfData != null && selfData.isLocked()) {
            plugin.getEffectManager().playGuiSound(player, "error");
            String msg = plugin.getMessageManager().get("bounty-locked-error", "&cYour Elo is locked, cannot use bounty feature!");
            player.sendMessage(EloGui.colorize(msg));
            player.closeInventory();
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if (slot == prevSlot || slot == nextSlot || slot == refreshSlot || slot == filSlot || slot == actSlot) {
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
            } else if (slot == actSlot) {
                plugin.getEffectManager().playGuiSound(player, "click");
                openBountySelectPlayer(plugin, player, 1);
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
            if (targetUuid == null) {
                targetUuid = player.getUniqueId();
            }
            if (targetUuid != null) {
                if (targetUuid.equals(player.getUniqueId()) && Bukkit.getOnlinePlayers().size() > 1 && !player.hasPermission("solarelo.admin")) {
                    plugin.getEffectManager().playGuiSound(player, "error");
                    player.sendMessage(EloGui.colorize("&cYou cannot place a bounty on yourself!"));
                    return;
                }
                org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetUuid);
                plugin.getEffectManager().playGuiSound(player, "click");
                openBountyCreate(plugin, player, target.getUniqueId(), target.getName() != null ? target.getName() : player.getName(), 0);
            }
        }
    }

    public static void handleBountyCreateClick(InventoryClickEvent event, EloGui.BountyCreateHolder holder, Player player, int slot, SolarElo plugin) {
        org.bukkit.configuration.file.FileConfiguration config = plugin.getGuiConfigManager().getBountyCreateConfig();
        if (config == null) return;

        int headSlot = config.getInt("target-head.slot", 13);
        int customSlot = config.getInt("custom-amount.slot", 31);
        int backSlot = config.getInt("back.slot", 49);

        if (slot == headSlot) {
            if (holder.getSelectedAmount() <= 0) {
                plugin.getEffectManager().playGuiSound(player, "error");
                player.sendMessage(EloGui.colorize("&cPlease select a bounty amount greater than 0!"));
                return;
            }
            player.closeInventory();
            plugin.getEloManager().placeBounty(player, holder.getTargetUuid(), holder.getTargetName(), holder.getSelectedAmount());
            return;
        }

        if (slot == customSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            player.closeInventory();
            GuiListener.chatPrompts.put(player.getUniqueId(), new GuiListener.ChatPromptData(holder.getTargetUuid(), holder.getTargetName(), "bounty_custom"));
            String msg = plugin.getMessageManager().get("bounty-custom-prompt", "&#00ff3c[Truy Nã] &fHãy nhập số tiền thưởng muốn treo lên đầu &c{target} &ftrong chat (hoặc gõ &#ff3c3ccancel&f để hủy):")
                    .replace("{target}", holder.getTargetName());
            player.sendMessage(EloGui.colorize(msg));
            return;
        }

        if (slot == backSlot) {
            plugin.getEffectManager().playGuiSound(player, "click");
            EloGui.openBounty(plugin, player, 1, "HIGH_TO_LOW");
            return;
        }

        org.bukkit.configuration.ConfigurationSection buttonsSec = config.getConfigurationSection("buttons");
        if (buttonsSec != null) {
            for (String key : buttonsSec.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection btn = buttonsSec.getConfigurationSection(key);
                if (btn != null && btn.getInt("slot", -1) == slot) {
                    int delta = 0;
                    if (key.equals("minus-10")) delta = -10;
                    else if (key.equals("minus-50")) delta = -50;
                    else if (key.equals("minus-100")) delta = -100;
                    else if (key.equals("minus-500")) delta = -500;
                    else if (key.equals("minus-1000")) delta = -1000;
                    else if (key.equals("plus-10")) delta = +10;
                    else if (key.equals("plus-50")) delta = +50;
                    else if (key.equals("plus-100")) delta = +100;
                    else if (key.equals("plus-500")) delta = +500;
                    else if (key.equals("plus-1000")) delta = +1000;

                    if (delta != 0) {
                        updateBountyCreateAmount(plugin, player, holder, delta);
                        return;
                    }
                }
            }
        }
    }

    private static void updateBountyCreateAmount(SolarElo plugin, Player player, EloGui.BountyCreateHolder holder, int delta) {
        plugin.getEffectManager().playGuiSound(player, "click");
        holder.addSelectedAmount(delta);
        openBountyCreate(plugin, player, holder.getTargetUuid(), holder.getTargetName(), holder.getSelectedAmount());
    }
}
