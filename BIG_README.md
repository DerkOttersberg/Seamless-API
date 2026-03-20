# Seamless-API - Big README

## Project Idea
Seamless-API is a lightweight integration layer that lets other mods add food buffs and combo definitions to Advanced Food System without patching its internals.

This API is the shared base for all of my mods, and it is intentionally designed to grow as my mod ecosystem grows.

Core design goals:
- Keep registration simple and stable.
- Keep gameplay authority in the main mod.
- Provide enough hook points for extension and interoperability.
- Stay safe under multiplayer server rules.
- Remain forward-looking so new systems can be added in future versions without breaking existing integrations.

Companion project:
- This API is consumed by Advanced Food System.
- Main gameplay architecture and built-in combos are documented in ../Advanced-Food-System/BIG_README.md.

## What This API Provides
- Food registration API with builder-based schema.
- Combo registration API with requirements/effects/capstone metadata.
- Buff lifecycle event classes for applied/removal/pre-apply interception.
- Query API for active buffs on server players.
- Dynamic modifier registry for magnitude/health/filter hooks.
- Immutable buff data transfer object for integrations.

## Integration Lifecycle
### 1) Registration Phase (before freeze)
Third-party mods call:
- SatiationAPI.registerFood(...)
- SatiationAPI.registerCombo(...)

### 2) Freeze and Merge
Advanced Food System calls:
- SatiationAPI.freezeAndGetAll()
- SatiationAPI.freezeAndGetCombos()

After freeze, late registration throws by design.

### 3) Runtime Query and Hooks
At runtime, mods can:
- inspect active buffs
- remove selected buff groups
- subscribe to or emit behavior around buff lifecycle events
- register dynamic modifiers/filters

## API Surface (Class Guide)

### Root
- SeamlessApiMod: minimal NeoForge mod entrypoint for the API jar.
- SatiationAPI: central registration gateway and freeze control.

### Data and Registration Models
- api/FoodBuffRegistration: immutable food buff descriptor with fluent builder.
- api/ComboRegistration: immutable combo descriptor with fluent builder.
- api/BuffData: immutable snapshot of one active buff instance.

### Runtime Extension APIs
- api/BuffQueryAPI:
  - getAllBuffs
  - getBuffsMatching
  - hasBuffWithId
  - getAggregateMagnitude
  - getActiveFoodBuffCount
  - removeBuffsWithId
  - removeBuffsFromSource

- api/BuffModifiers:
  - registerMagnitudeModifier
  - registerHealthModifier
  - registerApplicationFilter
  - applyMagnitudeModifiers (internal usage)
  - applyHealthModifiers (internal usage)
  - shouldApplyBuff (internal usage)

- api/BuffEvents:
  - BuffAppliedEvent
  - BuffRemovedEvent (with RemovalReason enum)
  - BuffApplyingEvent (cancel/mutate before apply)

## Typical Usage Patterns
### Add a Food
- Define buff ids, duration, magnitude, and hearts through FoodBuffRegistration.builder().
- Register during mod setup before load-complete.

### Add a Combo
- Define required food ids and buff effects through ComboRegistration.builder().
- Optionally mark capstone and final-heart unlock behavior.
- Register before freeze.

### React to Buff Lifecycle
- Subscribe to BuffEvents classes through NeoForge event bus.
- Trigger side effects (logging, achievements, conditions) when buffs apply/remove.

### Modulate Behavior Dynamically
- Use BuffModifiers to scale values from external systems (gear, biome, dimension, etc.).
- Use filters to block buff application contextually.

## Compatibility Contract
- API jar intentionally avoids owning gameplay authority.
- Advanced Food System remains the source of truth for storage, ticking, and effective application.
- Reflection bridges in query helpers are designed to degrade safely when internals are unavailable.

## Build and Publishing
From Seamless-API root:
- Windows: gradlew.bat clean build publishToMavenLocal

Typical local workflow:
1) publish Seamless-API to mavenLocal
2) build Advanced-Food-System against that artifact



## Why This Exists
Without this API, each modpack or addon would need custom hard patches into Advanced Food System.
With this API, ecosystem mods can register new foods and combo ideas quickly while keeping one consistent runtime pipeline.

This project is planned as a long-term foundation for all of my mods, with additional APIs and integration points to be added over time.
