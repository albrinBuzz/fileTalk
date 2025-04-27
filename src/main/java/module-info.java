module org.filetalk.filetalk {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.management;
    requires org.slf4j;

    requires java.logging;
    requires org.apache.logging.log4j;
    requires java.net.http;
    requires java.desktop;
    exports org.filetalk.filetalk.utils;
    exports org.filetalk.filetalk.test;
    opens org.filetalk.filetalk to javafx.fxml;
    exports org.filetalk.filetalk;
    exports org.filetalk.filetalk.Client;
    exports org.filetalk.filetalk.server;
    exports org.filetalk.filetalk.shared;
}