package rs.ac.uns.acs.nais.GraphDatabaseService.dto;

public class OrderItemDTO {

    private String productName;
    private String productId;

    public OrderItemDTO() {
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
