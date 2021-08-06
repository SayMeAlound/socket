package nio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @Classname ChatServer
 * @Description TODO
 * @Date 2021/08/06 02:50
 * @Created by zhaomo
 */
public class ChatServer {
    private static final  int DEFAULT_PORT = 8888;
    private static final String  QUIT = "quit";
    private static final int BUFFER = 1024;

    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.port = port;
    }

    private void start(){
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器, 监听端口:" + port +" .....");

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    // 处理被触发的事件
                    handles(key);
                }
                selectionKeys.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        // ACCEPT 事件  - 和客户端建立了连接
        if (key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel)key.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector,SelectionKey.OP_READ);
            System.out.println(getClientName(client) + "已连接");
        }
        // READ事件 - 客户端发送了消息
        else if (key.isReadable()){
            SocketChannel client = (SocketChannel)key.channel();
            String fwdMsg = recevie(client);
            if (fwdMsg.isEmpty()){
                // 客户端异常
                key.cancel();;
                selector.wakeup();
            }else {
                forwardMessage(client,fwdMsg);
                //检查用户是否退出
                if (readyToQuit(fwdMsg)){
                    key.cancel();
                    selector.wakeup();
                    System.out.println(getClientName(client)+ "]已断开连接");
                }
            }
        }


    }

    /**
     * 转发消息
     * @param clientChannel 客户端通道
     * @param fwdMsg 转发的消息
     */
    private void forwardMessage(SocketChannel clientChannel, String fwdMsg) throws IOException {
        //keys方法区别于selectedKeys,这个方法返回的是接下来需要被处理的通道key
        //而keys则返回与selector绑定的所有通道key
        //跳过ServerSocketChannel和本身
        for (SelectionKey selectionKey : selector.keys()) {
            SelectableChannel channel = selectionKey.channel();
            if(channel instanceof ServerSocketChannel)
                System.out.println("客户端" + clientChannel.socket().getPort() + "：" + fwdMsg);
            else if(selectionKey.isValid() && !channel.equals(clientChannel)){
                wBuffer.clear();
                //写入消息
                wBuffer.put(charset.encode("客户端" + clientChannel.socket().getPort() + "：" + fwdMsg));
                //转换为读模式
                wBuffer.flip();
                //有数据就一直读
                while(wBuffer.hasRemaining())
                    ((SocketChannel)channel).write(wBuffer);
            }
        }
    }
    private String recevie(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) >0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    private String getClientName(SocketChannel client) {
        return "客户端["+ client.socket().getPort() + "]";
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

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer(9999);
        chatServer.start();
    }
}
