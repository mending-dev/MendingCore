package dev.mending.core.api.gui;

import dev.mending.core.api.MendingCore;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Asks a player for a text in one of Minecraft's dialog windows. All text is yours; the buttons say OK and Cancel in
 * the player's language until you give them other labels. Inside a click handler open it with
 * {@link GuiClick#open(TextInput)}.
 *
 * One instance can be opened for many players. Main thread only.
 */
public final class TextInput {

    private static final String KEY = "text";
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 512;

    private final Component title;
    private final List<Component> body = new ArrayList<>();

    private Component label = Component.empty();
    private boolean labelVisible = true;
    private String initial = "";
    private int maxLength = 256;
    private int width = 200;
    private TextDialogInput.MultilineOptions multiline;
    private Component confirmLabel = Component.translatable("gui.ok");
    private Component cancelLabel = Component.translatable("gui.cancel");
    private Component confirmTooltip;
    private Component cancelTooltip;
    private boolean closeWithEscape = true;
    private Duration timeout = Duration.ofMinutes(10);
    private Function<? super String, ? extends @Nullable Component> validator = text -> null;
    private BiConsumer<Player, String> onSubmit = (player, text) -> { };
    private Consumer<Player> onCancel = player -> { };
    private Gui returnTo;
    private Consumer<DialogBase.Builder> baseOptions = base -> { };
    private Consumer<TextDialogInput.Builder> inputOptions = input -> { };

    private TextInput(@NotNull Component title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public static @NotNull TextInput create(@NotNull Component title) {
        return new TextInput(title);
    }

    /**
     * The name above the text field.
     */
    public @NotNull TextInput label(@NotNull Component label) {
        this.label = Objects.requireNonNull(label, "label");
        return this;
    }

    public @NotNull TextInput labelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
        return this;
    }

    /**
     * Text shown above the field, one call per paragraph.
     */
    public @NotNull TextInput message(@NotNull Component message) {
        this.body.add(Objects.requireNonNull(message, "message"));
        return this;
    }

    /**
     * What the field holds when it opens.
     */
    public @NotNull TextInput initial(@NotNull String initial) {
        this.initial = Objects.requireNonNull(initial, "initial");
        return this;
    }

    /**
     * The most characters a player can type. Default: 256.
     */
    public @NotNull TextInput maxLength(int maxLength) {
        if (maxLength < 1) {
            throw new IllegalArgumentException("maxLength must be at least 1");
        }
        this.maxLength = maxLength;
        return this;
    }

    /**
     * The width of the field in pixels, from 1 to 1024. Default: 200.
     */
    public @NotNull TextInput width(int width) {
        if (width < 1 || width > MAX_WIDTH) {
            throw new IllegalArgumentException("width must be 1 to " + MAX_WIDTH);
        }
        this.width = width;
        return this;
    }

    /**
     * Lets the player type several lines.
     *
     * @param maxLines the most lines
     * @param height the height of the field in pixels, from 1 to 512
     */
    public @NotNull TextInput multiline(int maxLines, int height) {
        if (maxLines < 1) {
            throw new IllegalArgumentException("maxLines must be at least 1");
        }
        if (height < 1 || height > MAX_HEIGHT) {
            throw new IllegalArgumentException("height must be 1 to " + MAX_HEIGHT);
        }
        this.multiline = TextDialogInput.MultilineOptions.create(maxLines, height);
        return this;
    }

    public @NotNull TextInput confirmButton(@NotNull Component label) {
        return confirmButton(label, null);
    }

    public @NotNull TextInput confirmButton(@NotNull Component label, @Nullable Component tooltip) {
        this.confirmLabel = Objects.requireNonNull(label, "label");
        this.confirmTooltip = tooltip;
        return this;
    }

    public @NotNull TextInput cancelButton(@NotNull Component label) {
        return cancelButton(label, null);
    }

    public @NotNull TextInput cancelButton(@NotNull Component label, @Nullable Component tooltip) {
        this.cancelLabel = Objects.requireNonNull(label, "label");
        this.cancelTooltip = tooltip;
        return this;
    }

    /**
     * Whether the Escape key closes the window. Default: true.
     */
    public @NotNull TextInput closeWithEscape(boolean closeWithEscape) {
        this.closeWithEscape = closeWithEscape;
        return this;
    }

    /**
     * How long the buttons work after opening. Default: 10 minutes.
     */
    public @NotNull TextInput timeout(@NotNull Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        return this;
    }

    /**
     * Checks the text before {@link #onSubmit(BiConsumer)}. Returning a message shows the window again with what the
     * player typed and this message; {@code null} accepts the text.
     */
    public @NotNull TextInput validate(@NotNull Function<? super String, ? extends @Nullable Component> validator) {
        this.validator = Objects.requireNonNull(validator, "validator");
        return this;
    }

    /**
     * What happens with an accepted text. Player-controlled text: never put it into MiniMessage.
     */
    public @NotNull TextInput onSubmit(@NotNull BiConsumer<Player, String> onSubmit) {
        this.onSubmit = Objects.requireNonNull(onSubmit, "onSubmit");
        return this;
    }

    public @NotNull TextInput onCancel(@NotNull Consumer<Player> onCancel) {
        this.onCancel = Objects.requireNonNull(onCancel, "onCancel");
        return this;
    }

    /**
     * Opens this menu again after submit and cancel, redrawn, so it shows what changed.
     */
    public @NotNull TextInput returnTo(@Nullable Gui returnTo) {
        this.returnTo = returnTo;
        return this;
    }

    /**
     * Full access to the window's options, applied last.
     */
    public @NotNull TextInput base(@NotNull Consumer<DialogBase.Builder> options) {
        this.baseOptions = Objects.requireNonNull(options, "options");
        return this;
    }

    /**
     * Full access to the text field's options, applied last.
     */
    public @NotNull TextInput input(@NotNull Consumer<TextDialogInput.Builder> options) {
        this.inputOptions = Objects.requireNonNull(options, "options");
        return this;
    }

    /**
     * Shows the window. An open inventory closes first, so a menu's input slots are refunded.
     */
    public void open(@NotNull Player player) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Text inputs must only be opened on the main thread");
        }
        show(player, this.initial, null);
    }

    /** The message that refuses this text, or null if it is fine. */
    @Nullable Component errorFor(@NotNull String text) {
        return this.validator.apply(text);
    }

    private void show(Player player, String text, @Nullable Component error) {
        player.closeInventory();
        player.showDialog(dialog(text, error));
    }

    private Dialog dialog(String text, @Nullable Component error) {
        final List<DialogBody> paragraphs = new ArrayList<>();
        this.body.forEach(message -> paragraphs.add(DialogBody.plainMessage(message)));
        if (error != null) {
            paragraphs.add(DialogBody.plainMessage(error));
        }

        final TextDialogInput.Builder field = DialogInput.text(KEY, this.label)
            .labelVisible(this.labelVisible)
            .initial(text)
            .maxLength(this.maxLength)
            .width(this.width)
            .multiline(this.multiline);
        this.inputOptions.accept(field);

        final DialogBase.Builder window = DialogBase.builder(this.title)
            .canCloseWithEscape(this.closeWithEscape)
            .body(paragraphs)
            .inputs(List.of(field.build()));
        this.baseOptions.accept(window);

        final ClickCallback.Options once = ClickCallback.Options.builder().uses(1).lifetime(this.timeout).build();
        final ActionButton confirm = ActionButton.builder(this.confirmLabel)
            .tooltip(this.confirmTooltip)
            .action(DialogAction.customClick((response, audience) ->
                onMainThread(audience, player -> submit(player, response.getText(KEY))), once))
            .build();
        final ActionButton cancel = ActionButton.builder(this.cancelLabel)
            .tooltip(this.cancelTooltip)
            .action(DialogAction.customClick((response, audience) -> onMainThread(audience, this::cancel), once))
            .build();

        final DialogBase base = window.build();
        return Dialog.create(factory -> factory.empty().base(base).type(DialogType.confirmation(confirm, cancel)));
    }

    private void submit(Player player, @Nullable String typed) {
        final String text = typed == null ? "" : typed;
        final Component error = errorFor(text);
        if (error != null) {
            show(player, text, error);
            return;
        }
        this.onSubmit.accept(player, text);
        backToMenu(player);
    }

    private void cancel(Player player) {
        this.onCancel.accept(player);
        backToMenu(player);
    }

    private void backToMenu(Player player) {
        if (this.returnTo != null && player.isOnline()) {
            this.returnTo.refresh();
            this.returnTo.open(player);
        }
    }

    private static void onMainThread(Audience audience, Consumer<Player> action) {
        if (!(audience instanceof Player player)) {
            return;
        }
        if (Bukkit.isPrimaryThread()) {
            action.accept(player);
        } else {
            Bukkit.getScheduler().runTask(MendingCore.get().plugin(), () -> action.accept(player));
        }
    }
}
