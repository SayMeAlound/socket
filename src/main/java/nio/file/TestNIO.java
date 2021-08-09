package nio.file;

import org.junit.Test;

import java.io.FileOutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Classname TestNIO
 * @Description TODO
 * @Date 2021/08/09 17:20
 * @Created by zhaomo
 */
public class TestNIO {

    @Test     // 往本地文件中写数据
    public void test1() throws Exception{
        //1.    创建输出流
        FileOutputStream fos = new FileOutputStream("basic.txt");
        //2.    从流中得到一个通道
        FileChannel fc = fos.getChannel();
        //3.    提供一个缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //4.    往缓冲区存入数据
        String str = "hello nio";
        ByteBuffer put = buffer.put(str.getBytes());

        //5.    翻转缓冲区
        buffer.flip();
        //5. 把缓冲区写到通道
        fc.write(buffer);

        //6.    关闭
        fos.close();


    }
}
