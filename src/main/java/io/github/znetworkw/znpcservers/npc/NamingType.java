package io.github.znetworkw.znpcservers.npc;

import io.github.znetworkw.znpcservers.utility.Utils;

/**
 * Enumerates all possible names for a {@link NPC}.
 */
public enum NamingType {
    DEFAULT {
        @Override
        public String resolve(NPC npc) {
            return Utils.randomString(FIXED_LENGTH);
        }
    };

    /** Default length for tab npc name. */
    private static final int FIXED_LENGTH = 6;

    /**
     * Resolves a npc name for the given npc.
     *
     * @param npc
     *      The npc for which the name will be resolved.
     * @return
     *      A new string for the npc name referenced
     *      from this implementation.
     */
    public abstract String resolve(NPC npc);
}
