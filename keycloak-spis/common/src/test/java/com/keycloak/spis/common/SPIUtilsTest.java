package com.keycloak.spis.common;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for SPIUtils
 */
public class SPIUtilsTest {
    
    @Test
    public void testIsValidString() {
        assertTrue(SPIUtils.isValidString("valid"));
        assertFalse(SPIUtils.isValidString(null));
        assertFalse(SPIUtils.isValidString(""));
        assertFalse(SPIUtils.isValidString("   "));
    }
    
    @Test
    public void testGetConfigValue() {
        String defaultValue = "default";
        String result = SPIUtils.getConfigValue("non.existent.key", defaultValue);
        assertEquals(defaultValue, result);
    }
}