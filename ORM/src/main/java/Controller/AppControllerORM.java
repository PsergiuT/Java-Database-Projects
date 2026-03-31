package Controller;

import Domain.Customer;
import Domain.Order;
import Service.ServiceApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;
import java.util.List;

public class AppControllerORM {

    public Button benchmarkButton;
    @FXML
    private TableView<Customer> clientTableView;
    @FXML
    private TableColumn<Customer, Long> tableColumnClientId;
    @FXML
    private TableColumn<Customer, String> tableColumnClientName;
    @FXML
    private TableColumn<Customer, String> tableColumnClientEmail;

    private final ObservableList<Customer> clientModel = FXCollections.observableArrayList();

    @FXML
    private TableView<Order> orderTableView;
    @FXML
    private TableColumn<Order, Long> tableColumnOrderId;
    @FXML
    private TableColumn<Order, Customer> tableColumnOrderClientId;
    @FXML
    private TableColumn<Order, Date> tableColumnOrderPurchaseDate;

    private final ObservableList<Order> orderModel = FXCollections.observableArrayList();

    @FXML private TextField clientFilterTextField;
    @FXML private ComboBox<String> clientSortComboBox;

    @FXML private TextField orderFilterTextField;
    @FXML private ComboBox<String> orderSortComboBox;

    @FXML private Button refreshButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TextField dateTextField;

    @FXML private Label errorLabel;

    private ServiceApp service;

    public void setService(ServiceApp service){
        this.service = service;
        initModel();
    }


    @FXML
    public void initialize() {
        tableColumnClientId.setCellValueFactory(new PropertyValueFactory<>("customer_id"));
        tableColumnClientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColumnClientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        clientTableView.setItems(clientModel);

        tableColumnOrderId.setCellValueFactory(new PropertyValueFactory<>("order_id"));
        tableColumnOrderClientId.setCellValueFactory(new PropertyValueFactory<>("customer_id"));
        tableColumnOrderPurchaseDate.setCellValueFactory(new PropertyValueFactory<>("purchase_date"));
        orderTableView.setItems(orderModel);
    }



    private void initModel(){
        try {
            List<Customer> customers = service.GetCustomers();
            clientModel.setAll(customers);

            List<Order> orders = service.GetOrders();
            orderModel.setAll(orders);
        }
        catch (Exception e){
            errorLabel.setText(e.getMessage());
        }
    }


    @FXML
    private void handleRefreshAction(ActionEvent event) {
        initModel();
    }


    @FXML
    private void handleAddAction(ActionEvent event) {
        Customer selectedCustomer = clientTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            try {
                service.addOrder(selectedCustomer.getCustomer_id(), dateTextField.getText());
            }
            catch (Exception e){
                errorLabel.setText(e.getMessage());
            }
        }
        else{
            errorLabel.setText("Selectati un client");
        }
    }


    @FXML
    private void handleEditAction(ActionEvent event) {
        Order selectedOrder = orderTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                service.editOrder(selectedOrder.getOrder_id(), dateTextField.getText());
            }
            catch (Exception e){
                errorLabel.setText(e.getMessage());
            }
        }
        else{
            errorLabel.setText("Selectati o comanda");
        }
    }


    @FXML
    private void handleDeleteAction(ActionEvent event) {
        Order selectedOrder = orderTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                service.deleteOrder(selectedOrder.getOrder_id());
            }
            catch (Exception e){
                errorLabel.setText(e.getMessage());
            }
        }
        else{
            errorLabel.setText("Selectati o comanda");
        }
    }

    @FXML
    private void handleBenchmarkAction(ActionEvent actionEvent) {
        service.ConnectionOverheadBenchmark();
    }

    @FXML
    private void handleLeakAction(ActionEvent actionEvent) {
        service.ConnectionLeak();
    }
}

