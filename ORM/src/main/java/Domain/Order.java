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

    @ManyToOne(fetch=FetchType.LAZY)        // can be a performance problem here because in the controller
                                            // I initialize the Customer ID column by setting the Customer
                                            // field to fetch type EAGER, when it would be asier to just read
                                            // the entire row from the database.

                                            // I can just call customer.getCustomer_id() to get the customer
                                            // id without the database executing any supplementary queues
                                            // because the proxy used by the ORM actually saves the customer_id
                                            // column in memory.
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
    public Customer getCustomer() {
        return customer;
    }
    public Long getCustomer_id(){
        return customer.getCustomer_id();
    }
    public List<OrderProduct> getProducts() {
        return order_products;
    }


    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public void setCustomer_id(Long customer_id) {
        this.customer.setCustomer_id(customer_id);
    }
    public void setPurchase_date(Date purchase_date) {
        this.purchase_date = purchase_date;
    }
    public void setProducts(List<OrderProduct> products) {
        this.order_products = products;
    }

}
