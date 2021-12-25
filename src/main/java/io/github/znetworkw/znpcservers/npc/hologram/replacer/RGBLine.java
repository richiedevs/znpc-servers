package io.github.znetworkw.znpcservers.npc.hologram.replacer;

import io.github.znetworkw.znpcservers.configuration.ConfigurationConstants;
import io.github.znetworkw.znpcservers.utility.Utils;
import net.md_5.bungee.api.ChatColor;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Enables RGB for the string.
 */
public class RGBLine implements LineReplacer {
    /** Default hex-color-code char. */
    private static final char HEX_COLOR_CHAR = '#';
    /** Default hex-color-code length. */
    private static final int HEX_COLOR_LENGTH = 6;

    @Override
    public String make(String string) {
        String rgbString = string;
        for (int i = 0; i < rgbString.length(); i++) {
            char charAt = rgbString.charAt(i);
            // check if the char is supposed to be a hex color code
            if (charAt == HEX_COLOR_CHAR) {
                int endIndex = i+HEX_COLOR_LENGTH+1;
                boolean success = true;
                StringBuilder hexCodeStringBuilder = new StringBuilder();
                for (int i2 = i; i2 < endIndex; i2++) {
                    // check if string length is in range (hex-default length)
                    if (rgbString.length() - 1 < i2) {
                        success = false;
                        break;
                    }
                    char hexCode = rgbString.charAt(i2);
                    hexCodeStringBuilder.append(ConfigurationConstants.RGB_ANIMATION
                        && hexCode != HEX_COLOR_CHAR ? Integer.toHexString(ThreadLocalRandom.current().nextInt(0xf+1)) : hexCode);
                }
                // found RGB Color!
                if (success) {
                    try {
                        // apply RGB Color to string..
                        rgbString = rgbString.substring(0, i) +
                            ChatColor.of(hexCodeStringBuilder.toString()) +
                            rgbString.substring(endIndex);
                    } catch (Exception e) {
                        // invalid hex string
                    }
                }
            }
        }
        return rgbString;
    }

    @Override
    public boolean isSupported() {
        return Utils.BUKKIT_VERSION > 15;
    }
}
