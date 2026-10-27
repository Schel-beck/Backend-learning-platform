package app.routes;

import app.security.routes.SecurityRoutes;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;
public class Routes {
    private SubjectRoute subjectRoute;
    private QuestionRoute questionRoute;
    private SecurityRoutes securityRoutes;
    private InteractiveChatRoute interactiveChatRoute;
    private User_questionRoutes userQuestionRoutes;
    private NotificationRoute notificationRoute;


    public EndpointGroup getRoutes() {
        return () -> {
            path("/", subjectRoute.getRoutes());
            path("/", questionRoute.getRoutes());
            path("/", securityRoutes.getSecurityRoutes());
            path("/", interactiveChatRoute.getRoutes());
            path("/", notificationRoute.getRoutes());
            path("/", userQuestionRoutes.getRoutes());

        };
    }

    public Routes(SubjectRoute subjectRoute, QuestionRoute questionRoute, SecurityRoutes securityRoutes,
                  InteractiveChatRoute interactiveChatRoute, User_questionRoutes userQuestionRoutes, NotificationRoute notificationRoute) {
        this.subjectRoute = subjectRoute;
        this.questionRoute = questionRoute;
        this.securityRoutes = securityRoutes;
        this.interactiveChatRoute = interactiveChatRoute;
        this.userQuestionRoutes = userQuestionRoutes;
        this.notificationRoute = notificationRoute;
    }
}
