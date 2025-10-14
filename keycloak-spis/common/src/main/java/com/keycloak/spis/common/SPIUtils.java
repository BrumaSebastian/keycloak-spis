package com.keycloak.spis.common;

import java.util.logging.Logger;

/**
 * Utility class for common operations across Keycloak SPIs
 */
public class SPIUtils {
    
    private static final Logger logger = Logger.getLogger(SPIUtils.class.getName());
    
    /**
     * Validates if a string is not null or empty
     */
    public static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Safely logs a message with null checks
     */
    public static void logSafely(String message, Object... params) {
        if (isValidString(message)) {
            logger.info(String.format(message.replace("{}", "%s"), params));
        }
    }
    
    /**
     * Gets a configuration value with a default fallback
     */
    public static String getConfigValue(String key, String defaultValue) {
        String value = System.getProperty(key);
        return isValidString(value) ? value : defaultValue;
    }
}