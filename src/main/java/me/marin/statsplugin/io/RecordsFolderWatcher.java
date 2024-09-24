package me.marin.statsplugin.io;

import com.google.gson.JsonObject;
import me.marin.statsplugin.StatsPlugin;
import me.marin.statsplugin.util.StatsPluginUtil;
import me.marin.statsplugin.stats.Session;
import me.marin.statsplugin.stats.StatsRecord;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecordsFolderWatcher extends FileWatcher {

    // https://stackoverflow.com/questions/69389964/how-to-convert-a-temporalaccessor-a-milliseconds-timestamp-using-instant
    public static final DateTimeFormatter DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("M/d/yyyy HH:mm:ss")
            .toFormatter()
            .withZone(ZoneId.systemDefault());
    private static final long ONE_HOUR_MS = 1000 * 60 * 60;

    private int wallResetsSincePrev = 0;
    private int splitlessResets = 0;
    private long RTASincePrev = 0;
    private long breakRTASincePrev = 0;
    private long wallTimeSincePrev = 0;

    private String RTADistribution = "";

    // Completed runs get updated on completion and on reset, which means they would get tracked twice - this prevents that.
    private final List<String> completedRunsRecordIds = new ArrayList<>();

    // Sometimes record files get updated twice (without changing)
    // FIXME: if more splits get added, ignore this
    private final List<String> mostRecentRecordIds = new ArrayList<>();

    public RecordsFolderWatcher(Path path) {
        super("records-folder-watcher", path.toFile());
        Jingle.log(Level.DEBUG, "(StatsPlugin) Records folder watcher is running...");
    }

    @Override
    protected void handleFileUpdated(File file) {
        Jingle.log(Level.DEBUG, "(StatsPlugin) records> " + file.getName());
        if (!StatsPluginSettings.getInstance().trackerEnabled) {
            return;
        }
        if (completedRunsRecordIds.contains(file.getName()) || mostRecentRecordIds.contains(file.getName())) {
            Jingle.log(Level.DEBUG, "(StatsPlugin) Not saving run because it was already completed/recently updated.");
            return;
        }

        JsonObject recordJSON = StatsPluginUtil.readJSON(file);
        if (recordJSON == null || recordJSON.isJsonNull()) {
            Jingle.log(Level.DEBUG, "(StatsPlugin) Not saving run because record is null.");
            return;
        }

        RecordParser recordParser = new RecordParser(recordJSON);
        if (!recordParser.validateRSG()) {
            Jingle.log(Level.DEBUG, "(StatsPlugin) Not saving run because it's not rsg.");
            return;
        }
        if (recordParser.getRTA() - recordParser.getIGT() > ONE_HOUR_MS) {
            Jingle.log(Level.DEBUG, "(StatsPlugin) Not saving run because it happened too long ago.");
            return;
        }

        long finalRTA = recordJSON.get("final_rta").getAsLong();
        Long LAN = recordParser.getOpenLAN();
        if (LAN != null && LAN <= finalRTA) {
            finalRTA = LAN;
        }

        Jingle.log(Level.DEBUG, "(StatsPlugin) records> finalRTA " + finalRTA);

        // wall time calculation
        for (RSGAttemptsWatcher attemptsWatcher : InstanceManagerRunnable.instanceWatcherMap.values()) {
            breakRTASincePrev += attemptsWatcher.getBreakRTASincePrev();
            wallResetsSincePrev += attemptsWatcher.getWallResetsSincePrev();
            wallTimeSincePrev += attemptsWatcher.getWallTimeSincePrev();
            attemptsWatcher.reset();
        }

        Jingle.log(Level.DEBUG, "(StatsPlugin) records> read from attempts watcher");

        if (finalRTA == 0) {
            // wallResetsSincePrev++;
            Jingle.log(Level.DEBUG, "(StatsPlugin) final rta = 0, skipping");
            return;
        }

        mostRecentRecordIds.add(file.getName());
        if (mostRecentRecordIds.size() > 5) {
            mostRecentRecordIds.remove(0);
        }

        RTADistribution += finalRTA/1000 + "$";

        if (!recordParser.hasDoneAnySplit()) {
            Jingle.log(Level.DEBUG, "(StatsPlugin) Not saving run because it has no splits. (" + finalRTA + "ms rta)");
            splitlessResets++;
            RTASincePrev += finalRTA;
            return;
        }

        Jingle.log(Level.DEBUG, "(StatsPlugin) Done splits: " + recordParser.hasObtainedIron() + ", " + recordParser.hasObtainedWood() + ", " + recordParser.hasObtainedWood() + ", " + recordParser.getTimelinesMap());

        String date = DATETIME_FORMATTER.format(Instant.ofEpochMilli(recordParser.getDate()));
        Map<String, Long> timelines = recordParser.getTimelinesMap();

        StatsRecord csvRecord = new StatsRecord(
                date,
                recordParser.getIronSource(),
                recordParser.getEnterType(),
                recordParser.getSpawnBiome(),
                recordParser.getRTA(),
                recordParser.getWoodObtainedTime(),
                recordParser.getPickaxeTime(),
                timelines.get("enter_nether"),
                timelines.get("enter_bastion"),
                timelines.get("enter_fortress"),
                timelines.get("nether_travel"),
                timelines.get("enter_stronghold"),
                timelines.get("enter_end"),
                recordParser.getRTT(),
                recordParser.getIGT(),
                String.valueOf(recordParser.getBlazeRodsPickedUp()),
                String.valueOf(recordParser.getBlazesKilled()),
                recordParser.getIronObtainedTime(),
                String.valueOf(wallResetsSincePrev),
                String.valueOf(splitlessResets),
                Math.max(0, RTASincePrev),
                breakRTASincePrev,
                wallTimeSincePrev,
                StatsPlugin.CURRENT_SESSION.isEmpty() ? Session.SESSION_MARKER : "",
                RTADistribution
        );

        if (recordParser.isCompleted()) {
            completedRunsRecordIds.add(file.getName());
        }

        StatsPlugin.CURRENT_SESSION.addRun(csvRecord, true);
        StatsFileIO.getInstance().writeStats(csvRecord);
        if (StatsPluginSettings.getInstance().useSheets && StatsPlugin.googleSheets.isConnected()) {
            StatsPlugin.googleSheets.insertRecord(csvRecord);
        }

        wallResetsSincePrev = 0;
        splitlessResets = 0;
        RTASincePrev = 0;
        breakRTASincePrev = 0;
        wallTimeSincePrev = 0;
        RTADistribution = "";
    }



    @Override
    protected void handleFileCreated(File file) {
        // ignored
    }


}
