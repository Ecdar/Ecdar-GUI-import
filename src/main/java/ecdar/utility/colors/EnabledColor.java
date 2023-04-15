package ecdar.utility.colors;

import javafx.scene.input.KeyCode;

import java.util.ArrayList;

public class EnabledColor {
    public static final ArrayList<EnabledColor> enabledColors = new ArrayList<>() {{
        add(new EnabledColor(Color.GREY_BLUE, Color.Intensity.I700, KeyCode.DIGIT0));
        add(new EnabledColor(Color.DEEP_ORANGE, Color.Intensity.I700, KeyCode.DIGIT1));
        add(new EnabledColor(Color.PINK, Color.Intensity.I500, KeyCode.DIGIT2));
        add(new EnabledColor(Color.PURPLE, Color.Intensity.I500, KeyCode.DIGIT3));
        add(new EnabledColor(Color.INDIGO, Color.Intensity.I500, KeyCode.DIGIT4));
        add(new EnabledColor(Color.BLUE, Color.Intensity.I600, KeyCode.DIGIT5));
        add(new EnabledColor(Color.CYAN, Color.Intensity.I700, KeyCode.DIGIT6));
        add(new EnabledColor(Color.GREEN, Color.Intensity.I600, KeyCode.DIGIT7));
        add(new EnabledColor(Color.LIME, Color.Intensity.I500, KeyCode.DIGIT8));
        add(new EnabledColor(Color.BROWN, Color.Intensity.I500, KeyCode.DIGIT9));
    }};

    public final Color color;
    public final Color.Intensity intensity;
    public final KeyCode keyCode;

    public EnabledColor(final Color color, final Color.Intensity intensity) {
        this(color, intensity, null);
    }

    public EnabledColor(final Color color, final Color.Intensity intensity, final KeyCode keyCode) {
        this.color = color;
        this.intensity = intensity;
        this.keyCode = keyCode;
    }

    public static String getIdentifier(final Color color) {
        for (final EnabledColor enabledColor : enabledColors) {
            if (enabledColor.color.equals(color)) {
                return enabledColor.keyCode.getName();
            }
        }

        return "";
    }

    public static EnabledColor fromIdentifier(final String identifier) {
        for (final EnabledColor enabledColor : enabledColors) {
            if (enabledColor.keyCode.getName().equals(identifier)) {
                return enabledColor;
            }
        }

        return null;
    }

    public static EnabledColor getDefault() {
        return enabledColors.get(0);
    }

    public javafx.scene.paint.Color getTextColor() {
        return color.getTextColor(intensity);
    }

    public javafx.scene.paint.Color getPaintColor() {
        return color.getColor(intensity);
    }

    public javafx.scene.paint.Color getStrokeColor() {
        return nextIntensity(2).getPaintColor();
    }

    public String getTextColorRgbaString() {
        return color.getTextColorRgbaString(intensity);
    }

    public EnabledColor getLowestIntensity() {
        return new EnabledColor(color, intensity.lowest());
    }

    public EnabledColor nextIntensity() {
        return nextIntensity(1);
    }

    public EnabledColor nextIntensity(final int levelIncrement) {
        return new EnabledColor(this.color, this.intensity.next(levelIncrement));
    }

    public EnabledColor setIntensity(int i) {
        return getLowestIntensity().nextIntensity(i);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof EnabledColor && ((EnabledColor) obj).color.equals(this.color);
    }
}