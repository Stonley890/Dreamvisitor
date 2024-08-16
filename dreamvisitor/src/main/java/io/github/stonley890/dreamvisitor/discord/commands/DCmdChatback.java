package io.github.stonley890.dreamvisitor.discord.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.ExecutableCommand;
import dev.jorel.commandapi.arguments.LongArgument;
import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.commands.DVCommand;
import io.github.stonley890.dreamvisitor.functions.Chatback;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DCmdChatback implements DVCommand {
    @NotNull
    @Override
    public ExecutableCommand<?, ?> getCommand() {
        return new CommandAPICommand("chatback")
                .withOptionalArguments(new LongArgument("messageID"))
                .executesPlayer((sender, args) -> {
                    Long messageID = (Long) args.get(0);
                    if (messageID == null) {
                        sender.sendMessage(ChatColor.GRAY + "Canceled chatback.");
                        Chatback.nextChatback.remove(sender);
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "Your next message will be a reply. Run /chatback to cancel.");
                        Bot.getGameChatChannel().retrieveMessageById(messageID).queue(message -> {
                            User author = message.getAuthor();
                            Bot.getGameChatChannel().getGuild().retrieveMemberById(author.getId()).queue(member -> {
                                Chatback.nextChatback.put(sender, new Chatback.ReplyMessage(
                                        member.getEffectiveName(),
                                        author.getName(),
                                        message.getContentRaw(),
                                        message.getIdLong()
                                ));
                            });
                        });
                    }
                });
    }
}
