package Controller;

import Domain.Customer;
import Domain.Employee;
import Domain.Order;
import Service.ServiceApp;
import Util.Page;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;
import java.util.List;

public class AppController {
    private ServiceApp service;

    // ---- Limit/Offset ----- //

    private int pageSize = 20;      //default
    private int pageNumber = 0;     //default

    @FXML
    private TextField pageNumberTextField;
    @FXML
    private TextField pageSizeTextField;

    @FXML
    private Label pageNumberLabel;
    @FXML
    private Label pageSizeLabel;
    @FXML
    private Label totalNumberOfElementsLabel;
    @FXML
    private Label totalNumberOfPagesLabel;
    @FXML
    private Label limitOffsetErrorLabel;

    @FXML
    private TableView<Employee> LimitOffsetTableView;
    @FXML
    private TableColumn<Employee, Long> tableColumnEmpLOId;
    @FXML
    private TableColumn<Employee, String> tableColumnEmpEmailLO;
    @FXML
    private TableColumn<Employee, Integer> tableColumnEmpDeptIdLO;
    @FXML
    private TableColumn<Employee, Long> tableColumnEmpSalaryLO;

    private final ObservableList<Employee> limitOffsetModel = FXCollections.observableArrayList();

    // ---- keyset ----- //

    private int pageSizeKS = 50;      //default
    private Long lastIndexKS = 0L;     //default
    private Long nextIndexKS = 0L;

    @FXML
    private TextField indexTextFieldKS;
    @FXML
    private TextField pageSizeTextFieldKS;

    @FXML
    private Label pageLastIndexKS;
    @FXML
    private Label pageSizeLabelKS;
    @FXML
    private Label totalNumberOfElementsLabelKS;
    @FXML
    private Label totalNumberOfPagesLabelKS;
    @FXML
    private Label limitOffsetErrorLabelKS;

    @FXML
    private TableView<Employee> KeysetTableView;
    @FXML
    private TableColumn<Employee, Long> tableColumnEmpKSId;
    @FXML
    private TableColumn<Employee, String> tableColumnEmpEmailKS;
    @FXML
    private TableColumn<Employee, Integer> tableColumnEmpDeptIdKS;
    @FXML
    private TableColumn<Employee, Long> tableColumnEmpSalaryKS;

    private final ObservableList<Employee> KeysetModel = FXCollections.observableArrayList();



    
    @FXML
    private void initialize(){
        tableColumnEmpLOId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnEmpEmailLO.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnEmpDeptIdLO.setCellValueFactory(new PropertyValueFactory<>("departmentId"));
        tableColumnEmpSalaryLO.setCellValueFactory(new PropertyValueFactory<>("salary"));
        LimitOffsetTableView.setItems(limitOffsetModel);


        tableColumnEmpKSId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnEmpEmailKS.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableColumnEmpDeptIdKS.setCellValueFactory(new PropertyValueFactory<>("departmentId"));
        tableColumnEmpSalaryKS.setCellValueFactory(new PropertyValueFactory<>("salary"));
        KeysetTableView.setItems(KeysetModel);

    }
    
    public void setService(ServiceApp service){
        this.service = service;
        initLimitOffsetModel();
        initKeysetModel(true);
    }

    @FXML
    public void handleNplus1Lazy(ActionEvent actionEvent) {
        try{
            service.Nplus1Lazy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleNplus1Eager(ActionEvent actionEvent) {
        try{
            service.Nplus1Eager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleBenchmarkIndex(ActionEvent actionEvent){
        try{
            service.BenchmarkIndex();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void handleReusingStatement(ActionEvent actionEvent){
        service.reusingStatement();
    }

    @FXML
    public void handleMassOperationOptimization(ActionEvent actionEvent){
        service.massOperationOptimization();
    }


    @FXML
    public void handleBenchmarkWithoutIndex(ActionEvent actionEvent){
        //TODO: implement Benchmark Without Index
    }

    @FXML
    public void handleBenchmarkCache(ActionEvent actionEvent){
        service.benchmarckCache();
    }


    private void initLimitOffsetModel(){
        limitOffsetErrorLabel.setText("");

        Page<Employee> employees = service.getEmployeesLimitOffset(pageNumber, pageSize);
        Integer count = service.getEmployeeCount();

        pageNumberLabel.setText(String.valueOf(pageNumber + 1));
        pageSizeLabel.setText(String.valueOf(pageSize));
        totalNumberOfElementsLabel.setText(count + "");
        totalNumberOfPagesLabel.setText((count / pageSize + (count % pageSize == 0 ? 0 : 1)) + "" );

        limitOffsetModel.setAll(employees.getElementsOnPage());
    }


    @FXML
    public void handleNextPage(ActionEvent actionEvent){
        pageNumber++;
        if(pageNumber * pageSize >= service.getEmployeeCount()){
            limitOffsetErrorLabel.setText("Page number is out of range");
            pageNumber--;
            return;
        }
        initLimitOffsetModel();
    }

    @FXML
    public void handlePreviousPage(ActionEvent actionEvent){
        pageNumber--;
        if(pageNumber < 0){
            limitOffsetErrorLabel.setText("Page number is out of range");
            pageNumber++;
            return;
        }
        initLimitOffsetModel();
    }


    @FXML
    public void handlePageSizeChange(ActionEvent actionEvent){
        try {
            int newPageSize = Integer.parseInt(pageSizeTextField.getText());
            if(newPageSize < 1){
                limitOffsetErrorLabel.setText("Page size must be greater than 0");
                return;
            }
            if(newPageSize > 100){
                limitOffsetErrorLabel.setText("Page size must be less than 100");
                return;
            }
            pageSize = newPageSize;
            initLimitOffsetModel();
        } catch (NumberFormatException e) {
            limitOffsetErrorLabel.setText("Invalid page size");
        }
    }


    @FXML
    public void handlePageNumberChange(ActionEvent actionEvent){
        try {
            int newPageNumber = Integer.parseInt(pageNumberTextField.getText()) - 1;
            if(newPageNumber < 0){
                limitOffsetErrorLabel.setText("Page number must be greater than 0");
                return;
            }
            if(newPageNumber > (service.getEmployeeCount() / pageSize + (service.getEmployeeCount() % pageSize == 0 ? 0 : 1))){
                limitOffsetErrorLabel.setText("Page number must be less than " + (service.getEmployeeCount() / pageSize + (service.getEmployeeCount() % pageSize == 0 ? 0 : 1)));
                return;
            }
            pageNumber = newPageNumber;
            initLimitOffsetModel();
        } catch (NumberFormatException e) {
            limitOffsetErrorLabel.setText("Invalid page number");
        }
    }


    private void initKeysetModel(boolean isNext){
        limitOffsetErrorLabelKS.setText("");

        Page<Employee> employees;
        if(isNext){
            lastIndexKS = nextIndexKS;
            employees = service.getEmployeesKeySetNext(lastIndexKS, pageSizeKS);
        }
        else{
            employees = service.getEmployeesKeySetPrevious(lastIndexKS, pageSizeKS);
        }
        Integer count = service.getEmployeeCount();

        lastIndexKS = employees.getElementsOnPage().get(0).getId();
        nextIndexKS = employees.getElementsOnPage().get(employees.getElementsOnPage().size() - 1).getId();

        pageLastIndexKS.setText(String.valueOf(lastIndexKS + 1));
        pageSizeLabelKS.setText(String.valueOf(pageSizeKS));
        totalNumberOfElementsLabelKS.setText(count + "");
        totalNumberOfPagesLabelKS.setText((count / pageSizeKS + (count % pageSizeKS == 0 ? 0 : 1)) + "" );

        KeysetModel.setAll(employees.getElementsOnPage());
    }


    @FXML
    public void handleNextPageKS(ActionEvent actionEvent){
        if(lastIndexKS >= service.getEmployeeCount()){
            limitOffsetErrorLabelKS.setText("Index out of range");
            return;
        }
        initKeysetModel(true);
    }

    @FXML
    public void handlePreviousPageKS(ActionEvent actionEvent){
        if(lastIndexKS - pageSizeKS <= 0){
            limitOffsetErrorLabelKS.setText("Index out of range");
            return;
        }
        initKeysetModel(false);
    }

}
