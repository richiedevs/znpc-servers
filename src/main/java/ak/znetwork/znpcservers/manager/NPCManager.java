/*
 *
 * ZNServersNPC
 * Copyright (C) 2019 Gaston Gonzalez (ZNetwork)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package ak.znetwork.znpcservers.manager;

import ak.znetwork.znpcservers.npc.NPC;

import java.util.LinkedHashSet;

public class NPCManager {

    private final LinkedHashSet<NPC> npcs;

    public NPCManager() {
        this.npcs = new LinkedHashSet<>();
    }

    public LinkedHashSet<NPC> getNpcs() {
        return npcs;
    }
}
