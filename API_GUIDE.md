# Seamless-API Documentation

A lightweight, extensible NeoForge 21.0+ API for registering custom food buffs with the Advanced Food System. Enables any mod to add dynamic, configurable food effects—walk speed boosts, damage reduction, regeneration, and more.

## Quick Start

### Step 1: Add Dependency

In your mod's `build.gradle`:

```gradle
repositories {
    mavenLocal()  // Or wherever you publish Seamless-API
}

dependencies {
    modCompileOnly 'com.derko:seamlessapi:1.0'
    modRuntimeOnly 'com.derko:seamlessapi:1.0'
}
```

### Step 2: Register a Food

In your mod's constructor or `FMLCommonSetupEvent`:

```java
package com.example.mymod;

import com.derko.seamlessapi.SatiationAPI;
import com.derko.seamlessapi.api.FoodBuffRegistration;

public class MyModFoods {
    public static void register() {
        // Spicy Pepper: +attack speed, +damage, 5 minutes duration
        SatiationAPI.registerFood(
            "mymod:spicy_pepper",
            FoodBuffRegistration.builder()
                .buff("attack_speed")
                .buff("damage_reduction")
                .duration(300)      // seconds
                .magnitude(0.25)    // effect strength
                .hearts(1.0)        // health bonus in hearts
                .build()
        );
        
        // Cooling Melon: regeneration, 10 minutes
        SatiationAPI.registerFood(
            "mymod:cool_melon",
            FoodBuffRegistration.builder()
                .buff("regeneration")
                .duration(600)
                .magnitude(0.15)
                .hearts(0.5)
                .build()
        );
    }
}
```

Then call `MyModFoods.register()` early in your mod initialization.

## API Reference

### Core Registration

#### `SatiationAPI.registerFood(String itemId, FoodBuffRegistration registration)`

Registers a custom food with the Advanced Food System.

- **itemId**: Full registry ID, e.g. `"mymod:item_name"`
- **registration**: Built via `FoodBuffRegistration.builder()`
- **When to call**: Mod constructor or `FMLCommonSetupEvent`
- **Throws**: `IllegalStateException` if called after game load completes

#### `FoodBuffRegistration.builder()`

Fluent builder for food buff configurations:

```java
FoodBuffRegistration.builder()
    .buff(String buffId)              // Add a buff type (call multiple times for multi-buff foods)
    .duration(int seconds)            // Buff duration in seconds (default: 1200)
    .magnitude(double strength)       // Effect strength 0-1+ (default: 0.20)
    .hearts(double bonus)             // Health bonus in half-hearts (default: 0.5)
    .build()
```

#### Available Buff Types

| Buff ID | Effect | Notes |
|---------|--------|-------|
| `walk_speed` | ↑ Movement speed | Synced walk/strafe speed |
| `attack_speed` | ↑ Attack speed | Punch rate increase |
| `mining_speed` | ↑ Block break speed | Client-side only |
| `damage_reduction` | ↓ Damage taken | Capped at 75% reduction |
| `regeneration` | ⚕️ Health regen | ~0.5 health/sec at magnitude 0.20 |
| `saturation_boost` | 🍗 Saturation | Instant saturation on consume |
| `knockback_resistance` | ⛓️ Knockback reduce | Stun reduction |
| `hunger_efficiency` | 🍖 Hunger loss slower | Reduces practical hunger drain |

All buff effects are:
- **Server-side calculated** (authority on server)
- **Configurable by game admin** (via in-game config menu or JSON)
- **Stackable** (multiple foods with same buff ID accumulate)
- **Multiplayer-safe** (persistent across save/load, proper network sync)

### Querying Active Buffs

#### `BuffQueryAPI.getAllBuffs(ServerPlayer player)`

Returns all active buffs for a player.

```java
import com.derko.seamlessapi.api.BuffQueryAPI;
import com.derko.seamlessapi.api.BuffData;

@SubscribeEvent
public static void onPlayerTick(PlayerTickEvent.Post event) {
    if (event.getEntity() instanceof ServerPlayer player) {
        List<BuffData> buffs = BuffQueryAPI.getAllBuffs(player);
        for (BuffData buff : buffs) {
            System.out.println(buff.buffId() + ": " + buff.remainingTicks() + "t left");
        }
    }
}
```

#### `BuffQueryAPI.hasBuffWithId(ServerPlayer player, String buffId)`

Check if player has a specific buff type.

```java
if (BuffQueryAPI.hasBuffWithId(player, "regeneration")) {
    player.heal(1.0f);  // Extra regen trigger
}
```

#### `BuffQueryAPI.getAggregateMagnitude(ServerPlayer player, String buffId)`

Sum magnitude of all buffs with a given ID.

```java
double totalSpeedBoost = BuffQueryAPI.getAggregateMagnitude(player, "walk_speed");
System.out.println("Total speed boost: " + (totalSpeedBoost * 100) + "%");
```

#### `BuffQueryAPI.removeBuffsWithId(ServerPlayer player, String buffId)`

Remove all buffs of a specific type.

```java
BuffQueryAPI.removeBuffsWithId(player, "walk_speed");  // Remove speed boosts
```

#### `BuffQueryAPI.getActiveFoodBuffCount(ServerPlayer player)`

Count active food slots (max 3 by default).

```java
if (BuffQueryAPI.getActiveFoodBuffCount(player) >= 3) {
    System.out.println("Player slots full!");
}
```

### Event Hooks

Subscribe to buff lifecycle events:

```java
import com.derko.seamlessapi.api.BuffEvents;
import net.neoforged.bus.api.SubscribeEvent;

public class MyModBuffListener {
    @SubscribeEvent
    public static void onBuffApplied(BuffEvents.BuffAppliedEvent event) {
        String playerName = event.getPlayer().getName().getString();
        String buffType = event.getBuff().buffId();
        System.out.println(playerName + " gained " + buffType);
    }

    @SubscribeEvent
    public static void onBuffRemoved(BuffEvents.BuffRemovedEvent event) {
        if (event.getReason() == BuffEvents.BuffRemovedEvent.RemovalReason.EXPIRED) {
            System.out.println("Buff expired: " + event.getBuff().buffId());
        }
    }

    @SubscribeEvent
    public static void onBuffApplying(BuffEvents.BuffApplyingEvent event) {
        // Can intercept and modify/cancel before application
        if (event.getFoodSource().contains("poisonous")) {
            event.setCanceled(true);  // Prevent poison buff
        }
        
        // Or modify on the fly
        event.setMagnitude(event.getMagnitude() * 1.5);  // Boost damage by 50%
    }
}
```

Then register in your event handler class:
```java
modEventBus.register(MyModBuffListener.class);
```

### Dynamic Buff Modifiers

Register functions that intercept and modify buff calculations:

```java
import com.derko.seamlessapi.api.BuffModifiers;

// Boost buff magnitude if player wears special armor
BuffModifiers.registerMagnitudeModifier(
    "mymod_armor_sync",
    "walk_speed",
    (player, buffId, baseMagnitude) -> {
        ServerPlayer sp = (ServerPlayer) player;
        if (sp.getInventory().contains(Items.NETHERITE_CHESTPLATE)) {
            return baseMagnitude * 1.5;  // +50% if wearing netherite chest
        }
        return baseMagnitude;
    }
);

// Prevent buffs in specific dimensions
BuffModifiers.registerApplicationFilter((player, foodSource, buffId) -> {
    ServerPlayer sp = (ServerPlayer) player;
    return !sp.level().dimension().equals(Level.NETHER);  // No buffs in nether
});
```

## BuffData Structure

When you get a buff via `BuffQueryAPI.getAllBuffs()`, you receive `BuffData` objects:

```java
public record BuffData(
    String buffId,              // Buff type (e.g., "walk_speed")
    int remainingTicks,         // Ticks until expiry
    int maxTicks,               // Original duration
    double magnitude,           // Effect strength
    double healthBonusHearts,   // Heart bonus value
    String foodSource,          // Food that gave this (e.g., "mymod:item")
    long appliedAtGameTime      // Server tick when applied
) {
    double progressRatio()      // 0.0 = just applied, 1.0 = about to expire
}
```

## Configuration & Persistence

Once registered via API, foods' effects are:
1. **Persisted** in `food_buffs.json` (in game config folder)
2. **Adjustable** per-effect via in-game config menu 
3. **Reloadable** without server restart (via ConfigManager.refreshFromNeoForgeConfigs())

Admins can modify effect strength multipliers (0.00-5.00) per buff type to globally scale:
- Walk speed boosts
- Damage reduction values
- Regen rates
- Etc.

Example `effect_strengths.json`:
```json
{
  "walk_speed": 1.20,
  "attack_speed": 0.80,
  "regeneration": 0.95
}
```

## Best Practices

### ✅ Do

- **Register in constructor or FMLCommonSetupEvent** — before load completes
- **Use descriptive buff IDs** — include your mod ID (e.g., `"mymod_speed_boost"`)
- **Keep magnitude 0.0 - 1.0** — for balanced effects
- **Test in multiplayer** — buffs should sync correctly across players
- **Document your custom buff types** — if you add new ones beyond the built-in 9

### ❌ Don't

- **Register buffs after load** — IllegalStateException will be thrown
- **Call SatiationAPI from client-only code** — it's server-only
- **Assume magic numbers** — read the config to find actual effect values
- **Directly modify player NBT** — use BuffQueryAPI to query, let the system manage storage
- **Block game thread** — keep buff calculation fast (< 1ms)

## Multiplayer Compatibility

- ✅ Buffs persist across save/load
- ✅ Automatic network sync via NeoForge NBT layer
- ✅ Death clears buffs (configurable)
- ✅ Anti-cheat protection (server-side authority)
- ✅ Compatible with existing mods (non-invasive registration)

## Example Mod Integration

See [example-mod/](example-mod/) for a complete working example.
