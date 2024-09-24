# Jingle Stats Plugin
A Jingle plugin to make tracking stats even easier. This tracker works almost exactly the same as [pncakespoons's tracker](https://github.com/pncakespoon1/ResetTracker), and it works with [Specnr's Stats Website](https://reset-analytics-dev.vercel.app/).

## Installation
Download the latest version from the [Releases page](https://github.com/marin774/Jingle-Stats-Plugin/releases), then drag and drop into your Jingle plugins folder, and restart Jingle.

## Setup
Once you installed the plugin and restarted Jingle, run the quick setup:
1. Open the "Plugins" tab in Jingle.
2. Click on "Stats" tab.
3. Click on "Setup Tracker" button and follow the setup.
![image](https://github.com/marin774/Julti-Stats-Plugin/assets/87690741/2670e8a6-7ac4-4b7f-acc4-3e7a47372faa)


> Note: Some stats that aren't being tracked with this plugin. This includes tracking world seed, dropped gold, blocks mined, pearls thrown, deaths etc. They might be added in a future update.

## Config
Depending on what you did during setup, this menu might look different.
![image](https://github.com/marin774/Julti-Stats-Plugin/assets/87690741/ad245fcc-6825-410a-8379-bf4294404719)

### Utility:
- **Configure OBS overlay** - has `%enters%`, `%nph%` and `%average%` variables, you can format it however you want
![image](https://github.com/marin774/Julti-Stats-Plugin/assets/87690741/a12ff5a1-e640-475a-b0bd-74ede434c3b8)

- **Clear SpeedrunIGT records** - deletes all record files in SpeedrunIGT record folder.
- **View Stats in browser** - opens [Specnr's Stats Website](https://reset-analytics-dev.vercel.app/) with your stats.
- **Open Google Sheet** - opens your stats Google Sheet

### Debug:
- **Reload settings** - reloads settings from disk if you manually edited them
- **Start a new session** - starts a new session (0 nethers, 0:00 average, 0 nph) with your next run
- **Reconnect to Google Sheets** - it does that

### Settings
- **Enable Tracker?** - if you don't want your stats to track, simply uncheck this
- **Edit file manually** - opens the `settings.json` file you can edit (remember to press **Reload Settings** once you're done!)

## Known issues
- SpeedrunIGT 'Make Record' setting has to be set to 'Every run' (and NOT 'Completed Run')
![image](https://github.com/marin774/Julti-Stats-Plugin/assets/87690741/50276c61-a03a-470d-b430-731337c4f811)

---

### If you have any questions, contact me on discord @marin774