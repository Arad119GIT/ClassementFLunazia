package fr.arad119.lunazia.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.configuration.file.FileConfiguration;

public class Database {
    private Connection connection;

    private FileConfiguration fileConfiguration;

    public Database(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public void connect() throws SQLException {
        String url = "jdbc:" + getEngine() + "://" + getHost() + ":" + getPort() + "/" + getDatabaseName() + "?autoReconnect=true";
        this.connection = DriverManager.getConnection(url, getUsername(), getPassword());
        String createPlayerTable =
                "CREATE TABLE IF NOT EXISTS " + getPlayerTableName() +
                        " (id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT, " +
                        "user VARCHAR(255) NOT NULL, " +
                        "factionId VARCHAR(255) NOT NULL, " +
                        "money INT(11) DEFAULT 0, " +
                        "kills INT(11) DEFAULT 0, " +
                        "deaths INT(11) DEFAULT 0)";
        String createFactionTable =
                "CREATE TABLE IF NOT EXISTS " + getFactionTableName() +
                        " (id INT(11) PRIMARY KEY NOT NULL AUTO_INCREMENT, " +
                        "factionName VARCHAR(255) NOT NULL, " +
                        "factionId VARCHAR(255) NOT NULL, " +
                        "points INT(11) DEFAULT 0, " +
                        "updatedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        Statement statement = this.connection.createStatement();
        statement.execute(createFactionTable);
        statement.execute(createPlayerTable);
        statement.close();
    }

    public void disconnect() {
        if (isConnected())
            try {
                this.connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public boolean isConnected() {
        return (this.connection != null);
    }

    public Connection getConnection() {
        return this.connection;
    }

    private int getPort() {
        return this.fileConfiguration.getInt("database.port");
    }

    private String getHost() {
        return this.fileConfiguration.getString("database.host");
    }

    public String getPlayerTableName() {
        return this.fileConfiguration.getString("database.playerTableName");
    }

    public String getFactionTableName() {
        return this.fileConfiguration.getString("database.factionTableName");
    }

    private String getPassword() {
        return this.fileConfiguration.getString("database.password");
    }

    private String getUsername() {
        return this.fileConfiguration.getString("database.username");
    }

    private String getEngine() {
        return this.fileConfiguration.getString("database.engine");
    }

    private String getDatabaseName() {
        return this.fileConfiguration.getString("database.database");
    }
}
