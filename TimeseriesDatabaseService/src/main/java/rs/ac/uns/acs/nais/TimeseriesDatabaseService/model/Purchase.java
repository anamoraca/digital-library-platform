package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

import java.time.Instant;

@Measurement(name = "purchases")
public class Purchase {

    @Column(tag = true)
    private String product_id;

    @Column(tag = true)
    private String customer_id;

    @Column
    private Double _value;

    @Column
    private String _field;

    @Column(timestamp = true)
    private Instant created;

    public Purchase() {
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public Double get_value() {
        return _value;
    }

    public void set_value(Double _value) {
        this._value = _value;
    }

    public String get_field() {
        return _field;
    }

    public void set_field(String _field) {
        this._field = _field;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}