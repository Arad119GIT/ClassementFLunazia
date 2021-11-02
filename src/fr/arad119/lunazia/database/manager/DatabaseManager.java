package fr.arad119.lunazia.database.manager;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.arad119.lunazia.MFactionRanking;
import fr.arad119.lunazia.data.FactionData;
import fr.arad119.lunazia.data.PlayerData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DatabaseManager {
    private MFactionRanking mFactionRanking;

    private FileConfiguration fileConfiguration;

    private int limit;

    private String broadcastUpdate;

    private String prefix;

    private String factionDatabaseName;

    private String playerDatabaseName;

    private HashMap<Faction, FactionData> factionFactionDataHashMap;

    private HashMap<Player, PlayerData> playerDataHashMap;

    public DatabaseManager(MFactionRanking mFactionRanking) {
        this.mFactionRanking = mFactionRanking;
        this.fileConfiguration = mFactionRanking.getConfig();
        this.limit = this.fileConfiguration.getInt("limit");
        this.broadcastUpdate = ChatColor.translateAlternateColorCodes('&', this.fileConfiguration.getString("messages.broadcast_update"));
        this.prefix = ChatColor.translateAlternateColorCodes('&', this.fileConfiguration.getString("messages.prefix"));
        this.factionDatabaseName = mFactionRanking.getDb().getFactionTableName();
        this.playerDatabaseName = mFactionRanking.getDb().getPlayerTableName();
        this.factionFactionDataHashMap = mFactionRanking.getFactionsData();
        this.playerDataHashMap = mFactionRanking.getPlayersData();
    }

    public void createPlayerAccount(Player player, Faction faction) {
        if (!hasAccount(player))
            try {
                Connection connection = this.mFactionRanking.getDb().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + this.playerDatabaseName + " (user, factionId) VALUES (?, ?)");
                preparedStatement.setString(1, player.getName());
                preparedStatement.setString(2, faction.getId());
                preparedStatement.execute();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        createPlayerData(player);
    }

    public boolean hasAccount(Player player) {
        try {
            Connection connection = this.mFactionRanking.getDb().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT user FROM " + this.playerDatabaseName + " WHERE user = ?");
            preparedStatement.setString(1, player.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            boolean hasAccount = resultSet.next();
            preparedStatement.close();
            return hasAccount;
        } catch (SQLException e) {
            return false;
        }
    }

    public void createPlayerData(Player player) {
        if (!this.playerDataHashMap.containsKey(player)) {
            try {
                Connection connection = this.mFactionRanking.getDb().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + this.playerDatabaseName + " WHERE user = ?");
                preparedStatement.setString(1, player.getName());
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    PlayerData playerData = new PlayerData(this.mFactionRanking);
                    playerData.setDeaths(resultSet.getInt("deaths"));
                    playerData.setFactionId(resultSet.getString("factionId"));
                    playerData.setKills(resultSet.getInt("kills"));
                    playerData.setMoney(player, resultSet.getInt("money"));
                    playerData.setPlayerName(player.getName());
                    this.playerDataHashMap.put(player, playerData);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Faction myFaction = FPlayers.getInstance().getByPlayer(player).getFaction();
            createFactionAccount(myFaction);
        }
    }

    public void createFactionAccount(Faction faction) {
        if (!faction.isNormal())
            return;
        if (!isFactionInDatabase(faction))
            try {
                Connection connection = this.mFactionRanking.getDb().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + this.factionDatabaseName + " (factionName, factionId, points) VALUES (?, ?, ?)");
                preparedStatement.setString(1, faction.getTag());
                preparedStatement.setString(2, faction.getId());
                preparedStatement.setInt(3, 0);
                preparedStatement.execute();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        createFactionData(faction);
    }

    public void removeFactionAccount(Faction faction) {
        if (isFactionInDatabase(faction))
            try {
                Connection connection = this.mFactionRanking.getDb().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + this.factionDatabaseName + " WHERE factionId = " + faction.getId());
                preparedStatement.execute();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    public boolean isFactionInDatabase(Faction faction) {
        boolean factionIsInDb = false;
        try {
            Connection connection = this.mFactionRanking.getDb().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT factionId FROM " + this.factionDatabaseName + " WHERE factionId = ?");
            preparedStatement.setString(1, faction.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            factionIsInDb = resultSet.next();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return factionIsInDb;
    }

    public void createFactionData(Faction faction) {
        if (this.mFactionRanking.getFactionsData().containsKey(faction)) {
            FactionData factiondata = this.factionFactionDataHashMap.get(faction);
            factiondata.setPoints(getPoints(faction));
            factiondata.update(new Timestamp((new Date()).getTime()));
            this.mFactionRanking.getFactionsData().remove(faction);
            this.mFactionRanking.getFactionsData().put(faction, factiondata);
        } else {
            FactionData factiondata = new FactionData(faction.getTag(), faction.getId(), getPoints(faction), new Timestamp((new Date()).getTime()));
            this.mFactionRanking.getFactionsData().put(faction, factiondata);
        }
    }

    public double getPoints(Faction faction) {
        double pointPerClaim, totalpoints = 0.0D;
        double pointPerKill = this.fileConfiguration.getDouble("points.stats.pointPerKill");
        double pointLostPerDeath = this.fileConfiguration.getDouble("points.stats.pointPerDeath");
        double pointPerMoney = this.fileConfiguration.getDouble("points.moneys.money");
        double moneypoints = this.fileConfiguration.getDouble("points.moneys.equalsTo");
        int claims = faction.getLandRounded();
        int totalkills = 0;
        int totaldeaths = 0;
        try {
            Connection connection = this.mFactionRanking.getDb().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT kills,deaths,money FROM " + this.playerDatabaseName + " WHERE factionId = ?");
            preparedStatement.setString(1, faction.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                totalkills += resultSet.getInt("kills");
                totaldeaths += resultSet.getInt("deaths");
                totalpoints += resultSet.getInt("money") / pointPerMoney * moneypoints;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (claims < 25) {
            pointPerClaim = this.fileConfiguration.getDouble("points.claims.pointPerClaim");
        } else if (claims > 25 && claims < 50) {
            pointPerClaim = this.fileConfiguration.getDouble("points.claims.pointAt25Claim");
        } else {
            pointPerClaim = this.fileConfiguration.getDouble("points.claims.pointAt50Claim");
        }
        return pointPerKill * totalkills - totaldeaths * pointLostPerDeath + pointPerClaim * claims + totalpoints;
    }

    public void updatePlayerData(Player player) {
        if (this.playerDataHashMap.containsKey(player)) {
            PlayerData playerData = this.playerDataHashMap.get(player);
            try {
                Connection connection = this.mFactionRanking.getDb().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + this.playerDatabaseName + " SET factionId = ?, kills = ?, deaths = ?, money = ? WHERE user = ? ");
                preparedStatement.setString(1, playerData.getFactionId());
                preparedStatement.setInt(2, playerData.getKills());
                preparedStatement.setInt(3, playerData.getDeaths());
                preparedStatement.setDouble(4, playerData.getPlayerMoney());
                preparedStatement.setString(5, player.getName());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateFaction(Faction faction) {
        try {
            Connection connection = this.mFactionRanking.getDb().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + this.factionDatabaseName + " SET factionName = ?, points = ? WHERE factionId = ? ");
            preparedStatement.setString(1, faction.getTag());
            preparedStatement.setDouble(2, getPoints(faction));
            preparedStatement.setString(3, faction.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (this.factionFactionDataHashMap.containsKey(faction)) {
            FactionData factionData = this.factionFactionDataHashMap.get(faction);
            factionData.setPoints(getPoints(faction));
            this.factionFactionDataHashMap.remove(faction);
            this.factionFactionDataHashMap.put(faction, factionData);
        }
    }

    public void addFactionPoints(Faction faction, int amount) {
        try {
            Connection connection = this.mFactionRanking.getDb().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + this.factionDatabaseName + " SET points = points + " + amount + " WHERE factionId = ? ");
            preparedStatement.setString(1, faction.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateFactionName(Faction faction, String factionTag) {
        try {
            Connection connection = this.mFactionRanking.getDb().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + this.factionDatabaseName + " SET factionName = ? WHERE factionId = ? ");
            preparedStatement.setString(1, factionTag);
            preparedStatement.setString(2, faction.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFactionPoints(Faction faction, int amount) {
        try {
            Connection connection = this.mFactionRanking.getDb().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + this.factionDatabaseName + " SET points = points - " + amount + " WHERE factionId = ? ");
            preparedStatement.setString(1, faction.getId());
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRankingList() {
        this.mFactionRanking.getRankingList().clear();
        try {
            Connection connection = this.mFactionRanking.getDb().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + this.factionDatabaseName + " ORDER BY points DESC LIMIT " + this.limit);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String factionName = resultSet.getString("factionName");
                int points = resultSet.getInt("points");
                this.mFactionRanking.getRankingList().put(factionName, Integer.valueOf(points));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.mFactionRanking.getServer().broadcastMessage(this.broadcastUpdate.replace("%prefix%", this.prefix));
    }
}
