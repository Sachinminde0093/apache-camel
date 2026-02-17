package migration.migration.processor;

import migration.migration.util.LogUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.concurrent.atomic.AtomicInteger;

public class OutboundRequestProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        AtomicInteger counter =
                exchange.getProperty("childCounter", AtomicInteger.class);

        String parentId =
                exchange.getProperty("parentRequestId", String.class);

        String childId =
                parentId + "-" + counter.incrementAndGet();

        exchange.setProperty("childRequestId", childId);

        LogUtil.logOutboundRequest(exchange);
    }
}
