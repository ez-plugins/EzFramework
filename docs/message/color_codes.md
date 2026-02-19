# Minecraft Color Codes & Formatting

This reference lists common Minecraft color and formatting codes. Many message providers accept legacy color codes (using `&` or `§`) and also support MiniMessage-style tags.

Colors (legacy `&` codes):

- `&0` — Black
- `&1` — Dark Blue
- `&2` — Dark Green
- `&3` — Dark Aqua
- `&4` — Dark Red
- `&5` — Dark Purple
- `&6` — Gold
- `&7` — Gray
- `&8` — Dark Gray
- `&9` — Blue
- `&a` — Green
- `&b` — Aqua
- `&c` — Red
- `&d` — Light Purple
- `&e` — Yellow
- `&f` — White

Formatting:

- `&l` — Bold
- `&o` — Italic
- `&n` — Underline
- `&m` — Strikethrough
- `&k` — Obfuscated (magic)
- `&r` — Reset formatting

Hex colors

- Many formatters accept hex color notation (MiniMessage tag `<#rrggbb>` or `&#rrggbb` style). Prefer hex when specific brand colors are required.

Examples

- Legacy: `&aHello &bWorld` → green Hello, aqua World
- MiniMessage: `<green>Hello</green> <aqua>World</aqua>`

Best practices

- Prefer MiniMessage or structured tags in resource files for readability.
- If supporting legacy server platforms, ensure your `MessageProvider` converts legacy codes correctly.
