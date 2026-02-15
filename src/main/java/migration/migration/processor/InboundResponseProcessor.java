package migration.migration.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundResponseProcessor implements Processor {

    private static final Logger log =
            LoggerFactory.getLogger(InboundResponseProcessor.class);

    @Override
    public void process(Exchange exchange) {

        Integer status = exchange.getMessage()
                .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

        String body = exchange.getMessage()
                .getBody(String.class);

        log.info("""
                ================= INBOUND RESPONSE =================
                RequestId : {}
                MetaId    : {}
                ExchangeId: {}
                Status    : {}
                Headers   : {}
                Payload   : {}
                ====================================================
                """,
                exchange.getProperty("childRequestId"),
                exchange.getProperty("metaId"),
                exchange.getExchangeId(),
                status,
                exchange.getMessage().getHeaders(),
                body
        );
    }
}
