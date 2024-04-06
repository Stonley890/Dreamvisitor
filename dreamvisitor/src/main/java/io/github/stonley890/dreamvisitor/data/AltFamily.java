package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AltFamily implements ConfigurationSerializable {

    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/alts.yml");
    private final long parent;
    @NotNull private List<Long> children = new ArrayList<>();

    public AltFamily(long parentId) {
        parent = parentId;
    }

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!file.exists()) {
            Dreamvisitor.debug("alts.yml does not exist. Creating one now...");
            if (!file.createNewFile()) Bukkit.getLogger().warning("Unable to create alts.yml!");
        }
    }

    private static @NotNull YamlConfiguration getConfig() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("alts.yml cannot be read! Does the server have read/write access? " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        } catch (InvalidConfigurationException e) {
            Bukkit.getLogger().severe("alts.yml is not a valid configuration! Is it formatted correctly? " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }
        return config;
    }

    private static @NotNull YamlConfiguration buildConfig(@NotNull List<AltFamily> altFamilyList) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("alts", altFamilyList);
        return config;
    }

    private static void saveToDisk(@NotNull YamlConfiguration config) {
        Dreamvisitor.debug("Saving alts.yml...");
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().severe("alts.yml cannot be written! Does the server have read/write access? " + e.getMessage() + "\nHere is the data that was not saved:\n" + config.saveToString());
            Bukkit.getPluginManager().disablePlugin(Dreamvisitor.getPlugin());
        }
        Dreamvisitor.debug("Done!");
    }

    /**
     * Get the children of a parent account.
     * @param parentId the ID of the user to get the child accounts of.
     * @return a {@link List<Long>} of child user IDs.
     * @throws NotChildException if the ID given is not a parent account.
     */
    public static @NotNull List<Long> getChildren(long parentId) throws NotChildException {
        List<AltFamily> altFamilyList = getAltFamilyList();
        for (AltFamily altFamily : altFamilyList) {
            if (altFamily.getParent() == parentId) return altFamily.getChildren();
        }
        if (getParent(parentId) == parentId) return new ArrayList<>();
        else throw new NotChildException();
    }

    /**
     * Get the parent of a child. If no parent is found, the child ID will be returned as the parent.
     * @param childId the child ID to search for
     * @return the parent ID
     */
    public static long getParent(long childId) {
        List<AltFamily> altFamilyList = getAltFamilyList();
        for (AltFamily altFamily : altFamilyList) {
            for (Long child : altFamily.getChildren()) {
                if (child == childId) return altFamily.getParent();
            }
        }
        return childId;
    }

    /**
     * Get the {@link AltFamily} of a given user ID. You can give either a parent or child account ID.
     * @param userId the ID of the user to search for.
     * @return the {@link AltFamily} connected to this account. It will have no children if the account has not been associated with any others.
     * @throws MismatchException if there is a mismatch with the parent and child of a family.
     */
    public static @NotNull AltFamily getFamily(long userId) throws MismatchException {
        List<AltFamily> altFamilyList = AltFamily.getAltFamilyList();
        long recordedParent = getParent(userId);
        if (recordedParent != userId) {
            for (AltFamily altFamily : altFamilyList) if (altFamily.getParent() == recordedParent) return altFamily;
            throw new MismatchException();
        } else {
            List<Long> recordedChildren = new ArrayList<>();
            try {
                recordedChildren = getChildren(userId);
            } catch (NotChildException ignored) {}
            if (recordedChildren.isEmpty()) return new AltFamily(userId);
            else {
                for (AltFamily altFamily : altFamilyList) if (altFamily.getChildren().equals(recordedChildren)) return altFamily;
                throw new MismatchException();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static @NotNull List<AltFamily> getAltFamilyList() {
        List<AltFamily> alts = (List<AltFamily>) getConfig().getList("alts");
        if (alts == null) alts = new ArrayList<>();
        return alts;
    }

    public static void updateFamily(AltFamily altFamily) {
        List<AltFamily> altFamilyList = getAltFamilyList();
        for (int i = 0; i < altFamilyList.size(); i++) {
            AltFamily family = altFamilyList.get(i);
            if (altFamily.parent == family.parent) {
                altFamilyList.set(i, altFamily);
                saveToDisk(buildConfig(altFamilyList));
                return;
            }
        }
        altFamilyList.add(altFamily);
        saveToDisk(buildConfig(altFamilyList));
    }

    public static void setAlt(long parentId, long childId) throws NotChildException {
        List<Long> children = getChildren(parentId);
        children.add(childId);
        AltFamily altFamily = new AltFamily(parentId);
        altFamily.setChildren(children);
        updateFamily(altFamily);

        List<Infraction> childInfractions = Infraction.getInfractions(childId);
        if (childInfractions.isEmpty()) return;
        List<Infraction> parentInfractions = Infraction.getInfractions(parentId);

        parentInfractions.addAll(childInfractions);
        childInfractions.clear();

        Infraction.setInfractions(childInfractions, childId);
        Infraction.setInfractions(parentInfractions, parentId);
    }

    @SuppressWarnings("unchecked")
    public static @NotNull AltFamily deserialize(@NotNull Map<String, Object> objectMap) {
        long parent = (long) objectMap.get("parent");
        List<Long> children = (List<Long>) objectMap.get("children");
        if (children == null) children = new ArrayList<>();
        AltFamily altFamily = new AltFamily(parent);
        altFamily.setChildren(children);
        return altFamily;
    }

    public long getParent() {
        return parent;
    }

    public @NotNull List<Long> getChildren() {
        return children;
    }

    public void setChildren(@NotNull List<Long> children) {
        this.children = children;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("parent", parent);
        objectMap.put("children", children);
        return objectMap;
    }

    public static class NotChildException extends Exception {
        public NotChildException() {}
    }

    public static class MismatchException extends Exception {
        public MismatchException() {}
    }
}
