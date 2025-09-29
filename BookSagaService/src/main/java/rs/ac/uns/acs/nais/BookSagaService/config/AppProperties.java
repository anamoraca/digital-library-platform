package rs.ac.uns.acs.nais.BookSagaService.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients")
public class AppProperties {

    private final Endpoint graph = new Endpoint();
    private final Endpoint ts = new Endpoint();

    public Endpoint getGraph() { return graph; }
    public Endpoint getTs() { return ts; }

    public static class Endpoint {
        /**
         * Base URL drugih servisa (npr. http://localhost:8081 ili http://graph-service:8081 u docker-u)
         */
        private String baseUrl;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }
}
