package me.william278.husktowns.commands.subcommands.town;

import me.william278.husktowns.MessageManager;
import me.william278.husktowns.commands.subcommands.TownSubCommand;
import me.william278.husktowns.data.DataManager;
import me.william278.husktowns.town.TownRole;
import org.bukkit.entity.Player;

public class TownRenameSubCommand extends TownSubCommand {

    public TownRenameSubCommand() {
        super("rename", "husktowns.command.town.rename", "", TownRole.MAYOR, "error_insufficient_rename_privileges");
    }

    @Override
    public void onExecute(Player player, String[] args) {
        if (args.length == 1) {
            final String townName = args[0];
            DataManager.renameTown(player, townName);
        } else {
            MessageManager.sendMessage(player, "error_invalid_syntax", getUsage());
        }
    }
}
