package rs.ac.uns.acs.nais.TimeseriesDatabaseService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.Purchase;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.service.PurchaseService;

import java.util.List;

@RestController
@RequestMapping("/purchase.json")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("findAll")
    public ResponseEntity<List<Purchase>> findAll() {
        return new ResponseEntity<>(purchaseService.findAll(), HttpStatus.OK);
    }

    @GetMapping("findAllByCustomerId")
    public ResponseEntity<List<Purchase>> findAllByCustomerId(@RequestParam("customerId") String customerId) {
        return new ResponseEntity<>(purchaseService.findAllByCustomerId(customerId), HttpStatus.OK);
    }

    @GetMapping("findAllByProductId")
    public ResponseEntity<List<Purchase>> findAllByProductId(@RequestParam("productId") String productId) {
        return new ResponseEntity<>(purchaseService.findAllByProductId(productId), HttpStatus.OK);
    }

    @PostMapping("save")
    public ResponseEntity<Boolean> save(@RequestBody Purchase purchase) {
        if(purchaseService.save(purchase)){
            return new ResponseEntity<>(true, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }

    }
}
