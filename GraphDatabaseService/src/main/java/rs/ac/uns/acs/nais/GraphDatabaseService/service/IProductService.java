package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.Customer;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Product;

import java.io.IOException;
import java.util.List;

public interface IProductService {
    List<Product> findAllProducts();
    Product addNewProduct(Product product);
    boolean deleteProduct(String id);
    boolean updateProduct(String id, String productName);
    List<Product> recommendProductsByPurchaseHistory(Long customerId);
    List<Product> recommendProductsByReviews(Long customerId);
    public byte[] export(List<Product> products) throws IOException;
}
