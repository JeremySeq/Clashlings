package com.jeremyseq.multiplayer_game.client;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageFilters {

    public static void tint(BufferedImage image, Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int r = (pixelColor.getRed() + color.getRed()) / 2;
                int g = (pixelColor.getGreen() + color.getGreen()) / 2;
                int b = (pixelColor.getBlue() + color.getBlue()) / 2;
                int a = pixelColor.getAlpha();
                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }

    public static void reverseTint(BufferedImage image, Color tint) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);

                // Reverse the averaging effect
                int r = (2 * pixelColor.getRed()) - tint.getRed();
                int g = (2 * pixelColor.getGreen()) - tint.getGreen();
                int b = (2 * pixelColor.getBlue()) - tint.getBlue();

                // Clamp the values to be within valid color range [0, 255]
                r = Math.min(Math.max(r, 0), 255);
                g = Math.min(Math.max(g, 0), 255);
                b = Math.min(Math.max(b, 0), 255);

                int a = pixelColor.getAlpha();
                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }

    public static void adjustSpecialContrast(BufferedImage image, float factor) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int r = pixelColor.getRed();
                int g = pixelColor.getGreen();
                int b = pixelColor.getBlue();
                int a = pixelColor.getAlpha();

                // Skip fully transparent pixels
                if (a == 0) {
                    continue;
                }

                float[] hsl = rgbToHsl(r, g, b);
                hsl[2] = applyContrast(hsl[2], factor); // Adjust lightness
                hsl[1] = adjustSaturation(hsl[1], hsl[2]); // Adjust saturation based on lightness

                int[] rgb = hslToRgb(hsl[0], hsl[1], hsl[2]);
                r = rgb[0];
                g = rgb[1];
                b = rgb[2];

                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }

    private static float applyContrast(float lightness, float factor) {
        return factor * (lightness - 0.5f) + 0.5f;
    }

    private static float adjustSaturation(float saturation, float lightness) {
        if (lightness < 0.5) {
            return Math.min(saturation * 1.2f, 1.0f); // Increase saturation for dark pixels
        } else {
            return Math.max(saturation * 0.8f, 0.0f); // Decrease saturation for bright pixels
        }
    }

    private static float[] rgbToHsl(int r, int g, int b) {
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));

        float h, s, l;
        l = (max + min) / 2.0f;

        if (max == min) {
            h = s = 0.0f;
        } else {
            float d = max - min;
            s = l > 0.5f ? d / (2.0f - max - min) : d / (max + min);
            if (max == rf) {
                h = (gf - bf) / d + (gf < bf ? 6.0f : 0.0f);
            } else if (max == gf) {
                h = (bf - rf) / d + 2.0f;
            } else {
                h = (rf - gf) / d + 4.0f;
            }
            h /= 6.0f;
        }

        return new float[]{h, s, l};
    }

    private static int[] hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0.0f) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5f ? l * (1.0f + s) : l + s - l * s;
            float p = 2.0f * l - q;
            r = hueToRgb(p, q, h + 1.0f / 3.0f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0f / 3.0f);
        }

        return new int[]{Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)};
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0.0f) t += 1.0f;
        if (t > 1.0f) t -= 1.0f;
        if (t < 1.0f / 6.0f) return p + (q - p) * 6.0f * t;
        if (t < 1.0f / 2.0f) return q;
        if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6.0f;
        return p;
    }

    public static void adjustContrast(BufferedImage image, float factor) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int r = applyContrast(pixelColor.getRed(), factor);
                int g = applyContrast(pixelColor.getGreen(), factor);
                int b = applyContrast(pixelColor.getBlue(), factor);
                int a = pixelColor.getAlpha();

                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }

    private static int applyContrast(int colorValue, float factor) {
        int newValue = (int) (factor * (colorValue - 128) + 128);
        return Math.min(Math.max(newValue, 0), 255);
    }

    // Adjust brightness
    public static void adjustBrightness(BufferedImage image, int amount) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int r = applyBrightness(pixelColor.getRed(), amount);
                int g = applyBrightness(pixelColor.getGreen(), amount);
                int b = applyBrightness(pixelColor.getBlue(), amount);
                int a = pixelColor.getAlpha();

                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }

    private static int applyBrightness(int colorValue, int amount) {
        int newValue = colorValue + amount;
        return Math.min(Math.max(newValue, 0), 255);
    }

    // Convert to grayscale
    public static void toGrayscale(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int gray = (int) (0.299 * pixelColor.getRed() + 0.587 * pixelColor.getGreen() + 0.114 * pixelColor.getBlue());
                int a = pixelColor.getAlpha();

                int rgba = (a << 24) | (gray << 16) | (gray << 8) | gray;
                image.setRGB(x, y, rgba);
            }
        }
    }

    // Apply sepia tone
    public static void applySepia(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int tr = (int) (0.393 * pixelColor.getRed() + 0.769 * pixelColor.getGreen() + 0.189 * pixelColor.getBlue());
                int tg = (int) (0.349 * pixelColor.getRed() + 0.686 * pixelColor.getGreen() + 0.168 * pixelColor.getBlue());
                int tb = (int) (0.272 * pixelColor.getRed() + 0.534 * pixelColor.getGreen() + 0.131 * pixelColor.getBlue());

                int r = Math.min(tr, 255);
                int g = Math.min(tg, 255);
                int b = Math.min(tb, 255);
                int a = pixelColor.getAlpha();

                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }

    // Invert colors
    public static void invertColors(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int r = 255 - pixelColor.getRed();
                int g = 255 - pixelColor.getGreen();
                int b = 255 - pixelColor.getBlue();
                int a = pixelColor.getAlpha();

                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }
}
