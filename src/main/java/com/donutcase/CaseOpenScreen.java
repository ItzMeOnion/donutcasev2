package com.donutcase;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CaseOpenScreen extends Screen {

    private static final int CARD_W = 84;
    private static final int CARD_H = 96;
    private static final int WINDOW_W = CARD_W * 7;
    private static final int WINDOW_H = 104;
    private static final int STRIP_LENGTH = 64;
    private static final int WINNER_INDEX = 52;
    private static final long SPIN_MS = 6500L;

    private static final int PANEL_BG = 0xF0101014;
    private static final int REEL_BG = 0xE0101014;

    private static final String DISCLAIMER =
            "Worth values are rough estimates based on prices at the time they were entered. "
            + "Prices change, so treat them as approximate. This is a cosmetic client-side mod: "
            + "nothing you see is a real item or reward, you cannot get these items through it, "
            + "and it has no connection to any server or its economy. "
            + "Check a server's rules before using any client mod on it.";

    private enum State { IDLE, SPINNING, RESULT }

    private final CaseDefinition caseDef;
    private final Random random = new Random();
    private final List<CaseItem> strip = new ArrayList<>();

    private State state = State.IDLE;
    private CaseItem result;
    private double offset;
    private double targetOffset;
    private long spinStart;
    private boolean showDisclaimer;

    private Button openButton;

    public CaseOpenScreen(CaseDefinition caseDef) {
        super(Component.literal(caseDef.name()));
        this.caseDef = caseDef;
    }

    @Override
    protected void init() {
        clearWidgets();
        int cx = width / 2;
        int y = height / 2 + 80;

        openButton = addRenderableWidget(Button.builder(Component.literal("Open case"), b -> startSpin())
                .bounds(cx - 110, y, 220, 20)
                .build());
        openButton.active = state != State.SPINNING;

        addRenderableWidget(Button.builder(Component.literal("Info"), b -> showDisclaimer = !showDisclaimer)
                .bounds(cx - 110, y + 26, 220, 20)
                .build());

        if (strip.isEmpty()) {
            fillIdleStrip();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void fillIdleStrip() {
        strip.clear();
        for (int i = 0; i < STRIP_LENGTH; i++) {
            strip.add(caseDef.roll(random));
        }
    }

    private void startSpin() {
        if (state == State.SPINNING) {
            return;
        }
        result = caseDef.roll(random);

        strip.clear();
        for (int i = 0; i < STRIP_LENGTH; i++) {
            strip.add(i == WINNER_INDEX ? result : caseDef.roll(random));
        }

        // Stop at a slightly different point inside the winning card each time, like CS:GO.
        double jitter = (random.nextDouble() - 0.5) * CARD_W * 0.6;
        targetOffset = WINNER_INDEX * CARD_W + CARD_W / 2.0 + jitter - WINDOW_W / 2.0;

        offset = 0;
        spinStart = System.currentTimeMillis();
        state = State.SPINNING;
        showDisclaimer = false;
        openButton.active = false;
        openButton.setMessage(Component.literal("Opening..."));
    }

    private void updateSpin() {
        if (state != State.SPINNING) {
            return;
        }
        double t = Math.min(1.0, (System.currentTimeMillis() - spinStart) / (double) SPIN_MS);
        // Quartic ease-out: fast start, smooth slow landing.
        double eased = 1.0 - Math.pow(1.0 - t, 4);
        offset = targetOffset * eased;

        if (t >= 1.0) {
            state = State.RESULT;
            openButton.active = true;
            openButton.setMessage(Component.literal("Open again"));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        int cx = width / 2;
        int reelX = cx - WINDOW_W / 2;
        int reelY = height / 2 - 130;

        graphics.drawCenteredString(font, Component.literal(caseDef.name()),
                cx, height / 2 - 160, caseDef.colour());

        updateSpin();
        drawReel(graphics, reelX, reelY);

        if (state == State.IDLE) {
            graphics.drawCenteredString(font, Component.literal("Press Open case to spin"),
                    cx, height / 2 - 14, 0xFFAAAAAA);
        }

        if (showDisclaimer) {
            drawDisclaimer(graphics);
        } else if (state == State.RESULT) {
            drawResult(graphics);
        }
    }

    private void drawReel(GuiGraphics g, int x, int y) {
        g.fill(x - 2, y - 2, x + WINDOW_W + 2, y + WINDOW_H + 2, 0xFF2A2A33);
        g.fill(x, y, x + WINDOW_W, y + WINDOW_H, REEL_BG);

        g.enableScissor(x, y, x + WINDOW_W, y + WINDOW_H);
        int shift = (int) Math.round(offset);
        for (int i = 0; i < strip.size(); i++) {
            int cardX = x + i * CARD_W - shift;
            if (cardX + CARD_W < x || cardX > x + WINDOW_W) {
                continue;
            }
            drawCard(g, strip.get(i), cardX, y + (WINDOW_H - CARD_H) / 2);
        }
        g.disableScissor();

        // Centre marker.
        int mid = x + WINDOW_W / 2;
        g.fill(mid - 1, y - 8, mid + 1, y + WINDOW_H + 8, 0xFFFFD84D);
    }

    private void drawCard(GuiGraphics g, CaseItem item, int x, int y) {
        int tier = tierColour(caseDef.chance(item));
        g.fill(x + 2, y, x + CARD_W - 2, y + CARD_H, tier);
        g.fill(x + 4, y + 2, x + CARD_W - 4, y + CARD_H - 2, 0xFF1B1B22);
        drawScaledItem(g, item.stack(), x + (CARD_W - 48) / 2, y + 12, 3f);
        g.drawCenteredString(font,
                Component.literal("$" + CaseItem.formatWorth(item.worth())),
                x + CARD_W / 2, y + CARD_H - 18, 0xFF55FF55);
    }

    private void drawResult(GuiGraphics g) {
        int cx = width / 2;
        int px = cx - 130;
        int py = height / 2 - 62;
        int pw = 260;
        int ph = 124;

        g.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, tierColour(caseDef.chance(result)));
        g.fill(px, py, px + pw, py + ph, PANEL_BG);

        drawScaledItem(g, result.stack(), cx - 32, py + 8, 4f);

        g.drawCenteredString(font, result.stack().getHoverName(), cx, py + 78, 0xFFFFFFFF);
        g.drawCenteredString(font,
                Component.literal("Worth: $" + CaseItem.formatWorth(result.worth()) + " (est.)"),
                cx, py + 92, 0xFF55FF55);
        g.drawCenteredString(font,
                Component.literal(String.format(Locale.ROOT, "Chance: %.3f%%", caseDef.chance(result) * 100)),
                cx, py + 106, 0xFF9A9AA5);
    }

    private void drawDisclaimer(GuiGraphics g) {
        int pw = 320;
        int px = width / 2 - pw / 2;
        int py = height / 2 - 62;
        int ph = 124;

        g.fill(px, py, px + pw, py + ph, PANEL_BG);

        List<FormattedCharSequence> lines = font.split(Component.literal(DISCLAIMER), pw - 20);
        int y = py + 10;
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, px + 10, y, 0xFFE0E0E0, false);
            y += 11;
        }
    }

    private static void drawScaledItem(GuiGraphics g, ItemStack stack, int x, int y, float scale) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1);
        g.renderItem(stack, 0, 0);
        g.pose().popPose();
    }

    /** Border colour by how rare the item is. */
    private static int tierColour(double chance) {
        if (chance >= 0.10) return 0xFFB0B0B0;
        if (chance >= 0.02) return 0xFF4FA3FF;
        if (chance >= 0.005) return 0xFFB15CFF;
        if (chance >= 0.001) return 0xFFFF4DB8;
        return 0xFFFFC53D;
    }
}
