package nio.chat;

import java.util.Scanner;

/**
 * @Classname Test
 * @Description TODO
 * @Date 2021/08/10 00:04
 * @Created by zhaomo
 */
//启动聊天程序客户端
public class TestChat {
    public static void main(String[] args) throws Exception {
        ChatClient chatClient=new ChatClient();

        new Thread(){
            public void run(){
                while(true){
                    try {
                        chatClient.receiveMsg();
                        Thread.sleep(2000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        Scanner scanner=new Scanner(System.in);
        while (scanner.hasNextLine()){
            String msg=scanner.nextLine();
            chatClient.sendMsg(msg);
        }

    }
}

