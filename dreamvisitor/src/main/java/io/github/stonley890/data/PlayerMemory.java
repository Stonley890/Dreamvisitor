package io.github.stonley890.data;

public class PlayerMemory {
    private boolean discordToggled;
    private boolean vanished;

    public void setDiscordToggled(boolean state) {
        discordToggled = state;
    }
    public boolean isDiscordToggled() {
        return discordToggled;
    }

    public void setVanished(boolean state) {
        vanished = state;
    }
    public boolean isVanished() {
        return vanished;
    }
}
