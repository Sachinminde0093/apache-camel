package migration.migration.routes;

import migration.migration.processor.*;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class MyRoute extends BaseRoute {

    private final AuthenticationProcessor authenticationProcessor;
    private final InboundRequestProcessor inboundRequestProcessor;
    private final OutboundRequestProcessor outboundRequestProcessor;
    private final FinalResponseProcessor finalResponseProcessor;
    private final InboundResponseProcessor inboundResponseProcessor;

    public MyRoute(AuthenticationProcessor authenticationProcessor) {
        this.authenticationProcessor = authenticationProcessor;
        this.inboundRequestProcessor = new InboundRequestProcessor();
        this.outboundRequestProcessor = new OutboundRequestProcessor();
        this.finalResponseProcessor = new FinalResponseProcessor();
        this.inboundResponseProcessor = new InboundResponseProcessor();
    }

    @Override
    protected void configureRoutes() {

        rest("/employee")
                .get()
                .to("direct:getEmployee");

        from("direct:getEmployee")
                .routeId("get-test-route")
                .process(inboundRequestProcessor)
                .removeHeader(Exchange.HTTP_PATH)
                .removeHeader(Exchange.HTTP_URI)
                .removeHeader(Exchange.HTTP_URL)
                .setProperty("downstreamUrl",
                        constant("{{downstreamUrl1}}{{employeePath}}"))
                .process(outboundRequestProcessor)
                .toD("${exchangeProperty.downstreamUrl}"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=false")
                .process(inboundResponseProcessor)
                .setProperty("downstreamUrl",
                        constant("{{downstreamUrl2}}{{employeePath}}"))
                .process(outboundRequestProcessor)
                .toD("${exchangeProperty.downstreamUrl}"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=false")
                .process(inboundResponseProcessor)
                .process(finalResponseProcessor);

    }
}
