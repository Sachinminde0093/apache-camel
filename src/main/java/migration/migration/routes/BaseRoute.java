package migration.migration.routes;

import migration.migration.exception.ServiceException;
import migration.migration.exception.ErrorResponse;
import migration.migration.util.LogUtil;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.http.HttpStatus;

public abstract class BaseRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /*
         =====================================================
                    GLOBAL EXCEPTION HANDLER
         =====================================================
         */

        onException(Exception.class)
                .handled(true)
                .process(exchange -> {

                    Exception exception =
                            exchange.getProperty(
                                    Exchange.EXCEPTION_CAUGHT,
                                    Exception.class
                            );

                    int status;
                    String errorMessage;
                    String errorText;

                    // 🔹 Custom API Exceptions
                    if (exception instanceof ServiceException apiEx) {

                        status = apiEx.getStatus();
                        errorMessage = apiEx.getMessage();
                        errorText = HttpStatus
                                .valueOf(status)
                                .getReasonPhrase();

                    }
                    // 🔹 Downstream HTTP Errors
                    else if (exception instanceof HttpOperationFailedException httpEx) {

                        status = httpEx.getStatusCode();
                        errorMessage = httpEx.getResponseBody();
                        errorText = httpEx.getStatusText();

                    }
                    // 🔹 Generic 500
                    else {

                        status = 500;
                        errorMessage = exception.getMessage();
                        errorText = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
                    }

                    ErrorResponse errorResponse =
                            new ErrorResponse(
                                    status,
                                    errorText,
                                    errorMessage,
                                    System.currentTimeMillis()
                            );

                    // Set HTTP response headers
                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE,
                            status
                    );

                    exchange.getMessage().setHeader(
                            Exchange.CONTENT_TYPE,
                            "application/json"
                    );

                    exchange.getMessage().setBody(errorResponse);

                    // 🔥 Centralized Logging
                    LogUtil.logException(
                            exchange,
                            status,
                            exception.getClass().getSimpleName(),
                            errorMessage
                    );
                })
                .marshal().json();

        /*
         =====================================================
                Allow Child Routes to Configure
         =====================================================
         */
        configureRoutes();
    }

    protected abstract void configureRoutes() throws Exception;
}
