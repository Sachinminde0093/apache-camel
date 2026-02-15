//package migration.migration.routes;
//
//import migration.migration.exception.ApiException;
//import migration.migration.exception.ErrorResponse;
//import migration.migration.processor.AuthenticationProcessor;
//import org.apache.camel.Exchange;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.http.base.HttpOperationFailedException;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Component
//public class MyRoute extends RouteBuilder {
//
//    private final AuthenticationProcessor authenticationProcessor;
//
//    public MyRoute(AuthenticationProcessor authenticationProcessor) {
//        this.authenticationProcessor = authenticationProcessor;
//    }
//
//    @Override
//    public void configure() {
//
//        // Custom API exceptions (401, 400, 403 etc.)
//        onException(ApiException.class)
//                .handled(true)
//                .process(exchange -> {
//
//                    ApiException ex = exchange.getProperty(
//                            Exchange.EXCEPTION_CAUGHT,
//                            ApiException.class);
//
//                    int status = ex.getStatus();
//
//                    exchange.getMessage().setHeader(
//                            Exchange.HTTP_RESPONSE_CODE, status);
//
//                    exchange.getMessage().setHeader(
//                            Exchange.CONTENT_TYPE, "application/json");
//
//                    ErrorResponse response = new ErrorResponse(
//                            status,
//                            HttpStatus.valueOf(status).getReasonPhrase(),
//                            ex.getMessage(),
//                            System.currentTimeMillis()
//                    );
//
//                    exchange.getMessage().setBody(response);
//                })
//                .marshal().json();
//
//
//        // Downstream HTTP errors
//        onException(HttpOperationFailedException.class)
//                .handled(true)
//                .process(exchange -> {
//
//                    HttpOperationFailedException ex =
//                            exchange.getProperty(
//                                    Exchange.EXCEPTION_CAUGHT,
//                                    HttpOperationFailedException.class);
//
//                    int status = ex.getStatusCode();
//
//                    exchange.getMessage().setHeader(
//                            Exchange.HTTP_RESPONSE_CODE, status);
//
//                    exchange.getMessage().setHeader(
//                            Exchange.CONTENT_TYPE, "application/json");
//
//                    ErrorResponse response = new ErrorResponse(
//                            status,
//                            ex.getStatusText(),
//                            ex.getResponseBody(),
//                            System.currentTimeMillis()
//                    );
//
//                    exchange.getMessage().setBody(response);
//                })
//                .marshal().json();
//
//
//        // Generic fallback
//        onException(Exception.class)
//                .handled(true)
//                .process(exchange -> {
//
//                    Exception ex = exchange.getProperty(
//                            Exchange.EXCEPTION_CAUGHT,
//                            Exception.class);
//
//                    exchange.getMessage().setHeader(
//                            Exchange.HTTP_RESPONSE_CODE, 500);
//
//                    exchange.getMessage().setHeader(
//                            Exchange.CONTENT_TYPE, "application/json");
//
//                    ErrorResponse response = new ErrorResponse(
//                            500,
//                            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
//                            ex.getMessage(),
//                            System.currentTimeMillis()
//                    );
//
//                    exchange.getMessage().setBody(response);
//                })
//                .marshal().json();
//
//
//
//        /*
//         =====================================================
//              1️⃣ CLIENT → YOUR API (INBOUND REQUEST)
//         =====================================================
//         */
//        interceptFrom()
//                .process(exchange -> {
//
//                    // Parent Request ID
//                    String parentRequestId = LocalDateTime.now()
//                            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
//
//                    exchange.setProperty("parentRequestId", parentRequestId);
//
//                    // Child counter
//                    exchange.setProperty("childCounter",
//                            new AtomicInteger(0));
//
//                    // MetaId (UUID)
//                    String metaId = UUID.randomUUID().toString();
//                    exchange.setProperty("metaId", metaId);
//
//                    exchange.setProperty("startTime",
//                            System.currentTimeMillis());
//
//                    String method = exchange.getIn()
//                            .getHeader(Exchange.HTTP_METHOD, String.class);
//
//                    String url = exchange.getIn()
//                            .getHeader(Exchange.HTTP_URL, String.class);
//
//                    exchange.setProperty("originalMethod", method);
//                    exchange.setProperty("originalUrl", url);
//
//                    String headers = exchange.getIn().getHeaders().toString();
//                    String body = exchange.getIn().getBody(String.class);
//
//                    log.info("""
//
//                            ================= INBOUND REQUEST =================
//                            RequestId : {}
//                            MetaId    : {}
//                            Method    : {}
//                            URL       : {}
//                            Headers   : {}
//                            Payload   : {}
//                            ====================================================
//                            """,
//                            parentRequestId,
//                            metaId,
//                            method,
//                            url,
//                            headers,
//                            body
//                    );
//                });
//
//
//        /*
//         =====================================================
//              2️⃣ OUTBOUND REQUEST + 3️⃣ INBOUND RESPONSE
//         =====================================================
//         */
//        interceptSendToEndpoint("http*")
//                .process(exchange -> {
//
//                    String parentId =
//                            exchange.getProperty("parentRequestId", String.class);
//
//                    AtomicInteger counter =
//                            exchange.getProperty("childCounter", AtomicInteger.class);
//
//                    int childNo = counter.incrementAndGet();
//                    String childRequestId = parentId + "-" + childNo;
//
//                    exchange.setProperty("childRequestId", childRequestId);
//
//                    String metaId =
//                            exchange.getProperty("metaId", String.class);
//
//                    String exchangeId = exchange.getExchangeId();
//
//                    String method =
//                            exchange.getIn().getHeader(Exchange.HTTP_METHOD, String.class);
//
//                    String uri =
//                            exchange.getProperty(Exchange.TO_ENDPOINT, String.class);
//
//                    String headers =
//                            exchange.getIn().getHeaders().toString();
//
//                    String body =
//                            exchange.getIn().getBody(String.class);
//
//                    log.info("""
//
//                            ================= OUTBOUND REQUEST =================
//                            RequestId : {}
//                            MetaId    : {}
//                            ExchangeId: {}
//                            Method    : {}
//                            URI       : {}
//                            Headers   : {}
//                            Payload   : {}
//                            ====================================================
//                            """,
//                            childRequestId,
//                            metaId,
//                            exchangeId,
//                            method,
//                            uri,
//                            headers,
//                            body
//                    );
//                })
//                .process(exchange -> {
//
//                    /*
//                       This runs AFTER HTTP call completes
//                     */
//
//                    String childRequestId =
//                            exchange.getProperty("childRequestId", String.class);
//
//                    String metaId =
//                            exchange.getProperty("metaId", String.class);
//
//                    String exchangeId = exchange.getExchangeId();
//
//                    Integer status =
//                            exchange.getMessage()
//                                    .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
//
//                    String headers =
//                            exchange.getMessage().getHeaders().toString();
//
//                    String body =
//                            exchange.getMessage().getBody(String.class);
//
//                    log.info("""
//
//                            ================= INBOUND RESPONSE =================
//                            RequestId : {}
//                            MetaId    : {}
//                            ExchangeId: {}
//                            Status    : {}
//                            Headers   : {}
//                            Payload   : {}
//                            ====================================================
//                            """,
//                            childRequestId,
//                            metaId,
//                            exchangeId,
//                            status,
//                            headers,
//                            body
//                    );
//                });
//
//
//        /*
//         =====================================================
//              4️⃣ FINAL RESPONSE TO CLIENT
//         =====================================================
//         */
//        onCompletion()
//                .process(exchange -> {
//
//                    String parentRequestId =
//                            exchange.getProperty("parentRequestId", String.class);
//
//                    String metaId =
//                            exchange.getProperty("metaId", String.class);
//
//                    String method =
//                            exchange.getProperty("originalMethod", String.class);
//
//                    String url =
//                            exchange.getProperty("originalUrl", String.class);
//
//                    Integer status =
//                            exchange.getMessage()
//                                    .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
//
//                    String body =
//                            exchange.getMessage().getBody(String.class);
//
//                    Long startTime =
//                            exchange.getProperty("startTime", Long.class);
//
//                    long timeTaken = startTime != null
//                            ? System.currentTimeMillis() - startTime
//                            : 0;
//
//                    log.info("""
//
//                            ================= OUTBOUND RESPONSE =================
//                            RequestId    : {}
//                            MetaId       : {}
//                            Method       : {}
//                            URL          : {}
//                            Status       : {}
//                            TimeTaken(ms): {}
//                            Payload      : {}
//                            ====================================================
//                            """,
//                            parentRequestId,
//                            metaId,
//                            method,
//                            url,
//                            status,
//                            timeTaken,
//                            body
//                    );
//                });
//        /*
//         =====================================================
//                           REST ROUTES
//         =====================================================
//         */
//
//        rest("/test")
//                .get()
//                .to("direct:testRoute");
//
//        from("direct:testRoute")
//                .routeId("test-route")
//                .log("Calling downstream service")
//                .removeHeaders("*")
//                .toD("http://localhost:8081/employee"
//                        + "?bridgeEndpoint=true"
//                        + "&throwExceptionOnFailure=true");
//
//
//        rest("/employee")
//                .post()
//                .to("direct:createEmployee");
//
//        from("direct:createEmployee")
//                .routeId("create-employee-route")
//                .log("Authenticating request")
//                .process(authenticationProcessor)
//                .log("Calling downstream employee service")
//                .toD("http://localhost:8081/employee"
//                        + "?bridgeEndpoint=true"
//                        + "&throwExceptionOnFailure=true");
//    }
//}

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
                .process(outboundRequestProcessor)
                .toD("http://localhost:8081/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=true")
                .process(inboundResponseProcessor)
                .process(finalResponseProcessor);

        rest("/employee")
                .post()
                .to("direct:createEmployee");

        from("direct:createEmployee")
                .routeId("create-test-route")
                .process(inboundRequestProcessor)
                .process(authenticationProcessor)
                .process(outboundRequestProcessor)
                .toD("http://localhost:8081/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=true")
                .process(inboundResponseProcessor)
                .process(finalResponseProcessor);
    }
}
