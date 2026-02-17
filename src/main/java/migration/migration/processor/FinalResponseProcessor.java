package migration.migration.processor;

import migration.migration.util.LogUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class FinalResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        LogUtil.logOutboundResponse(exchange);
    }
}
