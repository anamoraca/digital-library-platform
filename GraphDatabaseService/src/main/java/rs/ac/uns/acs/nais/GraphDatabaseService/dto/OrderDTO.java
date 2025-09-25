package rs.ac.uns.acs.nais.GraphDatabaseService.dto;
import java.util.List;


public class OrderDTO {

    private Long customerId;
    private List<OrderItemDTO> items;

    public OrderDTO() {
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

}
