package migration.migration.processor;

import migration.migration.util.LogUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class InboundResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        LogUtil.logInboundResponse(exchange);
    }
}
