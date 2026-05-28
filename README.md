# Splinter
A Minecraft mod that records bastion routes/splits and logs them into sets for comparison.
[Showcase Video](https://www.youtube.com/watch?v=hYmMJNPHtS8)

## Requirements:
Java 17+: https://www.youtube.com/watch?v=WGFEMkWilK0  
Fabric API 0.18.0 build 387: https://modrinth.com/mod/fabric-api/versions?g=1.16.1

## Features:
- Tick aligned Route timer with map trigger, block break, and position trigger support
- Multiple sets for comparing different route variations
- Stats panel showing best, average, and standard deviation
- Edit mode for configuring route triggers in-world

## Default Keybinds
- **Edit Select** - M
- **Open Edit GUI** - N
- **Open Sets GUI** - B
- **Toggle Edit Mode** - J
- **Toggle Timer** - ; (semicolon)

## Triggers
- **MAP** - Default, run when LBP Map starts and stops (pressing button / dropping pickaxe)
- **BLOCK_BREAK** - must select a valid block, triggers on break. if block is air then the run is invalidated
- **POSITION** - triggers when player walks into the selected block. can overlap START / END

## Edit Mode Usage
1. Enter idle mode by pressing the "■" button in the sets list
2. Press J to enter edit mode
3. Select a slot (START/END), choose trigger type in the Edit GUI, then select the position in-world
4. Confirm changes in the Edit GUI
5. Run the route - the timer starts and stops based on the selected triggers

## Known Issues
- Overlays (rename) text bleeding behind confirm button, overlay may become unresponsive or broken when pressing other buttons. press ESC and re-open to fix
- Route data not persistent between sessions (planned for 1.1.0)

## TODO:
- Persistent Data
- Unlimited active sets / Intermediary splits (splits between START and END)
- Set moving / rework general (drag n drop)
- Piglin Stats (Pig count, Barter Splits, Trade start/end)
- Barter triggers / Piglin-based triggers / Block Place triggers
- Customizable UI (block outline, timer, GUIs, HUD)
- Edit mode redesign (maybe some keybind overwrites / scroll wheel for more intuitive controls)
- ? Stay in edit mode after confirm
- Max trial amount (warning when reached)
- Creative mode on edit mode
- Reset to Default route (map triggers)
- ? Decay Setting
- Trimming means / outlier removal (probably as an option)
- Blockbreak trigger decay autofiller

## Credits
Inspired by and referenced [BastionHelper](https://github.com/LeSaRXD/BastionHelper) by Laysar.
