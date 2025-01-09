# Jingle Stats Plugin
A Jingle plugin to make tracking stats even easier. This tracker works almost exactly the same as [pncakespoons's tracker](https://github.com/pncakespoon1/ResetTracker), and it works with [Specnr's Stats Website](https://reset-analytics-dev.vercel.app/). It's compatible with SeedQueue.

## Installation
Download the latest version from the [Releases page](https://github.com/marin774/Jingle-Stats-Plugin/releases), then drag and drop into your Jingle plugins folder, and restart Jingle.

## Setup
Once you installed the plugin and restarted Jingle, run the quick setup:
1. Open the "Plugins" tab in Jingle.
2. Click on "Stats" tab.
3. Click on "Setup Tracker" button and follow the setup.
![image](https://github.com/user-attachments/assets/fcf4dbad-136d-467d-bd30-9746450a6e31)

> Note: Some stats aren't being tracked with this plugin. This includes tracking world seed, dropped gold, blocks mined, pearls thrown, deaths etc. They might be added in a future update.

## Config
Depending on what you did during setup, this menu might look different.
![image](https://github.com/user-attachments/assets/6225ae84-4906-4f4f-960e-d8569258186d)

### Utility:
- **Configure OBS overlay** - has `%enters%`, `%nph%`, `%rpe%` and `%average%` variables, you can format it in any way
![image](https://github.com/user-attachments/assets/8e57a790-f533-416e-a25b-c4e9d5eaf433)

- **View Stats** - opens [Specnr's Reset Analytics Website](https://reset-analytics-dev.vercel.app/) with your stats.
- **Open Google Sheet** - opens your stats Google Sheet

### Debug:
- **Start a new session** - starts a new session (0 nethers, 0:00 average, 0 nph) with your next run
- **Test Google Sheets connection** - it does that

### Settings
- **Enable Tracker?** - if you don't want your stats to track, simply uncheck this
- **Edit file manually** - opens the `settings.json` file you can edit (remember to press **Reload Settings** once you're done!)
- **Reload settings** - reloads settings from disk if you manually edited them

## Quick Action Buttons

- **Start new session** - starts a new session (0 nethers, 0:00 average, 0 nph) with your next run
  
![image](https://github.com/user-attachments/assets/f4d020ec-f491-478a-89bd-86dfd870fc38)

## Known issues
- SpeedrunIGT 'Make Record' setting has to be set to 'Every run' (and NOT 'Completed Run')
![image](https://github.com/marin774/Julti-Stats-Plugin/assets/87690741/50276c61-a03a-470d-b430-731337c4f811)

---

#### If you have any questions, contact me on discord @marin774
