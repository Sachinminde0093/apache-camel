//package migration.migration.config;
//
//import org.apache.camel.Exchange;
//import org.apache.camel.builder.RouteBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class GlobalLoggingRoute extends RouteBuilder {
//
//    private static final Logger log =
//            LoggerFactory.getLogger(GlobalLoggingRoute.class);
//
//    @Override
//    public void configure() {
//
//        /*
//         ============================================================
//                1️⃣ CLIENT → YOUR API (INBOUND REQUEST)
//         ============================================================
//         */
//        interceptFrom()
//                .process(exchange -> {
//
//                    exchange.setProperty("startTime",
//                            System.currentTimeMillis());
//
//                    String exchangeId = exchange.getExchangeId();
//                    String method = exchange.getIn()
//                            .getHeader(Exchange.HTTP_METHOD, String.class);
//                    String path = exchange.getIn()
//                            .getHeader(Exchange.HTTP_PATH, String.class);
//                    String body = exchange.getIn()
//                            .getBody(String.class);
//
//                    log.info("""
//
//                            ================= INBOUND REQUEST =================
//                            ExchangeId : {}
//                            Method     : {}
//                            Path       : {}
//                            Headers    : {}
//                            Payload    : {}
//                            ====================================================
//                            """,
//                            exchangeId,
//                            method,
//                            path,
//                            exchange.getIn().getHeaders(),
//                            body
//                    );
//                });
//
//        /*
//         ============================================================
//                2️⃣ YOUR API → DOWNSTREAM (OUTBOUND REQUEST)
//         ============================================================
//         */
//        interceptSendToEndpoint("http*")
//                .process(exchange -> {
//
//                    String exchangeId = exchange.getExchangeId();
//                    String method = exchange.getIn()
//                            .getHeader(Exchange.HTTP_METHOD, String.class);
//                    String uri = exchange.getProperty(
//                            Exchange.TO_ENDPOINT, String.class);
//
//                    String body = exchange.getIn()
//                            .getBody(String.class);
//
//                    log.info("""
//
//                            ================= OUTBOUND REQUEST =================
//                            ExchangeId : {}
//                            Method     : {}
//                            URI        : {}
//                            Headers    : {}
//                            Payload    : {}
//                            ====================================================
//                            """,
//                            exchangeId,
//                            method,
//                            uri,
//                            exchange.getIn().getHeaders(),
//                            body
//                    );
//                });
//
//        /*
//         ============================================================
//                3️⃣ YOUR API → CLIENT (OUTBOUND RESPONSE)
//         ============================================================
//         */
//        onCompletion()
//                .process(exchange -> {
//
//                    String exchangeId = exchange.getExchangeId();
//
//                    Integer status = exchange.getMessage()
//                            .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
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
//                            ExchangeId : {}
//                            Status     : {}
//                            TimeTaken(ms): {}
//                            =====================================================
//                            """,
//                            exchangeId,
//                            status,
//                            timeTaken
//                    );
//                });
//    }
//}
