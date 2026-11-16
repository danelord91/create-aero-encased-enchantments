# Create Aero: Encased Enchantments

Bring **Create: Enchantable Machinery’s enchantment effects** to supported machines from **Create Encased** and **Create: Design n’ Decor**, while keeping their original block IDs, models and appearance.

Use **Encased on its own, Design n’ Decor on its own, or both together**. Made for Dane Lord’s Create Aero, but usable independently of the modpack.

## Supported machinery

### Create Encased
Supports **75 built-in machine variants** across:
- Mechanical drills and saws
- Harvesters and ploughs
- Mechanical rollers
- Encased fans
- Mechanical mixers and presses
- Spouts

### Create: Design n’ Decor
Adds enchantment support for:
- **22 millstone variants**
- **22 crushing-wheel variants**

Design n’ Decor millstones and crushing wheels support **Efficiency**, including compatible mixed crushing-wheel pairs. Decorative large fans, gears and unrelated decorative blocks are not included.

## Enchantments

Combine a supported machine with an **enchanted book in an anvil**.

- **Efficiency:** speeds supported processing or working actions. Supports Encased drills, saws, ploughs, rollers, fans, mixers, presses and spouts, plus supported Design n’ Decor millstones and crushing wheels. It does not increase a contraption’s travel speed.
- **Fortune:** affects eligible drops from supported Encased drills, saws, harvesters and ploughs.
- **Silk Touch:** enables eligible Silk Touch drops for those same Encased machine families.

Fortune and Silk Touch follow normal incompatibility rules. Their effects depend on the machine’s supported behaviour and the relevant block loot tables. **Design n’ Decor machines do not gain Fortune or Silk Touch effects.**

## Persistence and contraptions

Enchantments are retained through supported placement, recovery, saving/loading and tested contraption capture/disassembly paths. Supported Encased moving actors retain their applicable enchantment effects.

Casing shimmer covers baked block-body geometry, not separately animated drill heads, blades, shafts or fluids.

## Requirements

**Minecraft 1.21.1 · NeoForge · Java 21**

Required:
- **NeoForge 21.1.265+**
- **Create 6.0.10+**
- **Create: Enchantable Machinery 3.6.0+**

Optional integrations—install either or both:
- **Create Encased 1.10.0-fix1+**
- **Create: Design n’ Decor 2.2b+**

These mods’ own dependencies are also required. Newer versions are permitted by the addon’s dependency ranges, but are not automatically guaranteed compatible.

## Installation

Install the addon in the `mods` folder on **both the server and every player’s client**. For single-player, install it in your client instance.

Remove the previous addon JAR before installing an update, and back up your world first.

**Before uninstalling:** recover enchanted machinery and back up your world. Removing the addon can discard stored enchantments, particularly from supported Encased ploughs.

## Compatibility and testing

Version 0.3.0 passed automated production-JAR server tests with **Encased only, Design n’ Decor only, and both together**.

These tests do not certify every modpack, shader or rendering configuration. Client rendering and full-pack gameplay have not been independently verified for every supported variant.

## Credits

An independent compatibility addon by **Dane Lord**, with thanks to the creators and contributors of **Create**, **Create Encased**, **Create: Design n’ Decor** and **Create: Enchantable Machinery**.

**Licence: MIT.** Dependencies retain their own licences.
