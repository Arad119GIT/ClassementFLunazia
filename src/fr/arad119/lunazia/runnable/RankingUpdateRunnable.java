package fr.arad119.lunazia.runnable;

import fr.arad119.lunazia.MFactionRanking;
import org.bukkit.plugin.Plugin;

public class RankingUpdateRunnable implements Runnable {
    private MFactionRanking mFactionRanking;

    private int timer;

    public RankingUpdateRunnable(MFactionRanking mFactionRanking) {
        this.mFactionRanking = mFactionRanking;
        this.timer = mFactionRanking.getConfig().getInt("timer");
    }

    public void run() {
        this.mFactionRanking.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.mFactionRanking, () -> this.mFactionRanking.getDatabaseManager().updateRankingList(), (20 * this.timer), (20 * this.timer));
    }
}
