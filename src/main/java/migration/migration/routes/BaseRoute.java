package migration.migration.routes;

import migration.migration.exception.ApiException;
import migration.migration.exception.ErrorResponse;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /*
         =====================================================
                CUSTOM API EXCEPTIONS
         =====================================================
         */
        /*
 =====================================================
        CUSTOM API EXCEPTIONS
 =====================================================
 */
        onException(ApiException.class)
                .handled(true)
                .process(exchange -> {

                    ApiException ex = exchange.getProperty(
                            Exchange.EXCEPTION_CAUGHT,
                            ApiException.class);

                    int status = ex.getStatus();

                    ErrorResponse errorResponse =
                            new ErrorResponse(
                                    status,
                                    HttpStatus.valueOf(status).getReasonPhrase(),
                                    ex.getMessage(),
                                    System.currentTimeMillis()
                            );

                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE, status);

                    exchange.getMessage().setHeader(
                            Exchange.CONTENT_TYPE, "application/json");

                    exchange.getMessage().setBody(errorResponse);

                    // 🔥 Convert to JSON for logging
                    String jsonPayload =
                            new com.fasterxml.jackson.databind.ObjectMapper()
                                    .writeValueAsString(errorResponse);

                    log.info("""
                    ================= API EXCEPTION =================
                    RequestId : {}
                    MetaId    : {}
                    Status    : {}
                    ErrorMsg  : {}
                    Method    : {}
                    URL       : {}
                    Headers   : {}
                    Payload   : {}
                    =================================================
                    """,
                            exchange.getProperty("parentRequestId"),
                            exchange.getProperty("metaId"),
                            status,
                            ex.getMessage(),
                            exchange.getProperty("originalMethod"),
                            exchange.getProperty("originalUrl"),
                            exchange.getIn().getHeaders(),
                            jsonPayload
                    );
                })
                .marshal().json();




        /*
         =====================================================
                DOWNSTREAM HTTP ERROR
         =====================================================
         */
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

                    exchange.getMessage().setBody(
                            new ErrorResponse(
                                    status,
                                    ex.getStatusText(),
                                    ex.getResponseBody(),
                                    System.currentTimeMillis()
                            )
                    );
                })
                .marshal().json();


        /*
         =====================================================
                GENERIC 500
         =====================================================
         */
        onException(Exception.class)
                .handled(true)
                .process(exchange -> {

                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE, 500);

                    exchange.getMessage().setHeader(
                            Exchange.CONTENT_TYPE, "application/json");

                    exchange.getMessage().setBody(
                            new ErrorResponse(
                                    500,
                                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                    "Internal Server Error",
                                    System.currentTimeMillis()
                            )
                    );
                })
                .marshal().json();

        configureRoutes();
    }

    protected abstract void configureRoutes() throws Exception;
}
