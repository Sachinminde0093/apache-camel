package migration.migration.config;

import migration.migration.exception.ApiException;
import migration.migration.exception.ErrorResponse;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GlobalExceptionRoute extends RouteBuilder {

    @Override
    public void configure() {

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
                .marshal().json();


        // Downstream HTTP errors
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


        // Generic fallback
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
    }
}
