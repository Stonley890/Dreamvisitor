package io.github.stonley890.dreamvisitor.commands;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.ExecutableCommand;
import io.github.stonley890.dreamvisitor.functions.ItemBanList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdItemBanList implements DVCommand {

    @NotNull
    @Override
    public ExecutableCommand<?, ?> getCommand() {
        return new CommandAPICommand("itembanlist")
                .withPermission(CommandPermission.fromString("dreamvisitor.itembanlist"))
                .withHelp("Manage the item ban list.", "Open the item ban list inventory GUI.")
                .executesNative(((sender, args) -> {
                    if (sender instanceof Player player) {
                        if (ItemBanList.badItems != null) {
                            ItemBanList.inv.setContents(ItemBanList.badItems);
                        }
                        player.openInventory(ItemBanList.inv);
                    } else throw CommandAPI.failWithString("This can only be run by players!");
                }));
    }
}
