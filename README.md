# GE Tax Helper

A RuneLite plugin that shows what you'll actually receive after the 2% Grand Exchange tax while setting up a sell offer, including a flipping margin and break-even price.

## Features

- 💰 **Post-Tax Proceeds** — Net per item and total received after the 2% GE tax, live as you adjust price and quantity
- 🧾 **Tax Breakdown** — Tax per item and total tax for the offer, with the 5m per-item cap and tax-exempt items handled correctly
- 📈 **Flipping Margin** — Post-tax margin per item against the GE guide buy price, colored red when the sale is a loss
- ⚖️ **Break-Even Price** — The lowest sell price that still covers the guide buy price after tax

The overlay only appears while you're setting up a **sell** offer at the Grand Exchange — buy offers are never taxed, so it stays out of the way.

## How the tax is calculated

Following the [OSRS Wiki](https://oldschool.runescape.wiki/w/Grand_Exchange#Tax) rules:

- 2% of the sale price per item, rounded down (sales only)
- Capped at 5,000,000 coins per item — anything priced 250m or more pays a flat 5m
- Items priced below 50 coins pay nothing, since 2% rounds down to zero
- Fully exempt regardless of price: Old school bonds, basic tools (chisel, hammer, spade, rake, etc.) and the new-player staples added on 29 May 2025 (low-level food, basic arrows and darts, mind runes, energy potions, rings of dueling, games necklaces and common teleport tablets)

## Installation

This plugin is available on the [RuneLite Plugin Hub](https://runelite.net/plugin-hub).

1. Open RuneLite
2. Click the Plugin Hub icon (plug icon at the top of the configuration panel)
3. Search for **GE Tax Helper**
4. Click **Install**

## Use Cases

- Know exactly what a sale pays out before you confirm the offer
- Spot loss-making flips before they happen — the margin goes red when the post-tax proceeds dip below the guide buy price
- Price your sells precisely using the break-even price instead of estimating the tax in your head

## Links

For updates and other gaming content, follow me on:

🎥 [YouTube](https://youtube.com/@SeismicRy)

🐦 [X / Twitter](https://x.com/SeismicRy)

🎵 [TikTok](https://www.tiktok.com/@seismicry)

## Building

This project uses Gradle. To build locally:

```bash
./gradlew build
```

## License

BSD 2-Clause — see [LICENSE](LICENSE) for details.
