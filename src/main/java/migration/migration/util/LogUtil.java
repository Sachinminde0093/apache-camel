package migration.migration.util;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogUtil {

    private static final Logger log =
            LoggerFactory.getLogger(LogUtil.class);

    private LogUtil() {}

    public static void logInboundRequest(Exchange exchange) {

        log.info("""
                {}
                RequestId : {}
                MetaId    : {}
                Method    : {}
                URL       : {}
                Headers   : {}
                Payload   : {}
                {}
                """,
                LogConstants.INBOUND_REQUEST,
                exchange.getProperty("parentRequestId"),
                exchange.getProperty("metaId"),
                exchange.getProperty("originalMethod"),
                exchange.getProperty("originalUrl"),
                exchange.getIn().getHeaders(),
                exchange.getIn().getBody(String.class),
                LogConstants.FOOTER
        );
    }

    public static void logOutboundRequest(Exchange exchange) {

        log.info("""
                {}
                RequestId : {}
                MetaId    : {}
                ExchangeId: {}
                Method    : {}
                URI       : {}
                Headers   : {}
                Payload   : {}
                {}
                """,
                LogConstants.OUTBOUND_REQUEST,
                exchange.getProperty("childRequestId"),
                exchange.getProperty("metaId"),
                exchange.getExchangeId(),
                exchange.getIn().getHeader(Exchange.HTTP_METHOD),
                exchange.getProperty("downstreamUrl"),
                exchange.getIn().getHeaders(),
                exchange.getIn().getBody(String.class),
                LogConstants.FOOTER
        );
    }

    public static void logInboundResponse(Exchange exchange) {

        log.info("""
                {}
                RequestId : {}
                MetaId    : {}
                ExchangeId: {}
                URI       : {}
                Status    : {}
                Headers   : {}
                Payload   : {}
                {}
                """,
                LogConstants.INBOUND_RESPONSE,
                exchange.getProperty("childRequestId"),
                exchange.getProperty("metaId"),
                exchange.getExchangeId(),
                exchange.getProperty("downstreamUrl"),
                exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE),
                exchange.getMessage().getHeaders(),
                exchange.getMessage().getBody(String.class),
                LogConstants.FOOTER
        );
    }

    public static void logOutboundResponse(Exchange exchange) {

        long timeTaken = CommonUtil.calculateTime(
                exchange.getProperty("startTime", Long.class)
        );

        log.info("""
                {}
                RequestId    : {}
                MetaId       : {}
                Method       : {}
                URL          : {}
                Status       : {}
                TimeTaken(ms): {}
                Payload      : {}
                {}
                """,
                LogConstants.OUTBOUND_RESPONSE,
                exchange.getProperty("parentRequestId"),
                exchange.getProperty("metaId"),
                exchange.getProperty("originalMethod"),
                exchange.getProperty("originalUrl"),
                exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE),
                timeTaken,
                exchange.getMessage().getBody(String.class),
                LogConstants.FOOTER
        );
    }

    public static void logException(Exchange exchange,
                                    int status,
                                    String errorType,
                                    String errorMessage) {

        log.info("""
                {}
                RequestId : {}
                MetaId    : {}
                Status    : {}
                ErrorType : {}
                ErrorMsg  : {}
                Method    : {}
                URL       : {}
                {}
                """,
                LogConstants.EXCEPTION,
                exchange.getProperty("parentRequestId"),
                exchange.getProperty("metaId"),
                status,
                errorType,
                errorMessage,
                exchange.getProperty("originalMethod"),
                exchange.getProperty("originalUrl"),
                LogConstants.FOOTER
        );
    }
}
