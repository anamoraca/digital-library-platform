package rs.ac.uns.acs.nais.GraphDatabaseService.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Product;

import java.util.List;


@Repository
public interface ProductRepository extends Neo4jRepository<Product, String> {

    @Query("MATCH (customer:Customer)-[:PURCHASED]->(purchasedProduct:Product) " +
            "WHERE customer.idOriginal = $customerId " +
            "MATCH (customer)-[:PURCHASED]->(:Product)<-[:PURCHASED]-(similarCustomer)-[p1 :PURCHASED]->(recommendedProduct:Product) " +
            "WHERE NOT (customer)-[:PURCHASED]->(recommendedProduct) " +
            "WITH recommendedProduct, AVG(p1.numberOfPurchasedItems) AS avgPurchaseCount " +
            "WHERE avgPurchaseCount > 2 " +
            "RETURN DISTINCT recommendedProduct")
    List<Product> recommendProductsByPurchaseHistory(Long customerId);

    /**
     * Preporučuje proizvode kupcu na osnovu njegovih recenzija.
     * Računa prosečnu ocenu za svaki proizvod i predlaže proizvode sa prosečnom ocenom većom od ukupnog proseka.
     *
     * @param customerId ID kupca
     * @return Lista objektnih nizova, svaki sadrži preporučeni proizvod i prosečnu ocenu
     */
    @Query("MATCH (customer:Customer)-[:REVIEWED]->(:Product)<-[:REVIEWED]-(otherCustomer)-[r1:REVIEWED]->(recommendedProduct:Product) " +
            "WHERE customer.idOriginal = $customerId " +
            "WITH recommendedProduct, AVG(r1.rating) AS avgRating " +
            "WHERE avgRating > 3 " +
            "RETURN recommendedProduct")
    List<Product> recommendProductsByReviews(Long customerId);

}
