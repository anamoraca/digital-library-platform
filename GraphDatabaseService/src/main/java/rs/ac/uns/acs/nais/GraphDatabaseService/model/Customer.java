package rs.ac.uns.acs.nais.GraphDatabaseService.model;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.ArrayList;
import java.util.List;
@Node
public class Customer {

    @Id
    private Long idOriginal;

    private String email;

    private Boolean isActive;

    @Relationship(value = "REVIEWED", direction = Relationship.Direction.OUTGOING)
    private List<Review> reviewed = new ArrayList<>();

    @Relationship(value = "PURCHASED", direction = Relationship.Direction.OUTGOING)
    private List<Purchase> purchased = new ArrayList<>();

    public Customer() {
    }

    public Customer(Long id, String email, List<Review> reviewed) {
        this.idOriginal = id;
        this.email = email;
        this.reviewed = reviewed;
    }

    public Long getId() {
        return idOriginal;
    }

    public void setId(Long id) {
        this.idOriginal = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Review> getReviewed() {
        return reviewed;
    }

    public void setReviewed(List<Review> reviewed) {
        this.reviewed = reviewed;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public void addReview(Review review){
        this.reviewed.add(review);
    }

    public void addPurchase(Purchase purchase){
        this.purchased.add(purchase);
    }
}
