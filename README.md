# XaeroUnfair

A client-side Fabric mod for Minecraft: Java Edition that blocks incoming chat messages containing specific `§` color-code sequences, as if the server had never sent them, and simulates the reset sequence whenever you join a world or server.

It is designed for servers that enforce Xaero's FairPlay restrictions by sending an invisible chat message. A server may use a plugin, a mod, or a datapack to send that message.

When those restrictions are sent this way, filtering the message and simulating the reset can allow **Cave Mode and Mob Radar in Xaero's Minimap** to remain available on your client. This mod only targets that invisible-chat-message method; it does not bypass other enforcement methods. In particular, it does not help when the server itself runs Xaero's Minimap as a server-side mod (available for NeoForge, Forge, Fabric, and Quilt).

## Download

Releases and supported Minecraft versions are listed on the [Releases](https://github.com/Fisupala/XaeroUnfair/releases) page. Requires Fabric Loader and Fabric API.

## Features

- Blocks chat messages containing the targeted `§` code sequences before anything else can see them
- Runs first in the chat pipeline, so other mods never receive the filtered fragments
- Simulates the reset sequence as if it came from the server every time you join a world or server
- Client-side only: nothing needs to be installed on the server

## How it works

1. When an incoming chat packet arrives, XaeroUnfair checks it before Minecraft or other mods handle it.
2. If the message contains one of the targeted `§` sequences, the entire message is dropped, so downstream code does not receive it.
3. On joining a world or server (including a Velocity server switch), the mod injects the reset sequence into chat as if the server had sent it. The reset sequence itself is never blocked, only simulated.

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