package Domain;

public class Employee {
    private Long id;
    private String name;
    private Integer salary;
    private Long id_departament;

    public Employee(Long id, String name, Integer salary, Long id_departament) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.id_departament = id_departament;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getSalary() {
        return salary;
    }

    public Long getId_departament() {
        return id_departament;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public void setId_departament(Long id_departament) {
        this.id_departament = id_departament;
    }
}
