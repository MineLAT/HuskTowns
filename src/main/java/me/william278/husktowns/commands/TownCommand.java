package me.william278.husktowns.commands;

import me.william278.husktowns.HuskTowns;
import me.william278.husktowns.MessageManager;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.object.cache.PlayerCache;
import me.william278.husktowns.object.chunk.ClaimedChunk;
import me.william278.husktowns.object.town.Town;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TownCommand extends CommandBase {

    @Override
    protected void onCommand(Player player, Command command, String label, String[] args) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "create":
                case "found":
                    if (player.hasPermission("husktowns.create_town")) {
                        if (args.length == 2) {
                            String townName = args[1];
                            DataManager.createTown(player, townName);
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town create <name>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "deposit":
                    if (player.hasPermission("husktowns.command.town.deposit")) {
                        if (args.length == 2) {
                            try {
                                double amountToDeposit = Double.parseDouble(args[1]);
                                DataManager.depositMoney(player, amountToDeposit);
                            } catch (NumberFormatException ex) {
                                MessageManager.sendMessage(player, "error_invalid_amount");
                            }
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town deposit <amount>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "leave":
                    if (player.hasPermission("husktowns.command.town.leave")) {
                        DataManager.leaveTown(player);
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "rename":
                    if (!player.hasPermission("husktowns.command.town.rename")) {
                        MessageManager.sendMessage(player, "error_no_permission");
                        return;
                    }
                    if (args.length == 2) {
                        String townName = args[1];
                        DataManager.renameTown(player, townName);
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town rename <new name>");
                    }
                    break;
                case "setspawn":
                case "setwarp":
                    if (player.hasPermission("husktowns.command.town.spawn.set")) {
                        DataManager.updateTownSpawn(player);
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "spawn":
                case "warp":
                    if (player.hasPermission("husktowns.command.town.spawn")) {
                        if (args.length == 2) {
                            DataManager.teleportPlayerToOtherSpawn(player, args[1]);
                        } else {
                            DataManager.teleportPlayerToSpawn(player);
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "publicspawn":
                case "privatespawn":
                case "publicwarp":
                case "privatewarp":
                    if (player.hasPermission("husktowns.command.town.spawn.privacy")) {
                        DataManager.toggleTownPrivacy(player);
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "info":
                case "view":
                case "about":
                case "check":
                    if (args.length == 2) {
                        DataManager.sendTownInfoMenu(player, args[1]);
                    } else {
                        DataManager.sendTownInfoMenu(player);
                    }
                    break;
                case "greeting":
                    if (player.hasPermission("husktowns.command.town.greeting")) {
                        if (args.length >= 2) {
                            StringBuilder description = new StringBuilder();
                            for (int i = 2; i <= args.length; i++) {
                                description.append(args[i - 1]).append(" ");
                            }

                            DataManager.updateTownGreeting(player, description.toString().trim());
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town greeting <new message>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "farewell":
                    if (player.hasPermission("husktowns.command.town.farewell")) {
                        if (args.length >= 2) {
                            StringBuilder description = new StringBuilder();
                            for (int i = 2; i <= args.length; i++) {
                                description.append(args[i - 1]).append(" ");
                            }

                            DataManager.updateTownFarewell(player, description.toString().trim());
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town farewell <new message>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "bio":
                case "description":
                    if (player.hasPermission("husktowns.command.town.bio")) {
                        if (args.length >= 2) {
                            StringBuilder description = new StringBuilder();
                            for (int i = 2; i <= args.length; i++) {
                                description.append(args[i - 1]).append(" ");
                            }

                            DataManager.updateTownBio(player, description.toString().trim());
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town bio <new bio>");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "chat":
                    if (args.length >= 2) {
                        StringBuilder description = new StringBuilder();
                        for (int i = 2; i <= args.length; i++) {
                            description.append(args[i - 1]).append(" ");
                        }

                        player.performCommand("townchat " + description.toString().trim());
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town chat <message>");
                    }
                    break;
                case "list":
                    if (args.length == 2) {
                        player.performCommand("townlist " + args[1]);
                    } else {
                        player.performCommand("townlist");
                    }
                    break;
                case "kick":
                    if (args.length == 2) {
                        player.performCommand("evict " + args[1]);
                    } else {
                        player.performCommand("evict");
                    }
                    break;
                case "claims":
                case "autoclaim":
                case "claimlist":
                case "claimslist":
                case "invite":
                case "map":
                case "evict":
                case "promote":
                case "demote":
                case "trust":
                case "untrust":
                case "claim":
                case "unclaim":
                case "delclaim":
                case "abandonclaim":
                case "plot":
                case "farm":
                case "transfer":
                    StringBuilder commandArgs = new StringBuilder();
                    for (String arg : args) {
                        commandArgs.append(arg).append(" ");
                    }
                    player.performCommand(commandArgs.toString());
                    break;
                case "disband":
                case "delete":
                    if (args.length == 1) {
                        if (HuskTowns.getPlayerCache().isPlayerInTown(player.getUniqueId())) {
                            if (HuskTowns.getPlayerCache().getPlayerRole(player.getUniqueId()) == Town.TownRole.MAYOR) {
                                MessageManager.sendMessage(player, "disband_town_confirm");
                            } else {
                                MessageManager.sendMessage(player, "error_insufficient_disband_privileges");
                            }
                        } else {
                            MessageManager.sendMessage(player, "error_not_in_town");
                        }
                    } else if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("confirm")) {
                            DataManager.disbandTown(player);
                        } else {
                            MessageManager.sendMessage(player, "error_invalid_syntax", "/town disband [confirm]");
                        }
                    }
                    break;
                case "settings":
                case "config":
                case "flags":
                case "prefs":
                case "preferences":
                    if (player.hasPermission("husktowns.command.town.settings")) {
                        if (args.length == 2) {
                            DataManager.sendTownSettings(player, args[1]);
                        } else {
                            DataManager.sendTownSettings(player);
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_no_permission");
                    }
                    break;
                case "flag":
                    String townName = null;
                    ClaimedChunk.ChunkType chunkType;
                    String flagIdentifier;
                    boolean value;
                    boolean showSettingsMenu = false;
                    if (args.length == 6) {
                        showSettingsMenu = (args[5].equals("-settings"));
                    }
                    int argIndexer = 1;
                    if (args.length == 5 || args.length == 6) {
                        townName = args[argIndexer];
                        argIndexer++;
                    }
                    if (args.length >= 5 && args.length <= 6) {
                        try {
                            chunkType = ClaimedChunk.ChunkType.valueOf(args[argIndexer].toUpperCase());
                            argIndexer++;
                            flagIdentifier = args[argIndexer];
                            argIndexer++;
                            value = Boolean.parseBoolean(args[argIndexer]);
                            if (townName == null) {
                                DataManager.setTownFlag(player, chunkType, flagIdentifier, value, showSettingsMenu);
                            } else {
                                DataManager.setTownFlag(player, townName, chunkType, flagIdentifier, value, showSettingsMenu);
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            MessageManager.sendMessage(player, "error_invalid_chunk_type");
                        }
                    } else {
                        MessageManager.sendMessage(player, "error_invalid_syntax", "/town flag [town] <chunk_type> <flag> <value>");
                    }
                    break;
                case "help":
                    if (args.length == 2) {
                        try {
                            int pageNo = Integer.parseInt(args[1]);
                            HuskTownsCommand.showHelpMenu(player, pageNo);
                        } catch (NumberFormatException ex) {
                            MessageManager.sendMessage(player, "error_invalid_page_number");
                        }
                    } else {
                        HuskTownsCommand.showHelpMenu(player, 1);
                    }
                    return;
                default:
                    MessageManager.sendMessage(player, "error_invalid_syntax", command.getUsage());
                    break;
            }
        } else {
            DataManager.sendTownInfoMenu(player);
        }
    }

    public static class TownTab implements TabCompleter {

        final static String[] COMMAND_TAB_ARGS = {"create", "deposit", "leave", "rename", "setspawn", "publicspawn",
                "privatespawn", "spawn", "info", "greeting", "farewell", "kick", "claims", "invite", "promote", "settings",
                "demote",  "claim", "unclaim", "plot", "farm", "map", "transfer", "disband", "list", "help", "bio", "flag"};

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
            Player p = (Player) sender;
            if (command.getPermission() != null) {
                if (!p.hasPermission(command.getPermission())) {
                    return Collections.emptyList();
                }
            }
            switch (args.length) {
                case 1:
                    final List<String> tabCompletions = new ArrayList<>();
                    StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMAND_TAB_ARGS), tabCompletions);
                    Collections.sort(tabCompletions);
                    return tabCompletions;
                case 2:
                    final PlayerCache playerCache = HuskTowns.getPlayerCache();
                    if (!playerCache.hasLoaded()) {
                        return Collections.emptyList();
                    }
                    if (playerCache.getPlayerTown(p.getUniqueId()) == null) {
                        return Collections.emptyList();
                    }
                    switch (args[0].toLowerCase()) {
                        case "kick":
                        case "evict":
                        case "promote":
                        case "demote":
                        case "trust":
                        case "untrust":
                        case "transfer":
                            final List<String> playerListTabCom = new ArrayList<>();
                            HashSet<String> playersInTown = playerCache.getPlayersInTown(playerCache.getPlayerTown(p.getUniqueId()));
                            if (playersInTown.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], playersInTown, playerListTabCom);
                            Collections.sort(playerListTabCom);
                            return playerListTabCom;
                        case "info":
                        case "about":
                        case "view":
                        case "check":
                            final List<String> townListTabCom = new ArrayList<>();
                            if (playerCache.getTowns().isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], HuskTowns.getPlayerCache().getTowns(), townListTabCom);
                            Collections.sort(townListTabCom);
                            return townListTabCom;
                        case "warp":
                        case "spawn":
                            final List<String> publicTownSpawnTabList = new ArrayList<>();
                            if (playerCache.getTowns().isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], HuskTowns.getTownDataCache().getPublicSpawnTowns(), publicTownSpawnTabList);
                            Collections.sort(publicTownSpawnTabList);
                            return publicTownSpawnTabList;
                        case "invite":
                            final List<String> inviteTabCom = new ArrayList<>();
                            final ArrayList<String> players = new ArrayList<>();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                players.add(player.getName());
                            }
                            if (players.isEmpty()) {
                                return Collections.emptyList();
                            }
                            StringUtil.copyPartialMatches(args[1], players, inviteTabCom);
                            Collections.sort(inviteTabCom);
                            return inviteTabCom;
                        case "list":
                            final String[] townListCom = {"oldest", "newest", "name", "level"};
                            final List<String> townListCompletions = new ArrayList<>();
                            StringUtil.copyPartialMatches(args[1], Arrays.asList(townListCom), townListCompletions);
                            Collections.sort(townListCompletions);
                            return townListCompletions;
                        default:
                            return Collections.emptyList();
                    }
                default:
                    return Collections.emptyList();
            }

        }

    }
}