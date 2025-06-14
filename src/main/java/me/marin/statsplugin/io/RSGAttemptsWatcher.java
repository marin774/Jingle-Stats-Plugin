package me.marin.statsplugin.io;

import lombok.Getter;
import me.marin.statsplugin.util.FileStillEmptyException;
import me.marin.statsplugin.util.StatsPluginUtil;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.util.ExceptionUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static me.marin.statsplugin.StatsPlugin.log;

/**
 * Reads Atum's rsg-attempts.txt for reset counts, and then checks wpstateout.txt in the same instance.
 * If the state is "wall", it calculates wall/break time.
 * <p>
 * This data is then used by {@link RecordsFolderWatcher}.
 */
public class RSGAttemptsWatcher extends FileWatcher {

    private final static String RSG_ATTEMPTS = "rsg-attempts.txt";
    private final Path wpStateoutPath;

    public RSGAttemptsWatcher(Path atumDirectory, Path wpStateoutPath) {
        super("rsg-attempts-watcher", atumDirectory.toFile());
        this.wpStateoutPath = wpStateoutPath;

        //log(Level.DEBUG, "rsg-attempts.txt watcher is running...");
        previousAtumResets = getAtumResets();
    }

    private String getWpStateout() {
        try {
            return StatsPluginUtil.readFile(wpStateoutPath);
        } catch (FileStillEmptyException e) {
            log(Level.ERROR, "wpstateout.txt is empty?\n" + ExceptionUtil.toDetailedString(e));
            return null;
        }
    }

    private long getAtumResets() {
        Path path = Paths.get(file.toPath().toString(), RSG_ATTEMPTS);

        String resetString;
        try {
            resetString = StatsPluginUtil.readFile(path);
        } catch (FileStillEmptyException e) {
            log(Level.ERROR, "rsg-attempts.txt is empty?\n" + ExceptionUtil.toDetailedString(e));
            return -1;
        }
        try {
            return Long.parseLong(resetString);
        } catch (NumberFormatException e) {
            log(Level.ERROR, "invalid number in rsg-attempts.txt '" + resetString + "':\n" + ExceptionUtil.toDetailedString(e));
            return -1;
        }
    }

    @Getter
    private int wallResetsSincePrev = 0;
    @Getter
    private long breakRTASincePrev = 0;
    @Getter
    private long wallTimeSincePrev = 0;

    private long lastActionMillis = 0;

    private long previousAtumResets = 0;

    /**
     * Important: This method is called after each world is created, and considering that
     *  SeedQueue could be creating worlds in the background for seconds after
     *  players reset a world, *wall time and break time can not be fully accurate*.
     */
    @Override
    protected void handleFileUpdated(File file) {
        if (!StatsPluginSettings.getInstance().trackerEnabled) {
            return;
        }
        if (!file.getName().equals(RSG_ATTEMPTS)) {
            return;
        }
        String state = getWpStateout();
        boolean isWallActive = "wall".equals(state) && Jingle.isInstanceActive();

        long atumResets = getAtumResets();
        if (atumResets < 0 || atumResets == previousAtumResets) {
            return;
        }
        //tryLog(Level.DEBUG, "Resets: " + atumResets + ", state: " + state, atumResets);

        if (isWallActive) {
            long delta = Math.max(0, atumResets - previousAtumResets);
            if (delta > 1000) {
                delta = 1; // prevent messing up stats if file is manually edited.
            }
            wallResetsSincePrev += (int) delta;
            //tryLog(Level.DEBUG, "Wall resets +" + delta + " (" + wallResetsSincePrev + " total).", atumResets);
        }
        previousAtumResets = atumResets;

        long now = System.currentTimeMillis();

        // Calculate wall breaks
        if (lastActionMillis > 0) {
            long delta = now - lastActionMillis;
            if (isWallActive) {
                if (delta > StatsPluginSettings.getInstance().breakThreshold * 1000L) {
                    breakRTASincePrev += delta;
                    //tryLog(Level.DEBUG, "Break RTA +" + delta + "ms (" + breakRTASincePrev + "ms total).", atumResets);
                } else {
                    wallTimeSincePrev += delta;
                    //tryLog(Level.DEBUG, "Wall time since prev. +" + delta + "ms (" + wallTimeSincePrev + "ms total).", atumResets);
                }
            }
        }
        this.lastActionMillis = now;
    }

    @Override
    protected void handleFileCreated(File file) {
        // ignored
    }

    public void reset() {
        wallResetsSincePrev = 0;
        breakRTASincePrev = 0;
        wallTimeSincePrev = 0;
    }

    private void tryLog(Level level, String message, long atumResets) {
        if (atumResets % 50 == 0) {
            log(level, message);
        }
    }

}
