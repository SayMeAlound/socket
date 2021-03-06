package nio.socket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Classname NIOClient
 * @Description TODO
 * @Date 2021/08/09 20:12
 * @Created by zhaomo
 */
public class NIOClient {
    public static void main(String[] args) throws Exception{

        //1. 得到一个网络通道
        SocketChannel channel = SocketChannel.open();

        //2. 设置非阻塞方式
        channel.configureBlocking(false);

        //3. 提供服务器端的IP地址和端口号
        InetSocketAddress address = new InetSocketAddress("127.0.0.1",9999);

        //4. 连接服务器
        if (!channel.connect(address)){
            while (!channel.finishConnect()) {  //nio作为非阻塞的优势
                System.out.println("Client: 连接服务器端的同时,我还可以做一些其他的事情");
            }
        }

        //5. 得到一个缓冲区并存入数据
        String msg = " hello Server";
        ByteBuffer wirteBuf = ByteBuffer.wrap(msg.getBytes());


        //6. 发送数据
        channel.write(wirteBuf);

        System.in.read();

    }

}




