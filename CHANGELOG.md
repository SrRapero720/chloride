# RELEASE 1.8.2
- [✨] Added Coordinates overlay (enabled by default)
  - ℹ️ Modes: `NO`, `SIMPLE`, `CORDS+ROT`, `CORDS+FACING`, `ALL`
  - ℹ️ Displays clean integers with colored axes (X red, Y green, Z blue), rotation as "X 0° | Y 0°" and the cardinal facing
  - ℹ️ Rotation/facing can render on their own line, with their own transparency
- [✨] Added Biome overlay
  - ℹ️ Tints the biome name by category: cold cyan, plains/forest light green, jungle/swamp dark green, ocean royal blue, hot orange, nether red-orange, cherry pink, mushroom brown
- [✨] Added Running overlay (displays "Sprinting" while sprinting, with fade in/out)
- [✨] Added "Light on Change" for Biome and Running overlays (full opacity for 3 seconds on changes, then a 0.75s ease-out fade back)
- [✨] Added active engine display (displays "... | GPU 72% (OpenGL)")
- [✨] Added max memory in GB (displays "... | MEM 72% (8.5GB)")
- [✨] Added transparency, scale (`50%`-`200%`) and contrast (`NONE`, `SHADOW`, `BACKGROUND`) settings for overlays
- [✨] Added Adaptive V-Sync (V-Sync option is now `OFF`, `ON`, `ADAPTIVE`)
  - ℹ️ Adaptive tears instead of stalling when a frame arrives late; behaves as `ON` if the driver lacks EXT_swap_control_tear
- [✨] Added Fast Sneak (removes the camera height smoothing when sneaking)
- [✨] Added Clear Spectator (blocks vanilla post-process shaders like creeper/enderman spectator effects)
- [✨] Added toast toggles (master switch plus Advancement, Recipe, System and Tutorial)
- [✨] Added persistent Advanced Item Tooltips (like F3+H, but kept across restarts)
- [✨] Added persistent Profiler Pie (keeps the profiler pie chart always on screen)
- [✨] Added Piston Push toggle (disable it to skip the piston moving-block render)
- [✨] Exposed vanilla Distortion Effects and FOV Effects sliders in the video settings
- [🛠️] Moved overlay settings (fps, cords, biome, running) into a new Overlay Settings page
- [🛠️] Added a new Player Settings page (Fast Sneak, FOV Effects, Clear Spectator)
- [🛠️] Reorganized the client config into nested sections and groups (old values are not migrated)
- [🛠️] Prepared the settings screen for future design styles
- [✨] Added the TexTrue settings screen with a searchable mod and page sidebar
- [🛠️] Improved Populi navigation with adaptive mod details and clearer selected pages
- [🛠️] Unified the Chloride, Populi and TexTrue settings layouts and scrollbars
- [✨] Added per-setting undo and reset-to-default buttons
- [🐛] Fixed the TexTrue and Populi settings screen styles opening without controls
- [🐛] Fixed severe frame drops when moving high-polling mice across menus

# RELEASE 1.8.1
- [🐛] Fixed chloride mixins for Leaves Block collides with sodiums mixins (only warns, but breaks our culling)
- [🐛] Fixed Nametag of item entities never got renderer despite being enabled on settings

# RELEASE 1.8.0
- [✨] Added a void horizon height slider (sets the Y level of the below-horizon void plane, 63 keeps vanilla behavior)
- [✨] Added a toggle to enhance Skybox looking on low render distances
- [🛠️] Moved per-particle toggles to a separated screen with an id/mod search box (fixes [#174](https://github.com/SrRapero720/chloride/issues/174))
- [🛠️] Replaced the custom config with WaterConfig (config is now chloride-client.toml), recovers old settings from .json file and deletes the file once is done.
- [🛠️] Declared Smooth Skies incompatible (its features are already in chloride) (fixes [#170](https://github.com/SrRapero720/chloride/issues/170))
- [🛠️] Updated Sodium to 0.8.12
- [🐛] Fixed bed faces were still culled with Fast Beds disabled (fixes [#126](https://github.com/SrRapero720/chloride/issues/126))
- [🐛] Fixed JEI integration crash on latest JEI (fixes [#172](https://github.com/SrRapero720/chloride/issues/172))
- [🐛] Fixed solid chests pack reused the solid beds pack id
- [🐛] Fixed JEI/REI/EMI mixins applied even when those mods weren't installed

# RELEASE 1.7.9
- [✨] [1.21.1] Updated to sodium 0.8.12-beta.1 (brings back video setting screen)

# RELEASE 1.7.8
- [✨] [1.21.1] Added Sable support for entity culling
- [✨] Added setting to disable Borderless optimization applied by driver (fixes black window blinking, false by default)
- [✨] Added setting to cap the entity rendering count on screen (entities are sorted by distance, unlimited by default)
- [🛠️] Changed Maximum horizontal distance culling to 128 and minimum to 0
- [🛠️] Changed Maximum vertical distance culling to 256 and minimum to 0
- [🛠️] Disabled customFog by default
- [🐛] [1.21.5] Fixed crash on neoforge loading unexisting resources
- [🐛] Fixed chloride's basic zoom stays enabled when other zoom mods are present
- [🐛] Fix borderless feature removes window borders when chloride settings desyncs with vanilla settings

# RELEASE 1.7.7
- [🐛] Fixed F11 switching (on attached mode) stucks you in fullscreen mode

# RELEASE 1.7.6
- [✨] Added toggles to disable fog per level (overworld, nether, end) separately (keeps global toggle
- [🛠️] Modpack mode now hides custom fog toggles (disabling fog/custom fog)
- [🛠️] Reverted fix of true darkness applied on full brightness biomes (like alex caves)
  - Fixes bugs on true darkness support
- [🐛] Fixed window border was still shown on borderless mode and fix bad performance
- [🐛] [1.21.1] Fixed FPS overlay wasn't hidden pressing F1 or opening F3
- [🐛] [1.21.1] Fixed missing MIN fps overlay
- [🐛] [1.21.1] Fixed crashes enabling modpack mode

# RELEASE 1.7.5
- [🐛] Fixed JEI integration crashes on latest versions of JEI

# RELEASE 1.7.4
- [🐛] Fixed whitelist wasn't working for elements with wildcards (modid:all)

# RELEASE 1.7.3
- [✨] Added a config option to hide the "Hide JREMI until searching" message
- [🐛] Fixed darkness not working with Chloride's entity culling
- [🐛] Fixed biomes with full brightness (like Alex's Caves) are affected by True Darkness
- [🐛] Fixed chart was not rendering when BetterF3 is installed
- [🐛] Fixed player effects (potions like blindness) are not working with custom fog
- [🐛] Fixed dimensions with no skylight (like The Nether) shows broken coloring when blueband is disabled

# RELEASE 1.7.2
- [✨] Fixed crashes clicking on Video Settings button after update Chloride

# RELEASE 1.7.1
- [🛠️] Structural changes on codebase with no visible changes (NeoForge re-porting)

# RELEASE 1.7.0
- [✨] Refurbished menus of the mod
  - 🛠️ Now are more organized and easier to use
- [✨] Refurbished translations of the mod (EN, ES_MX)
  - 🛠️ This removes the French, Russian, Chinese traditional and Chinese simplified translations
- [✨] Reimplemented Fast models feature (FastChests and FastBeds)
  - 🛠️ Turning it off requires close and reopen the world
  - 🛠️ Disabling this option will also disable the static resource pack (avoiding breakage on other mods)
- [✨] Added particles controlling
  - 🛠️ Vanilla particles controller (ALL, MINIMAL, NONE) was moved to chloride Particles tab
  - 🛠️ You can turn off each particle individually, including for other mods
  - 🛠️ You can also turn off water particles of Rain and Rain drops individually.
  - 🛠️ You can also turn off block particles on cracking and breaking individually.
- [✨] Added a _small_ optimization on making particles through particle engine
- [✨] Added VSEureka compatibility for entity culling
- [🛠️] Enhanced EMI mixins

# RELEASE 1.6.0
- [✨] [1.21.1] Ported to NeoForge (Sodium)
- [✨] Added blocklightOnly option to True Darkness
  - 🛠️ This option will only darken the block light, not the sky light
  - 🛠️ This option is disabled by default
- [✨] Enhanced FPS Display alignment
  - 🛠️ Now you can choose between TOP, CENTER and BOTTOM
  - 🛠️ Margin is now split for horizontal and vertical
  - 🛠️ Increased maximum margin
- [✨] Added a helper text on JEI/REI/EMI when is nothing typed on search bar
- [✨] Added custom fog configurations
  - 🛠️ Now you can configure fog start, distance and shape
- [🐛] Fixed fullscreen mode wasn't saving applying the options
- [🐛] Fixed maps have a black square on corners
- [🐛] Fixed certain configurations are not saving
- [🐛] Fixed fog wasn't enabled under water or lava (acting as a cheat)

# RELEASE 1.5.5
- [🐛] Fixed whitelist wasn't working using `modid:all` wildcard

# RELEASE 1.5.4
- [🐛] Fixed crashes clicking F11

# RELEASE 1.5.3
- [🐛] Fixed crashes on server-side (mod has no srv features, but it might not crash)

# RELEASE 1.5.2
- [🐛] Fixed few mistakes

# RELEASE 1.5.1
- [🛠️] Tweak modpack mode to only hide Zoom and True darkness page
- [🌐] Updated translations
  - [🍾] Updated Russian translation (by mpustovoi)

# RELEASE 1.5.0
- [✨] Added option "ModpackMode": disables chloride pages registration (forces applied configs)
- [✨] Added Zoom key defaulted to (C)
  - 🛠️ It gots disabled by default when JustZoom or Zume is installed
- [🛠️] Replace ForgeConfigAPI with a custom config utility
- [🛠️] Renamed Metrics page to Overlay
- [🛠️] Removed Zume translations
- [🛠️] Removed Zume integrations
- [🛠️] Updated all JEI, REI and EMI integrations to their latest versions
- [🐛] Fixed crashes caused by Create 0.6.1 (new Flywheel Update)
- [🐛] Fixed crashes with Cubes without Borders
- [🐛] Fixed crashes with Forgery

# RELEASE 1.2.13
- [🐛] Fixed crashes due to non-renamed accessors (fixes BadOptimizations mod crashes)

# RELEASE 1.2.12
- [🛠️] Unshipped MixinExtra (embeddium has it now)
- [🛠️] Re-moved unused resources 
- [🛠️] Performed some sanity changes on the codee

# RELEASE 1.2.11
- [🐛] Fixed NullPointerException crashes opening video settings (mb)

# RELEASE 1.2.10
- [❌] Dropped 1.18.2 and 1.19.2 (this version will be the last release of EMB++ for those versions)
- [❌] (1.18-1.19) Removed ZUME integration (prevent crashes with modern zume)
- [🌐] Updated translations (I forgot to rebase again)
  - [🍾] Updated Russian translation (by mpustovoi)
