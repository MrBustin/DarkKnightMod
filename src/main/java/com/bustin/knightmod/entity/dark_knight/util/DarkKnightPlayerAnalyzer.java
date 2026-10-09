package com.bustin.knightmod.entity.dark_knight.util;

import java.util.Arrays;

public class DarkKnightPlayerAnalyzer {

    public enum PlayerStyle {
        MELEE,
        RANGED,
        BALANCED
    }

    public enum AggressionLevel {
        PASSIVE,
        MODERATE,
        AGGRESSIVE
    }

    // ==============================
    // CONFIGURATION
    // ==============================

    private static final int OBSERVATION_WINDOW = 10;
    private static final double MELEE_DISTANCE = 5.0;

    // Number of damage events within 10 seconds
    private static final int PASSIVE_THRESHOLD = 2;
    private static final int AGGRESSIVE_THRESHOLD = 7;

    // Minimum time between counted damage events
    private static final long DAMAGE_EVENT_COOLDOWN = 5;

    // ==============================
    // OBSERVATIONS
    // ==============================

    private final boolean[] distanceObservations =
            new boolean[OBSERVATION_WINDOW];

    private final int[] damageObservations =
            new int[OBSERVATION_WINDOW];

    private int index = 0;
    private int count = 0;

    private int pendingDamageEvents = 0;
    private long lastDamageEventTick = Long.MIN_VALUE;

    // ==============================
    // PLAYER DISTANCE
    // ==============================

    /**
     * Called once every 20 ticks.
     *
     * Records the player's distance and advances
     * the observation window by one second.
     */
    public void observe(double distance) {

        distanceObservations[index] =
                distance < MELEE_DISTANCE;

        damageObservations[index] =
                pendingDamageEvents;

        pendingDamageEvents = 0;

        index = (index + 1) % OBSERVATION_WINDOW;

        if (count < OBSERVATION_WINDOW) {
            count++;
        }
    }

    /**
     * Determines the player's preferred distance
     * over the last 10 seconds.
     */
    public PlayerStyle getPlayerStyle() {

        if (count < OBSERVATION_WINDOW) {
            return PlayerStyle.BALANCED;
        }

        int closeObservations = 0;

        for (boolean close : distanceObservations) {
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

    // ==============================
    // PLAYER AGGRESSION
    // ==============================

    /**
     * Called when the player successfully
     * damages the Dark Knight.
     *
     * @param gameTick Current server game time
     */
    public void recordDamageEvent(long gameTick) {

        if (lastDamageEventTick != Long.MIN_VALUE &&
                gameTick - lastDamageEventTick
                        < DAMAGE_EVENT_COOLDOWN) {
            return;
        }

        pendingDamageEvents++;
        lastDamageEventTick = gameTick;
    }

    /**
     * Returns the number of recorded damage events
     * during the observation window.
     */
    public int getRecentDamageEvents() {

        int total = pendingDamageEvents;

        for (int damage : damageObservations) {
            total += damage;
        }

        return total;
    }

    /**
     * Classifies how aggressively the player
     * has been attacking.
     */
    public AggressionLevel getAggressionLevel() {

        int attacks = getRecentDamageEvents();

        if (attacks <= PASSIVE_THRESHOLD) {
            return AggressionLevel.PASSIVE;
        }

        if (attacks >= AGGRESSIVE_THRESHOLD) {
            return AggressionLevel.AGGRESSIVE;
        }

        return AggressionLevel.MODERATE;
    }

    // ==============================
    // RESET
    // ==============================

    /**
     * Clears all collected combat observations.
     */
    public void reset() {

        Arrays.fill(distanceObservations, false);
        Arrays.fill(damageObservations, 0);

        index = 0;
        count = 0;

        pendingDamageEvents = 0;
        lastDamageEventTick = Long.MIN_VALUE;
    }
}
