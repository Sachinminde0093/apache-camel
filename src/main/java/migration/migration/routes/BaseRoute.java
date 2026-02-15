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
              1️⃣ INBOUND REQUEST
         =====================================================
         */
        interceptFrom()
                .process(exchange -> {

                    String parentRequestId = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

                    exchange.setProperty("parentRequestId", parentRequestId);
                    exchange.setProperty("childCounter", new AtomicInteger(0));
                    exchange.setProperty("metaId", UUID.randomUUID().toString());
                    exchange.setProperty("startTime", System.currentTimeMillis());

                    String method = exchange.getIn()
                            .getHeader(Exchange.HTTP_METHOD, String.class);

                    String url = exchange.getIn()
                            .getHeader(Exchange.HTTP_URL, String.class);

                    exchange.setProperty("originalMethod", method);
                    exchange.setProperty("originalUrl", url);

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
                            exchange.getProperty("metaId"),
                            method,
                            url,
                            exchange.getIn().getHeaders(),
                            exchange.getIn().getBody(String.class)
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

                    String childRequestId =
                            parentId + "-" + counter.incrementAndGet();

                    exchange.setProperty("childRequestId", childRequestId);

                    String baseUri =
                            exchange.getProperty(Exchange.TO_ENDPOINT, String.class);

                    String path =
                            exchange.getIn().getHeader(Exchange.HTTP_PATH, String.class);

                    String uri = baseUri != null
                            ? baseUri.split("\\?")[0]
                            : "";

                    if (path != null && !uri.endsWith(path)) {
                        uri = uri + path;
                    }

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
                            exchange.getProperty("metaId"),
                            exchange.getExchangeId(),
                            exchange.getIn().getHeader(Exchange.HTTP_METHOD),
                            uri,
                            exchange.getIn().getHeaders(),
                            exchange.getIn().getBody(String.class)
                    );
                })
                .process(exchange -> {

                    Integer status = exchange.getMessage()
                            .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

                    if (status == null) {
                        status = exchange.getIn()
                                .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
                    }

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
                            exchange.getProperty("childRequestId"),
                            exchange.getProperty("metaId"),
                            exchange.getExchangeId(),
                            status,
                            exchange.getMessage().getHeaders(),
                            exchange.getMessage().getBody(String.class)
                    );
                });

        /*
         =====================================================
              4️⃣ FINAL RESPONSE
         =====================================================
         */
        onCompletion()
                .process(exchange -> {

                    Long start =
                            exchange.getProperty("startTime", Long.class);

                    long timeTaken =
                            start != null
                                    ? System.currentTimeMillis() - start
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
                            exchange.getProperty("parentRequestId"),
                            exchange.getProperty("metaId"),
                            exchange.getProperty("originalMethod"),
                            exchange.getProperty("originalUrl"),
                            exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE),
                            timeTaken,
                            exchange.getMessage().getBody(String.class)
                    );
                });

        configureRoutes();
    }

    protected abstract void configureRoutes() throws Exception;
}
