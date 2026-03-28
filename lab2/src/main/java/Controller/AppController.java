package Controller;

import Domain.Employee;
import Service.ServiceApp;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppController {

    public Label errorLabel;
    @FXML
    private Button btnDirtyReads;
    @FXML
    private Button btnNonRepeatableRead;
    @FXML
    private Button btnPhantomRead;
    @FXML
    private Button btnLostUpdate;
    @FXML
    private Button btnDeadlock;
    @FXML
    private Button btnFixedDeadlock;
    @FXML
    private Button btnBatchProcessing;

    @FXML
    private TextArea taTransactionA;
    @FXML
    private TextArea taTransactionB;

    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    private ServiceApp serviceApp;

    public void setService(ServiceApp serviceApp){
        this.serviceApp = serviceApp;
    }

    @FXML
    public void initialize() {
        btnDirtyReads.setOnAction(event -> executorService.submit(this::handleDirtyReads));
        btnNonRepeatableRead.setOnAction(event -> executorService.submit(this::handleNonRepeatableRead));
        btnPhantomRead.setOnAction(event -> executorService.submit(this::handlePhantomRead));
        btnLostUpdate.setOnAction(event -> executorService.submit(this::handleLostUpdate));
        btnDeadlock.setOnAction(event -> executorService.submit(this::handleDeadlock));
        btnFixedDeadlock.setOnAction(event -> executorService.submit(this::handleFixedDeadlock));
        btnBatchProcessing.setOnAction(event -> executorService.submit(this::handleBatch));
    }


    private void handleDirtyReads(){
        Platform.runLater(() -> {
            taTransactionA.clear();
            taTransactionB.clear();
            errorLabel.setText("");
        });
        serviceApp.DirtyReads(message1 -> {
            Platform.runLater(() -> taTransactionA.appendText(message1 + "\n"));
        }, message2 -> {
            Platform.runLater(() -> taTransactionB.appendText(message2 + "\n"));
        }, error ->{
            Platform.runLater(() -> errorLabel.setText(error));
        });
    }

    private void handleNonRepeatableRead(){
        Platform.runLater(() -> {
            taTransactionA.clear();
            taTransactionB.clear();
            errorLabel.setText("");
        });
        serviceApp.NonRepeatableReads(message1 -> {
            Platform.runLater(() -> taTransactionA.appendText(message1 + "\n"));
        }, message2 -> {
            Platform.runLater(() -> taTransactionB.appendText(message2 + "\n"));
        }, error ->{
            Platform.runLater(() -> errorLabel.setText(error));
        });
    }

    private void handlePhantomRead(){
        Platform.runLater(() -> {
            taTransactionA.clear();
            taTransactionB.clear();
            errorLabel.setText("");
        });
        serviceApp.PhantomReads(message1 -> {
            Platform.runLater(() -> taTransactionA.appendText(message1 + "\n"));
        }, message2 -> {
            Platform.runLater(() -> taTransactionB.appendText(message2 + "\n"));
        }, error ->{
            Platform.runLater(() -> errorLabel.setText(error));
        });

    }

    private void handleLostUpdate(){
        Platform.runLater(() -> {
            taTransactionA.clear();
            taTransactionB.clear();
            errorLabel.setText("");
        });
        serviceApp.LostUpdates(message1 -> {
            Platform.runLater(() -> taTransactionA.appendText(message1 + "\n"));
        }, message2 -> {
            Platform.runLater(() -> taTransactionB.appendText(message2 + "\n"));
        }, error ->{
            Platform.runLater(() -> errorLabel.setText(error));
        });
    }
    
    
    
    private void handleDeadlock(){
        Platform.runLater(() -> {
            taTransactionA.clear();
            taTransactionB.clear();
            errorLabel.setText("");
        });
        serviceApp.Deadlock(message1 -> {
            Platform.runLater(() -> taTransactionA.appendText(message1 + "\n"));
        }, message2 -> {
            Platform.runLater(() -> taTransactionB.appendText(message2 + "\n"));
        }, error ->{
            Platform.runLater(() -> errorLabel.setText(error));
        });
    }
    
    private void handleFixedDeadlock(){
        Platform.runLater(() -> {
            taTransactionA.clear();
            taTransactionB.clear();
            errorLabel.setText("");
        });
        serviceApp.FixedDeadlock(message1 -> {
            Platform.runLater(() -> taTransactionA.appendText(message1 + "\n"));
        }, message2 -> {
            Platform.runLater(() -> taTransactionB.appendText(message2 + "\n"));
        }, error ->{
            Platform.runLater(() -> errorLabel.setText(error));
        });
    }



    private void handleBatch(){
        Platform.runLater(() -> {
            taTransactionA.clear();
            taTransactionB.clear();
            errorLabel.setText("");
        });
        Double timeAuto = serviceApp.StrategyAutoCommit();
        Double timeBatch = serviceApp.StrategyBatchCommit();
        Double timeAll = serviceApp.StrategyAllCommit();

        Platform.runLater(() -> {
            barChart.getData().clear();
            
            xAxis.setLabel("Strategy");
            yAxis.setLabel("Time");
            
            barChart.setTitle("Batch Processing");
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Processing Time");

            series.getData().add(new XYChart.Data<>("Auto Commit", timeAuto));
            series.getData().add(new XYChart.Data<>("Batch Commit", timeBatch));
            series.getData().add(new XYChart.Data<>("All Commit", timeAll));

            barChart.getData().add(series);
        });
        
    }
}