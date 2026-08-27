module cs3227.prototype {
    requires javafx.controls;
    requires javafx.fxml;

    opens cs3227.prototype to javafx.fxml;
    exports cs3227.prototype;
}
