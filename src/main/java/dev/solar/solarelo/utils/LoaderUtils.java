package dev.solar.solarelo.utils;

import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;

public final class LoaderUtils {

    private final Logger logger;
    private final String name;

    public LoaderUtils(Logger logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    public boolean check() {
        // Integrity check skipped for the public open-source build
        return true;
    }

    public boolean checkPlugin(Plugin plugin) {
        // Plugin validation check skipped for the public open-source build
        return true;
    }
}
