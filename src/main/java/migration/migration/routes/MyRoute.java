package migration.migration.routes;

import migration.migration.exception.ApiException;
import migration.migration.exception.ErrorResponse;
import migration.migration.processor.AuthenticationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class MyRoute extends RouteBuilder {

    private final AuthenticationProcessor authenticationProcessor;

    public MyRoute(AuthenticationProcessor authenticationProcessor) {
        this.authenticationProcessor = authenticationProcessor;
    }

    @Override
    public void configure() {

        intercept()
                .process(exchange -> {

                    if (exchange.getProperty("correlationId") == null) {
                        exchange.setProperty("correlationId",
                                java.util.UUID.randomUUID().toString());
                    }

                    String correlationId =
                            exchange.getProperty("correlationId", String.class);

                    String routeId = exchange.getFromRouteId();
                    String body = exchange.getMessage().getBody(String.class);

                    log.info("""
                ================= ROUTE EXECUTION =================
                CorrelationId: {}
                RouteId: {}
                Body: {}
                ===================================================
                """,
                            correlationId,
                            routeId,
                            body
                    );
                });

        interceptSendToEndpoint("http*")
                .process(exchange -> {

                    String correlationId =
                            exchange.getProperty("correlationId", String.class);

                    String endpoint =
                            exchange.getProperty(Exchange.TO_ENDPOINT, String.class);

                    String body =
                            exchange.getMessage().getBody(String.class);

                    log.info("""
                ================= OUTBOUND HTTP ====================
                CorrelationId: {}
                Endpoint: {}
                Body: {}
                ====================================================
                """,
                            correlationId,
                            endpoint,
                            body
                    );
                });



        /*
         =====================================================
                    GLOBAL EXCEPTION HANDLING
         =====================================================
         */

        // Custom API exceptions (401, 400, 403 etc.)
        onException(ApiException.class)
                .handled(true)
                .process(exchange -> {

                    ApiException ex = exchange.getProperty(
                            Exchange.EXCEPTION_CAUGHT,
                            ApiException.class);

                    int status = ex.getStatus();

                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE, status);

                    exchange.getMessage().setHeader(
                            Exchange.CONTENT_TYPE, "application/json");

                    ErrorResponse response = new ErrorResponse(
                            status,
                            HttpStatus.valueOf(status).getReasonPhrase(),
                            ex.getMessage(),
                            System.currentTimeMillis()
                    );

                    exchange.getMessage().setBody(response);
                })
                .marshal().json();   // 🔥 FIX FOR YOUR ERROR


        // Downstream HTTP errors (403, 404, 500, 502 etc.)
        onException(HttpOperationFailedException.class)
                .handled(true)
                .process(exchange -> {

                    HttpOperationFailedException ex =
                            exchange.getProperty(
                                    Exchange.EXCEPTION_CAUGHT,
                                    HttpOperationFailedException.class);

                    int status = ex.getStatusCode();

                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE, status);

                    exchange.getMessage().setHeader(
                            Exchange.CONTENT_TYPE, "application/json");

                    ErrorResponse response = new ErrorResponse(
                            status,
                            ex.getStatusText(),
                            ex.getResponseBody(),
                            System.currentTimeMillis()
                    );

                    exchange.getMessage().setBody(response);
                })
                .marshal().json();


        // Generic 500 fallback
        onException(Exception.class)
                .handled(true)
                .process(exchange -> {

                    Exception ex = exchange.getProperty(
                            Exchange.EXCEPTION_CAUGHT,
                            Exception.class);

                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE, 500);

                    exchange.getMessage().setHeader(
                            Exchange.CONTENT_TYPE, "application/json");

                    ErrorResponse response = new ErrorResponse(
                            500,
                            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            ex.getMessage(),
                            System.currentTimeMillis()
                    );

                    exchange.getMessage().setBody(response);
                })
                .marshal().json();


        /*
         =====================================================
                           REST ROUTES
         =====================================================
         */

        rest("/test")
                .get()
                .to("direct:testRoute");

        from("direct:testRoute")
                .routeId("test-route")
                .log("Calling downstream service")
                .removeHeaders("*")
                .toD("http://localhost:8081/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=true");


        rest("/employee")
                .post()
                .to("direct:createEmployee");

        from("direct:createEmployee")
                .routeId("create-employee-route")
                .log("Authenticating request")
                .process(authenticationProcessor)
                .log("Calling downstream employee service")
                .toD("http://localhost:8081/employee"
                        + "?bridgeEndpoint=true"
                        + "&throwExceptionOnFailure=true");
    }
}
