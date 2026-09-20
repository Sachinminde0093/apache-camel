package migration.migration.routes;

import migration.migration.processor.AuthenticationProcessor;
import migration.migration.processor.FinalResponseProcessor;
import migration.migration.processor.InboundRequestProcessor;
import migration.migration.processor.InboundResponseProcessor;
import migration.migration.processor.OutboundRequestProcessor;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class TestRoute extends BaseRoute {

    private final AuthenticationProcessor authenticationProcessor;
    private final InboundRequestProcessor inboundRequestProcessor;
    private final OutboundRequestProcessor outboundRequestProcessor;
    private final FinalResponseProcessor finalResponseProcessor;
    private final InboundResponseProcessor inboundResponseProcessor;

    public TestRoute(AuthenticationProcessor authenticationProcessor) {

        this.authenticationProcessor = authenticationProcessor;

        this.inboundRequestProcessor = new InboundRequestProcessor();
        this.outboundRequestProcessor = new OutboundRequestProcessor();
        this.finalResponseProcessor = new FinalResponseProcessor();
        this.inboundResponseProcessor = new InboundResponseProcessor();
    }

    @Override
    protected void configureRoutes() {

        /*
         * =====================================================
         * CAMEL REST CONFIGURATION
         * =====================================================
         */

        restConfiguration()
                .component("platform-http")
                .contextPath("/api")
                .apiContextPath("/openapi")
                .apiProperty("api.title", "Migration APIs")
                .apiProperty("api.version", "1.0.0")
                .apiProperty(
                        "api.description",
                        "Generic pass-through Camel APIs"
                );

        /*
         * =====================================================
         * TIMEOUT HANDLER
         * =====================================================
         */

        onException(
                java.net.SocketTimeoutException.class,
                java.util.concurrent.TimeoutException.class
        )
                .handled(true)
                .log("Timeout occurred : ${exception.message}")
                .setHeader(
                        Exchange.HTTP_RESPONSE_CODE,
                        constant(504)
                )
                .setHeader(
                        Exchange.CONTENT_TYPE,
                        constant("application/json")
                )
                .setBody(constant("""
                {
                  "status": "ERROR",
                  "message": "Downstream timeout after 25 seconds"
                }
                """));

        /*
         * =====================================================
         * GET
         * =====================================================
         */

        rest("/test")
                .get()
                .description("Generic GET pass-through API")
                .produces("*/*")
                .to("direct:createEmployee");

        from("direct:createEmployee")
                .routeId("test-route")

                .log("Calling downstream GET API")

                .to("http://localhost:8081/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=false"
                        + "&connectTimeout=5000"
                        + "&responseTimeout=25000")

                .log("GET downstream response received");

        /*
         * =====================================================
         * POST
         * =====================================================
         */

        rest("/xd/employee")
                .post()
                .description("Generic POST pass-through API")
                .consumes("*/*")
                .produces("*/*")
                .to("direct:createtestEmployee");

        from("direct:createtestEmployee")
                .routeId("route")

                .process(authenticationProcessor)

                .process(inboundRequestProcessor)

                .process(outboundRequestProcessor)

                .log("Calling downstream POST API")

                .to("http://localhost:8082/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=false"
                        + "&connectTimeout=5000"
                        + "&responseTimeout=25000")

                .log("POST downstream response received")

                .process(inboundResponseProcessor)

                .process(finalResponseProcessor);
    }
}