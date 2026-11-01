package dev.mending.core.api.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

final class TextInputTest {

    private static final Component TITLE = Component.text("Name");

    @Test
    void everyTextIsFineWithoutAValidator() {
        assertNull(TextInput.create(TITLE).errorFor(""));
    }

    @Test
    void theValidatorDecides() {
        final Component tooShort = Component.text("Too short");
        final TextInput input = TextInput.create(TITLE).validate(text -> text.length() < 3 ? tooShort : null);

        assertEquals(tooShort, input.errorFor("ab"));
        assertNull(input.errorFor("abc"));
    }

    @Test
    void impossibleSizesAreRefused() {
        final TextInput input = TextInput.create(TITLE);

        assertThrows(IllegalArgumentException.class, () -> input.maxLength(0));
        assertThrows(IllegalArgumentException.class, () -> input.width(0));
        assertThrows(IllegalArgumentException.class, () -> input.width(1025));
        assertThrows(IllegalArgumentException.class, () -> input.multiline(0, 100));
        assertThrows(IllegalArgumentException.class, () -> input.multiline(3, 513));
    }
}
