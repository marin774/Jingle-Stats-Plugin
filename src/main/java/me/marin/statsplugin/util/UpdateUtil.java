package me.marin.statsplugin.util;

import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import com.google.gson.JsonObject;
import lombok.Data;
import me.marin.statsplugin.StatsPlugin;
import me.marin.statsplugin.gui.UpdateProgressFrame;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.JingleAppLaunch;
import xyz.duncanruns.jingle.gui.JingleGUI;
import xyz.duncanruns.jingle.util.ExceptionUtil;
import xyz.duncanruns.jingle.util.GrabUtil;
import xyz.duncanruns.jingle.util.PowerShellUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Majority of the code from <a href="https://github.com/DuncanRuns/Jingle/blob/main/src/main/java/xyz/duncanruns/jingle/JingleUpdater.java">Jingle</a>
 */
public class UpdateUtil {

    public static void checkForUpdatesAndUpdate(boolean isOnLaunch) {
        StatsPluginUtil.runAsync("update-checker", () -> {
            UpdateUtil.UpdateInfo updateInfo = UpdateUtil.tryCheckForUpdates();
            if (updateInfo.isSuccess()) {
                int choice = JOptionPane.showConfirmDialog(null, updateInfo.getMessage(), "Update found!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    UpdateUtil.tryUpdateAndRelaunch(updateInfo.getDownloadURL());
                }
            } else {
                if (!isOnLaunch) {
                    JOptionPane.showMessageDialog(null, updateInfo.getMessage());
                }
            }
        });
    }

    public static UpdateInfo tryCheckForUpdates() {
        try {
            return checkForUpdates();
        } catch (Exception e) {
            return new UpdateInfo(false, "Could not check for updates. Github might be rate-limiting you, try again later.", null);
        }
    }

    public synchronized static UpdateInfo checkForUpdates() throws IOException {
        JsonObject meta = GrabUtil.grabJson("https://raw.githubusercontent.com/marin774/Jingle-Stats-Plugin/main/meta.json");

        Jingle.log(Level.DEBUG, "(StatsPlugin) Grabbed Stats meta: " + meta.toString());

        VersionUtil.Version latestVersion = VersionUtil.version(meta.get("latest").getAsString());
        String downloadURL = meta.get("latest_download").getAsString();
        boolean isOutdated = VersionUtil.CURRENT_VERSION.isOlderThan(latestVersion);

        if (isOutdated) {
            return new UpdateInfo(true, "New Stats Plugin version found: v" + latestVersion + "! Update now?", downloadURL);
        } else {
            return new UpdateInfo(false, "No new versions found.", null);
        }
    }

    @Data
    public static class UpdateInfo {
        private final boolean success;
        private final String message;
        private final String downloadURL;
    }

    public static void tryUpdateAndRelaunch(String download) {
        try {
            updateAndRelaunch(download);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unknown error while updating. Try again or update manually.");
            Jingle.log(Level.ERROR, "(StatsPlugin) Unknown error while updating:\n" + ExceptionUtil.toDetailedString(e));
        }
    }

    private static void updateAndRelaunch(String download) throws IOException, PowerShellExecutionException, InterruptedException, InvocationTargetException {
        Path newJarPath = StatsPlugin.PLUGINS_PATH.resolve(URLDecoder.decode(FilenameUtils.getName(download), StandardCharsets.UTF_8.name()));

        if (!Files.exists(newJarPath)) {
            Jingle.log(Level.DEBUG, "(StatsPlugin) Downloading new jar to " + newJarPath);
            downloadWithProgress(download, newJarPath);
            Jingle.log(Level.DEBUG, "(StatsPlugin) Downloaded new jar " + newJarPath.getFileName());
        }

        Path javaExe = Paths.get(System.getProperty("java.home")).resolve("bin").resolve("javaw.exe");

        // Release LOCK so updating can go smoothly
        JingleAppLaunch.releaseLock();
        Jingle.options.save();

        // Use powershell's start-process to start it detached
        String powerCommand = String.format("start-process '%s' '-jar \"%s\"'", javaExe, Jingle.getSourcePath());
        Jingle.log(Level.INFO, "(StatsPlugin) Exiting and running powershell command: " + powerCommand);
        PowerShellUtil.execute(powerCommand);

        System.exit(0);
    }

    private static void downloadWithProgress(String download, Path newJarPath) throws IOException {
        Point location = JingleGUI.get().getLocation();
        JProgressBar bar = new UpdateProgressFrame(location).getBar();
        bar.setMaximum((int) GrabUtil.getFileSize(download));
        GrabUtil.download(download, newJarPath, bar::setValue, 128);
    }

}
