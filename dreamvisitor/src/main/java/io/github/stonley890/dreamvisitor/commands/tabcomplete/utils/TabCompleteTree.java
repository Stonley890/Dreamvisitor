package io.github.stonley890.dreamvisitor.commands.tabcomplete.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TabCompleteTree {

    @NotNull
    private Map<String, TabCompleteTree> treeMap;

    public TabCompleteTree(@NotNull String arg) {
        treeMap = new HashMap<>();
        treeMap.put(arg, null);
    }

    public TabCompleteTree getChildren(String arg) {
        return treeMap.get(arg);
    }

    public Set<String> getNextSuggestions() {
        return treeMap.keySet();
    }

    /**
     * Add suggestions to this branch,
     * @param arg the suggestions to add.
     * @return The modified {@link TabCompleteTree}.
     */
    public TabCompleteTree addBranch(@NotNull String... arg) {
        for (String s : arg) {
            treeMap.put(s, null);
        }
        return this;
    }

    public TabCompleteTree addBranch(String suggestion, TabCompleteTree branch) {
        treeMap.put()
    }

}
