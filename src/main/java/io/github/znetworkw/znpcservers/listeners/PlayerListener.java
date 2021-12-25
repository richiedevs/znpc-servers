package io.github.znetworkw.znpcservers.listeners;

import io.github.znetworkw.znpcservers.ServersNPC;
import io.github.znetworkw.znpcservers.npc.event.NPCInteractEvent;
import io.github.znetworkw.znpcservers.npc.conversation.ConversationModel;
import io.github.znetworkw.znpcservers.user.EventService;
import io.github.znetworkw.znpcservers.user.ZUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    /**
     * Creates and register the necessary events for players.
     *
     * @param serversNPC The plugin instance.
     */
    public PlayerListener(ServersNPC serversNPC) {
        serversNPC.getServer().getPluginManager().registerEvents(this, serversNPC);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ZUser.find(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ZUser.unregister(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onTalk(AsyncPlayerChatEvent event) {
        ZUser zUser = ZUser.find(event.getPlayer());
        if (EventService.hasService(zUser, AsyncPlayerChatEvent.class)) {
            event.setCancelled(true);
            // gui logic
            EventService<AsyncPlayerChatEvent> eventService = EventService.findService(zUser, AsyncPlayerChatEvent.class);
            eventService.runAll(event);
            // remove
            zUser.getEventServices().remove(eventService);
        }
    }

    @EventHandler
    public void onConversation(NPCInteractEvent event) {
        ConversationModel conversationStorage = event.getNpc().getNpcPojo().getConversation();
        if (conversationStorage == null
            || conversationStorage.getConversationType() != ConversationModel.ConversationType.CLICK) {
            return;
        }
        event.getNpc().tryStartConversation(event.getPlayer());
    }
}
