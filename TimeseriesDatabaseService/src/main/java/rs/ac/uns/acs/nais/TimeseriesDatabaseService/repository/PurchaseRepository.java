package rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository;

import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.Purchase;

import javax.swing.text.StyledEditorKit;
import java.util.List;

@Repository
public interface PurchaseRepository {
    // Define custom methods if needed
    Boolean save(Purchase purchase);

    List<Purchase> findAllByCustomerId(String customerId);

    List<Purchase> findAllByProductId(String productId);
}
