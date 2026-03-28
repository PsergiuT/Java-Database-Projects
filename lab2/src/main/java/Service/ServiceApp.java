package Service;

import Domain.Employee;
import Repo.RepoApp;
import Util.DbConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

public class ServiceApp {
    private static final Logger logger = LogManager.getLogger();
    private final RepoApp repo;

    public ServiceApp(RepoApp repo){
        this.repo = repo;
    }


    // ------------------------------TRANSACTIONS--------------------------------- //


    public void DirtyReads(Consumer<String> trs1, Consumer<String> trs2, Consumer<String> error){
        logger.traceEntry();
        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs1.accept("[A] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                repo.update(conn, 1L,10000);
                trs1.accept("[A] The salary has been updated to 10000 (uncommited)");

                Thread.sleep(3000);

                trs1.accept("[A] Transaction rolled back");
                conn.rollback();

                Employee emp = repo.select(conn);
                trs1.accept("[A] Salary value after rollback: " + emp.getSalary());

            } catch (SQLException | InterruptedException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs2.accept("[B] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                Thread.sleep(500);

                Employee emp = repo.select(conn);
                trs2.accept("[B] Read salary value: " + emp.getSalary());

                trs2.accept("[B] Transaction Commited");
                conn.commit();
            }
            catch (SQLException | InterruptedException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }





    public void NonRepeatableReads(Consumer<String> trs1, Consumer<String> trs2, Consumer<String> error){
        logger.traceEntry();
        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs1.accept("[A] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                Employee emp = repo.select(conn);
                trs1.accept("[A] Read 1 salary value: " + emp.getSalary());

                Thread.sleep(3000);

                emp = repo.select(conn);
                trs1.accept("[A] Read 2 salary value: " + emp.getSalary());

                conn.commit();
                trs1.accept("[A] Transaction Commited");
            }
            catch (SQLException | InterruptedException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();


        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs2.accept("[B] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                repo.update(conn, 1L,12000);
                trs2.accept("[B] Updated users salary to 12000");

                conn.commit();
                trs2.accept("[B] Transaction Commited");
            }
            catch (SQLException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }




    public void PhantomReads(Consumer<String> trs1, Consumer<String> trs2, Consumer<String> error){
        logger.traceEntry();
        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs1.accept("[A] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                Integer count = repo.Count(conn);
                trs1.accept("[A] Count 1: " + count);

                Thread.sleep(3000);

                count = repo.Count(conn);
                trs1.accept("[A] Count 2: " + count);

                conn.commit();
                trs1.accept("[A] Transaction Commited");
            }catch (SQLException | InterruptedException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs2.accept("[B] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                trs2.accept("[B] Inserting new employee");
                repo.insert(conn, new Employee(null, "New Emp", 10000, 1L));

                conn.commit();
                trs2.accept("[B] Transaction Commited");
            }catch (SQLException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }



    public void LostUpdates(Consumer<String> trs1, Consumer<String> trs2, Consumer<String> error){
        logger.traceEntry();
        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs1.accept("[A] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                Employee emp = repo.select(conn);
                trs1.accept("[A] Read salary value: " + emp.getSalary());

                Integer salary = emp.getSalary();
                salary += 1000;

                Thread.sleep(3000);

                repo.update(conn, 1L,salary);
                trs1.accept("[A] Updated salary value: " + emp.getSalary());

                conn.commit();
                trs1.accept("[A] Transaction Commited");
            }catch (SQLException | InterruptedException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs2.accept("[B] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                Employee emp = repo.select(conn);
                trs2.accept("[B] Read salary value: " + emp.getSalary());

                Integer salary = emp.getSalary();
                salary += 500;

                repo.update(conn, 1L,salary);
                trs2.accept("[B] Updated salary value: " + emp.getSalary());

                conn.commit();
                trs2.accept("[B] Transaction Commited");
            }catch (SQLException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }

    // ------------------------------DEADLOCK--------------------------------- //


    public void Deadlock(Consumer<String> trs1, Consumer<String> trs2, Consumer<String> error){
        logger.traceEntry();
        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs1.accept("[A] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                trs1.accept("[A] Updating salary value to employee 1: " + 6000);
                repo.update(conn, 1L,6000);

                Thread.sleep(2000);

                trs1.accept("[A] Trying to update salary value to employee 2: " + 7000);
                repo.update(conn,2L, 7000);

                conn.commit();
                trs1.accept("[A] Transaction Commited");
            }catch (SQLException | InterruptedException e) {
                // TODO: Handle error from deadlock
                logger.error("Error connecting to database", e);
                error.accept("Deadlock: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs2.accept("[B] Begin Transaction");
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                trs2.accept("[B] Updating salary value to employee 2: " + 6000);
                repo.update(conn, 2L,6000);

                Thread.sleep(2000);

                trs2.accept("[B] Trying to update salary value to employee 1: " + 7000);
                repo.update(conn,1L, 7000);

                conn.commit();
                trs2.accept("[B] Transaction Commited");
            }catch (SQLException | InterruptedException e) {
                // TODO: Handle error from deadlock
                logger.error("Error connecting to database", e);
                error.accept("Deadlock: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }



    public void FixedDeadlock(Consumer<String> trs1, Consumer<String> trs2, Consumer<String> error){
        logger.traceEntry();
        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs1.accept("[A] Begin Transaction");

                trs1.accept("[A] Updating salary value to employee 1: " + 6000);
                repo.update(conn, 1L,6000);

                Thread.sleep(2000);

                trs1.accept("[A] Trying to update salary value to employee 2: " + 7000);
                repo.update(conn,2L, 7000);

                conn.commit();
                trs1.accept("[A] Transaction Commited");
            }catch (SQLException | InterruptedException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try(Connection conn = DbConnection.getConnection()){
                trs2.accept("[B] Begin Transaction");

                trs2.accept("[B] Trying to update salary value to employee 1: " + 7000);
                repo.update(conn,1L, 7000);

                Thread.sleep(2000);

                trs2.accept("[B] Updating salary value to employee 2: " + 6000);
                repo.update(conn, 2L,6000);

                conn.commit();
                trs2.accept("[B] Transaction Commited");
            }catch (SQLException | InterruptedException e) {
                logger.error("Error connecting to database", e);
                error.accept("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).start();
    }



    // ------------------------------BATCHING--------------------------------- //




    private void clearTable(){
        logger.traceEntry();
        try(Connection conn = DbConnection.getConnection()){
            try(PreparedStatement pstmt = conn.prepareStatement("DELETE FROM employees")){
                pstmt.executeUpdate();
            }
        }
        catch (SQLException e) {
            logger.error("Error connecting to database", e);
        }
    }


    public Double StrategyAutoCommit() {
        logger.traceEntry();
        clearTable();
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "insert into batch_employees (name) values (?)";
            conn.setAutoCommit(true);

            Long start = System.nanoTime();
            try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                for (int i = 0; i < 5000; i++) {
                    pstmt.setString(1, "Employee" + i);
                    pstmt.executeUpdate();
                }
            }
            Long end = System.nanoTime();
            Double time = (end - start) / 1_000_000.0;

            logger.info("Time: {}", time);
            return time;
        }
        catch (SQLException e){
            logger.error("Error connecting to database", e);
        }
        return null;
    }



    public Double StrategyBatchCommit() {
        logger.traceEntry();
        clearTable();
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "insert into batch_employees (name) values (?)";

            Long start = System.nanoTime();
            try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                for (int i = 0; i < 5000; i++) {
                    pstmt.setString(1, "Employee" + i);
                    pstmt.executeUpdate();

                    if (i % 100 == 0){
                        conn.commit();
                    }
                }
                conn.commit();
            }
            Long end = System.nanoTime();
            Double time = (end - start) / 1_000_000.0;

            logger.info("Time: {}", time);
            return time;
        }
        catch (SQLException e){
            logger.error("Error connecting to database", e);
        }
        return null;
    }



    public Double StrategyAllCommit() {
        logger.traceEntry();
        clearTable();
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "insert into batch_employees (name) values (?)";

            Long start = System.nanoTime();
            try(PreparedStatement pstmt = conn.prepareStatement(sql)){
                for (int i = 0; i < 5000; i++) {
                    pstmt.setString(1, "Employee" + i);
                    pstmt.addBatch();

                    if(i % 50 == 0){
                        pstmt.executeBatch();
                    }
                }
                conn.commit();
            }
            Long end = System.nanoTime();
            Double time = (end - start) / 1_000_000.0;

            logger.info("Time: {}", time);
            return time;
        }
        catch (SQLException e){
            logger.error("Error connecting to database", e);
        }
        return null;
    }


}
