package Domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customer_id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "email", length = 50)
    private String email;

    @OneToMany(mappedBy = "customer",cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

//    @OneToMany(mappedBy = "customer",cascade=CascadeType.ALL, fetch = FetchType.EAGER)            // <-- for eager evaluation situation
//    private List<Order> orders;

    public Customer(Long customer_id, String name, String email) {
        this.customer_id = customer_id;
        this.name = name;
        this.email = email;
    }

    public Customer() {
    }

    public Long getCustomer_id() {
        return customer_id;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setCustomer_id(Long customer_id) {
        this.customer_id = customer_id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
