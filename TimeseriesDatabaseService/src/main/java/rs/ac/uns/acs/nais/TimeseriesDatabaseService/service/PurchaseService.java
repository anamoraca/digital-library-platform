package rs.ac.uns.acs.nais.TimeseriesDatabaseService.service;


import org.springframework.stereotype.Service;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.Purchase;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository.PurchaseRepositoryImpl;

import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepositoryImpl purchaseRepository;

    public PurchaseService(PurchaseRepositoryImpl purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public boolean save(Purchase purchase){
        return purchaseRepository.save(purchase);
    }

    public List<Purchase> findAll(){
        System.out.println("hohoho5555");
        return purchaseRepository.retrieveDataFromInfluxDB();
    }

    public List<Purchase> findAllByCustomerId(String customerId) {
        return purchaseRepository.findAllByCustomerId(customerId);
    }

    public List<Purchase> findAllByProductId(String productId) {
        return purchaseRepository.findAllByProductId(productId);
    }
}
