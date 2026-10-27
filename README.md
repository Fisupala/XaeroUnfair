# XaeroUnfair

A client-side Fabric mod for Minecraft: Java Edition that filters selected incoming FairPlay chat messages and simulates the Xaero reset sequence according to your settings.

It is designed for servers that enforce Xaero's FairPlay restrictions by sending an invisible chat message. A server may use a plugin, a mod, or a datapack to send that message.

When those restrictions are sent this way, filtering the message and simulating the reset can allow **Cave Mode and Mob Radar in Xaero's Minimap** to remain available on your client. This mod only targets that invisible-chat-message method; it does not bypass other enforcement methods. In particular, it does not help when the server itself runs Xaero's Minimap as a server-side mod (available for NeoForge, Forge, Fabric, and Quilt).

## Download

Releases and supported Minecraft versions are listed on the [Releases](https://github.com/Fisupala/XaeroUnfair/releases) page. Requires Fabric Loader and Fabric API.

## Features

- Filters targeted `§` chat messages before anything else can see them, according to the active settings profile
- Runs first in the chat pipeline, so other mods never receive the filtered fragments
- In Default Manual mode, simulates the reset sequence on joining a world or server
- Applying a Manual profile resets Xaero, then simulates only that profile's FairPlay instruction
- Provides optional settings through Mod Menu, including per-server profiles
- Client-side only: nothing needs to be installed on the server

## Settings

Install Mod Menu to open XaeroUnfair's settings screen from the Mods list. Settings are saved locally in `config/xaerounfair.properties`.

Each multiplayer server can have its own profile, edited while connected to that server; servers without a saved profile use the global defaults. **Server enforced** passes all targeted messages through and does not simulate any messages. In **Manual** mode, choose a profile and press **Apply**. The mod resets Xaero first, then sends only that profile's instruction after its own filter so Xaero receives it without displaying it in chat:

- **Default**: filters every targeted FairPlay message and runs the reset sequence on join, preserving the mod's original behavior.
- **Minimap off**: applies only `nominimap` and filters other targeted restrictions.
- **Nether Cave**: applies only the Xaero Minimap Nether message (`xaerommnetherisfair`); it does not apply the general `fairxaero` restriction or the separate World Map Nether message.
- **Basic FairPlay**: applies only `fairxaero` and filters the Nether-specific and minimap restrictions.

The Apply action sends a reset immediately, repeats it after a short delay, then applies the selected instruction directly to Minecraft's client packet listener after XaeroUnfair's filter. The separate reset button runs only the reset sequence. Each simulated message is logged with its selected mode and raw `§` formatting code. These actions require an active world/server connection.

## How it works

1. When an incoming chat packet arrives, XaeroUnfair checks it before Minecraft or other mods handle it, using the active server's profile or the global defaults.
2. In Manual mode, the selected profile determines which server messages pass through. In Server enforced mode, all targeted server messages pass through unchanged.
3. Default Manual mode simulates the reset sequence on joining a world or server (including a Velocity server switch). Applying another Manual profile sends the reset sequence followed by only its associated instruction, inserted after XaeroUnfair's filter so Xaero receives it.

## Disclaimer

This mod only works against servers that enforce FairPlay by sending one of the targeted invisible chat messages. It does not bypass restrictions enforced by a server-side Xaero's Minimap mod or by other means. Bypassing server restrictions may break the server's rules; check the rules of the server you play on. You use this mod at your own risk.

## License

Copyright (C) 2026 fisupala

This project is licensed under the [GNU General Public License v3.0 only](LICENSE) (`GPL-3.0-only`). You may use, modify and redistribute it, but any distributed version, modified or not, must stay under the same license and make its source code available.

## Credits

Made by fisupala.

Xaero's Minimap and Xaero's World Map belong to their respective author; this project is not affiliated with them.

---

*Vibe-coded with [Claude](https://claude.ai) by Anthropic.*