package com.bustin.knightmod.entity.dark_knight.util;

public class DarkKnightPlayerAnalyzer {

    public enum PlayerStyle {
        MELEE,
        RANGED,
        BALANCED
    }

    private final boolean[] observations = new boolean[10];

    private int index = 0;
    private int count = 0;

    public void observe(double distance) {
        observations[index] = distance < 5.0;

        index = (index + 1) % observations.length;

        if (count < observations.length) {
            count++;
        }
    }

    public PlayerStyle getPlayerStyle() {
        if (count < observations.length) {
            return PlayerStyle.BALANCED;
        }

        int closeObservations = 0;

        for (boolean close : observations) {
            if (close) {
                closeObservations++;
            }
        }

        if (closeObservations >= 7) {
            return PlayerStyle.MELEE;
        }

        if (closeObservations <= 3) {
            return PlayerStyle.RANGED;
        }

        return PlayerStyle.BALANCED;
    }

    public void reset() {
        index = 0;
        count = 0;
    }
}
