# Case Opening Mod (Fabric 1.21.8)

A client-side case opening screen with a CS:GO-style reel. Cosmetic only: no items are given or unlocked.

## Current features (stage 1)

- Spinning reel with item previews and eased landing animation
- Rarity weighting: higher worth means lower drop chance
- Result panel showing the item, `Worth: $X (est.)` in green, and its chance
- Info button with a disclaimer
- Default "Starter Case" with dirt through dragon egg

Not yet included: the case editor, saving custom cases, and colour customisation.

## Build

1. Install JDK 21.
2. If `gradlew` is missing, run `gradle wrapper` once in this folder.
3. Run `./gradlew build` (Windows: `gradlew.bat build`).
4. The jar is in `build/libs/`.

## Install

Put the jar in your `mods` folder along with Fabric Loader and Fabric API for 1.21.8.

## Use

Press **O** in-game to open the case (rebind under Controls > Case Opening).

## Worth values

Worth values are rough estimates entered by hand in `CaseDefinition.java`. Update them there and rebuild.
