# XaeroUnfair

A client-side Fabric mod for Minecraft: Java Edition that filters selected incoming FairPlay chat messages and simulates the Xaero reset sequence according to your settings.

It is designed for servers that enforce Xaero's FairPlay restrictions by sending an invisible chat message. A server may use a plugin, a mod, or a datapack to send that message.

When those restrictions are sent this way, filtering the message and simulating the reset can allow **Cave Mode and Mob Radar in Xaero's Minimap** to remain available on your client. This mod only targets that invisible-chat-message method; it does not bypass other enforcement methods. In particular, it does not help when the server itself runs Xaero's Minimap as a server-side mod (available for NeoForge, Forge, Fabric, and Quilt).

## Download

Releases and supported Minecraft versions are listed on the [Releases](https://github.com/Fisupala/XaeroUnfair/releases) page. Requires Fabric Loader and Fabric API.

## Features

- Filters targeted `§` chat messages before anything else can see them, according to the active settings profile
- Runs first in the chat pipeline, so other mods never receive the filtered fragments
- In Manual mode, simulates the reset sequence on joining a world or server
- Provides optional settings through Mod Menu, including per-server profiles
- Client-side only: nothing needs to be installed on the server

## Settings

Install Mod Menu to open XaeroUnfair's settings screen from the Mods list. Settings are saved locally in `config/xaerounfair.properties`.

Each multiplayer server can have its own profile, edited while connected to that server; servers without a saved profile use the global defaults. In **Server enforced** mode, targeted restriction messages pass through and the reset sequence is not automatically simulated on join. In **Manual** mode, choose which server restrictions to honor: the general `fairxaero` restriction (no Cave Mode or entities), the Nether restriction (no entities while allowing Cave Mode), and the no-minimap restriction. Unchecked restrictions are filtered. The reset button runs the reset sequence on demand.

## How it works

1. When an incoming chat packet arrives, XaeroUnfair checks it before Minecraft or other mods handle it, using the active server's profile or the global defaults.
2. In Manual mode, selected restrictions are honored and the other targeted restriction messages are dropped. In Server enforced mode, all targeted messages pass through.
3. Manual mode simulates the reset sequence on joining a world or server (including a Velocity server switch). You can also trigger it from the settings screen while connected.

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