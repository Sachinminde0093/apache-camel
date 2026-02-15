package migration.migration.config;

import migration.migration.dto.ApiError;
import migration.migration.exception.ApiException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public abstract class BaseRouteBuilder extends RouteBuilder {

    @Override
    public void configure() {

        // Initialize request context
        interceptFrom()
                .process(exchange -> {
                    exchange.setProperty("startTime", System.currentTimeMillis());
                });

        // 🔥 Specific API exception FIRST
        onException(ApiException.class)
                .handled(true)
                .maximumRedeliveries(0)
                .process(exchange -> {

                    ApiException exception =
                            exchange.getProperty(Exchange.EXCEPTION_CAUGHT, ApiException.class);

                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE,
                            exception.getStatus()
                    );

                    exchange.getMessage().setBody(
                            new ApiError(exception.getStatus(), exception.getMessage())
                    );
                });

        // 🔥 Generic fallback LAST
        onException(Exception.class)
                .handled(true)
                .maximumRedeliveries(0)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(constant("Internal Server Error"));

        // Final logging block (ALWAYS executes)
        onCompletion()
                .process(exchange -> {

                    long start = exchange.getProperty("startTime", Long.class);
                    long timeTaken = System.currentTimeMillis() - start;

                    Exception ex =
                            exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

                    log.info("""
                            ================= REQUEST LOG =================
                            ExchangeId    : {}
                            Status        : {}
                            TimeTaken(ms) : {}
                            Exception     : {}
                            =================================================
                            """,
                            exchange.getExchangeId(),
                            exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE),
                            timeTaken,
                            ex != null ? ex.getClass().getSimpleName() : "NONE"
                    );
                });
    }
}
