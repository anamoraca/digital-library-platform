package rs.ac.uns.acs.nais.TimeseriesDatabaseService.repository;

import com.influxdb.client.InfluxDBClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.configuration.InfluxDBConnectionClass;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.Purchase;

import java.util.List;


@Repository
public class PurchaseRepositoryImpl implements PurchaseRepository{

    @Autowired
    private final InfluxDBConnectionClass inConn;

    public PurchaseRepositoryImpl(InfluxDBConnectionClass influxDBConnectionClass) {
        this.inConn = influxDBConnectionClass;
    }

    @Override
    public Boolean save(Purchase purchase) {

        InfluxDBClient influxDBClient = inConn.buildConnection();
        Boolean isSuccess = inConn.save(influxDBClient, purchase);
        influxDBClient.close();
        return isSuccess;
    }

    @Override
    public List<Purchase> findAllByCustomerId(String customerId) {
        InfluxDBClient influxDBClient = inConn.buildConnection();
        List<Purchase> purchases= inConn.findAllByCustomerId(influxDBClient, customerId);
        influxDBClient.close();
        return purchases;
    }

    @Override
    public List<Purchase> findAllByProductId(String productId) {
        InfluxDBClient influxDBClient = inConn.buildConnection();
        List<Purchase> purchases= inConn.findAllByProductId(influxDBClient, productId);
        influxDBClient.close();
        return purchases;
    }

    public List<Purchase> retrieveDataFromInfluxDB() {
        InfluxDBClient influxDBClient = inConn.buildConnection();
        List<Purchase> purchases= inConn.findAll(influxDBClient);
        influxDBClient.close();
        return purchases;
    }

}
