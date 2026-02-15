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


    }
}
