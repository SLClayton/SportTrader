package tools;

import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyLogHandler extends Handler {


    public MyLogHandler(){
        super();
    }

    @Override
    public void publish(LogRecord record) {
        String threadname = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> t.getId() == record.getThreadID())
                .findFirst()
                .map(Thread::getName)
                .orElseGet(() -> "TID-" + record.getThreadID());



        StringBuilder sb = new StringBuilder();
        String timestring = Instant.now().toString()
                .replace("T", " ")
                .substring(0, 24);
        sb.append(timestring)
                .append(String.format(" [%s] ", record.getLevel().toString()))
                .append(String.format("[%s] ", threadname))
                .append(record.getSourceMethodName())
                .append(": ")
                .append(record.getMessage());

        if (record.getLevel().equals(Level.SEVERE)){
            System.err.println("\n" + sb.toString() + "\n");
        }
        else if (record.getLevel().equals(Level.WARNING)){
            System.out.println(sb.toString());
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