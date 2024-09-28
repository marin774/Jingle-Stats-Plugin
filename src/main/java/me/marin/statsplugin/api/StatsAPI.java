package me.marin.statsplugin.api;

import me.marin.statsplugin.StatsPlugin;
import me.marin.statsplugin.stats.Session;

public class StatsAPI {

    public static Session getCurrentSession() {
        return StatsPlugin.CURRENT_SESSION;
    }

}
