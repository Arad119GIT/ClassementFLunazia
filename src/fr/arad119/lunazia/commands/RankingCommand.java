package fr.arad119.lunazia.commands;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import fr.arad119.lunazia.MFactionRanking;
import java.util.ArrayList;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class RankingCommand implements CommandExecutor {
    private MFactionRanking mFactionRanking;

    private String prefix;

    private String topMessage;

    private String rankingMessage;

    private String bottomMessage;

    private String errorCommandMessage;

    private String rankingPermission;

    private String helpPermission;

    private String addPointsPermission;

    private String removePointsPermission;

    private String reloadPermission;

    private String addPointsMessage;

    private String removePointsMessage;

    private ArrayList<String> helpMessage = new ArrayList<>();

    public RankingCommand(MFactionRanking mFactionRanking) {
        this.mFactionRanking = mFactionRanking;
        FileConfiguration fileConfiguration = mFactionRanking.getConfig();
        this.prefix = ChatColor.translateAlternateColorCodes('&', fileConfiguration.getString("messages.prefix"));
        this.topMessage = ChatColor.translateAlternateColorCodes('&', fileConfiguration.getString("messages.top_message"));
        this.rankingMessage = ChatColor.translateAlternateColorCodes('&', fileConfiguration.getString("messages.ranking_message"));
        this.bottomMessage = ChatColor.translateAlternateColorCodes('&', fileConfiguration.getString("messages.bottom_message"));
        this.errorCommandMessage = ChatColor.translateAlternateColorCodes('&', fileConfiguration.getString("messages.error_command"));
        this.addPointsMessage = ChatColor.translateAlternateColorCodes('&', fileConfiguration.getString("messages.add_points_messages"));
        this.removePointsMessage = ChatColor.translateAlternateColorCodes('&', fileConfiguration.getString("messages.remove_points_messages"));
        this.rankingPermission = fileConfiguration.getString("permissions.classement");
        this.helpPermission = fileConfiguration.getString("permissions.help");
        this.addPointsPermission = fileConfiguration.getString("permissions.add_points");
        this.removePointsPermission = fileConfiguration.getString("permissions.remove_points");
        this.reloadPermission = fileConfiguration.getString("permissions.reload");
        fileConfiguration.getStringList("messages.help_message").forEach(line -> {

        });
    }

    public boolean onCommand(CommandSender commandSender, Command command, String arg, String[] args) {
        if (args.length == 0) {
            if (commandSender.hasPermission(this.rankingPermission)) {
                commandSender.sendMessage(this.topMessage);
                this.mFactionRanking.getRankingList().keySet().forEach(factionName -> {
                    Integer points = (Integer)this.mFactionRanking.getRankingList().get(factionName);
                    paramCommandSender.sendMessage(this.rankingMessage.replace("%faction%", factionName).replace("%points%", String.valueOf(points)));
                });
                commandSender.sendMessage(this.bottomMessage);
            }
        } else {
            String str;
            switch ((str = args[0]).hashCode()) {
                case -934641255:
                    if (!str.equals("reload"))
                        break;
                    if (commandSender.hasPermission(this.reloadPermission))
                        this.mFactionRanking.getDatabaseManager().updateRankingList();
                    return false;
                case -28056985:
                    if (!str.equals("removepoints"))
                        break;
                    if (args.length != 3) {
                        if (commandSender.hasPermission(this.helpPermission)) {
                            commandSender.sendMessage(this.topMessage);
                            this.helpMessage.forEach(commandSender::sendMessage);
                            commandSender.sendMessage(this.bottomMessage);
                        }
                    } else if (commandSender.hasPermission(this.removePointsPermission) && args[1] != null && args[2] != null) {
                        if (NumberUtils.isNumber(args[2])) {
                            if (this.mFactionRanking.getServer().getPlayer(args[1]) == null) {
                                commandSender.sendMessage("Le joueur n'est pas connecte ou n'existe pas");
                                return false;
                            }
                            FPlayer faction = FPlayers.getInstance().getByPlayer(this.mFactionRanking.getServer().getPlayer(args[1]));
                            this.mFactionRanking.getDatabaseManager().removeFactionPoints(faction.getFaction(), Integer.parseInt(args[2]));
                            commandSender.sendMessage(this.removePointsMessage.replace("%prefix%", this.prefix).replace("%amount%", args[2]).replace("%faction%", faction.getFaction().getTag()));
                        } else {
                            commandSender.sendMessage(this.errorCommandMessage);
                        }
                    }
                    return false;
                case 826218692:
                    if (!str.equals("addpoints"))
                        break;
                    if (args.length != 3) {
                        if (commandSender.hasPermission(this.helpPermission)) {
                            commandSender.sendMessage(this.topMessage);
                            this.helpMessage.forEach(commandSender::sendMessage);
                            commandSender.sendMessage(this.bottomMessage);
                        }
                    } else if (commandSender.hasPermission(this.addPointsPermission) && args[1] != null && args[2] != null) {
                        if (NumberUtils.isNumber(args[2])) {
                            if (this.mFactionRanking.getServer().getPlayer(args[1]) == null) {
                                commandSender.sendMessage("Le joueur n'est pas connecte ou n'existe pas");
                                return false;
                            }
                            FPlayer faction = FPlayers.getInstance().getByPlayer(this.mFactionRanking.getServer().getPlayer(args[1]));
                            this.mFactionRanking.getDatabaseManager().addFactionPoints(faction.getFaction(), Integer.parseInt(args[2]));
                            commandSender.sendMessage(this.addPointsMessage.replace("%prefix%", this.prefix).replace("%amount%", args[2]).replace("%faction%", faction.getFaction().getTag()));
                        } else {
                            commandSender.sendMessage(this.errorCommandMessage);
                        }
                    }
                    return false;
            }
            if (commandSender.hasPermission(this.helpPermission)) {
                commandSender.sendMessage(this.topMessage);
                this.helpMessage.forEach(commandSender::sendMessage);
                commandSender.sendMessage(this.bottomMessage);
            }
        }
        return false;
    }
}
