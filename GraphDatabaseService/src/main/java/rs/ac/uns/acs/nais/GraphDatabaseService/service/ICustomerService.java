package rs.ac.uns.acs.nais.GraphDatabaseService.service;

import rs.ac.uns.acs.nais.GraphDatabaseService.dto.OrderDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.ReviewDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Customer;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Product;

import java.util.List;

public interface ICustomerService {

     List<Customer> findAll();

     Customer addNewCustomer(Customer customer);

     boolean deleteCustomer(String customerEmail);

     boolean updateCustomer(String customerEmailOld, String customerEmailNew);

     Customer addReview(ReviewDTO reviewDTO);

     void addPurchase(OrderDTO orderDTO);


     List<Product> recommendProductsByPurchaseHistory(Long customerId);

     List<Product> recommendProductsByReviews(Long customerId);
}
