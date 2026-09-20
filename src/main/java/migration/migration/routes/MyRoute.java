//package migration.migration.routes;
//
//import migration.migration.processor.*;
//import org.apache.camel.Exchange;
//import org.springframework.stereotype.Component;
//
//@Component
//public class MyRoute extends BaseRoute {
//
//    private final AuthenticationProcessor authenticationProcessor;
//    private final InboundRequestProcessor inboundRequestProcessor;
//    private final OutboundRequestProcessor outboundRequestProcessor;
//    private final FinalResponseProcessor finalResponseProcessor;
//    private final InboundResponseProcessor inboundResponseProcessor;
//
//    public MyRoute(AuthenticationProcessor authenticationProcessor) {
//        this.authenticationProcessor = authenticationProcessor;
//        this.inboundRequestProcessor = new InboundRequestProcessor();
//        this.outboundRequestProcessor = new OutboundRequestProcessor();
//        this.finalResponseProcessor = new FinalResponseProcessor();
//        this.inboundResponseProcessor = new InboundResponseProcessor();
//    }
//
//    @Override
//    protected void configureRoutes() {
//
//        rest("/test")
//                .get("/{*id}")
//                .to("direct:getEmployeeDetails");
//
//
//        from("direct:getEmployeeDetails")
//                .routeId("get-details-route")
//                .process(setApiNameProcessor)
//                .process(inboundRequestProcessor)
//                .choice()
//                .when(header(StringConstant.REQUESTED_API).isEqueal(StringConstant.policytType))
//                .log("policyDetail")
//                .toD()
//                .when(header(StringConstant.REQUESTED_API).isEqueal(StringConstant.policytType))
//                .log("policyDetail")
//                .toD()
//                .otherWise().log().process((ex)-> {}).end()
//                .process(inboundResponseProcessor)
//                .wireTap()
//                .process(exchange -> {
//
//                    String id = exchange.getIn()
//                            .getHeader("id", String.class);
//                    // Example: so3/mpl/25363772
//
//                    String downstreamUrl =
//                            "{{downstreamUrl1}}/" + id + "/details";
//
//                    exchange.setProperty("downstreamUrl", downstreamUrl);
//                })
//
//                .process(outboundRequestProcessor)
//
//                .toD("${exchangeProperty.downstreamUrl}"
//                        + "?bridgeEndpoint=true"
//                        + "&throwExceptionOnFailure=false")
//
//                .process(inboundResponseProcessor)
//                .process(finalResponseProcessor);
//
//
//
//    }
//}
