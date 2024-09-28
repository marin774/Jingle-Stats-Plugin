package me.marin.statsplugin;

import com.google.common.io.Resources;
import me.marin.statsplugin.gui.OBSOverlayGUI;
import me.marin.statsplugin.gui.StatsGUI;
import me.marin.statsplugin.io.*;
import me.marin.statsplugin.stats.Session;
import me.marin.statsplugin.util.GoogleSheets;
import me.marin.statsplugin.util.StatsPluginUtil;
import me.marin.statsplugin.util.UpdateUtil;
import me.marin.statsplugin.util.VersionUtil;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import xyz.duncanruns.jingle.gui.JingleGUI;
import xyz.duncanruns.jingle.plugin.PluginManager;
import xyz.duncanruns.jingle.util.ExceptionUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import static me.marin.statsplugin.util.VersionUtil.CURRENT_VERSION;

public class StatsPlugin {

    public static final Path STATS_FOLDER_PATH = Jingle.FOLDER.resolve("stats-plugin");
    public static final Path PLUGINS_PATH = Jingle.FOLDER.resolve("plugins");
    public static final Path GOOGLE_SHEETS_CREDENTIALS_PATH = STATS_FOLDER_PATH.resolve("credentials.json");
    public static final Path STATS_SETTINGS_PATH = STATS_FOLDER_PATH.resolve("settings.json");
    public static final Path OBS_OVERLAY_TEMPLATE_PATH = STATS_FOLDER_PATH.resolve("obs-overlay-template");
    public static final Path OBS_OVERLAY_PATH = STATS_FOLDER_PATH.resolve("obs-overlay.txt");

    public static Session CURRENT_SESSION = new Session();

    public static StatsGUI statsGUI;
    public static GoogleSheets googleSheets;

    private static final int THREE_HOURS_MS = 1000 * 60 * 60 * 3;

    public static void main(String[] args) throws IOException {
        JingleAppLaunch.launchWithDevPlugin(args, PluginManager.JinglePluginData.fromString(
                Resources.toString(Resources.getResource(StatsPlugin.class, "/jingle.plugin.json"), Charset.defaultCharset())
        ), StatsPlugin::initialize);
    }

    public static void initialize() {
        Jingle.log(Level.INFO, "(StatsPlugin) Running StatsPlugin v" + CURRENT_VERSION + "!");

        boolean isFirstLaunch = !STATS_FOLDER_PATH.toFile().exists();
        STATS_FOLDER_PATH.toFile().mkdirs();

        if (isFirstLaunch) {
            UpdateUtil.importSettingsFromJulti();
        }

        OBSOverlayGUI.createDefaultFile();

        Session previousSession = StatsFileIO.getInstance().getLatestSession();
        if (!previousSession.isEmpty()) {
            String dateTime = previousSession.getLatestRecord().dateTime();

            long timeSince = Math.abs(Duration.between(Instant.now(), StatsPluginUtil.dateTimeToInstant(dateTime)).toMillis());
            Jingle.log(Level.DEBUG, "(StatsPlugin) Last record in previous session: " + dateTime + "(" + timeSince + "ms ago)");

            if (timeSince < THREE_HOURS_MS) {
                // less than 3 hours since previous session, merge
                CURRENT_SESSION = previousSession;
                Jingle.log(Level.INFO, "(StatsPlugin) Continuing previous session.");
            }
        }

        CURRENT_SESSION.updateOverlay();

        StatsPluginSettings.load();
        VersionUtil.Version version = getVersionFromSettings();
        if (version.isOlderThan(CURRENT_VERSION)) {
            updateFrom(version);
        }

        StatsPlugin.reloadGoogleSheets();

        if (StatsPluginSettings.getInstance().deleteOldRecords) {
            StatsPluginUtil.runAsync("old-record-bopper", new OldRecordBopperRunnable());
        }

        Path recordsPath;
        try {
            recordsPath = Paths.get(StatsPluginSettings.getInstance().recordsPath);
        } catch (Exception e) {
            Jingle.log(Level.ERROR, "(StatsPlugin) Invalid SpeedrunIGT records folder in settings, change it manually settings.json and restart Jingle!\n"+ ExceptionUtil.toDetailedString(e));
            return;
        }
        StatsPluginUtil.runAsync("records-folder-watcher", new RecordsFolderWatcher(recordsPath));
        StatsPluginUtil.runTimerAsync(new InstanceManagerRunnable(), 1000);

        VersionUtil.deleteOldVersionJars();
        UpdateUtil.checkForUpdatesAndUpdate(true);

        statsGUI = new StatsGUI();
        JingleGUI.addPluginTab("Stats", statsGUI);
    }

    private static VersionUtil.Version getVersionFromSettings() {
        return VersionUtil.version(StatsPluginSettings.getInstance().version);
    }

    public static void updateFrom(VersionUtil.Version version) {
        Jingle.log(Level.INFO, "(StatsPlugin) Updating from version " + version + ".");

        // currently unused.

        StatsPluginSettings.getInstance().version = CURRENT_VERSION.toString();
        StatsPluginSettings.save();
        Jingle.log(Level.INFO, "(StatsPlugin) Updated to v" + CURRENT_VERSION + "!");
    }

    public static boolean reloadGoogleSheets() {
        if (StatsPluginSettings.getInstance().useSheets && Files.exists(GOOGLE_SHEETS_CREDENTIALS_PATH) && StatsPluginSettings.getInstance().sheetLink != null) {
            googleSheets = new GoogleSheets(GOOGLE_SHEETS_CREDENTIALS_PATH);
            return googleSheets.connect();
        }
        return false;
    }


}
