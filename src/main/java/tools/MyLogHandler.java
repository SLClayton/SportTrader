package tools;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MyLogHandler extends Handler {

    String pwd = FileSystems.getDefault().getPath(".").toString();

    File info_log_file;
    FileWriter info_fr;

    File warning_log_file;
    FileWriter warning_fr;

    File severe_log_file;
    FileWriter severe_fr;



    public MyLogHandler() throws IOException {
        super();

        // Create log folder if it doesn't exist
        File log_dir = new File(pwd + "/logs");
        if (!log_dir.exists()){
            log_dir.mkdir();
        }

        info_log_file = new File(pwd + "/logs/info.log");
        warning_log_file = new File(pwd + "/logs/warning.log");
        severe_log_file = new File(pwd + "/logs/severe.log");

        // Delete previous log files if they exist
        if (info_log_file.exists()){
            info_log_file.delete();
        }
        if (warning_log_file.exists()){
            warning_log_file.delete();
        }
        if (severe_log_file.exists()){
            severe_log_file.delete();
        }

        // Create new files and filewriters
        info_log_file.createNewFile();
        warning_log_file.createNewFile();
        severe_log_file.createNewFile();

        info_fr = new FileWriter(info_log_file);
        warning_fr = new FileWriter(warning_log_file);
        severe_fr = new FileWriter(severe_log_file);
    }

    @Override
    public void publish(LogRecord record) {
        String threadname = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> t.getId() == record.getThreadID())
                .findFirst()
                .map(Thread::getName)
                .orElseGet(() -> "TID-" + record.getThreadID());



        StringBuilder sb = new StringBuilder();
        Instant time = Instant.ofEpochMilli(record.getMillis());
        String timestring = time.toString()
                .replace("T", " ");
        if (timestring.length() > 23){
            timestring = timestring.substring(0, 23);
        }
        sb.append(timestring)
                .append(String.format(" [%s] ", threadname))
                .append(String.format("[%s] ", record.getLevel().toString()))
                //.append(record.getSourceMethodName())
                .append("")
                .append(record.getMessage());

        String log_msg = sb.toString();


        if (record.getLevel().equals(Level.SEVERE)){
            System.err.println("\n" + log_msg + "\n");
            try {
                info_fr.write("\n" + log_msg + "\n");
                info_fr.flush();
                severe_fr.write(log_msg + "\n");
                severe_fr.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println(log_msg);
            try {
                info_fr.write(log_msg + "\n");
                info_fr.flush();
            }
            catch (IOException e){
                e.printStackTrace();
            }
            if (record.getLevel().equals(Level.WARNING)){
                try {
                    warning_fr.write(log_msg + "\n");
                    warning_fr.flush();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}