package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import org.neo4j.cypherdsl.core.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.OrderDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.dto.ReviewDTO;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Customer;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Product;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.impl.CustomerService;

import java.util.List;

@RestController
@RequestMapping("/customers.json")
public class CustomerController {

    @Autowired
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("")
    public ResponseEntity<List<Customer>> findAll() {
        List<Customer> customers = customerService.findAll();
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }


    @PostMapping
    public ResponseEntity<Customer> addNewCustomer(@RequestBody Customer customer){
        Customer createdCustomer = customerService.addNewCustomer(customer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Customer> deleteCustomer(@RequestParam String customerEmail){
        if(customerService.deleteCustomer(customerEmail)){
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping
    public ResponseEntity<Customer> updateCustomer(@RequestParam("customerEmailOld") String customerEmailOld, @RequestParam("customerEmailNew") String customerEmailNew ){
        if(customerService.updateCustomer(customerEmailOld, customerEmailNew)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("addReview")
    public ResponseEntity addNewReview(@RequestBody ReviewDTO reviewDTO){
        if(customerService.addReview(reviewDTO) != null){
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("addPurchase")
    public ResponseEntity<Customer> addNewPurchase(@RequestBody OrderDTO orderDTO){
        customerService.addPurchase(orderDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }



}

