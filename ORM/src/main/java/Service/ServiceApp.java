package Service;

import Domain.Customer;
import Domain.Order;
import Repo.IRepo;
import Util.JPAUtilPoolConnection;
import Util.Props;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class ServiceApp {
    private final Logger logger = LogManager.getLogger();
    private final IRepo repo;

    public ServiceApp(IRepo repo) {
        this.repo = repo;
    }

    public List<Customer> GetCustomers(){
        logger.traceEntry();
        try {
            return repo.GetCustomers();
        }
        catch (Exception e){
            logger.error("Error getting customers: {}", e.getMessage());
        }
        return null;
    }

    public List<Order> GetOrders(){
        logger.traceEntry();
        try{
            return repo.GetOrders();
        }
        catch (Exception e){
            logger.error("Error getting orders: {}", e.getMessage());
        }
        return null;
    }

    public void addOrder(Long selected_customer_id, String date){
        logger.traceEntry();
        try{
            Date purchase_date = Date.valueOf(date);
            repo.addOrder(selected_customer_id, purchase_date);}
        catch (Exception e){
            logger.error("Error adding order: {}", e.getMessage());
        }

    }

    public void editOrder(Long order_id, String new_date){
        logger.traceEntry();
        try{
            Date new_purchase_date = Date.valueOf(new_date);
            repo.editOrder(order_id, new_purchase_date);
        }
        catch (Exception e){
            logger.error("Error editing order: {}", e.getMessage());
        }
    }

    public void deleteOrder(Long order_id){
        logger.traceEntry();
        try{
            repo.deleteOrder(order_id);
        }
        catch (Exception e){
            logger.error("Error deleting order: {}", e.getMessage());
        }
    }


    public void ConnectionOverheadBenchmark(){
        System.out.println(" --- Creating 100 connections without pooling ---");
        Properties prop = Props.getProperties();
        String url = prop.getProperty("db.url");
        String username = prop.getProperty("db.username");
        String password = prop.getProperty("db.password");
        Long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            try (Connection conn = DriverManager.getConnection(url, username, password)) {}
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        Long end = System.nanoTime();
        System.out.println("Time taken: " + (end - start) / 1_000_000.0 + " milliseconds");
        System.out.println("Time taken per connection: " + ((end - start) / 1_000_000.0) / 100.0 + " milliseconds");



        System.out.println("\n --- Creating 100 connections with pooling ---");
        start = System.nanoTime();
        HikariDataSource dataSource = JPAUtilPoolConnection.getDataSource();
        for (int i = 0; i < 100; i++) {
            try(Connection conn = dataSource.getConnection()) {}
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        end = System.nanoTime();
        System.out.println("Time taken: " + (end - start) / 1_000_000.0 + " milliseconds");
        System.out.println("Time taken per connection: " + ((end - start) / 1_000_000.0) / 100.0 + " milliseconds");
    }



    public void ConnectionLeak(){
        System.out.println("--- Connection leak ---\n");
        HikariDataSource dataSource = JPAUtilPoolConnection.getDataSource();
        for(int i = 1; i <= 15; i++){
            try {
                System.out.println("Granted connection number: " + i);
                Connection conn = dataSource.getConnection();
            }
            catch (Exception e){
                System.out.println("Error granting connection: " + e.getMessage());
            }
        }
    }

}
