package io.github.stonley890.dreamvisitor.discord.commands;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import io.github.stonley890.dreamvisitor.functions.Whitelist;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class DCmdToggleweb implements DiscordCommand {
    @Override
    public @NotNull SlashCommandData getCommandData() {
        return Commands.slash("toggleweb", "Toggle the web whitelist system on or off.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);
    }

    @Override
    public void onCommand(@NotNull SlashCommandInteractionEvent event) {
        if (!Dreamvisitor.webWhitelistEnabled) {
            Whitelist.startWeb(Dreamvisitor.getPlugin().getConfig().getInt("whitelistPort"));
            Dreamvisitor.webWhitelistEnabled = true;
            event.reply("Web whitelist enabled.").queue();
        } else {
            Whitelist.stopWeb();
            Dreamvisitor.webWhitelistEnabled = false;
            event.reply("Web whitelist disabled.").queue();
        }
        Dreamvisitor.getPlugin().saveConfig();
    }
}
