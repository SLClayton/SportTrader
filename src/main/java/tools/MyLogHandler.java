package tools;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyLogHandler extends Handler {

    public MyLogHandler(){
        super();
    }

    @Override
    public void publish(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getInstant().toString().replace("T", " "))
                .append(" [")
                //.append(record.getSourceClassName())
                //.append(".")
                .append(record.getLevel().toString())
                .append("] ")
                .append(record.getSourceMethodName())
                .append(": ")
                .append(record.getMessage());

        if (record.getLevel().equals(Level.SEVERE)){
            System.err.println("\n" + sb.toString() + "\n");
        }
        else if (record.getLevel().equals(Level.WARNING)){
            System.err.println(sb.toString());
        }
        else {
            System.out.println(sb.toString());
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}