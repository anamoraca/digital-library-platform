package rs.ac.uns.acs.nais.GraphDatabaseService.service.impl;

import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.OrderDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.OrderItemDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.ReviewDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Customer;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Product;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Review;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.ProductRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.repository.CustomerRepository;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.ICustomerService;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CustomerService(CustomerRepository customerRepository, ProductRepository productRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer addNewCustomer(Customer customer) {
        customer.setActive(true);
        Customer customer1 = customerRepository.save(customer);
        return customer1;
    }

    @Override
    public boolean deleteCustomer(String customerEmail) {
        Customer customer = customerRepository.findByEmail(customerEmail);
        if(customer != null){
            customer.setActive(false);
            customerRepository.save(customer);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateCustomer(String customerEmailOld, String customerEmailNew) {
        Customer customer = customerRepository.findByEmail(customerEmailOld);
        if(customer != null){
            customer.setEmail(customerEmailNew);
            customerRepository.save(customer);
            return true;
        }
        return false;
    }

    @Override
    public Customer addReview(ReviewDTO reviewDTO) {
        Customer customer = customerRepository.findByEmail(reviewDTO.getCustomerEmail());
        Optional<Product> product = productRepository.findById(reviewDTO.getProductId());
        if(customer != null && product.isPresent()){
            Review review = new Review();
            review.setProduct(product.get());
            review.setRating(reviewDTO.getRating());
            customer.addReview(review);
            return customerRepository.save(customer);
        }
        return null;
    }

    @Override
    public void addPurchase(OrderDTO orderDTO) {
        for(OrderItemDTO orderItemDTO: orderDTO.getItems()){
            if (customerRepository.hasPurchasedProduct(orderDTO.getCustomerId(), orderItemDTO.getProductId())){
                customerRepository.purchaseProduct(orderDTO.getCustomerId(), orderItemDTO.getProductId());
            }else{
                customerRepository.createPurchase(orderDTO.getCustomerId(), orderItemDTO.getProductId());
            }

        }

    }

    @Override
    public List<Product> recommendProductsByPurchaseHistory(Long customerId) {
        return productRepository.recommendProductsByPurchaseHistory(customerId);
    }

    @Override
    public List<Product> recommendProductsByReviews(Long customerId) {
        return productRepository.recommendProductsByReviews(customerId);
    }

}
