package migration.migration.processor;


import migration.migration.exception.UnauthorizedException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        String token = exchange.getIn().getHeader("Authorization", String.class);

        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing Authorization Header");
        }
    }
}
