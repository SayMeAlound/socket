package third;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Classname Client
 * @Description TODO
 * @Date 2021/08/06 12:46
 * @Created by zhaomo
 */
public class Client {

    final String LOCALHOST = "localhost";
    final int DEFAULT_PORT = 8888;
    AsynchronousSocketChannel clientChannel;

    public synchronized void close(Closeable closeable){
        if (closeable != null){
            try {
                closeable.close();
                System.out.println("关闭serverSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public  void start(){
        //  创建channel
        try {
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(new InetSocketAddress(LOCALHOST, DEFAULT_PORT));
            future.get();

            //  等待用户的输入
            BufferedReader consoleReader = new BufferedReader(
                    new InputStreamReader(System.in));
            while (true) {
                String  input = consoleReader.readLine();

                byte[] inputBytes = input.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(inputBytes);
                Future<Integer> writeResult = this.clientChannel.write(buffer);

                writeResult.get();
                buffer.flip();
                Future<Integer> readResult = clientChannel.read(buffer);

                readResult.get();
                String echo = new String(buffer.array());
                buffer.clear();

                System.out.println(echo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }finally {
            close(clientChannel);
        }
    }


    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
