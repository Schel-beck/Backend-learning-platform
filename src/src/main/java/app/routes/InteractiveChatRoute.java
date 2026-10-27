package app.routes;

import app.controllers.InteractiveChatController;
import app.service.InteractiveChat;
import io.javalin.apibuilder.EndpointGroup;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;
@AllArgsConstructor
@NoArgsConstructor
public class InteractiveChatRoute {
    private InteractiveChat interactiveChat = new InteractiveChat();

    InteractiveChatController chatController = new InteractiveChatController(interactiveChat);
    protected EndpointGroup getRoutes() {

        return () -> {
            post("/chat", chatController::chat);
        };
    }

}
