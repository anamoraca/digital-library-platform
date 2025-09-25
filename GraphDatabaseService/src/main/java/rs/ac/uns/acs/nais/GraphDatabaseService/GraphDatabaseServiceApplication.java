package rs.ac.uns.acs.nais.GraphDatabaseService;

import org.neo4j.cypherdsl.core.renderer.Configuration;
import org.neo4j.cypherdsl.core.renderer.Dialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class GraphDatabaseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphDatabaseServiceApplication.class, args);
	}

	@Bean
	Configuration cypherDslConfiguration() {
		return Configuration.newConfig()
				.withDialect(Dialect.NEO4J_5).build();
	}

}
