package migration.migration.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class LoggingContextProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        String requestId = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        String correlationId = UUID.randomUUID().toString();

        exchange.setProperty("requestId", requestId);
        exchange.setProperty("correlationId", correlationId);
        exchange.setProperty("startTime", System.currentTimeMillis());

        exchange.setProperty("inboundMethod",
                exchange.getIn().getHeader(Exchange.HTTP_METHOD));
        exchange.setProperty("inboundPath",
                exchange.getIn().getHeader(Exchange.HTTP_PATH));
        exchange.setProperty("inboundBody",
                exchange.getIn().getBody(String.class));
    }
}
