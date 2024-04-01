package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Bot;
import io.github.stonley890.dreamvisitor.Dreamvisitor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class Infraction implements ConfigurationSerializable {

    public static final String actionBan = "ban"; // regualar MC temp-ban or ban & notify
    public static final String actionUserBan = "user_ban"; // notify only
    public static final String actionNoBan = "no_ban"; // no ban, no notify
    public static final String actionAllBan = "all_ban"; // ban from all
    public static final int BAN_POINT = 3;
    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/infractions.yml");

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!file.exists()) {
            Dreamvisitor.debug("infractions.yml does not exist. Creating one now...");
            if (!file.createNewFile()) Bukkit.getLogger().warning("Unable to create infractions.yml!");
        }
    }

    private static @NotNull YamlConfiguration getConfig() throws IOException, InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.load(file);
        return config;
    }

    private static void saveToDisk(@NotNull YamlConfiguration config) throws IOException {
        Dreamvisitor.debug("Saving infractions.yml...");
        config.save(file);
        Dreamvisitor.debug("Done!");
    }

    /**
     * Fetch the infractions of a member from disk.
     *
     * @param memberId the Discord Snowflake ID of the member whose infractions to fetch.
     * @return a non-null {@link List<Infraction>}
     * @throws IOException                   if there is an error reading from disk.
     * @throws InvalidConfigurationException if the file is not YAML-formatted.
     */
    @SuppressWarnings("unchecked")
    public static @NotNull List<Infraction> getInfractions(long memberId) throws IOException, InvalidConfigurationException {
        List<Infraction> infractions = (List<Infraction>) getConfig().getList(memberId + ".infractions");
        if (infractions == null) return new ArrayList<>();
        else return infractions;
    }

    /**
     * Get the value of all of a member's infractions.
     *
     * @param infractions  the infractions to count.
     * @param countExpired whether to count expired infractions.
     * @return the total value as a {@code byte}.
     */
    @Contract(pure = true)
    public static byte getInfractionCount(@NotNull List<Infraction> infractions, boolean countExpired) {
        byte count = 0;
        for (Infraction infraction : infractions) {
            if (countExpired || !infraction.isExpired()) count += infraction.value;
        }
        return count;
    }

    /**
     * Overwrite a member's infraction list with a new list and saves to disk.
     *
     * @param infractions the {@link List<Infraction>} to write.
     * @param memberId    the Discord Snowflake ID of the member to write to.
     * @throws IOException                   if read/write to disk fails.
     * @throws InvalidConfigurationException if the file is not YAML-formatted.
     */
    public static void setInfractions(List<Infraction> infractions, long memberId) throws IOException, InvalidConfigurationException {
        YamlConfiguration config = getConfig();
        config.set(memberId + ".infractions", infractions);
        saveToDisk(config);
    }

    public static void setTempban(long memberId, boolean state) throws IOException, InvalidConfigurationException {
        YamlConfiguration config = getConfig();
        config.set(memberId + ".tempban", state);
        saveToDisk(config);
    }

    public static boolean hasTempban(long memberId) throws IOException, InvalidConfigurationException {
        return getConfig().getBoolean(memberId + ".tempban");
    }

    public static byte getInfractionsUntilBan(long memberId) throws IOException, InvalidConfigurationException {
        return (byte) (BAN_POINT - getInfractionCount(getInfractions(memberId), false));
    }

    public static void execute(@NotNull Infraction infraction, @NotNull Member member, boolean silent, @NotNull String actionId) throws IOException, InvalidConfigurationException {

        if (!actionId.equals(actionBan) && !actionId.equals(actionAllBan) && !actionId.equals(actionNoBan) && !actionId.equals(actionUserBan))
            throw new InvalidObjectException("Action string does not match any valid actions!");

        byte infractionsUntilBan = getInfractionsUntilBan(member.getIdLong());

        boolean banPoint;
        banPoint = (infractionsUntilBan + infraction.value >= BAN_POINT);
        boolean hasTempban = hasTempban(member.getIdLong());
        List<Infraction> infractions = getInfractions(member.getIdLong());
        byte infractionCount = getInfractionCount(infractions, false);
        byte totalInfractionCount;
        if (!hasTempban) totalInfractionCount = infractionCount;
        else totalInfractionCount = (byte) (infractionCount + BAN_POINT);
        boolean notifyBan = (!actionId.equals(actionNoBan));
        boolean doBan = (actionId.equals(actionBan));
        boolean totalBan = (actionId.equals(actionAllBan));

        if (totalBan) {
            UUID uuid = AccountLink.getUuid(member.getIdLong());
            if (uuid != null) Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), bukkitTask -> {
                ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
                banList.addBan(Bukkit.createPlayerProfile(uuid), infraction.reason, (Date) null, "Dreamvisitor");
            });
            member.ban(0, infraction.reason).queue();
            return;
        }

        if (!silent) {
            JDA jda = Bot.getJda();
            Category category = jda.getCategoryById(Dreamvisitor.getPlugin().getConfig().getLong("infractions-category-id"));
            if (category == null) {
                throw new InvalidConfigurationException("Category of infractions-category-id is null!");
            }
            category.createTextChannel("infraction-" + member.getUser().getName()).queue(channel -> {

                channel.upsertPermissionOverride(member).setAllowed(Permission.VIEW_CHANNEL).queue();

                EmbedBuilder embed = new EmbedBuilder();

                StringBuilder description = new StringBuilder("You have recieved an infraction for the following reason:\n");
                description.append("**").append(infraction.reason).append("**\n\n");

                if (infraction.value == 0) description.append("This infraction does not count towards a ban.");
                else {
                    if (infraction.value == 1) description.append("This infraction brings your total count to ").append(totalInfractionCount).append(". ");
                    else description.append("This infraction is worth ").append(infraction.value).append(" warns as opposed to one, bringing your total to ").append(totalInfractionCount).append(". ");

                    if (banPoint) {
                        description.append("This infraction is your third warn within ")
                                .append(Dreamvisitor.getPlugin().getConfig().getInt("infraction-expire-time-days"))
                                .append(" days. ");
                        if (notifyBan) {
                            if (!hasTempban) {
                                if (doBan) description.append("You will be temporarily banned from the Minecraft server for two weeks. You cannot join until the two weeks has passed.");
                                else description.append("You will be temporarily banned from the Minecraft server. You cannot join until your temporary ban is over.");
                            } else description.append("You will be permanently banned from the Minecraft server. You cannot rejoin the Minecraft server.");
                        }
                    }
                }

                description.append("\n\nIf you want an explanation for this infraction, press the secondary button below and a staff member will provide more information. Press the primary button to dismiss this message.\n\n");

                if (!hasTempban) description.append("**You do not have a previous temp-ban. You will receive a temp-ban after ").append(infractionsUntilBan).append(" more infractions.**");
                else description.append("**You have previously been temp-banned. You will be permanently banned after ").append(infractionsUntilBan).append(" more infractions.**");

                embed.setTitle("Infraction Notice").setDescription(description).setFooter("See the #rules channel for more information about our rules system.").setColor(Color.getHSBColor(17, 100, 100));

                channel.sendMessage(member.getAsMention()).setEmbeds(embed.build()).queue();
            });
        }

        if (doBan) {
            UUID uuid = AccountLink.getUuid(member.getIdLong());
            if (uuid != null) Bukkit.getScheduler().runTask(Dreamvisitor.getPlugin(), bukkitTask -> {
                ProfileBanList banList = Bukkit.getBanList(BanList.Type.PROFILE);
                if (!hasTempban) banList.addBan(Bukkit.createPlayerProfile(uuid), infraction.reason, Instant.from(LocalDateTime.now().plusDays(7)), "Dreamvisitor");
                else banList.addBan(Bukkit.createPlayerProfile(uuid), infraction.reason, (Date) null, "Dreamvisitor");
            });
        }

        if (banPoint) {
            List<Infraction> disabledInfractions = new ArrayList<>();
            for (Infraction existingInfraction : infractions) {
                existingInfraction.expire();
                disabledInfractions.add(existingInfraction);
            }
            setInfractions(disabledInfractions, member.getIdLong());
            if (!hasTempban) setTempban(member.getIdLong(), true);
        }

        infraction.save(member.getIdLong());

    }

    private final byte value;
    private boolean expired = false;
    @Nullable
    private final String reason;
    @NotNull
    private final LocalDateTime time;

    /**
     * Creates and saves an infraction to disk.
     *
     * @param infractionValue  the value of the infraction.
     * @param infractionReason the reason for the infraction.
     */
    public Infraction(byte infractionValue, @Nullable String infractionReason, @NotNull LocalDateTime dateTime) {
        value = infractionValue;
        reason = infractionReason;
        time = dateTime;
    }

    // need to check for alts and make sure a child account cannot be warned

    /**
     * Save an infraction to a member and write to disk.
     *
     * @param memberId the Discord Snowflake ID of the member.
     * @throws IOException                   if read/write to disk fails.
     * @throws InvalidConfigurationException if the file is not YAML-formatted.
     */
    private void save(long memberId) throws IOException, InvalidConfigurationException {
        YamlConfiguration config = getConfig();
        List<Map<?, ?>> mapList = config.getMapList(memberId + ".infractions");
        mapList.add(serialize());
        config.set(memberId + ".infractions", mapList);
        saveToDisk(config);
    }

    public byte getValue() {
        return value;
    }

    public @Nullable String getReason() {
        return reason;
    }

    public @NotNull LocalDateTime getTime() {
        return time;
    }

    public boolean isExpired() {
        expireCheck();
        return expired;
    }

    public void expire() {
        expired = true;
    }

    private void expireCheck() {
        int expireTimeDays = Dreamvisitor.getPlugin().getConfig().getInt("infraction-expire-time-days");
        if (time.plusDays(expireTimeDays).isBefore(LocalDateTime.now())) expired = true;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("value", value);
        objectMap.put("reason", reason);
        objectMap.put("time", time);

        return objectMap;
    }

    @Contract("_ -> new")
    private static @NotNull Infraction deserialize(@NotNull Map<String, Object> map) {
        return new Infraction((Byte) map.get("value"), (String) map.get("reason"), (LocalDateTime) map.get("time"));
    }
}
