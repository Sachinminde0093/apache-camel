package migration.migration.processor;

import migration.migration.util.LogUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class InboundRequestProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        String requestId = timestamp + random;

        exchange.setProperty("parentRequestId", requestId);
        exchange.setProperty("metaId", UUID.randomUUID().toString());
        exchange.setProperty("childCounter", new AtomicInteger(0));
        exchange.setProperty("startTime", System.currentTimeMillis());

        exchange.setProperty("originalMethod",
                exchange.getIn().getHeader(Exchange.HTTP_METHOD));

        exchange.setProperty("originalUrl",
                exchange.getIn().getHeader(Exchange.HTTP_URL));

        LogUtil.logInboundRequest(exchange);
    }
}
