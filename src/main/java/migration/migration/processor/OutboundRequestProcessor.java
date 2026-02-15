package migration.migration.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OutboundRequestProcessor implements Processor {

    private static final Logger log =
            LoggerFactory.getLogger(OutboundRequestProcessor.class);

    @Override
    public void process(Exchange exchange) {

        String parentId =
                exchange.getProperty("parentRequestId", String.class);

        AtomicInteger counter =
                exchange.getProperty("childCounter", AtomicInteger.class);

        int childNo = counter.incrementAndGet();
        String childRequestId = parentId + "-" + childNo;

        exchange.setProperty("childRequestId", childRequestId);

        log.info("""
                
                ================= OUTBOUND REQUEST =================
                RequestId : {}
                MetaId    : {}
                ExchangeId: {}
                URI       : {}
                Headers   : {}
                Payload   : {}
                ====================================================
                """,
                childRequestId,
                exchange.getProperty("metaId"),
                exchange.getExchangeId(),
                exchange.getProperty(Exchange.TO_ENDPOINT),
                exchange.getIn().getHeaders(),
                exchange.getIn().getBody(String.class)
        );
    }
}
