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
                CUSTOM API EXCEPTIONS (401, 400 etc.)
         =====================================================
         */
        onException(ApiException.class)
                .handled(true)
                .process(exchange -> {

                    ApiException ex =
                            exchange.getProperty(
                                    Exchange.EXCEPTION_CAUGHT,
                                    ApiException.class);

                    int status = ex.getStatus();

                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE, status);

                    exchange.getMessage().setHeader(
                            Exchange.CONTENT_TYPE, "application/json");

                    exchange.getMessage().setBody(
                            new ErrorResponse(
                                    status,
                                    HttpStatus.valueOf(status).getReasonPhrase(),
                                    ex.getMessage(),
                                    System.currentTimeMillis()
                            )
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

        /*
         =====================================================
              1️⃣ CLIENT → YOUR API (INBOUND REQUEST)
         =====================================================
         */
        interceptFrom()
                .process(exchange -> {

                    // Parent Request ID
                    String parentRequestId = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

                    exchange.setProperty("parentRequestId", parentRequestId);

                    // Child counter
                    exchange.setProperty("childCounter",
                            new AtomicInteger(0));

                    // MetaId (UUID)
                    String metaId = UUID.randomUUID().toString();
                    exchange.setProperty("metaId", metaId);

                    exchange.setProperty("startTime",
                            System.currentTimeMillis());

                    String method = exchange.getIn()
                            .getHeader(Exchange.HTTP_METHOD, String.class);

                    String url = exchange.getIn()
                            .getHeader(Exchange.HTTP_URL, String.class);

                    exchange.setProperty("originalMethod", method);
                    exchange.setProperty("originalUrl", url);

                    String headers = exchange.getIn().getHeaders().toString();
                    String body = exchange.getIn().getBody(String.class);

                    log.info("""

                            ================= INBOUND REQUEST =================
                            RequestId : {}
                            MetaId    : {}
                            Method    : {}
                            URL       : {}
                            Headers   : {}
                            Payload   : {}
                            ====================================================
                            """,
                            parentRequestId,
                            metaId,
                            method,
                            url,
                            headers,
                            body
                    );
                });


        /*
         =====================================================
              2️⃣ OUTBOUND REQUEST + 3️⃣ INBOUND RESPONSE
         =====================================================
         */
        interceptSendToEndpoint("http*")
                .process(exchange -> {

                    String parentId =
                            exchange.getProperty("parentRequestId", String.class);

                    AtomicInteger counter =
                            exchange.getProperty("childCounter", AtomicInteger.class);

                    int childNo = counter.incrementAndGet();
                    String childRequestId = parentId + "-" + childNo;

                    exchange.setProperty("childRequestId", childRequestId);

                    String metaId =
                            exchange.getProperty("metaId", String.class);

                    String exchangeId = exchange.getExchangeId();

                    String method =
                            exchange.getIn().getHeader(Exchange.HTTP_METHOD, String.class);

                    String uri =
                            exchange.getProperty(Exchange.TO_ENDPOINT, String.class);

                    String headers =
                            exchange.getIn().getHeaders().toString();

                    String body =
                            exchange.getIn().getBody(String.class);

                    log.info("""

                            ================= OUTBOUND REQUEST =================
                            RequestId : {}
                            MetaId    : {}
                            ExchangeId: {}
                            Method    : {}
                            URI       : {}
                            Headers   : {}
                            Payload   : {}
                            ====================================================
                            """,
                            childRequestId,
                            metaId,
                            exchangeId,
                            method,
                            uri,
                            headers,
                            body
                    );
                })
                .process(exchange -> {

                    /*
                       This runs AFTER HTTP call completes
                     */

                    String childRequestId =
                            exchange.getProperty("childRequestId", String.class);

                    String metaId =
                            exchange.getProperty("metaId", String.class);

                    String exchangeId = exchange.getExchangeId();

                    Integer status =
                            exchange.getMessage()
                                    .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

                    String headers =
                            exchange.getMessage().getHeaders().toString();

                    String body =
                            exchange.getMessage().getBody(String.class);

                    log.info("""

                            ================= INBOUND RESPONSE =================
                            RequestId : {}
                            MetaId    : {}
                            ExchangeId: {}
                            Status    : {}
                            Headers   : {}
                            Payload   : {}
                            ====================================================
                            """,
                            childRequestId,
                            metaId,
                            exchangeId,
                            status,
                            headers,
                            body
                    );
                });


        /*
         =====================================================
              4️⃣ FINAL RESPONSE TO CLIENT
         =====================================================
         */
        onCompletion()
                .process(exchange -> {

                    String parentRequestId =
                            exchange.getProperty("parentRequestId", String.class);

                    String metaId =
                            exchange.getProperty("metaId", String.class);

                    String method =
                            exchange.getProperty("originalMethod", String.class);

                    String url =
                            exchange.getProperty("originalUrl", String.class);

                    Integer status =
                            exchange.getMessage()
                                    .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

                    String body =
                            exchange.getMessage().getBody(String.class);

                    Long startTime =
                            exchange.getProperty("startTime", Long.class);

                    long timeTaken = startTime != null
                            ? System.currentTimeMillis() - startTime
                            : 0;

                    log.info("""

                            ================= OUTBOUND RESPONSE =================
                            RequestId    : {}
                            MetaId       : {}
                            Method       : {}
                            URL          : {}
                            Status       : {}
                            TimeTaken(ms): {}
                            Payload      : {}
                            ====================================================
                            """,
                            parentRequestId,
                            metaId,
                            method,
                            url,
                            status,
                            timeTaken,
                            body
                    );
                });

        // 🔥 Important: allow subclasses to define routes
        configureRoutes();
    }

    // Subclasses will implement this
    protected abstract void configureRoutes() throws Exception;
}
