import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Classname FileCopyDemo
 * @Description TODO
 * @Date 2021/08/05 12:29
 * @Created by zhaomo
 */

interface FileCopyRunner {
    void copyFile(File source,File target);
}
public class FileCopyDemo {

    private static final int ROUNDS = 5 ;

    private static void benchmark(FileCopyRunner test,File source,File target){
        long elapsed = 0L;
        for (int i = 0; i < ROUNDS; i++) {
            long startTime = System.currentTimeMillis();
            test.copyFile(source,target);
            elapsed += System.currentTimeMillis() -startTime;
            target.delete();
        }
        System.out.println(test + ": " + elapsed / ROUNDS);
    }

    private static void close(Closeable closeable){
        if (closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        FileCopyRunner noBufferStreamCopy = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new FileInputStream(source);
                    fout = new FileOutputStream(target);

                    int result;
                    while ((result = fin.read()) != -1){
                        fout.write(result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }
            @Override
            public String toString(){
                return "noBufferStreamCopy";
            }
        };

        FileCopyRunner bufferedStreamCopy = new FileCopyRunner(){
            @Override
            public void copyFile(File source, File target) {
                InputStream fin = null;
                OutputStream fout = null;
                try {
                    fin = new BufferedInputStream(new FileInputStream(source));
                    fout = new BufferedOutputStream(new FileOutputStream(target));
                    byte[] buffer = new byte[1024];

                    int result;
                    while ((result = fin.read(buffer)) != -1) {
                        fout.write(buffer,0, result);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }
            @Override
            public String toString(){
                return "bufferedStreamCopy";
            }
        };

        FileCopyRunner nioBufferCopy = new FileCopyRunner(){
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;

                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();

                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    // 通道 和  buffer  写入和读取是相反的
                    while ((fin.read(buffer)) != -1){
                        buffer.flip();
                        while (buffer.hasRemaining()){
                            fout.write(buffer);
                        }
                        buffer.clear();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }
            @Override
            public String toString(){
                return "nioBufferCopy";
            }
        };

        FileCopyRunner nioTransferCopy = new FileCopyRunner(){
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin = null;
                FileChannel fout = null;
                try {
                    fin = new FileInputStream(source).getChannel();
                    fout = new FileOutputStream(target).getChannel();
                    long transfered = 0L;
                    long size = fin.size();
                    while (transfered != size) {
                         transfered += fin.transferTo(0,fin.size(),fout);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }
            @Override
            public String toString(){
                return "nioTransferCopy";
            }
        };

        File smallFile  = new File("C:\\Users\\zhaomo\\Desktop\\File\\Git.md");
        File smallFileCopy  = new File("C:\\Users\\zhaomo\\Desktop\\File\\Git_copy.md");
        System.out.println("-----Coping small file-------------");
        benchmark(noBufferStreamCopy,smallFile,smallFileCopy);
        benchmark(bufferedStreamCopy,smallFile,smallFileCopy);
        benchmark(nioBufferCopy,smallFile,smallFileCopy);
        benchmark(nioTransferCopy,smallFile,smallFileCopy);


        File bigFile  = new File("C:\\Users\\zhaomo\\Desktop\\File\\Git.pdf");
        File bigFileCopy  = new File("C:\\Users\\zhaomo\\Desktop\\File\\Git_copy.pdf");
        System.out.println("-----Coping big file-------------");
        benchmark(noBufferStreamCopy,bigFile,bigFileCopy);
        benchmark(bufferedStreamCopy,bigFile,bigFileCopy);
        benchmark(nioBufferCopy,bigFile,bigFileCopy);
        benchmark(nioTransferCopy,bigFile,bigFileCopy);

    }

}
