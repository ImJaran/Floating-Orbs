# Floating Orbs

Floating Orbs is a RuneLite plugin that adds customizable, movable Prayer and Special Attack orbs.

## What It Does

- Adds a movable Prayer orb button
- Adds a movable Special Attack orb button
- Supports snap mode so both orbs move together
- Supports orb size, shape, color, and blink customization
- Supports optional points text display
- Uses 1:1 clicks in-game (no macro behavior)

## Main Features

- Prayer orb toggles quick prayers
- Special orb toggles special attack
- Round or square orb styles
- Per-orb scale and width/height scaling
- Per-orb ON/OFF/BLINK colors
- Configurable blink interval and thresholds
- Auto-hide behavior while blocking interfaces are open

## Compliance Notes

- Designed for direct user interaction (uses 1:1 clicks)
- No botting, no loops, no automated gameplay logic
- Intended to stay within RuneLite and Jagex rules

## Development

### Requirements

- Java 17
- Gradle (wrapper included)

### Build

```bash
./gradlew build
```

### Local test launcher

```bash
./gradlew testClasses
```

Run `com.floatingorbs.FloatingOrbsPluginLauncher` from your IDE.

## Project Metadata

- Plugin class: `com.floatingorbs.FloatingOrbsPlugin`
- Config group: `floatingorbs`
- Display name: `Floating Orbs`

## License

MIT. See `LICENSE`.
