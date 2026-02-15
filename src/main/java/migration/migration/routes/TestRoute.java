

package migration.migration.routes;

import migration.migration.processor.AuthenticationProcessor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class TestRoute extends BaseRoute {

    private final AuthenticationProcessor authenticationProcessor;

    public TestRoute(AuthenticationProcessor authenticationProcessor) {
        this.authenticationProcessor = authenticationProcessor;
    }

    @Override
    protected void configureRoutes() {

        rest("/test")
                .get()
                .to("direct:createEmployee");

        from("direct:createEmployee")
                .routeId("test-route")
                .toD("http://localhost:8081/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=true");

        rest("/test/employee")
                .post()
                .to("direct:createtestEmployee");

        from("direct:createtestEmployee")
                .routeId("route")
                .toD("http://localhost:8082/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=true");
    }
}
