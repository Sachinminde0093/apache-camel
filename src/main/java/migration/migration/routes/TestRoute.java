package migration.migration.routes;

import migration.migration.processor.*;
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
         =====================================================
                FIRST ROUTE (GET)
         =====================================================
         */
        rest("/test")
                .get()
                .to("direct:createEmployee");

        from("direct:createEmployee")
                .routeId("test-route")
                .toD("http://localhost:8081/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=false");


        /*
         =====================================================
                SECOND ROUTE (POST WITH LOGGING)
         =====================================================
         */
        rest("/xd/employee")
                .post()
                .to("direct:createtestEmployee");

        from("direct:createtestEmployee")
                .routeId("route")

                .process(inboundRequestProcessor)

                .process(outboundRequestProcessor)

                .toD("http://localhost:8082/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=false")

                .process(inboundResponseProcessor)

                .process(finalResponseProcessor);
    }
}
