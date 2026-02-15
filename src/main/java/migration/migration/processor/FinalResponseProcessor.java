package migration.migration.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinalResponseProcessor implements Processor {

    private static final Logger log =
            LoggerFactory.getLogger(FinalResponseProcessor.class);

    @Override
    public void process(Exchange exchange) {

        Long start = exchange.getProperty("startTime", Long.class);

        long timeTaken = start != null
                ? System.currentTimeMillis() - start
                : 0;

        Integer status = exchange.getMessage()
                .getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);

        log.info("""
                ================= OUTBOUND RESPONSE =================
                RequestId    : {}
                MetaId       : {}
                Method       : {}
                URL          : {}
                Status       : {}
                TimeTaken(ms): {}
                Payload      : {}
                ====================================================
                """,
                exchange.getProperty("parentRequestId"),
                exchange.getProperty("metaId"),
                exchange.getProperty("originalMethod"),
                exchange.getProperty("originalUrl"),
                status,
                timeTaken,
                exchange.getMessage().getBody(String.class)
        );
    }
}
