package server;


import javax.xml.bind.annotation.XmlType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Classname ChatServer
 * @Description TODO
 * @Date 2021/08/05 02:39
 * @Created by zhaomo
 */
public class ChatServer {

    private int DEFAULT_PORT = 8888;
    private final String  QUIT = "quit";


    private ExecutorService executorService;

    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedClients;

    public ChatServer(){
        executorService = Executors.newFixedThreadPool(10);
        connectedClients = new HashMap<>();
    }

    public synchronized void  addClient(Socket socket) throws IOException{
        if (socket != null){
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );
            connectedClients.put(port,writer);
            System.out.println("客户端[" +port + "] 已连接到服务器");
        }
    }

    public synchronized void removeClient(Socket socket) throws IOException{
        if (socket != null){
            int port = socket.getPort();
            if (connectedClients.containsKey(port)){
                connectedClients.get(port).close();
            }
            connectedClients.remove(port);
            System.out.println("客户端[" +port + "] 已断开连接");
        }
    }

    public synchronized void forwardMessage(Socket socket,String fwdmsg) throws IOException{
        for(Integer id :connectedClients.keySet()){
            if (!id.equals(socket.getPort())){
                Writer writer = connectedClients.get(id);
                writer.write(fwdmsg);
                writer.flush();
            }
        }
    }

    public synchronized void close(){
        if (serverSocket != null){
            try {
                serverSocket.close();
                System.out.println("关闭serverSocket");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        // 绑定监听端口
        try {
            serverSocket  = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器,监听端口: "+DEFAULT_PORT +" ......");
            while (true){
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                // 创建ChatHandler线程
                //new Thread(new ChatHandler(this,socket)).start();
                executorService.execute(new ChatHandler(this,socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public Boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
