package io.github.joseetoon.genlib.config;

import org.hjson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HjsonFileConfigTest {

    @Test
    void nestedPathsCanBeCreatedFromEmptyConfig() {
        final HjsonFileConfig config = new HjsonFileConfig(
            new File("build/tmp/test-hjson-config.hjson"),
            new JsonObject()
        );
        final List<String> path = List.of("general", "enableGlobalLibCommands");

        assertFalse(config.contains(path));

        config.set(path, true);
        config.setComment(path, "test comment");

        assertTrue(config.contains(path));
        assertEquals(Boolean.TRUE, config.getRaw(path));
        assertEquals("test comment", config.getComment(path));
    }
}
