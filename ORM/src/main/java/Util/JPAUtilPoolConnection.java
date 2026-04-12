package Util;

import jakarta.persistence.EntityManagerFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JPAUtilPoolConnection {
    private static EntityManagerFactory emf;
    private static HikariDataSource dataSource;

    public static EntityManagerFactory getEntityManagerFactory(){
        if(emf == null){
            Properties properties = Props.getProperties();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));
            config.setDriverClassName(properties.getProperty("db.driver"));
            config.setConnectionTimeout(1000);
            config.setMaxLifetime(10000);
            config.setMaximumPoolSize(10);

            dataSource = new HikariDataSource(config);

            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.url", properties.getProperty("db.url"));

            props.put("hibernate.connection.datasource", dataSource);

            emf = Persistence.createEntityManagerFactory("orm", props);

        }
        return emf;
    }

    public static HikariDataSource getDataSource() {
        if (dataSource == null) {
            getEntityManagerFactory();
        }
        return dataSource;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) emf.close();
        if (dataSource != null) dataSource.close();
    }
}
