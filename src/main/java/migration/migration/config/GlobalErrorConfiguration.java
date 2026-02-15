//package migration.migration.config;
//
//import migration.migration.exception.ApiException;
//import migration.migration.exception.ErrorResponse;
//import org.apache.camel.Exchange;
//import org.apache.camel.builder.RouteConfigurationBuilder;
//import org.apache.camel.http.base.HttpOperationFailedException;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//
//@Component
//public class GlobalErrorConfiguration extends RouteConfigurationBuilder {
//
//    @Override
//    public void configuration() {
//
//        routeConfiguration("global-error")
//
//                /*
//                 ============================================
//                   CUSTOM API EXCEPTION (401, 400 etc.)
//                 ============================================
//                 */
//                .onException(ApiException.class)
//                .handled(true)
//                .process(exchange -> {
//
//                    ApiException ex =
//                            exchange.getProperty(
//                                    Exchange.EXCEPTION_CAUGHT,
//                                    ApiException.class);
//
//                    int status = ex.getStatus();
//
//                    exchange.getMessage().setHeader(
//                            Exchange.HTTP_RESPONSE_CODE, status);
//
//                    exchange.getMessage().setHeader(
//                            Exchange.CONTENT_TYPE, "application/json");
//
//                    exchange.getMessage().setBody(
//                            new ErrorResponse(
//                                    status,
//                                    HttpStatus.valueOf(status).getReasonPhrase(),
//                                    ex.getMessage(),
//                                    System.currentTimeMillis()
//                            )
//                    );
//                })
//                .marshal().json()
//
//                /*
//                 ============================================
//                   DOWNSTREAM HTTP ERROR
//                 ============================================
//                 */
//                .onException(HttpOperationFailedException.class)
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
//                    exchange.getMessage().setBody(
//                            new ErrorResponse(
//                                    status,
//                                    ex.getStatusText(),
//                                    ex.getResponseBody(),
//                                    System.currentTimeMillis()
//                            )
//                    );
//                })
//                .marshal().json()
//
//                /*
//                 ============================================
//                   GENERIC 500
//                 ============================================
//                 */
//                .onException(Exception.class)
//                .handled(true)
//                .process(exchange -> {
//
//                    exchange.getMessage().setHeader(
//                            Exchange.HTTP_RESPONSE_CODE, 500);
//
//                    exchange.getMessage().setHeader(
//                            Exchange.CONTENT_TYPE, "application/json");
//
//                    exchange.getMessage().setBody(
//                            new ErrorResponse(
//                                    500,
//                                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
//                                    "Internal Server Error",
//                                    System.currentTimeMillis()
//                            )
//                    );
//                })
//                .marshal().json();
//    }
//}
