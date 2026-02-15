package migration.migration.config;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalLoggingRoute extends RouteBuilder {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalLoggingRoute.class);

    @Override
    public void configure() {

        /*
         ==================================================
                1️⃣ CLIENT → YOUR API (INBOUND)
         ==================================================
         */
        intercept()
                .process(exchange -> {

                    String exchangeId = exchange.getExchangeId();
                    String routeId = exchange.getFromRouteId();
                    String body = exchange.getIn().getBody(String.class);

                    log.info("""
                            
                            ================= INBOUND =================
                            ExchangeId : {}
                            RouteId    : {}
                            Body       : {}
                            ==========================================
                            """,
                            exchangeId, routeId, body);
                });


        /*
         ==================================================
                2️⃣ YOUR API → DOWNSTREAM (OUTBOUND)
         ==================================================
         */
        interceptSendToEndpoint("http*")
                .process(exchange -> {

                    String exchangeId = exchange.getExchangeId();
                    String endpoint =
                            exchange.getProperty(Exchange.TO_ENDPOINT, String.class);
                    String body = exchange.getIn().getBody(String.class);

                    log.info("""
                            
                            ================= OUTBOUND REQUEST =================
                            ExchangeId : {}
                            Endpoint   : {}
                            Body       : {}
                            =====================================================
                            """,
                            exchangeId, endpoint, body);
                });


        /*
         ==================================================
                4️⃣ YOUR API → CLIENT (FINAL RESPONSE)
         ==================================================
         */
        onCompletion()
                .process(exchange -> {

                    String exchangeId = exchange.getExchangeId();
                    Integer status = exchange.getMessage()
                            .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
                    String body = exchange.getMessage().getBody(String.class);

                    log.info("""
                            
                            ================= OUTBOUND RESPONSE =================
                            ExchangeId : {}
                            Status     : {}
                            Body       : {}
                            =====================================================
                            """,
                            exchangeId, status, body);
                });
    }
}
