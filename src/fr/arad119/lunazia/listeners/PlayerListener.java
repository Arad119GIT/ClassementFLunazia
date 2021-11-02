package fr.arad119.lunazia.listeners;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRenameEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import fr.arad119.lunazia.MFactionRanking;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private MFactionRanking mFactionRanking;

    public Map<String, Integer> money = new HashMap<>();

    public PlayerListener(MFactionRanking mFactionRanking) {
        this.mFactionRanking = mFactionRanking;
        FileConfiguration fileConfiguration = mFactionRanking.getConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
        if (faction != null)
            this.mFactionRanking.getDatabaseManager().createFactionAccount(faction);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.mFactionRanking.getDatabaseManager().updatePlayerData(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getServer().getConsoleSender(), "classement removepoints " + player.getName() + " " + ChatColor.translateAlternateColorCodes('&', this.mFactionRanking.getConfig().getString("points.stats.pointPerDeath")));
    }

    @EventHandler
    public void onLandClaim(LandClaimEvent event) {
        Player player = event.getPlayer();
        Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getServer().getConsoleSender(), "classement addpoints " + player.getName() + " " + ChatColor.translateAlternateColorCodes('&', this.mFactionRanking.getConfig().getString("points.claims.pointPerClaim")));
    }

    @EventHandler
    public void onUnclaimLand(LandUnclaimEvent event) {
        Player player = event.getPlayer();
        Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getServer().getConsoleSender(), "classement removepoints " + player.getName() + " " + ChatColor.translateAlternateColorCodes('&', this.mFactionRanking.getConfig().getString("points.claims.pointPerClaim")));
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null &&
                event.getEntity().getKiller().getType() == EntityType.PLAYER) {
            Player player = event.getEntity().getKiller();
            Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getServer().getConsoleSender(), "classement addpoints " + player.getName() + " " + ChatColor.translateAlternateColorCodes('&', this.mFactionRanking.getConfig().getString("points.stats.pointPerKill")));
        }
    }

    @EventHandler
    public void onFactionDisband(FactionDisbandEvent event) {
        this.mFactionRanking.getRankingList().remove(event.getFaction().getTag());
        this.mFactionRanking.getDatabaseManager().removeFactionAccount(event.getFaction());
    }

    @EventHandler
    public void onFactionCreate(FactionCreateEvent event) {
        this.mFactionRanking.getRankingList().put(event.getFaction().getTag(), Integer.valueOf(0));
        this.mFactionRanking.getDatabaseManager().createFactionAccount(event.getFaction());
    }

    @EventHandler
    public void onPlayerLeaveFaction(FPlayerLeaveEvent event) {
        if (event.getFaction().getFPlayers().size() == 1) {
            this.mFactionRanking.getRankingList().remove(event.getFaction().getTag());
            this.mFactionRanking.getDatabaseManager().removeFactionAccount(event.getFaction());
        }
    }

    @EventHandler
    public void onFactionTagChange(FactionRenameEvent event) {
        this.mFactionRanking.getRankingList().remove(event.getFaction().getTag());
        this.mFactionRanking.getDatabaseManager().updateFactionName(event.getFaction(), event.getFactionTag());
    }
}
