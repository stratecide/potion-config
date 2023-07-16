package com.stratecide.potion_config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PotionColor {
    final List<Color> colors;
    final int framesPerColor;

    public static PotionColor neutral() {
        List<Color> colors = new ArrayList<>();
        colors.add(Color.parse("385DC6"));
        return new PotionColor(colors, 1);
    }

    public static PotionColor parse(JsonElement json) {
        List<Color> colors = new ArrayList<>();
        int framesPerColor = 1;
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            if (jsonArray.size() < 2) {
                throw new RuntimeException("Color Array for Potion colors should have a length of at least 3");
            }
            framesPerColor = jsonArray.get(0).getAsInt();
            for (int i = 1; i < jsonArray.size(); i++) {
                colors.add(Color.parse(jsonArray.get(i).getAsString()));
            }
        } else {
            colors.add(Color.parse(json.getAsString()));
        }
        return new PotionColor(colors, framesPerColor);
    }

    private PotionColor(List<Color> colors, int framesPerColor) {
        this.colors = colors;
        this.framesPerColor = framesPerColor;
    }

    public int getColor() {
        if (framesPerColor <= 0) {
            int colorIndex = (int) (Math.random() * (double) colors.size());
            return colors.get(colorIndex).interpolate(colors.get(colorIndex), 0);
        }
        long total_frames = framesPerColor * colors.size();
        int frame = (int) ((new Date().getTime() / 50) % total_frames);
        int colorIndex = frame / framesPerColor;
        Color before = colors.get(colorIndex);
        Color after = colors.get((colorIndex + 1) % colors.size());
        return before.interpolate(after, ((double) (frame % framesPerColor)) / ((double) framesPerColor));
    }

    record Color(int red, int green, int blue) {
        static Color parse(String hex) {
            int red = Integer.parseInt(hex.substring(0, 2), 16);
            int green = Integer.parseInt(hex.substring(2, 4), 16);
            int blue = Integer.parseInt(hex.substring(4, 6), 16);
            return new Color(red, green, blue);
        }

        int interpolate(Color other, double progress) {
            int red = (int) Math.min(255.0, ((double) this.red) * (1.0 - progress) + ((double) other.red) * progress);
            int green = (int) Math.min(255.0, ((double) this.green) * (1.0 - progress) + ((double) other.green) * progress);
            int blue = (int) Math.min(255.0, ((double) this.blue) * (1.0 - progress) + ((double) other.blue) * progress);
            return 0x10000 * red + 0x100 * green + blue;
        }
    }
}
