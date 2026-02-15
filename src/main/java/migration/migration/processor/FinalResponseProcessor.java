package migration.migration.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FinalResponseProcessor implements Processor {

    private static final Logger log =
            LoggerFactory.getLogger(FinalResponseProcessor.class);

    @Override
    public void process(Exchange exchange) {

        Long startTime =
                exchange.getProperty("startTime", Long.class);

        long timeTaken = startTime != null
                ? System.currentTimeMillis() - startTime
                : 0;

        log.info("""
                
                ================= OUTBOUND RESPONSE =================
                RequestId    : {}
                MetaId       : {}
                Status       : {}
                TimeTaken(ms): {}
                Payload      : {}
                ====================================================
                """,
                exchange.getProperty("parentRequestId"),
                exchange.getProperty("metaId"),
                exchange.getMessage()
                        .getHeader(Exchange.HTTP_RESPONSE_CODE),
                timeTaken,
                exchange.getMessage().getBody(String.class)
        );
    }
}
