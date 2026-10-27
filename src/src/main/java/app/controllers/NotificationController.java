package app.controllers;

import io.javalin.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller responsible for showing how many player are online.
 * Uses a set<WsContext> clients to keep track of online users
 */
public class NotificationController {

    private final Set<WsContext> clients = ConcurrentHashMap.newKeySet();
    private final Logger log = LoggerFactory.getLogger(NotificationController.class);


    /**
     * Calls handleConnect on connect, and calls handleClose on close.
     *
     * @param wsConfig handles the lifecycle events for the websocket fx. onConnect and onClose
     */

    public void handleNotification(WsConfig wsConfig){
        wsConfig.onConnect(this::handleConnect);
        wsConfig.onClose(this::handleClose);
    }
    /**
     * handles what happens when a client connects.
     *
     * @param ctx the Javalin context represents the connect event for the websocket
     */

    private void handleConnect(WsConnectContext ctx){
        ctx.enableAutomaticPings();
        clients.add(ctx);
        log.info("Client connected: " + ctx.sessionId());
        broadcastPlayerCount();

    }
    /**
     * handles what happens when a client closes.
     *
     * @param ctx the Javalin context represents the close event for the websocket
     */

    private void handleClose(WsCloseContext ctx) {
        clients.remove(ctx);
        log.info("Client disconnected: " + ctx.sessionId());
        broadcastPlayerCount();
    }

    /**
     * handles the clients set by checking if any clients closes.
     *
     * @param message is the message the online users will get
     */
    private void broadcast(String message) {
        clients.removeIf(ctx -> !ctx.session.isOpen());
        clients.forEach(client -> client.send(message));
    }

    /**
     * Calls broadcast(String message) and inserts the message which will be conveyed to online clients
     *
     */
    private void broadcastPlayerCount() {
        String json = """
                {
                  "type": "players",
                  "count": %d
                }
                """.formatted(clients.size());

        broadcast(json);
    }
}
