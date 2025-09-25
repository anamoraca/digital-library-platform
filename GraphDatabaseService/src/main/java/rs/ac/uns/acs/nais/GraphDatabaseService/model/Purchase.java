package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public class Purchase {

    @RelationshipId
    private  Long id;
    @TargetNode
    private  Product product;
    private int numberOfPurchasedItems;

    public Purchase() {
    }

    public Purchase(Long id, Product product, int numberOfPurchasedItems) {
        this.id = id;
        this.product = product;
        this.numberOfPurchasedItems = numberOfPurchasedItems;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getNumberOfPurchasedItems() {
        return numberOfPurchasedItems;
    }

    public void setNumberOfPurchasedItems(int numberOfPurchasedItems) {
        this.numberOfPurchasedItems = numberOfPurchasedItems;
    }
}
