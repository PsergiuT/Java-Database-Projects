package Domain;

import Util.DateUtils;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
@Table(name="orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long order_id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="customer_id")
    private Customer customer;

    @Column(name="purchase_date")
    private Date purchase_date;

    @OneToMany(mappedBy = "order",cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderProduct> order_products;


    public Long getOrder_id() {
        return order_id;
    }
    public Date getPurchase_date() {
        return purchase_date;
    }
    public List<OrderProduct> getProducts() {
        return order_products;
    }


    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }
    public void setPurchase_date(Date purchase_date) {
        this.purchase_date = purchase_date;
    }
    public void setProducts(List<OrderProduct> products) {
        this.order_products = products;
    }

}
