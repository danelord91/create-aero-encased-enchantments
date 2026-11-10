# 0.2.1 — Initial public upload package

- Adds compatibility for 75 built-in Create Encased machine variants with supported Efficiency, Fortune and Silk Touch behaviour.
- Preserves Encased casing identity and models, enchantment storage, placement and supported item-recovery paths.
- Includes the 0.2.1 fix for missing enchantment shimmer on machine casings carried by contraptions.
- Retains existing placed-machine casing shimmer and original actor animations.
- Respects the Enchantable Machinery renderGlint setting; unenchanted and unrelated blocks are excluded from the overlay.
- Passed 22/22 independent production-JAR GameTests, 8/8 glint-wiring checks and 8/8 actual exported client transformation checks. Isolated client startup passed; the owner confirmed contraption drill-casing shimmer in-game.

Requirements: Minecraft 1.21.1, Java 21, NeoForge 21.1.265 tested, Create 6.0.10, Create Encased 1.10.0-fix1 and Create: Enchantable Machinery 3.6.0.

Known limitations: the shimmer applies to baked casings, not separate animated heads/blades/shafts. Other render backends, shaders, third-party casing extensions and full-pack combinations are not comprehensively certified.

Installation: replace any older compatibility addon on both client and multiplayer server. Back up first.
