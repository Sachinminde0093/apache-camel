package migration.migration.config;

import migration.migration.dto.ApiError;
import migration.migration.exception.ApiException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class GlobalExceptionRoute extends RouteBuilder {

    @Override
    public void configure() {

        // 🔥 Handle custom API exceptions
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

        // 🔥 Generic fallback
        onException(Exception.class)
                .handled(true)
                .maximumRedeliveries(0)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(constant("Internal Server Error"));
    }
}
