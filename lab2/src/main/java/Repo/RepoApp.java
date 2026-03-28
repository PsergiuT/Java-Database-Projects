package Repo;

import Domain.Employee;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepoApp {
    private static final Logger logger = LogManager.getLogger();

    public Employee select(Connection conn) {
        logger.traceEntry();
        try(PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM employees WHERE id = 1")){
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    Long id = rs.getLong("id");
                    String name = rs.getString("name");
                    Integer salary = rs.getInt("salary");
                    Long id_departament = rs.getLong("id_departament");

                    return new Employee(id, name, salary, id_departament);
                }
            }
        }
        catch(SQLException sql){
            logger.error(sql);
        }
        return null;
    }

    public void update(Connection conn, Long id, Integer salary)throws SQLException {
        logger.traceEntry();
        try(PreparedStatement pstmt = conn.prepareStatement("UPDATE employees SET salary = ? WHERE id = ?")){
            pstmt.setInt(1, salary);
            pstmt.setLong(2, id);
            pstmt.executeUpdate();
        }
        catch(SQLException sql){
            logger.error(sql);
            throw new SQLException(sql);
        }
    }


    public Integer Count(Connection conn){
        logger.traceEntry();
        try(PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM employees WHERE id_departament = 1")){
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return rs.getInt(1);
                }
            }
        }
        catch(SQLException sql){
            logger.error(sql);
        }
        return null;
    }


    public void insert(Connection conn, Employee employee){
        logger.traceEntry();
        try(PreparedStatement pstmt = conn.prepareStatement("INSERT INTO employees (name, salary, id_departament) VALUES (?, ?, ?)")){
            pstmt.setString(1, employee.getName());
            pstmt.setInt(2, employee.getSalary());
            pstmt.setLong(3, employee.getId_departament());
            pstmt.executeUpdate();
        }
        catch(SQLException sql){
            logger.error(sql);
        }
    }
}
