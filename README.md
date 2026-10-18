# XaeroUnfair

A client-side Fabric mod for Minecraft: Java Edition that filters specific `§` color-code sequences out of incoming chat messages, as if the server had never sent them, and re-sends the reset sequence to the chat whenever you join a new world or server.

It is aimed at keeping **Xaero's Minimap** and **Xaero's World Map** behaving the way you want regardless of what the server announces in chat.

## Download

Releases and supported Minecraft versions are listed on the [Releases](https://github.com/Fisupala/XaeroUnfair/releases) page. Requires Fabric Loader and Fabric API.

## Features

- Removes specific `§` code fragments from chat before anything else can see them
- Runs first in the chat pipeline, so other mods never receive the filtered fragments
- Simulates the reset sequence as if it came from the server every time you join a world or server
- Client-side only: nothing needs to be installed on the server

## How it works

1. When a chat message arrives, XaeroUnfair inspects it before any other mod handles it.
2. Matching `§` sequences are stripped from the message, so downstream mods behave as if the server never sent them.
3. On joining a world or server (including a Velocity server switch), the mod injects the reset sequence into chat as if the server had sent it. The reset sequence itself is never blocked, only simulated.

## Disclaimer

Some servers use chat codes to restrict features of client-side mods, and bypassing those restrictions may break the server's rules. Check the rules of the server you play on. You use this mod at your own risk.

## License

Copyright (C) 2026 fisupala

This project is licensed under the [GNU General Public License v3.0 only](LICENSE) (`GPL-3.0-only`). You may use, modify and redistribute it, but any distributed version, modified or not, must stay under the same license and make its source code available.

## Credits

Made by fisupala.

Xaero's Minimap and Xaero's World Map belong to their respective author; this project is not affiliated with them.

---

*Vibe-coded with [Claude](https://claude.ai) by Anthropic.*