# XaeroUnfair

A client-side Fabric mod for Minecraft: Java Edition that filters selected incoming FairPlay chat messages and simulates the Xaero reset sequence according to your settings.

It is designed for servers that enforce Xaero's FairPlay restrictions by sending an invisible chat message. A server may use a plugin, a mod, or a datapack to send that message.

When those restrictions are sent this way, filtering the message and simulating the reset can allow **Cave Mode and Mob Radar in Xaero's Minimap** to remain available on your client. This mod only targets that invisible-chat-message method; it does not bypass other enforcement methods. In particular, it does not help when the server itself runs Xaero's Minimap as a server-side mod (available for NeoForge, Forge, Fabric, and Quilt).

## Download

Releases and supported Minecraft versions are listed on the [Releases](https://github.com/Fisupala/XaeroUnfair/releases) page. Requires Fabric Loader and Fabric API.

## Features

- In Manual mode, filters all targeted `§` chat messages before anything else can see them
- Runs first in the chat pipeline, so other mods never receive the filtered fragments
- On joining a world or server in Manual mode, resets Xaero and simulates the selected profile's instruction
- Applying a Manual profile resets Xaero, then simulates only that profile's FairPlay instruction
- Provides optional settings through Mod Menu, including per-server profiles
- Client-side only: nothing needs to be installed on the server

## Settings

Install Mod Menu to open XaeroUnfair's settings screen from the Mods list. Settings are saved locally in `config/xaerounfair.properties`.

Each multiplayer server can have its own profile, edited while connected to that server; servers without a saved profile use the global defaults. **Server enforced** passes all targeted messages through and does not simulate any messages automatically. Reminder: changes to Server enforced are intentionally applied by Xaero only after reconnecting or switching worlds. Right-clicking the mode button cycles modes; switching to Server enforced replays the last five targeted server messages in their original order. Left-clicking the mode button opens the message history. This history is kept in memory for the current game session. In **Manual** mode, choose a profile and press **Apply**. The mod filters every targeted server instruction, resets Xaero, then simulates only the selected profile's instruction without displaying the synthetic message:

- **Default**: filters every targeted FairPlay message and runs the reset sequence on join.
- **Minimap off**: filters every targeted FairPlay message and simulates only `nominimap`.
- **Nether Cave**: filters every targeted FairPlay message and simulates `fairxaero` followed by the Xaero Minimap Nether message (`xaerommnetherisfair`), disabling entity radar and Cave Mode generally while allowing Cave Mode only in the Nether.
- **Basic FairPlay**: filters every targeted FairPlay message and simulates only `fairxaero`.

On join or Apply, the mod sends a reset immediately, repeats it after a short delay, then applies the selected instruction directly to Minecraft's client packet listener. Synthetic messages are allowed through Xaero's message handler but hidden from chat and narration. Each simulated message is logged with its selected mode and raw `§` formatting code. These actions require an active world/server connection.

## How it works

1. When an incoming chat packet arrives, XaeroUnfair checks it before Minecraft or other mods handle it, using the active server's profile or the global defaults.
2. In Manual mode, every targeted server instruction is filtered. In Server enforced mode, all targeted server messages pass through unchanged.
3. Every Manual profile simulates the reset sequence and then only its associated instruction on joining a world or server (including a Velocity server switch). Applying a Manual profile does the same immediately.

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