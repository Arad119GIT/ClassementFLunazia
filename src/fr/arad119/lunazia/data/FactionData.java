package fr.arad119.lunazia.data;

import java.sql.Timestamp;

public class FactionData {
    private String factionName;

    private String factionUUID;

    private double points;

    private Timestamp lastUpdatedDate;

    public FactionData(String factionName, String factionUUID, double points, Timestamp lastUpdatedDate) {
        this.factionName = factionName;
        this.factionUUID = factionUUID;
        this.points = points;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getFactionName() {
        return this.factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName;
    }

    public String getFactionUUID() {
        return this.factionUUID;
    }

    public void setFactionUUID(String factionUUID) {
        this.factionUUID = factionUUID;
    }

    public double getPoints() {
        return this.points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public Timestamp getLastUpdatedDate() {
        return this.lastUpdatedDate;
    }

    public void update(Timestamp timestamp) {
        this.lastUpdatedDate = timestamp;
    }
}
