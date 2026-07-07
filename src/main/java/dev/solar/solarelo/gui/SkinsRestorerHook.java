package dev.solar.solarelo.gui;

import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.PropertyUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class SkinsRestorerHook {
    private static Boolean skinsRestorerEnabled = null;

    public static boolean isEnabled() {
        if (skinsRestorerEnabled == null) {
            try {
                skinsRestorerEnabled = Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer");
            } catch (Throwable t) {
                skinsRestorerEnabled = false;
            }
        }
        return skinsRestorerEnabled;
    }

    private static final java.util.Map<UUID, PlayerProfile> profileCache = new java.util.concurrent.ConcurrentHashMap<>();

    public static void applySkin(SkullMeta skullMeta, UUID uuid, String name) {
        if (uuid == null) return;

        if (profileCache.size() > 500) {
            profileCache.clear();
        }

        PlayerProfile cached = profileCache.get(uuid);
        if (cached != null) {
            skullMeta.setOwnerProfile(cached);
            return;
        }

        org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            try {
                PlayerProfile profile = onlinePlayer.getPlayerProfile();
                if (profile.getTextures().getSkin() != null) {
                    profileCache.put(uuid, profile);
                    skullMeta.setOwnerProfile(profile);
                    return;
                }
            } catch (Throwable t) {
            }
        }

        if (isEnabled()) {
            try {
                Optional<SkinProperty> skin = SkinsRestorerProvider.get().getPlayerStorage().getSkinOfPlayer(uuid);
                if (skin.isPresent()) {
                    String skinUrl = PropertyUtils.getSkinTextureUrl(skin.get());
                    if (skinUrl != null && !skinUrl.isEmpty()) {
                        PlayerProfile profile = Bukkit.createProfile(uuid, name);
                        PlayerTextures textures = profile.getTextures();
                        textures.setSkin(URI.create(skinUrl).toURL());
                        profile.setTextures(textures);
                        profileCache.put(uuid, profile);
                        skullMeta.setOwnerProfile(profile);
                        return;
                    }
                }
            } catch (Throwable t) {
            }
        }

        try {
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            PlayerProfile profile = offlinePlayer.getPlayerProfile();
            if (profile != null && profile.getTextures().getSkin() != null) {
                profileCache.put(uuid, profile);
                skullMeta.setOwnerProfile(profile);
            }
        } catch (Throwable t) {
        }
    }
}
