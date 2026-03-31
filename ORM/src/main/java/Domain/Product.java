package Domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long product_id;

    @Column(name="price", precision = 12, scale =2)
    private BigDecimal price;

    @Column(name="description", length = 50)
    private String description;

    @OneToMany(mappedBy = "product",cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    public Product() {
    }

    public Long getProduct_id() {
        return product_id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setProduct_id(Long product_id) {
        this.product_id = product_id;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
