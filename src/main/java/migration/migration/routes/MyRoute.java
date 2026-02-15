package migration.migration.routes;

import migration.migration.processor.AuthenticationProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MyRoute extends RouteBuilder {

    private final AuthenticationProcessor authenticationProcessor;

    public MyRoute(AuthenticationProcessor authenticationProcessor) {
        this.authenticationProcessor = authenticationProcessor;
    }

    @Override
    public void configure() {


        rest("/test")
                .get()
                .to("direct:testRoute");

        from("direct:testRoute")
                .log("Outbound request to Google")
                .removeHeaders("*")
                .toD("http://localhost:8081/employee?bridgeEndpoint=true");

        rest("/employee")
                .post()
                .to("direct:createEmployee");

        from("direct:createEmployee")// thread pool safe (no MDC used)
                .process(authenticationProcessor)
                .process(exchange -> {
                    exchange.setProperty("downstreamUri",
                            "http://localhost:8081/employee");
                })
                .toD("http://localhost:8081/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=false");
    }
}
