package Lab.mensager;


import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;

public class App {
    private static final Logger log4j = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) {
        try {
            String log4jConfPath = "src/main/resources/log4j2.xml";
            PropertyConfigurator.configure(log4jConfPath);
            log4j.info("this is a testmessage " + Instant.now().toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
