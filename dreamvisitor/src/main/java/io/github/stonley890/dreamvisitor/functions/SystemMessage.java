package io.github.stonley890.dreamvisitor.functions;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SystemMessage {

    private static final String PREFIX = ChatColor.DARK_BLUE + "✧ ";

    /**
     * Format a message as a system message
     * @param string the message to format
     * @return the formatted message
     */
    @NotNull
    @Contract(pure = true)
    public static String formatPrivate(String string) {
        return PREFIX + Type.INFO.color + string;
    }

    /**
     * Format a message as a system message
     * @param string the message to format
     * @param type the {@link Type} of message this is
     * @return the formatted message
     */
    @NotNull
    @Contract(pure = true)
    public static String formatPrivate(String string, @NotNull Type type) {
        return PREFIX + type.color + string;
    }

    /**
     * Format a message as a system message
     * @param baseComponents the message to format
     * @return the formatted message
     */
    public static BaseComponent[] formatPrivateComponents(BaseComponent[] baseComponents) {
        ComponentBuilder builder = new ComponentBuilder(PREFIX);
        builder.append(baseComponents).color(Type.INFO.color);
        return builder.create();
    }

    /**
     * Format a message as a system message
     * @param baseComponents the message to format
     * @param type the {@link Type} of message this is
     * @return the formatted message
     */
    public static BaseComponent[] formatPrivateComponents(BaseComponent[] baseComponents, @NotNull Type type) {
        ComponentBuilder builder = new ComponentBuilder(PREFIX);
        builder.append(baseComponents).color(type.color);
        return builder.create();
    }

    /**
     * Format a message as a system message
     * @param string the message to format
     * @return the formatted message
     */
    public static BaseComponent[] formatPrivateComponents(String string) {
        ComponentBuilder builder = new ComponentBuilder(PREFIX);
        builder.append(string);
        return builder.create();
    }

    /**
     * Format a message as a system message
     * @param string the message to format
     * @param type the {@link Type} of message this is
     * @return the formatted message
     */
    public static BaseComponent[] formatPrivateComponents(String string, @NotNull Type type) {
        ComponentBuilder builder = new ComponentBuilder(PREFIX);
        builder.append(string).color(type.color);
        return builder.create();
    }

    /**
     * Format a message as a system message
     * @param string the message to format
     * @return the formatted message
     */
    @NotNull
    @Contract(pure = true)
    public static String formatPublic(String string) {
        return PREFIX + string;
    }

    @NotNull
    @Contract(pure = true)
    public static String sIfPlural(Number number) {
        if (Objects.equals(number, 1)) return "";
        else return "s";
    }

    @NotNull
    @Contract(pure = true)
    public static String iesIfPlural(Number number) {
        if (Objects.equals(number, 1)) return "y";
        else return "ies";
    }

    public enum Type {
        INFO,
        WARNING,
        DANGER,
        ERROR;

        public ChatColor color;

        static {
            INFO.color = ChatColor.of("#ccccff");
            WARNING.color = ChatColor.of("#fbd17a");
            DANGER.color = ChatColor.of("#ff5b2b");
            ERROR.color = ChatColor.of("ff2d00");
        }
    }

}
