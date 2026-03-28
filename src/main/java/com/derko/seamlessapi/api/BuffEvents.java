package com.derko.seamlessapi.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event hooks for buff lifecycle.
 *
 * <p>Other mods can subscribe to these to react to buff changes.
 * Subscribe on your mod's {@code IEventBus} or static handler class with {@code @SubscribeEvent}.
 *
 * <p>Example:
 * <pre>
 *   @SubscribeEvent
 *   public static void onBuffApplied(BuffAppliedEvent event) {
 *       ServerPlayer player = event.getPlayer();
 *       BuffData buff = event.getBuff();
 *       System.out.println(player.getName().getString() + " got " + buff.buffId());
 *   }
 * </pre>
 */
public final class BuffEvents {

    private BuffEvents() {}

    /**
     * Fired when a buff is successfully applied to a player.
     * Fired on server thread after buff is added to storage.
     */
    public static final class BuffAppliedEvent extends Event {
        private final ServerPlayer player;
        private final BuffData buff;

        public BuffAppliedEvent(ServerPlayer player, BuffData buff) {
            this.player = player;
            this.buff = buff;
        }

        public ServerPlayer getPlayer() { return player; }
        public BuffData getBuff() { return buff; }
    }

    /**
     * Fired when a buff is removed (either expired or via API call).
     * Fired on server thread after buff is removed from storage.
     */
    public static final class BuffRemovedEvent extends Event {
        private final ServerPlayer player;
        private final BuffData buff;
        private final RemovalReason reason;

        public enum RemovalReason {
            /** Buff duration expired naturally */
            EXPIRED,
            /** Player died and buffs were cleared */
            DEATH,
            /** API or command removed it */
            FORCED_REMOVAL,
            /** Slot capacity reached, buff was displaced */
            DISPLACED
        }

        public BuffRemovedEvent(ServerPlayer player, BuffData buff, RemovalReason reason) {
            this.player = player;
            this.buff = buff;
            this.reason = reason;
        }

        public ServerPlayer getPlayer() { return player; }
        public BuffData getBuff() { return buff; }
        public RemovalReason getReason() { return reason; }
    }

    /**
     * Fired before a buff would be applied, allowing cancellation.
     * Fired on server thread.
     * Call {@link #setCanceled(true)} to prevent the buff from being applied.
     */
    public static final class BuffApplyingEvent extends Event {
        private final ServerPlayer player;
        private final String foodSource;
        private final String primaryBuffId;
        private double magnitude;
        private double healthBonus;
        private boolean canceled = false;

        public BuffApplyingEvent(ServerPlayer player, String foodSource, String primaryBuffId,
                                 double magnitude, double healthBonus) {
            this.player = player;
            this.foodSource = foodSource;
            this.primaryBuffId = primaryBuffId;
            this.magnitude = magnitude;
            this.healthBonus = healthBonus;
        }

        public ServerPlayer getPlayer() { return player; }
        public String getFoodSource() { return foodSource; }
        public String getPrimaryBuffId() { return primaryBuffId; }
        public double getMagnitude() { return magnitude; }
        public double getHealthBonus() { return healthBonus; }

        /** Modify the magnitude (strength) of the buff about to be applied. */
        public void setMagnitude(double magnitude) { this.magnitude = magnitude; }

        /** Modify the health bonus of the buff about to be applied. */
        public void setHealthBonus(double healthBonus) { this.healthBonus = healthBonus; }

        public boolean isCanceled() { return canceled; }
        public void setCanceled(boolean canceled) { this.canceled = canceled; }
    }
}
