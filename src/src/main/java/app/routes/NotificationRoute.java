package app.routes;

import app.controllers.NotificationController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class NotificationRoute {
    private final NotificationController notificationController;

    public NotificationRoute(NotificationController notificationController) {
        this.notificationController = notificationController;
    }

    protected EndpointGroup getRoutes() {

        return () -> {
            ws("notification", notificationController::handleNotification);
        };
    }
}
