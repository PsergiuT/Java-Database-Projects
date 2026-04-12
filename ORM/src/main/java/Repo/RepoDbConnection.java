package Repo;

import Domain.Customer;
import Domain.Order;
import Util.JPAUtilPoolConnection;
import Util.JPAUtilSingleConnection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepoDbConnection implements IRepo{
    private EntityManagerFactory emf;
    private static final Logger logger = LogManager.getLogger();

    public RepoDbConnection(ConnectionType type){
        switch (type){
            case POOL:
                emf = JPAUtilPoolConnection.getEntityManagerFactory();
            case SINGLE:
                emf = JPAUtilSingleConnection.getEntityManagerFactory();
            default:
        }
    }

    @Override
    public List<Customer> GetCustomers() throws Exception {
        logger.traceEntry();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        logger.info("Transaction created");
        try{
            tx.begin();
            List<Customer> customers = em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
            tx.commit();

            return customers;
        }catch(Exception e){
            if(tx != null &&tx.isActive()){
                tx.rollback();
            }
            throw new Exception(e.getMessage());
        }finally{
            em.close();
        }
    }


    @Override
    public List<Order> GetOrders() throws Exception{
        logger.traceEntry();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            List<Order> orders = em.createQuery("SELECT o FROM Order o", Order.class).getResultList();
            tx.commit();

            return orders;
        }
        catch (Exception e){
            if(tx != null && tx.isActive()){
                tx.rollback();
            }
            throw new Exception(e.getMessage());
        }
        finally{
            em.close();
        }
    }


    public List<String> GetCustomerOrderDates() throws Exception {
        logger.traceEntry();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            List<Customer> customers = em.createQuery("SELECT c FROM Customer c LEFT JOIN FETCH c.orders", Customer.class).getResultList();
            List<String> customerOrderDates = new ArrayList<>();
            for(Customer c: customers){
                StringBuilder s = new StringBuilder(c.getName() + " order dates: ");
                for(Order o: c.getOrders()){
                    s.append(o.getPurchase_date()).append(", ");
                }
                customerOrderDates.add(s.toString());
            }
            tx.commit();

            return customerOrderDates;
        } catch (Exception e) {
            if(tx != null && tx.isActive()){
                tx.rollback();
            }
            throw new Exception(e.getMessage());
        }
        finally{
            em.close();
        }
    }


    @Override
    public void addOrder(Long selected_customer_id, Date purchase_date) throws Exception{
        logger.traceEntry();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            Customer proxyCustomer = em.getReference(Customer.class, selected_customer_id);  // create a fake customer so it can be passed into orders
            Order ord = new Order();
            ord.setCustomer(proxyCustomer);
            ord.setPurchase_date(purchase_date);
            em.persist(ord);
            tx.commit();
        }
        catch (Exception e){
            throw new Exception(e.getMessage());
        }
        finally{
            em.close();
        }
    }


    @Override
    public void editOrder(Long order_id, Date new_purchase_date) throws Exception{
        logger.traceEntry();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            Order ord = em.find(Order.class, order_id);
            ord.setPurchase_date(new_purchase_date);
            tx.commit();
        }
        catch (Exception e){
            throw new Exception(e.getMessage());
        }
        finally{
            em.close();
        }
    }

    @Override
    public void deleteOrder(Long order_id) throws Exception{
        logger.traceEntry();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            Order ord = em.find(Order.class, order_id);
            em.remove(ord);
            tx.commit();
        }
        catch (Exception e){
            throw new Exception(e.getMessage());
        }
        finally{
            em.close();
        }
    }
}
