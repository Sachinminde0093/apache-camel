package migration.migration.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InboundRequestProcessor implements Processor {

    private static final Logger log =
            LoggerFactory.getLogger(InboundRequestProcessor.class);

    @Override
    public void process(Exchange exchange) {

        String requestId = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        exchange.setProperty("parentRequestId", requestId);
        exchange.setProperty("metaId", UUID.randomUUID().toString());
        exchange.setProperty("childCounter", new AtomicInteger(0));
        exchange.setProperty("startTime", System.currentTimeMillis());

        String method = exchange.getIn()
                .getHeader(Exchange.HTTP_METHOD, String.class);

        String url = exchange.getIn()
                .getHeader(Exchange.HTTP_URL, String.class);

        exchange.setProperty("originalMethod", method);
        exchange.setProperty("originalUrl", url);

        log.info("""
                ================= INBOUND REQUEST =================
                RequestId : {}
                MetaId    : {}
                Method    : {}
                URL       : {}
                Headers   : {}
                Payload   : {}
                ====================================================
                """,
                requestId,
                exchange.getProperty("metaId"),
                method,
                url,
                exchange.getIn().getHeaders(),
                exchange.getIn().getBody(String.class)
        );
    }
}
