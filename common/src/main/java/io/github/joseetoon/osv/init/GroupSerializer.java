package io.github.joseetoon.osv.init;

import io.github.joseetoon.osv.config.Cfg;
import io.github.joseetoon.osv.config.DefaultOres;
import io.github.joseetoon.osv.config.DefaultStones;
import io.github.joseetoon.osv.util.Group;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupSerializer {

    public static Map<String, Group> loadPropertyGroups() {
        return loadEntries(Cfg.propertyGroups(), DefaultOres.LISTED);
    }

    public static Map<String, Group> loadBlockGroups() {
        return loadEntries(Cfg.blockGroups(), DefaultStones.LISTED);
    }

    private static Map<String, Group> loadEntries(final Map<String, List<String>> listed, final Group[] defaults) {
        final Map<String, Group> groups = new HashMap<>();
        for (final Map.Entry<String, List<String>> entry : listed.entrySet()) {
            groups.put(entry.getKey(), new Group(entry.getKey(), entry.getValue()));
        }
        final Set<Group> currentDefaults = Stream.of(defaults).map(g -> groups.get(g.getName())).collect(Collectors.toSet());
        groups.put(Group.ALL, Group.collect(Group.ALL, groups.values()));
        groups.put(Group.DEFAULT, Group.collect(Group.DEFAULT, currentDefaults));
        return groups;
    }
}
