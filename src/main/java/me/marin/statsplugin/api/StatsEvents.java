package me.marin.statsplugin.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.marin.statsplugin.stats.StatsRecord;
import org.apache.logging.log4j.Level;
import xyz.duncanruns.jingle.Jingle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class StatsEvents {

    private static final Map<Type, List<Consumer<StatsEvent>>> observersMap = new HashMap<>();

    public static void registerListener(Type type, Consumer<StatsEvent> onEvent) {
        observersMap.computeIfAbsent(type, k -> new ArrayList<>()).add(onEvent);
        Jingle.log(Level.DEBUG, "Registered a listener for " + type + ".");
    }

    public static void broadcastEvent(StatsEvent event) {
        observersMap.getOrDefault(event.type, new ArrayList<>()).forEach(c -> c.accept(event));
    }


    @AllArgsConstructor
    public static class StatsEvent {
        private Type type;
    }

    public enum Type {
        NEW_RUN,
    }

    @Getter
    public static class NewRunEvent extends StatsEvent {
        private final StatsRecord record;

        public NewRunEvent(StatsRecord record) {
            super(Type.NEW_RUN);
            this.record = record;
        }
    }



}
