package Service;

import Domain.Employee;
import Repo.RepoDB;
import Util.Page;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;

public class ServiceApp {
    private final RepoDB repo;

    public ServiceApp(RepoDB repo){
        this.repo = repo;
    }

    public void Nplus1Lazy(){
        repo.Nplus1Lazy();
    }

    public void Nplus1Eager(){
        repo.Nplus1Eager();
    }


    public void BenchmarkIndex(){
        repo.BenchmarkIndex();
    }

    public Integer getEmployeeCount(){
        return repo.getEmployeeCount();
    }

    public Page<Employee> getEmployeesLimitOffset(int pageNumber, int pageSize){
        return repo.getEmployeesLimitOffset(pageNumber, pageSize);
    }

    public Page<Employee> getEmployeesKeySetNext(Long lastId, int pageSize){
        return repo.getEmployeesKeySetNext(lastId, pageSize);
    }

    public Page<Employee> getEmployeesKeySetPrevious(Long lastId, int pageSize){
        Page<Employee> employees = repo.getEmployeesKeySetPrevious(lastId, pageSize);
        List<Employee> employeesList = employees.getElementsOnPage();
        Collections.reverse(employeesList);
        return new Page<>(employeesList, employees.getPageNumber(), employees.getPageSize());

    }

    public void reusingStatement(){
        repo.reusingStatement();
    }

    public void massOperationOptimization()
    {
        repo.massOperationOptimization();
    }
}
