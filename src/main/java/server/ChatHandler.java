package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @Classname ChatHandle
 * @Description TODO
 * @Date 2021/08/05 02:39
 * @Created by zhaomo
 */
public class ChatHandler implements Runnable{
    private ChatServer server;
    private Socket socket;

    public ChatHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 存储新上线用户
            server.addClient(socket);

            // 读取用户发送的消息
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            String msg = null;
            while ((msg = reader.readLine()) != null){
                String fwdmsg = "客户端[" + socket.getPort() + "] :"+msg + "\n";
                System.out.println(fwdmsg);
                // 将收到的消息转发给聊天在线的所有用户
                server.forwardMessage(socket,fwdmsg);

                // 检查用户是否准备退出
               if (server.readyToQuit(msg)){
                   break;
               }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
