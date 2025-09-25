package rs.ac.uns.acs.nais.GraphDatabaseService.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import rs.ac.uns.acs.nais.GraphDatabaseService.model.Product;
import rs.ac.uns.acs.nais.GraphDatabaseService.service.impl.ProductService;

import java.io.ByteArrayOutputStream;
import java.util.List;

import rs.ac.uns.acs.nais.GraphDatabaseService.model.Customer;

@RestController
@RequestMapping("/products.json")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    public ResponseEntity<List<Product>> getAllProduct() {
        List<Product> products = productService.findAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Product> addNewProduct(@RequestBody Product product){
        Product createdProduct = productService.addNewProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Product> deleteProduct(@RequestParam("id") String id){
        if(productService.deleteProduct(id)){
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping
    public ResponseEntity<Customer> updateProduct(@RequestParam("id") String id, @RequestParam("productName") String productName){
        if(productService.updateProduct(id, productName)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("recommendByPurchase")
    public ResponseEntity<List<Product>> recommendProductsByPurchaseHistory(@RequestParam("customerId") Long customerId){

        return new ResponseEntity<>(productService.recommendProductsByPurchaseHistory(customerId),HttpStatus.OK);
    }
    @GetMapping("recommendByReview")
    public ResponseEntity<List<Product>> recommendProductsByReviews(@RequestParam("customerId") Long customerId){

        return new ResponseEntity<>(productService.recommendProductsByReviews(customerId),HttpStatus.OK);
    }

    @GetMapping(value = "/export-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf() {
        List<Product> products = productService.findAllProducts();
        try {
            byte[] pdfContents = productService.export(products);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "products.pdf");

            return ResponseEntity.ok()
                                 .headers(headers)
                                 .body(pdfContents);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

