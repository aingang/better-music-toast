# Better Music Toast

A Fabric mod for Minecraft 26.2 that shows which song is currently playing, in a small beige box with a brown border that fits the vanilla HUD.

You can choose where it shows up, how long it stays, and it moves out of the way when advancements or other notifications pop up.

## Requirements

- Minecraft 26.2 with Fabric Loader
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional)

Client-side only, not needed on servers.

## Settings

Options → Music & Sounds → **Better Music Toast**, or through Mod Menu.
Settings are saved in `config/bettermusictoast.json`.

## Building

```
gradlew build
```

The finished mod ends up in `build/libs/better-music-toast-<version>.jar`.

## Testing

```
gradlew runClientGameTest
```

Starts a test client, goes through every position and notification case, and saves screenshots to `build/run/clientGameTest/screenshots/`.

## License

MIT, see [LICENSE](LICENSE).
