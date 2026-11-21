# HyFlez

**Minecraft Bedrock • Android • UI Prototype**

HyFlez is an experimental Android companion-client project. The first milestone is a mobile-first RGB interface prototype that can later guide a native Android overlay implementation.

## Current milestone
- Floating RGB launcher button (prototype inside the page)
- Right-side sliding panel
- Tabs: Combat, Movement, Visual, Player, Settings
- Responsive layout for phone screens
- Locally saved interface preferences

## Important
This repository currently contains a **web UI prototype**, not an installable APK and not a Minecraft modification. A webpage cannot draw over another Android app or change Minecraft gameplay by itself. A real floating overlay requires a native Android implementation and the user's Android overlay permission.

The project is intended for legitimate customization, HUD/visual preferences, and private-world experimentation. It does not implement cheats or tools designed to give an unfair advantage on public servers.

## Run the prototype
Open `index.html` in a mobile browser, or use GitHub Pages after enabling it in repository settings.

## Project structure
- `index.html` — interface structure
- `style.css` — RGB visual design
- `script.js` — panel, tabs, switches, and local preferences
