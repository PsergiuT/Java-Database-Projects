package Util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JPAUtilSingleConnection {
    private static EntityManagerFactory emf;

    public static EntityManagerFactory getEntityManagerFactory(){
        if (emf == null){
            Map<String, String> props = new HashMap<>();
            Properties properties = Props.getProperties();
            props.put("jakarta.persistence.jdbc.url", properties.getProperty("db.url"));
            props.put("jakarta.persistence.jdbc.user", properties.getProperty("db.username"));
            props.put("jakarta.persistence.jdbc.password", properties.getProperty("db.password"));
            props.put("jakarta.persistence.jdbc.driver", properties.getProperty("db.driver"));

            emf = Persistence.createEntityManagerFactory("orm", props);
        }
        return emf;
    }

    public static void closeEntityManagerFactory(){
        if (emf != null && emf.isOpen()){
            emf.close();
        }
    }
}

