package test;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.ArrayList;
import java.util.concurrent.*;
class TextSender implements Runnable {
    private ArrayList<String> list;
    private PipedWriter writer;
    public TextSender(PipedWriter writer) {
        list = new ArrayList<String>() {
            {
                add("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
                add("Praesent tempus, lectus quis laoreet fermentum, nibh nisl placerat ante, id luctus ligula tellus sed felis.");
                add("Sed nisi lacus, maximus ut mauris eget, pharetra pellentesque nisi.");
            }
        };
        this.writer = writer;
    }
    @Override
    public void run() {
        try {
            writer.write(list.size());
            writer.flush();
            for (String s : list) {
                writer.write(s);
                //signal reader about the end of line
                writer.write('|');
                writer.flush();
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class TextReceiver implements Callable<ArrayList<String>> {
    private ArrayList<String> list;
    private PipedReader reader;
    public TextReceiver(PipedReader reader) {
        list = new ArrayList<>();
        this.reader = reader;
    }
    @Override
    public ArrayList<String> call() {
        try {
            int count = reader.read();
            for (int i = 0; i < count; i++) {
                int a;
                StringBuilder builder = new StringBuilder();
                while ((a = reader.read()) != -1) {
                    //if is signalled about the end of line
                    if (a == '|') {
                        list.add(builder.toString());
                        builder.delete(0, builder.length());
                    }
                    else {
                        builder.append((char) a);
                    }
                }
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
public class Test {
    public static void main(String[] args) {
        try {
            PipedReader reader = new PipedReader();
            PipedWriter writer = new PipedWriter(reader);
            TextSender sender = new TextSender(writer);
            TextReceiver receiver = new TextReceiver(reader);
            ExecutorService service = Executors.newFixedThreadPool(2);
            service.execute(sender);
            Future<ArrayList<String>> future = service.submit(receiver);
            service.shutdown();
            ArrayList<String> strings = future.get();
            strings.forEach(System.out::println);
        }
        catch (IOException|InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
    }
}