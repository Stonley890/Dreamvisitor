package io.github.stonley890.dreamvisitor.functions;

import io.github.stonley890.dreamvisitor.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;

public class Battlewinner {

    static EnderDragon dragon = null;
    static BukkitTask battlewinnerTickingTask = null;
    static float arenaRadius = 16.0f;
    static Phase currentPhase = Phase.NO_ATTACK;
    static int ticksUntilNextPhase = 20*20;

    static final int ticksBetweenFireballs = 20;
    static int ticksUntilNextFireball = ticksBetweenFireballs;

    static final int ticksCloudSpinDuration =  60;
    static int ticksUntilCloudSpinEnd = ticksCloudSpinDuration;

    static final int ticksSpinBeforeExplosion = 2*20;
    static int ticksUntilExplosion = ticksSpinBeforeExplosion;
    static final int ticksSpinAfterExplosion = 2*20;
    static int ticksUntilExplosionSpinEnd = ticksSpinAfterExplosion;

    enum Phase {
        NO_ATTACK,
        FIREBALL,
        CLOUD,
        EXPLOSION,
        BIG_FIREBALL,
        FLY_AROUND,
        LAVA_SPLASH
    }

    public static void spawn(@NotNull Location spawnLocation, float arenaRadius) {

        dragon = Objects.requireNonNull(spawnLocation.getWorld()).spawn(spawnLocation, EnderDragon.class);
        dragon.setPhase(EnderDragon.Phase.HOVER);
        spawnLocation.getWorld().playSound(dragon.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 1, 1);

        battlewinnerTickingTask = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, 0, 1);

    }

    public static void kill() {
        dragon.remove();
        battlewinnerTickingTask.cancel();
    }

    private static void tick() {

        if (currentPhase == Phase.FIREBALL) fireballTick();
        else if (currentPhase == Phase.BIG_FIREBALL) bigFireballTick();
        else if (currentPhase == Phase.CLOUD) cloudTick();
        else if (currentPhase == Phase.EXPLOSION) explosionTick();

    }

    private static void fireballTick() {

        Player nearestPlayer = (Player) Bukkit.selectEntities(dragon, "@p").get(0);
        Vector3f rotation = lookAtTarget(dragon.getLocation().toVector().toVector3f(), nearestPlayer.getLocation().toVector().toVector3f());
        dragon.setRotation(rotation.z, rotation.y);
        if (ticksUntilNextFireball > 0) ticksUntilNextFireball--;
        else {
            dragon.launchProjectile(LargeFireball.class);
            ticksUntilNextFireball = ticksBetweenFireballs;
        }
    }

    private static void fireballStart() {
        dragon.setPhase(EnderDragon.Phase.HOVER);
        ticksUntilNextFireball = ticksBetweenFireballs;
    }

    private static void bigFireballTick() {
        if (ticksUntilNextFireball > 0) ticksUntilNextFireball--;
        else {
            dragon.launchProjectile(DragonFireball.class);
            ticksUntilNextFireball = ticksBetweenFireballs;
        }
    }
    
    private static void bigFireballStart() {
        dragon.setPhase(EnderDragon.Phase.HOVER);
        ticksUntilNextFireball = ticksBetweenFireballs;
    }

    private static void cloudTick() {
        if (ticksUntilCloudSpinEnd > 0) ticksUntilCloudSpinEnd--;
        else {
            dragon.setRotation(dragon.getLocation().getYaw() + 2, dragon.getLocation().getPitch());
        }
    }

    private static void cloudStart() {
        dragon.setPhase(EnderDragon.Phase.HOVER);
        ticksUntilNextFireball = ticksBetweenFireballs;
        Objects.requireNonNull(dragon.getLocation().getWorld()).playSound(dragon.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
        
    }

    private static void explosionTick() {
        if (ticksUntilExplosion > 0) ticksUntilExplosion--;
        else if (ticksUntilExplosionSpinEnd > 0) ticksUntilExplosionSpinEnd--;
    }


    @Contract("_, _ -> new")
    public static @NotNull Vector3f lookAtTarget(Vector3f centerLoc, Vector3f targetLoc) {
        // Difference between current location and target location
        Vector3f direction = new Vector3f();
        direction.sub(centerLoc, targetLoc);

        // Calculate the yaw (rotation around Y-axis)
        float yaw = (float) Math.atan2(direction.x, direction.z); // in radians
        yaw = (float) Math.toDegrees(yaw); // convert to degrees
        if (direction.x < 0) {
            yaw = -yaw; // If we're facing the opposite direction, negate the yaw value
        }

        // Calculate the pitch (rotation around X-axis)
        float pitch = 0;
        if (Math.abs(direction.y) > Math.min(Math.abs(direction.x), Math.abs(direction.z))) {
            pitch = (float) ((float) Math.atan(direction.y / Math.sqrt(Math.pow(direction.x, 2) + Math.pow(direction.z, 2))) * (2 * Math.PI));
            pitch = (float) Math.toDegrees(pitch); // convert to degrees
        }

        return new Vector3f(0, pitch, yaw);
    }

}
