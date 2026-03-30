package com.derko.seamlessapi.api;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    private static final List<Consumer<BuffAppliedEvent>> APPLIED_LISTENERS = new ArrayList<>();
    private static final List<Consumer<BuffRemovedEvent>> REMOVED_LISTENERS = new ArrayList<>();
    private static final List<Consumer<BuffApplyingEvent>> APPLYING_LISTENERS = new ArrayList<>();

    private BuffEvents() {}

    /** Register a listener that fires after a buff is applied. */
    public static void onBuffApplied(Consumer<BuffAppliedEvent> listener) {
        APPLIED_LISTENERS.add(listener);
    }

    /** Register a listener that fires after a buff is removed. */
    public static void onBuffRemoved(Consumer<BuffRemovedEvent> listener) {
        REMOVED_LISTENERS.add(listener);
    }

    /** Register a listener that fires before a buff is applied. */
    public static void onBuffApplying(Consumer<BuffApplyingEvent> listener) {
        APPLYING_LISTENERS.add(listener);
    }

    /** Internal fire helper for applied events. */
    public static void fireBuffApplied(ServerPlayerEntity player, BuffData buff) {
        if (APPLIED_LISTENERS.isEmpty()) {
            return;
        }
        BuffAppliedEvent event = new BuffAppliedEvent(player, buff);
        for (Consumer<BuffAppliedEvent> listener : APPLIED_LISTENERS) {
            listener.accept(event);
        }
    }

    /** Internal fire helper for removed events. */
    public static void fireBuffRemoved(ServerPlayerEntity player, BuffData buff, BuffRemovedEvent.RemovalReason reason) {
        if (REMOVED_LISTENERS.isEmpty()) {
            return;
        }
        BuffRemovedEvent event = new BuffRemovedEvent(player, buff, reason);
        for (Consumer<BuffRemovedEvent> listener : REMOVED_LISTENERS) {
            listener.accept(event);
        }
    }

    /** Internal fire helper for pre-apply events. */
    public static BuffApplyingEvent fireBuffApplying(ServerPlayerEntity player, String foodSource,
                                                     String primaryBuffId, double magnitude, double healthBonus) {
        BuffApplyingEvent event = new BuffApplyingEvent(player, foodSource, primaryBuffId, magnitude, healthBonus);
        for (Consumer<BuffApplyingEvent> listener : APPLYING_LISTENERS) {
            listener.accept(event);
        }
        return event;
    }

    /**
     * Fired when a buff is successfully applied to a player.
     * Fired on server thread after buff is added to storage.
     */
    public static final class BuffAppliedEvent {
        private final ServerPlayerEntity player;
        private final BuffData buff;

        public BuffAppliedEvent(ServerPlayerEntity player, BuffData buff) {
            this.player = player;
            this.buff = buff;
        }

        public ServerPlayerEntity getPlayer() { return player; }
        public BuffData getBuff() { return buff; }
    }

    /**
     * Fired when a buff is removed (either expired or via API call).
     * Fired on server thread after buff is removed from storage.
     */
    public static final class BuffRemovedEvent {
        private final ServerPlayerEntity player;
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

        public BuffRemovedEvent(ServerPlayerEntity player, BuffData buff, RemovalReason reason) {
            this.player = player;
            this.buff = buff;
            this.reason = reason;
        }

        public ServerPlayerEntity getPlayer() { return player; }
        public BuffData getBuff() { return buff; }
        public RemovalReason getReason() { return reason; }
    }

    /**
     * Fired before a buff would be applied, allowing cancellation.
     * Fired on server thread.
     * Call {@link #setCanceled(true)} to prevent the buff from being applied.
     */
    public static final class BuffApplyingEvent {
        private final ServerPlayerEntity player;
        private final String foodSource;
        private final String primaryBuffId;
        private double magnitude;
        private double healthBonus;
        private boolean canceled = false;

        public BuffApplyingEvent(ServerPlayerEntity player, String foodSource, String primaryBuffId,
                                 double magnitude, double healthBonus) {
            this.player = player;
            this.foodSource = foodSource;
            this.primaryBuffId = primaryBuffId;
            this.magnitude = magnitude;
            this.healthBonus = healthBonus;
        }

        public ServerPlayerEntity getPlayer() { return player; }
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
