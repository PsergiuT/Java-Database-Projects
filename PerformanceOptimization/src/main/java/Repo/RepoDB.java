package Repo;

import Domain.Customer;
import Domain.Employee;
import Domain.Order;
import Util.JPAUtilPoolConnection;
import Util.Page;
import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;


import java.time.Duration;
import java.util.List;


public class RepoDB {
    private static EntityManagerFactory emf;
    private static final Logger logger = LogManager.getLogger();

    private final CacheManager cacheManager;
    private final Cache<Long, Employee> cache;

    public RepoDB(){
        emf = JPAUtilPoolConnection.getEntityManagerFactory();

        this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        this.cache = cacheManager.createCache("employees",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Employee.class,
                        ResourcePoolsBuilder.heap(100))          // max 100 employees in cache
                    .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(3))) // TTL 3 seconds
                    .build());
    }


    public Employee getEmployeeById(Long id){
        Employee employee = cache.get(id);

        if (employee == null) {
            EntityManager em = emf.createEntityManager();
            employee = em.find(Employee.class, id);
            if (employee != null) {
                cache.put(id, employee);
                System.out.println("[CACHE MISS] Employee " + id + " not found in cache, added to cache");
                return employee;
            }
        }
        System.out.println("[CACHE HIT] Employee " + id + " found in cache");
        return employee;
    }


    public void update(Employee emp){
        System.out.println("[UPDATE] Employee " + emp.getId() + ". Evicting cache...");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            emp.setSalary(emp.getSalary() - 1);
            em.merge(emp);
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        cache.remove(emp.getId());
    }

    public void close(){
        cacheManager.close();
    }


    public void benchmarckCache(){
        System.out.println("----- Benchmarking cache -----\n");
        System.out.println("First load (no cache)");
        solveIndexProblems(() -> {getEmployeeById(2478L);});
        System.out.println("\n");
        System.out.println("Second load (cached)");
        solveIndexProblems(() -> {getEmployeeById(2478L);});
        System.out.println("\n");

        System.out.println("Cache eviction:");
        Employee emp = getEmployeeById(2478L);
        update(emp);
        solveIndexProblems(() -> {getEmployeeById(2478L);});
        System.out.println("\n");

        System.out.println("Waiting for TTL to expire...");
        try {
            Thread.sleep(3200);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        solveIndexProblems(() -> {getEmployeeById(2478L);});

        close();
    }


    public void Nplus1Lazy(){
        logger.traceEntry();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try{
            tx.begin();
            List<Customer> customers = em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
            for (Customer customer : customers) {
                List<Order> orders = customer.getOrders();      // N queries
                System.out.println("Customer: " + customer.getCustomer_id() + " has " + orders.size() + " orders" );
            }
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void Nplus1Eager(){
        logger.traceEntry();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try{
            tx.begin();
            List<Customer> customers = em.createQuery("SELECT c FROM Customer c JOIN FETCH c.orders", Customer.class).getResultList();
            for (Customer customer : customers) {
                List<Order> orders = customer.getOrders();              // fetched in the above query
                System.out.println("Customer: " + customer.getCustomer_id() + " has " + orders.size() + " orders" );
            }
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }





    public static void create_500k_EmployeesUtil(){
        emf = JPAUtilPoolConnection.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        int batchSize = 50;
        try{
            tx.begin();
            for(int i = 10_001;i <= 500_000; i++){
                Employee empl = new Employee();
                empl.setDepartmentId((int)(Math.random() * 100 + 1));
                empl.setEmail("emp" + i + "@gmail.com");
                empl.setSalary((long)(100_000 - Math.random() * 80_000 + 1)); // 20.000 -> 100.000

                em.persist(empl);

                if(i % batchSize == 0){
                    em.flush();
                    em.clear();
                    logger.info("Processed batch: {}", i);
                }
            }
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private void solveIndexProblems(Runnable runnable){
        long start =  System.nanoTime();

        try {
            runnable.run();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        long end = System.nanoTime();
        double time_in_millis = (end - start) / 1_000_000.0;
        System.out.println("Time took: " + time_in_millis + " ms");
    }



    public void BenchmarkIndex(){
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        System.out.println("----- Benchmark using index -----\n");
        // ----------------------------------- //
        System.out.println("Search based on email");
        solveIndexProblems(() -> {
            tx.begin();
            em.clear();      // clears first level cache
            Long count = em.createQuery("select count(e) from Employee e where e.email like 'emp159735@gmail.com'", Long.class).getSingleResult();
            tx.commit();
        });


        // ----------------------------------- //
        System.out.println("Search based on department id");
        solveIndexProblems(() -> {
            tx.begin();
            em.clear();      // clears first level cache
            Long count = em.createQuery("select count(e) from Employee e where e.department_id = 5", Long.class).getSingleResult();
            tx.commit();
        });


        // ----------------------------------- //
        System.out.println("Search based on salary interval");
        solveIndexProblems(() -> {
            tx.begin();
            em.clear();      // clears first level cache
            Long count = em.createQuery("select count(e) from Employee e where e.salary between 50000 and 60000", Long.class).getSingleResult();
            tx.commit();
        });



        // ----------------------------------- //
        System.out.println("Search based on 2 columns (department_id and salary)");
        solveIndexProblems(() -> {
            tx.begin();
            em.clear();      // clears first level cache
            Long count = em.createQuery("select count(e) from Employee e where e.department_id = 5 and salary > 80000", Long.class).getSingleResult();
            tx.commit();
        });

    }


    public Integer getEmployeeCount(){
        EntityManager em = emf.createEntityManager();
        return em
                .createQuery("select count(e) from Employee e", Long.class)
                .getSingleResult()
                .intValue();
    }


    public Page<Employee> getEmployeesLimitOffset(int pageNumber, int pageSize){
        int offset = pageNumber * pageSize;
        EntityManager em = emf.createEntityManager();

        List<Employee> employees = em
                .createQuery("SELECT e FROM Employee e ORDER BY e.id ASC", Employee.class)
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();

        return new Page<>(employees, pageNumber, pageSize);
    }


    public Page<Employee> getEmployeesKeySetNext(Long lastId, int pageSize) {
        EntityManager em = emf.createEntityManager();
        List<Employee> employees = em
                .createQuery("select e from Employee e where e.id > :lastId order by e.id asc", Employee.class)
                .setParameter("lastId", lastId)
                .setMaxResults(pageSize)
                .getResultList();

        return new Page<>(employees, 0, pageSize);
    }

    public Page<Employee> getEmployeesKeySetPrevious(Long lastId, int pageSize) {
        EntityManager em = emf.createEntityManager();
        List<Employee> employees = em
                .createQuery("select e from Employee e where e.id < :lastId order by e.id desc", Employee.class)
                .setParameter("lastId", lastId)
                .setMaxResults(pageSize)
                .getResultList();

        return new Page<>(employees, 0, pageSize);
    }





    public void reusingStatement(){
        System.out.println(" ----- No statement reusing ----- ");
        EntityManager em = emf.createEntityManager();

        long start =  System.nanoTime();
        for(int i = 1; i <= 1000; i++){
            Query query = em.createQuery("select e from Employee e where e.id = :id");
            query.setParameter("id", (long) i);
            query.getSingleResult();
        }
        long end = System.nanoTime();
        double time_in_millis = (end - start) / 1_000_000.0;
        System.out.println("Time took: " + time_in_millis + " ms");

        System.out.println(" ----- Query statement reusing ----- ");

        start =  System.nanoTime();
        Query query = em.createQuery("select e from Employee e where e.id = :id");
        for(int i = 1; i <= 1000; i++){
            query.setParameter("id", (long) i);
            query.getSingleResult();
        }
        end = System.nanoTime();
        time_in_millis = (end - start) / 1_000_000.0;
        System.out.println("Time took: " + time_in_millis + " ms");
    }




    public void massOperationOptimization(){
        System.out.println(" ----- Individual updates ----- ");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        long start =  System.nanoTime();
        List<Employee> employees = em.createQuery("select e from Employee e where e.id <= 10000", Employee.class).getResultList();
        for(var emp: employees){
            emp.setSalary((long) (emp.getSalary() * 1.1));
            em.merge(emp);
        }
        long end = System.nanoTime();
        double time_in_millis = (end - start) / 1_000_000.0;
        System.out.println("Time took: " + time_in_millis + " ms");



        System.out.println(" ----- Mass updates ----- ");
        em.clear();
        try{
            tx.begin();
            start =  System.nanoTime();
            em.createQuery("update Employee e set e.salary = cast(e.salary * 1.1 as long) where e.id <= 10000").executeUpdate();
            end = System.nanoTime();
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        time_in_millis = (end - start) / 1_000_000.0;
        System.out.println("Time took: " + time_in_millis + " ms");



        System.out.println(" ----- Batch updates ----- ");
        em.clear();
        start =  System.nanoTime();
        employees = em.createQuery("select e from Employee e where e.id <= 10000", Employee.class).getResultList();
        int batchSize = 200;
        try{
            tx.begin();
            for(int i = 0 ; i < employees.size(); i++){
                Employee emp = employees.get(i);
                emp.setSalary((long) (emp.getSalary() * 0.9));

                if(i % batchSize == 0){
                    em.flush();
                    em.clear();
                }
            }
            end = System.nanoTime();
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        time_in_millis = (end - start) / 1_000_000.0;
        System.out.println("Time took: " + time_in_millis + " ms");

    }
}
