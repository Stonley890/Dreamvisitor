package io.github.stonley890.dreamvisitor.data;

import io.github.stonley890.dreamvisitor.Dreamvisitor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AltFamily implements ConfigurationSerializable {

    static final File file = new File(Dreamvisitor.getPlugin().getDataFolder().getPath() + "/alts.yml");

    public static void init() throws IOException {
        // If the file does not exist, create one
        if (!file.exists()) {
            Dreamvisitor.debug("alts.yml does not exist. Creating one now...");
            if (!file.createNewFile()) Bukkit.getLogger().warning("Unable to create alts.yml!");
        }
    }

    private static @NotNull YamlConfiguration getConfig() throws IOException, InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.load(file);
        return config;
    }

    private static @NotNull YamlConfiguration buildConfig(@NotNull List<AltFamily> altFamilyList) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("alts", altFamilyList);
        return config;
    }

    private static void saveToDisk(@NotNull YamlConfiguration config) throws IOException {
        Dreamvisitor.debug("Saving alts.yml...");
        config.save(file);
        Dreamvisitor.debug("Done!");
    }

    public static @NotNull List<Long> getChildren(long parentId) throws IOException, InvalidConfigurationException {
        List<AltFamily> altFamilyList = getAltFamilyList();
        for (AltFamily altFamily : altFamilyList) {
            if (altFamily.getParent() == parentId) return altFamily.getChildren();
        }
        if (getParent(parentId) == parentId) return new ArrayList<>();
        else throw new InvalidObjectException("This account is a child to another account!");
    }

    /**
     * Get the parent of a child. If no parent is found, the child ID will be returned as the parent.
     * @param childId the child ID to search for
     * @return the parent ID
     * @throws IOException if there is a disk error reading from the file.
     * @throws InvalidConfigurationException if the file cannot be parsed as YAML.
     */
    public static long getParent(long childId) throws IOException, InvalidConfigurationException {
        List<AltFamily> altFamilyList = getAltFamilyList();
        for (AltFamily altFamily : altFamilyList) {
            for (Long child : altFamily.getChildren()) {
                if (child == childId) return altFamily.getParent();
            }
        }
        return childId;
    }

    public static @NotNull AltFamily getFamily(long userId) throws IOException, InvalidConfigurationException {
        List<AltFamily> altFamilyList = AltFamily.getAltFamilyList();
        long recordedParent = getParent(userId);
        if (recordedParent != userId) {
            for (AltFamily altFamily : altFamilyList) if (altFamily.getParent() == recordedParent) return altFamily;
            throw new InvalidObjectException("Parent and child do not match!");
        } else {
            List<Long> recordedChildren = getChildren(userId);
            if (recordedChildren.isEmpty()) return new AltFamily(userId);
            else {
                for (AltFamily altFamily : altFamilyList) if (altFamily.getChildren().equals(recordedChildren)) return altFamily;
                throw new InvalidObjectException("Parent and child do not match!");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static @NotNull List<AltFamily> getAltFamilyList() throws IOException, InvalidConfigurationException {
        List<AltFamily> alts = (List<AltFamily>) getConfig().getList("alts");
        if (alts == null) alts = new ArrayList<>();
        return alts;
    }

    public static void updateFamily(AltFamily altFamily) throws IOException, InvalidConfigurationException {
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

    public static void setAlt(long parentId, long childId) throws IOException, InvalidConfigurationException {
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

    public static boolean checkValidity(long parent, List<Long> children) throws IOException, InvalidConfigurationException {
        return AltFamily.getChildren(parent).equals(children);
    }

    // non-static methods

    private final long parent;
    @NotNull private List<Long> children = new ArrayList<>();

    public AltFamily(long parentId) {
        parent = parentId;
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

    @SuppressWarnings("unchecked")
    public static @NotNull AltFamily deserialize(@NotNull Map<String, Object> objectMap) {
        long parent = (long) objectMap.get("parent");
        List<Long> children = (List<Long>) objectMap.get("children");
        if (children == null) children = new ArrayList<>();
        AltFamily altFamily = new AltFamily(parent);
        altFamily.setChildren(children);
        return altFamily;
    }
}
