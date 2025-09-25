package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.*;

@Node
public class Product {

    @Id
    private String idOriginal;

    private String name;

    private boolean isAvailable;

    public Product() {
    }

    public String getId() {
        return idOriginal;
    }

    public void setId(String idOriginal) {
        this.idOriginal = idOriginal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
