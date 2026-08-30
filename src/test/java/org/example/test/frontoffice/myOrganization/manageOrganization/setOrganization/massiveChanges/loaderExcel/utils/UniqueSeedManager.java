package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.utils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Single Source of Truth for generating unique seeds and tracking registered organization
 * and position names across test data factories and Excel loaders.
 */
public class UniqueSeedManager {

    private static final AtomicLong COUNTER = new AtomicLong(1);
    private static final Set<String> REGISTERED_ORG_NAMES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<String> REGISTERED_POS_NAMES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static volatile String activeSeed = null;

    /**
     * Generates a new unique seed incorporating timestamp and atomic sequence.
     */
    public static synchronized String generateUniqueSeed() {
        activeSeed = "-" + System.currentTimeMillis() + "-" + COUNTER.getAndIncrement();
        return activeSeed;
    }

    /**
     * Gets current active seed or generates a new one if null.
     */
    public static synchronized String getActiveSeed() {
        if (activeSeed == null) {
            return generateUniqueSeed();
        }
        return activeSeed;
    }

    /**
     * Registers an organization name to prevent duplicate creation.
     */
    public static boolean registerOrgName(String name) {
        return REGISTERED_ORG_NAMES.add(name);
    }

    /**
     * Registers a position name to prevent duplicate creation.
     */
    public static boolean registerPosName(String name) {
        return REGISTERED_POS_NAMES.add(name);
    }

    /**
     * Checks if organization name is already registered.
     */
    public static boolean isOrgNameRegistered(String name) {
        return REGISTERED_ORG_NAMES.contains(name);
    }

    /**
     * Checks if position name is already registered.
     */
    public static boolean isPosNameRegistered(String name) {
        return REGISTERED_POS_NAMES.contains(name);
    }

    /**
     * Clears registered names cache for fresh test isolation if needed.
     */
    public static void clearRegistry() {
        REGISTERED_ORG_NAMES.clear();
        REGISTERED_POS_NAMES.clear();
        activeSeed = null;
    }
}
