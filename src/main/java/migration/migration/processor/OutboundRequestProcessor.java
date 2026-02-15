package migration.migration.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class OutboundRequestProcessor implements Processor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboundRequestProcessor.class);

    @Override
    public void process(Exchange exchange) {

        AtomicInteger counter =
                exchange.getProperty("childCounter", AtomicInteger.class);

        String parentId =
                exchange.getProperty("parentRequestId", String.class);

        String childId =
                parentId + "-" + counter.incrementAndGet();

        exchange.setProperty("childRequestId", childId);

        // Dynamically get endpoint
        String endpoint =
                exchange.getProperty(Exchange.TO_ENDPOINT, String.class);

        if (endpoint != null) {
            endpoint = endpoint.split("\\?")[0];
        }

        log.info("""
                ================= OUTBOUND REQUEST =================
                RequestId : {}
                MetaId    : {}
                ExchangeId: {}
                Method    : {}
                URI       : {}
                Headers   : {}
                Payload   : {}
                ====================================================
                """,
                childId,
                exchange.getProperty("metaId"),
                exchange.getExchangeId(),
                exchange.getIn().getHeader(Exchange.HTTP_METHOD),
                endpoint,
                exchange.getIn().getHeaders(),
                exchange.getIn().getBody(String.class)
        );
    }
}
