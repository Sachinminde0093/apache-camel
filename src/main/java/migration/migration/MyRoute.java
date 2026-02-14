package migration.migration;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MyRoute extends RouteBuilder {

    @Override
    public void configure() {


        rest("/test")
                .get()
                .to("direct:testRoute");

        from("direct:testRoute")
                .log("Outbound request to Google")
                .removeHeaders("*")
                .to("https://www.geeksforgeeks.org/");
    }
}
