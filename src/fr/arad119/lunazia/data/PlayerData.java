package fr.arad119.lunazia.data;

import fr.arad119.lunazia.MFactionRanking;
import java.util.HashMap;
import org.bukkit.entity.Player;

public class PlayerData {
    private String playerName;

    private String factionId;

    private double playerMoney;

    private int kills;

    private int deaths;

    private HashMap<Player, PlayerData> playerDataHashMap;

    public PlayerData(MFactionRanking mFactionRanking) {
        this.playerDataHashMap = mFactionRanking.getPlayersData();
    }

    public PlayerData(String playerName, String factionId, double playerMoney, int kills, int deaths) {
        this.playerName = playerName;
        this.factionId = factionId;
        this.playerMoney = playerMoney;
        this.kills = kills;
        this.deaths = deaths;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getFactionId() {
        return this.factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public double getPlayerMoney() {
        return this.playerMoney;
    }

    public void setPlayerMoney(double playerMoney) {
        this.playerMoney = playerMoney;
    }

    public int getKills() {
        return this.kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return this.deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void addKill(Player player, int amount) {
        if (this.playerDataHashMap.containsKey(player)) {
            PlayerData playerData = this.playerDataHashMap.get(player);
            int coins = playerData.getKills() + amount;
            playerData.setKills(coins);
            this.playerDataHashMap.remove(player);
            this.playerDataHashMap.put(player, playerData);
        }
    }

    public void addDeath(Player player, int amount) {
        if (this.playerDataHashMap.containsKey(player)) {
            PlayerData playerData = this.playerDataHashMap.get(player);
            int coins = playerData.getDeaths() + amount;
            playerData.setDeaths(coins);
            this.playerDataHashMap.remove(player);
            this.playerDataHashMap.put(player, playerData);
        }
    }

    public void setMoney(Player player, double amount) {
        if (this.playerDataHashMap.containsKey(player)) {
            PlayerData playerData = this.playerDataHashMap.get(player);
            playerData.setPlayerMoney(amount);
            this.playerDataHashMap.remove(player);
            this.playerDataHashMap.put(player, playerData);
        }
    }
}
