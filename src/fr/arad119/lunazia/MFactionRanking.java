package fr.arad119.lunazia;

import com.massivecraft.factions.Faction;
import fr.arad119.lunazia.commands.RankingCommand;
import fr.arad119.lunazia.data.FactionData;
import fr.arad119.lunazia.data.PlayerData;
import fr.arad119.lunazia.database.Database;
import fr.arad119.lunazia.database.manager.DatabaseManager;
import fr.arad119.lunazia.listeners.PlayerListener;
import fr.arad119.lunazia.runnable.RankingUpdateRunnable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MFactionRanking extends JavaPlugin {
    private Database database;

    private DatabaseManager databaseManager;

    private ConsoleCommandSender consoleSender = getServer().getConsoleSender();

    private HashMap<Faction, FactionData> factionsData = new HashMap<>();

    private HashMap<Player, PlayerData> playersData = new HashMap<>();

    private LinkedHashMap<String, Integer> rankingList = new LinkedHashMap<>();

    private String prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix"));

    public static Economy economy = null;

    public void onEnable() {
        saveDefaultConfig();
        this.database = new Database(getConfig());
        this.databaseManager = new DatabaseManager(this);
        try {
            this.database.connect();
            this.consoleSender.sendMessage(String.valueOf(this.prefix) + ChatColor.YELLOW + " ========== ENABLE START =========");
            this.consoleSender.sendMessage(String.valueOf(this.prefix) + ChatColor.AQUA + " Database successfully connected !");
            this.consoleSender.sendMessage(String.valueOf(this.prefix) + ChatColor.YELLOW + " ==========================================");
        } catch (SQLException ex) {
            this.consoleSender.sendMessage(String.valueOf(this.prefix) + ChatColor.YELLOW + " ========== SQL CONNECTION FAILED =========");
            this.consoleSender.sendMessage(String.valueOf(this.prefix) + ChatColor.RED + " Your SQL has to be checked in your config.yml");
            this.consoleSender.sendMessage(String.valueOf(this.prefix) + ChatColor.RED + " Plugin is disabling...");
            this.consoleSender.sendMessage(String.valueOf(this.prefix) + ChatColor.YELLOW + " ==========================================");
            getServer().getPluginManager().disablePlugin((Plugin)this);
            return;
        }
        (new RankingUpdateRunnable(this)).run();
        getCommand("classement").setExecutor((CommandExecutor)new RankingCommand(this));
        getServer().getPluginManager().registerEvents((Listener)new PlayerListener(this), (Plugin)this);
        getServer().dispatchCommand((CommandSender)getServer().getConsoleSender(), "classement reload");
        setupEconomy();
    }

    public static boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        economy = (Economy)rsp.getProvider();
        return (economy != null);
    }

    public void onDisable() {
        getServer().dispatchCommand((CommandSender)getServer().getConsoleSender(), "classement reload");
        if (this.database.isConnected())
            this.database.disconnect();
    }

    public Database getDb() {
        return this.database;
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public HashMap<Faction, FactionData> getFactionsData() {
        return this.factionsData;
    }

    public HashMap<Player, PlayerData> getPlayersData() {
        return this.playersData;
    }

    public LinkedHashMap<String, Integer> getRankingList() {
        return this.rankingList;
    }
}
