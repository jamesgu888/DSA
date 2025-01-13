module com.example.astar {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.astar to javafx.fxml;
    exports com.example.astar;
}