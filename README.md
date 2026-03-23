# Seamless-API

**A lightweight, extensible NeoForge API library for multiple Seamless mod systems.**

Seamless-API enables any modder to register systems across Seamless mods through stable integration hooks.

Current modules:
- **Food Buff API** (stable) — register foods, combos, modifiers, queries, events
- **Deconstruction API** (new) — register item deconstruction mappings and output modifiers

Food system use cases:

- **Food mods** → Spicy peppers, magical fruits, potions
- **Difficulty mods** → Special foods that help with specific challenges
- **Crossover mods** → Integrate your mod's foods with a unified buff system
- **Server admins** → Adjust effect potency per-server via JSON config

## Key Features

✨ **Non-invasive** — Register via simple API, no core mod changes  
⚡ **Multiplayer-ready** — Automatic network sync, persistent buffs  
🎮 **In-game configurable** — Admin menu to adjust effect strengths  
🔌 **Event-driven** — Hook into buff lifecycle for custom reactions  
📊 **Query API** — Check active buffs, magnitudes, remaining durations  
🛡️ **Server-authoritative** — All calculations verified server-side

## Installation

### Publish to Maven Local (for testing)

From `Seamless-API/` root:
```bash
./gradlew.bat publishToMavenLocal
```

### Depend in Your Mod

In your `build.gradle`:
```gradle
repositories {
    mavenLocal()
}

dependencies {
    modCompileOnly 'com.derko:seamlessapi:1.0'
    modRuntimeOnly 'com.derko:seamlessapi:1.0'
}
```

## Food Usage Example

```java
import com.derko.seamlessapi.SatiationAPI;
import com.derko.seamlessapi.api.FoodBuffRegistration;

public class MyModFoods {
    public static void setup() {
        // Fiery Pepper: +attack speed for 5 minutes
        SatiationAPI.registerFood(
            "mymod:fiery_pepper",
            FoodBuffRegistration.builder()
                .buff("attack_speed")
                .duration(300)
                .magnitude(0.25)
                .hearts(1.0)
                .build()
        );
        
        // Frozen Apple: -damage taken for 10 minutes
        SatiationAPI.registerFood(
            "mymod:frozen_apple",
            FoodBuffRegistration.builder()
                .buff("damage_reduction")
                .duration(600)
                .magnitude(0.20)
                .hearts(0.5)
                .build()
        );
    }
}
```

Call `MyModFoods.setup()` from your mod's constructor or `FMLCommonSetupEvent`.

## Deconstruction Usage Example

```java
import com.derko.seamlessapi.DeconstructionAPI;
import com.derko.seamlessapi.api.deconstruction.DeconstructionContext;
import com.derko.seamlessapi.api.deconstruction.DeconstructionRegistration;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyModDeconstruction {
    public static void setup() {
        DeconstructionAPI.registerDeconstruction(
            "mymod:bronze_sword",
            DeconstructionRegistration.builder()
                .ingredient("mymod:bronze_ingot", 2.0)
                .ingredient("minecraft:stick", 1.0)
                .damageScalingEnabled(true)
                .build()
        );

        DeconstructionAPI.registerModifier((DeconstructionContext ctx, Map<String, Integer> output) -> {
            if (!ctx.damageable() || ctx.durabilityFraction() >= 0.90D) {
                return output;
            }

            Map<String, Integer> reduced = new LinkedHashMap<>();
            output.forEach((item, count) -> {
                int adjusted = (int) Math.floor(count * 0.9D);
                if (adjusted > 0) {
                    reduced.put(item, adjusted);
                }
            });
            return reduced;
        });
    }
}
```

## Combo Registration Example

```java
import com.derko.seamlessapi.SatiationAPI;
import com.derko.seamlessapi.api.ComboRegistration;

public class MyModCombos {
    public static void setup() {
        SatiationAPI.registerCombo(
            "mymod:combo_scholar",
            ComboRegistration.builder()
                .requiresFood("minecraft:apple")
                .requiresFood("minecraft:bread")
                .requiresFood("mymod:crystal_berry")
                .effect("xp_gain", 0.10)
                .effect("hunger_efficiency", 0.03)
                .capstone(true)
                .grantsFinalHeart(false)
                .build()
        );
    }
}
```

## API Reference

### Registration
- `SatiationAPI.registerFood(itemId, registration)` — Register a custom food
- `SatiationAPI.registerCombo(comboId, registration)` — Register a custom combo
- `DeconstructionAPI.registerDeconstruction(itemId, registration)` — Register deconstruction units for an item
- `DeconstructionAPI.registerModifier(modifier)` — Register output modifier hook

### Queries
- `BuffQueryAPI.getAllBuffs(player)` — Get all active buffs
- `BuffQueryAPI.hasBuffWithId(player, buffId)` — Check if buff active
- `BuffQueryAPI.getAggregateMagnitude(player, buffId)` — Sum buff magnitudes
- `BuffQueryAPI.removeBuffsWithId(player, buffId)` — Remove buffs

### Events
- `BuffAppliedEvent` — Fire when buff applied
- `BuffRemovedEvent` — Fire when buff expires/removed
- `BuffApplyingEvent` — Cancel or modify before apply

### Modifiers
- `BuffModifiers.registerMagnitudeModifier()` — Intercept magnitude calculation
- `BuffModifiers.registerApplicationFilter()` — Veto buff application

See [API_GUIDE.md](API_GUIDE.md) for full documentation.

## Built-in Buff Types

- `walk_speed` — Movement speed boost
- `attack_speed` — Attack speed boost
- `mining_speed` — Block break speed (client)
- `damage_reduction` — Reduced incoming damage
- `regeneration` — Health regen
- `saturation_boost` — Saturation restore
- `knockback_resistance` — Reduce knockback
- `jump_height` — Jump height boost
- `hunger_efficiency` — Slower hunger loss

## Dependencies

- NeoForge 21.0.167+  
- Minecraft 1.21+
- Java 21+

Advanced Food System mod (for gameplay; API works standalone)

## Project Structure

```
Seamless-API/
├── src/main/java/com/derko/seamlessapi/
│   ├── SatiationAPI.java              # Main registration entry point
│   ├── DeconstructionAPI.java         # Deconstruction registration + modifier API
│   ├── SeamlessAPI.java               # Unified facade (optional)
│   ├── SeamlessApiMod.java            # Mod class (no gameplay logic)
│   └── api/
│       ├── FoodBuffRegistration.java  # Food config builder
│       ├── ComboRegistration.java     # Combo config builder
│       ├── BuffData.java              # Immutable buff snapshot
│       ├── BuffQueryAPI.java          # Query API for active buffs
│       ├── BuffEvents.java            # Event hooks
│       └── BuffModifiers.java         # Dynamic modifier system
│       └── deconstruction/
│           ├── DeconstructionRegistration.java
│           ├── DeconstructionContext.java
│           └── DeconstructionModifier.java
├── API_GUIDE.md                       # Comprehensive usage guide
└── README.md                          # This file
```

## Integration with Advanced Food System

This API is designed to work seamlessly with [Advanced-Food-System](https://github.com/DerkOttersberg/Advanced-Food-System):

1. **Automatic registration** — Foods registered via SatiationAPI are merged into the main mod's food list
2. **Config inheritance** — API-registered foods respect global and per-effect multipliers
3. **No conflicts** — Multiple mods can register without collision
4. **Persistence** — Buffs saved to player NBT automatically
5. **Networking** — Handled transparently via NeoForge's NBT sync

## Contributing

Suggestions welcome! Open an issue or create a pull request.

## License

See LICENSE file in main Advanced Food System repository.

---

**v1.0** — First stable release  
**Target**: NeoForge 21.0+, Minecraft 1.21+
