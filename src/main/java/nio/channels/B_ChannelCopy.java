
package nio.channels;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channels;
import java.io.IOException;

/**
 * Test copying between channels.
 * 通道之间复制数据
 * <p>
 * Created and tested: Dec 31, 2001
 * Revised for NIO book, April 2002
 *
 * @author Ron Hitchens (ron@ronsoft.com)
 * @version $Id: ChannelCopy.java,v 1.4 2002/04/21 05:10:56 ron Exp $
 */
public class B_ChannelCopy {
    /**
     * This code copies data from stdin to stdout.  Like the 'cat'
     * command, but without any useful options.
     */
    public static void main(String[] argv)
            throws IOException {
        ReadableByteChannel source = Channels.newChannel(System.in);
        WritableByteChannel dest = Channels.newChannel(System.out);

        channelCopy1(source, dest);
        // alternatively, call channelCopy2 (source, dest);

        source.close();
        dest.close();

    }

    /**
     * 这个实现使用了临时缓冲区上的压缩来打包数据，如果缓冲区没有完全排空。。这可能导致数据复制,但最大限度减少系统调用,他需要一个清除循环来确保所有数据都被发送
     * Channel copy method 1.  This method copies data from the src
     * channel and writes it to the dest channel until EOF on src.
     * This implementation makes use of compact() on the temp buffer
     * to pack down the data if the buffer wasn't fully drained.  This
     * may result in data copying, but minimizes system calls.  It also
     * requires a cleanup loop to make sure all the data gets sent.
     */
    private static void channelCopy1(ReadableByteChannel src,
                                     WritableByteChannel dest)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        /*
        ByteChannel的read( ) 和write( )方法使用ByteBuffer对象作为参数。
        两种方法均返回已传输的字节数，可能比缓冲区的字节数少甚至可能为零。缓冲区的位置也会发生与已传输字节相同数量的前移。
        如果只进行了部分传输，缓冲区可以被重新提交给通道并从上次中断的地方继续传输。该过程重复进行直到缓冲区的hasRemaining( )方法返回false值
         */
        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();

            // write to the channel, may block
            dest.write(buffer);

            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            //如果部分转移，移位余数向下,如果缓冲区是空的，同做clear()
            buffer.compact();
        }

        // EOF will leave buffer in fill state
        //EOF将使缓冲区处于填充状态。
        buffer.flip();

        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }

    /**
     * Channel copy method 2.  This method performs the same copy, but
     * assures the temp buffer is empty before reading more data.  This
     * never requires data copying but may result in more systems calls.
     * No post-loop cleanup is needed because the buffer will be empty
     * when the loop is exited.
     * 读取数据之前,确保临时缓冲区是空的,这不需要数据复制,但可能导致更多的系统调用,不需要循环后清除,因为缓冲区将为空
     */
    private static void channelCopy2(ReadableByteChannel src,
                                     WritableByteChannel dest)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        while (src.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();

            // make sure the buffer was fully drained.
            while (buffer.hasRemaining()) {
                dest.write(buffer);
            }

            // make the buffer empty, ready for filling
            buffer.clear();
        }
    }
}

