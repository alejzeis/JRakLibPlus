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
            if(!this.hooks.containsKey(hook)) return;
            this.hooks.get(hook).onHook(session, params);
        }
    }

    /**
     * Adds a hook, which is a <code>HookRunnable</code> that is called when a
     * specific event has occurred. One example of this is when a packet is received.
     * @param hook The type of hook or event.
     * @param r The HookRunnable to be ran when the hook has been triggered.
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
