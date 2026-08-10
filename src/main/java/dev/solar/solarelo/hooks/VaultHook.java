package dev.solar.solarelo.hooks;

import dev.solar.solarelo.SolarElo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private final SolarElo plugin;
    private Economy economy = null;

    public VaultHook(SolarElo plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public double getBalance(OfflinePlayer player) {
        if (economy == null) return 0;
        return economy.getBalance(player);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        if (economy == null) return true;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        if (economy == null) return true;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public String format(double amount) {
        if (economy == null) return "$" + ((long) amount);
        return economy.format(amount);
    }
}
