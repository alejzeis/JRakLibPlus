/*
 * JRakLibPlus is not affiliated with Jenkins Software LLC or RakNet.
 * This software is an enhanced port of RakLib https://github.com/PocketMine/RakLib.

 * This file is part of JRakLibPlus.
 *
 * JRakLibPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JRakLibPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JRakLibPlus.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.jython234.jraklibplus.server;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the different hooks for the server.
 */
public class HookManager {
    @Getter private RakNetServer server;
    private final Map<Hook, HookRunnable> hooks = new HashMap<>();

    public HookManager(RakNetServer server) {
        this.server = server;

    }

    protected void activateHook(Hook hook, Session session, Object... params) {
        synchronized (this.hooks) {
            if (!this.hooks.containsKey(hook)) return;
            this.hooks.get(hook).onHook(session, params);
        }
    }

    /**
     * Adds a hook, which is a <code>HookRunnable</code> that is called when a
     * specific event has occurred. One example of this is when a packet is received.
     *
     * @param hook The type of hook or event.
     * @param r    The HookRunnable to be ran when the hook has been triggered.
     * @see Hook
     */
    public void addHook(Hook hook, HookRunnable r) {
        synchronized (this.hooks) {
            this.hooks.put(hook, r);
        }
    }

    public enum Hook {
        PACKET_RECIEVED,
        SESSION_OPENED,
        SESSION_CLOSED;
    }

    public interface HookRunnable {
        void onHook(Session session, Object... params);
    }
}
