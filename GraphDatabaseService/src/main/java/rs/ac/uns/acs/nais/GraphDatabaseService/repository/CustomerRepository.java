package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Customer;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Product;

import java.util.List;

@Repository
public interface CustomerRepository extends Neo4jRepository<Customer, Long> {
    Customer findByEmail(String email);
    /**
     * Preporučuje proizvode kupcu na osnovu njegove istorije kupovine.
     * Računa prosečan broj kupljenih stavki od strane ostalih kupaca koji su kupili slične proizvode.
     * Predlaže proizvode sa prosečnim brojem kupovina većim od ukupnog proseka.
     *
     * @param customerId ID kupca
     * @return Lista objektnih nizova, svaki sadrži preporučeni proizvod i prosečan broj kupovina
     */


    @Query("MATCH (c:Customer {idOriginal: $customerId}) " +
            "OPTIONAL MATCH (c)-[r:PURCHASED]->(p:Product {idOriginal: $productId}) " +
            "WITH c, p, COALESCE(r, 0) AS rel " +
            "MERGE (c)-[purchase: PURCHASED]->(p) " +
            "ON CREATE SET purchase.numberOfPurchasedItems = 1 " +
            "ON MATCH SET purchase.numberOfPurchasedItems = rel.numberOfPurchasedItems + 1")
    void purchaseProduct(Long customerId, String productId);


    @Query("MATCH (c:Customer {idOriginal: $customerId})-[r:PURCHASED]->(p:Product {idOriginal: $productId}) " +
            "RETURN count(r) > 0")
    boolean hasPurchasedProduct(Long customerId, String productId);

    @Query("MATCH (c:Customer {idOriginal: $customerId}) " +
            "MATCH (p:Product {idOriginal: $productId}) " +
            "CREATE (c)-[purchase:PURCHASED]->(p) " +
            "SET purchase.numberOfPurchasedItems = 1")
    void createPurchase(Long customerId, String productId);

}
