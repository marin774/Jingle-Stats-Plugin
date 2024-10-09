package me.marin.statsplugin.io;

import me.marin.statsplugin.util.StatsPluginUtil;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.instance.InstanceChecker;
import xyz.duncanruns.jingle.instance.OpenedInstanceInfo;
import xyz.duncanruns.jingle.util.ExceptionUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static me.marin.statsplugin.StatsPlugin.log;

/**
 * Creates new rsg-attempts.txt watchers when new instances appear.
 */
public class InstanceManagerRunnable implements Runnable {

    public static final Map<String, RSGAttemptsWatcher> instanceWatcherMap = new HashMap<>();

    private final HashSet<String> previousOpenInstancePaths = new HashSet<>();

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            log(Level.DEBUG, "(StatsPlugin) Error while tracking resets & wall time:\n" + ExceptionUtil.toDetailedString(e));
        }
    }

    public void doRun() {
        Set<OpenedInstanceInfo> currentOpenInstances = InstanceChecker.getAllOpenedInstances();

        Set<String> currentOpenInstancePaths = currentOpenInstances.stream()
                .map(i -> i.instancePath.toString())
                .collect(Collectors.toSet());

        HashSet<String> closedInstancePaths = new HashSet<>(previousOpenInstancePaths);
        closedInstancePaths.removeAll(currentOpenInstancePaths);

        for (String closedInstancePath : closedInstancePaths) {
            if (instanceWatcherMap.containsKey(closedInstancePath)) {
                // close old watchers (this instance was just closed)
                instanceWatcherMap.get(closedInstancePath).stop();
                instanceWatcherMap.remove(closedInstancePath);
                log(Level.DEBUG, "Closed a FileWatcher for instance: " + closedInstancePath);
            }
        }

        for (OpenedInstanceInfo instance : currentOpenInstances) {
            String path = instance.instancePath.toString();
            if (!instanceWatcherMap.containsKey(path)) {
                Path rsgAttemptsPath = Paths.get(path, "config", "mcsr", "atum", "rsg-attempts.txt");

                // Wait until the file exists (if they just set up/updated Atum OR if it's a misc instance, this won't exist)
                if (!Files.exists(rsgAttemptsPath)) {
                    continue;
                }

                // start a new watcher
                Path atumDirectory = Paths.get(path, "config", "mcsr", "atum");
                Path wpStateoutPath = Paths.get(path, "wpstateout.txt");

                log(Level.DEBUG, "Starting a new FileWatcher for instance: " + path);

                RSGAttemptsWatcher watcher = new RSGAttemptsWatcher(atumDirectory, wpStateoutPath);
                StatsPluginUtil.runAsync("rsg-attempts-watcher", watcher);
                instanceWatcherMap.put(path, watcher);
            }
        }

        previousOpenInstancePaths.clear();
        previousOpenInstancePaths.addAll(currentOpenInstancePaths);
    }

}
