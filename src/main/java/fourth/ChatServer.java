package fourth;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Classname ChatServer
 * @Description TODO
 * @Date 2021/08/06 15:42
 * @Created by zhaomo
 */
public class ChatServer {
    private static final String LOCALHOST = "localhost";
    private static final  int DEFAULT_PORT = 8888;
    private static final String  QUIT = "quit";
    private static final int BUFFER = 1024;
    private static final int THREADPOOL_SIZE = 8;

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverChannel;
    private List<ClientHandler> connectedClients;
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.port = port;
        this.connectedClients =new ArrayList<>();
    }
    public Boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

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

    public void start(){
        ExecutorService executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE);
        try {
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
            serverChannel.bind(new InetSocketAddress(LOCALHOST,port));
            System.out.println("启动服务器,监听端口: " +port);

            while (true){
                serverChannel.accept(null,new AcceptHandler());
                System.in.read();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverChannel);
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();

    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel,Object> {
        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
            if (serverChannel.isOpen()){
                serverChannel.accept(null,this);
            }
            if (clientChannel !=null && clientChannel.isOpen()){
//                ClientHandler handler = new ClientHandler();
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER);

                //  将新用户添加到在线用户列表中去
//                addClient(handler);
//                clientChannel.read(buffer,buffer,handler);

            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接失败: "+exc);
        }
    }

    private class ClientHandler implements CompletionHandler<Integer,Object>{

        private AsynchronousSocketChannel clientChannel;

        public ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            ByteBuffer buffer = (ByteBuffer) attachment;
            if (buffer != null){
                if (result <= 0){
                    //  客户端异常
                    // TODO: 2021/08/06  将客户端移出在线客户端

                }else {
                   buffer.flip();
//                   String fwdMsg = receive(buffer);
//                   System.out.println(getClientName(clientChannel)+ ": "+ fwdMsg);
//                   forwardMessage(clientChannel,fwdMsg);
//                   buffer.clear();


                }
            }
        }

        private String getClientName(SocketChannel client) {
            return "客户端["+ client.socket().getPort() + "]";
        }
        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接失败: "+exc);
        }
    }
}
