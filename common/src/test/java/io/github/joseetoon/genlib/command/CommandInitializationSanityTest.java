package io.github.joseetoon.genlib.command;

import io.github.joseetoon.genlib.util.LibReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandInitializationSanityTest {

    @Test
    void libCommandSuggestionsUseValidVendoredNamespace() {
        assertEquals("genlib", LibReference.MOD_ID);
        assertDoesNotThrow(() -> Class.forName(CommandSuggestions.class.getName()));
    }
}
