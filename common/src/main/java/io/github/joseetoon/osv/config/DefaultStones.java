package io.github.joseetoon.osv.config;

import lombok.experimental.UtilityClass;
import io.github.joseetoon.osv.util.Group;

@UtilityClass
public class DefaultStones {

    public static final Group[] LISTED = {
        Group.named("biomesoplenty").withEntries("white_sandstone", "orange_sandstone", "black_sandstone", "red_rock").implicitNamespace(),
        Group.named("biomeswevegone").withEntries("rocky_stone", "red_rock", "dacite", "white_dacite", "mossy_stone", "windswept_sand", "windswept_sandstone").implicitNamespace(),
        Group.named("create").withEntries("limestone", "scoria", "ochrum", "veridium", "crimsite", "asurine", "scorchia").implicitNamespace(),
        Group.named("minecraft").withEntries(
            "stone", "andesite", "diorite", "granite", "tuff", "deepslate", "calcite", "blackstone", "smooth_basalt", "dripstone_block", "clay", "moss_block"),
        Group.named("quark").withEntries("jasper", "limestone", "shale").implicitNamespace(),
        Group.named("regions_unexplored").withEntries("asphalt", "chalk").implicitNamespace()
    };

    public static final Group[] UNLISTED = {
        Group.named("minecraft").withEntries("gravel", "magma_block"),
        Group.named("quark").withEntries("myalite").implicitNamespace()
    };

    public static final String[] NAMES = { "biomesoplenty", "biomeswevegone", "create", "minecraft", "quark", "regions_unexplored" };
}
