package rs.ac.uns.acs.nais.TimeseriesDatabaseService.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxConfig {
    @Value("${spring.influx.url:${influx.url:}}")
    private String url;

    @Value("${spring.influx.token:${influx.token:}}")
    private String token;

    @Value("${spring.influx.org:${influx.org:}}")
    private String org;

    @Bean
    public InfluxDBClient influxDBClient() {
        char[] tokenChars = (token == null) ? null : token.toCharArray();
        return InfluxDBClientFactory.create(url, tokenChars, org);
    }
}
