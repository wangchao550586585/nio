package nio.channels;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class A_Channel {
    public static void main(String[] args) throws IOException {
        /** 通道*/
        /*通道是一种途径，借助该途径，可以用最小的总开销来访问操作系统本身的I/O服务。缓冲区则是通道内部用来发送和接收数据的端点。*/
        //I/O可以分为广义的两大类别：File I/O和Stream I/O。那么相应地有两种类型的通道也就不足为怪了，它们是文件（file）通道和套接字（socket）通道

        /* 打开通道*/
        //Socket通道有可以直接创建新socket通道的工厂方法。
        SocketChannel open = SocketChannel.open();
        open.connect(new InetSocketAddress("", 1000));

        ServerSocketChannel open1 = ServerSocketChannel.open();
        open1.socket().bind(new InetSocketAddress(1000));
        DatagramChannel open2 = DatagramChannel.open();

        //但是一个FileChannel对象却只能通过在一个打开的RandomAccessFile、FileInputStream或FileOutputStream对象上调用getChannel( )方法来获取。
        RandomAccessFile randomAccessFile = new RandomAccessFile("", "");
        FileChannel channel = randomAccessFile.getChannel();

        /*使用通道*/
        //通道会连接一个特定I/O服务且通道实例（channel instance）的性能受它所连接的I/O服务的特征限制，
        // 一个连接到只读文件的Channel实例不能进行写操作，即使该实例所属的类可能有write( )方法。
        //根据底层文件句柄的访问模式，通道实例可能不允许使用read()或write()方法。
        FileInputStream filename = new FileInputStream("filename");
        FileChannel channel1 = filename.getChannel();
        //会抛出异常,因为FileInputStream对象总是以read-only的权限打开文件
        channel.write(ByteBuffer.allocate(100));

        //通道可以以阻塞（blocking）或非阻塞（nonblocking）模式运行。
        //非阻塞模式的通道永远不会让调用的线程休眠。请求的操作要么立即完成，要么返回一个结果表明未进行任何操作。
        //只有面向流的（stream-oriented）的通道，如sockets和pipes才能使用非阻塞模式。

        /*关闭通道*/
        //一个打开的通道即代表与一个特定I/O服务的特定连接并封装该连接的状态。当通道关闭时，那个连接会丢失，然后通道将不再连接任何东西。
        //如果一个通道实现InterruptibleChannel接口,如果一个线程在一个通道上被阻塞并且同时被中断（由调用该被阻塞线程的interrupt( )方法的另一个线程中断），那么该通道将被关闭，该被阻塞线程也会产生一个ClosedByInterruptException异常。
        //假如一个线程的interrupt status被设置并且该线程试图访问一个通道，那么这个通道将立即被关闭，同时将抛出相同的ClosedByInterruptException异常。
        //当前线程的interrupt status可以通过调用静态的Thread.interrupted( )方法清除。

        /*Scatter/Gather
        对于一个write操作而言，数据是从几个缓冲区按顺序抽取（称为gather）并沿着通道发送的。缓冲区本身并不需要具备这种gather的能力（通常它们也没有此能力）。
        该gather过程的效果就好比全部缓冲区的内容被连结起来，并在发送数据前存放到一个大的缓冲区中。
        对于read操作而言，从通道读取的数据会按顺序被散布（称为scatter）到多个缓冲区，将每个缓冲区填满直至通道中的数据或者缓冲区的最大空间被消耗完。*/
        ByteBuffer head = ByteBuffer.allocate(10);
        ByteBuffer body = ByteBuffer.allocate(80);
        SocketChannel socketChannel = SocketChannel.open();
        ByteBuffer[] buffers = {head, body};
        long read = socketChannel.read(buffers);
        //如果read返回48,head包含前10,body包含后38

        //用一个gather操作将多个缓冲区的数据组合并发送出去,使用相同的缓冲区，汇总数据并在一个socket通道上发送包
        long write = channel.write(buffers);
        //从哪个缓冲区开始,指定使用缓冲区数量
        long write1 = channel.write(buffers, 1, 3);

        /*访问文件*/
        //position同时被作为通道引用获取来源的文件对象共享
        RandomAccessFile randomAccessFile1 = new RandomAccessFile("filename", "r");
        randomAccessFile.seek(1000);
        FileChannel channel2 = randomAccessFile.getChannel();
        System.out.println(channel2.position());
        randomAccessFile.seek(500);
        System.out.println(channel2.position());

        //truncate:砍掉指定新size之外的所有数据
        //force:文件的所有待定修改立即同步到磁盘,布尔型参数表示在方法返回值前文件的元数据（metadata）是否也要被同步更新到磁盘

        /*文件锁定 E_LockTest*/
        //锁的对象是文件而不是通道或线程

        /*内存映射文件 G_MapFile*/
        //MapMode.PRIVATE表示您想要一个写时拷贝（copy-on-write）的映射。
        //写时拷贝:如果缓冲区还没对某个页做出修改，那么这个页就会反映被映射文件的相应位置上的内容。一旦某个页因为写操作而被拷贝，之后就将使用该拷贝页，并且不能被其他缓冲区或文件更新所修改
        //关闭通道,写实拷贝不会关闭,因为映射缓冲区没有绑定到创建它的通道上
        //使用MappedByteBuffer.force( )而非FileChannel.force( )，因为通道对象可能不清楚通过映射缓冲区做出的文件的全部更改。

        // 通过put( )方法所做的任何修改都会导致产生一个私有的数据拷贝并且该拷贝中的数据只有MappedByteBuffer实例可以看到
        //必须以read/write权限来打开文件以建立MapMode.PRIVATE映射。只有这样，返回的MappedByteBuffer对象才能允许使用put( )方法
        FileInputStream filename2 = new FileInputStream("filename");
        FileChannel channel3 = filename2.getChannel();
        MappedByteBuffer map = channel3.map(FileChannel.MapMode.READ_ONLY, 100, 200);//映射100到299（包含299）位置的字节
        map = channel3.map(FileChannel.MapMode.READ_ONLY, 100, channel3.size());//映射整个文件

        /*Channel-to-Channel传输 H_ChannelTransfer*/
        /*transferTo:传输给一个socketChannel/FileChannel通道
        transferFrom:将数据从一个socketChannel/FileChannel直接读取到一个文件中*/

        /*Socket通道*/
        /*直接实例化一个Socket对象，不会有关联的SocketChannel并且getChannel( )返回null。
        全部socket通道类（DatagramChannel、SocketChannel和ServerSocketChannel）在被实例化时都会创建一个对等socket对象
        socket可以通过调用socket( )方法从一个通道上获取
        * */

        /*非阻塞模式*/
        /*要把一个socket通道置于非阻塞模式，SelectableChannel*/
        ServerSocketChannel sc = ServerSocketChannel.open();
        sc.configureBlocking(true);//阻塞
        SocketChannel socket = null;
        synchronized (sc.blockingLock()) {    //blockingLock:防止socket通道的阻塞模式被更改
            boolean prevState = sc.isBlocking();//判断是否阻塞
            sc.configureBlocking(false);
            socket = sc.accept();
            sc.configureBlocking(prevState);
        }
        if (socket != null) {
            //doSomethingWithTheSocket
        }

        /*ServerSocketChannel:I_ChannelAccept*/
        ServerSocketChannel open3 = ServerSocketChannel.open();//创建一个新的ServerSocketChannel对象，将会返回同一个未绑定的java.net.ServerSocket关联的通道
        ServerSocket socket1 = open3.socket();//获取socket,可通过getChannel获取通道对象
        socket1.bind(new InetSocketAddress(1234));//监听端口

        /*SocketChannel:J_ConnectAsync*/
        //必须连接了才有用并且只能连接一次
        //sockets是面向流的而非包导向的。它们可以保证发送的字节会按照顺序到达但无法承诺维持字节分组
        //connect( )和finishConnect( )方法是互相同步的，并且只要其中一个操作正在进行，任何读或写的方法调用都会阻塞
        SocketChannel socketChannel1 = SocketChannel.open(new InetSocketAddress("", 1000));
        //二者等价
        SocketChannel socketChannel12 = SocketChannel.open();
        socketChannel12.connect(new InetSocketAddress("", 1000));

        socketChannel1.configureBlocking(false);//非阻塞
        socketChannel1.finishConnect();//完成套接字通道的连接过程
        //isConnectPending:判断此通道上是否正在进行连接操作
        //isConnected:判断是否已连接此通道的网络套接字
        socketChannel1.close();


        /*DatagramChanne:K_TimeClient/K_TimeServer */
        DatagramChannel open4 = DatagramChannel.open();
        open4.socket().bind(new InetSocketAddress(900));
        //receive( )方法将下次将传入的数据报的数据净荷复制到预备好的ByteBuffer中并返回一个SocketAddress对象以指出数据来源,如果包内的数据超出缓冲区能承受的范围，多出的数据都会被悄悄地丢弃
        //send( )会发送给定ByteBuffer对象的内容到给定SocketAddress对象所描述的目的地址和端口，内容范围为从当前position开始到末尾处结束。如果传输队列没有足够空间来承载整个数据报，那么什么内容都不会被发送。
        //如果安装了安全管理器，那么每次调用send( )或receive( )时安全管理器的checkConnect( )方法都会被调用以验证目的地址，除非通道处于已连接的状态
        //将DatagramChannel置于已连接的状态可以使除了它所“连接”到的地址之外的任何其他源地址的数据报被忽略
        //当DatagramChannel已连接时，使用同样的令牌，您不可以发送包到除了指定给connect( )方法的目的地址以外的任何其他地址。试图一定要这样做的话会导致一个SecurityException异常。
        //可以任意次数地进行连接或断开连接。每次连接都可以到一个不同的远程地址。
        //当一个DatagramChannel处于已连接状态时，发送数据将不用提供目的地址而且接收时的源地址也是已知的。这意味着DatagramChannel已连接时可以使用常规的read( )和write( )方法以及scatter/gather形式的读写来组合或分拆包的数据
        //一个未绑定的DatagramChannel仍能接收数据包。当一个底层socket被创建时，一个动态生成的端口号就会分配给它。
        //不论通道是否绑定，所有发送的包都含有DatagramChannel的源地址（带端口号）
      /*  选择数据报socket而非流socket的理由：
             您的程序可以承受数据丢失或无序的数据。
             您希望“发射后不管”（fire and forget）而不需要知道您发送的包是否已接收。
             数据吞吐量比可靠性更重要。
             您需要同时发送数据给多个接受者（多播或者广播）。
             包隐喻比流隐喻更适合手边的任务。*/

        /*管道:L_PipeTest*/
        //管道被用来连接一个进程的输出和另一个进程的输入,创建的管道是进程内（在Java虚拟机进程内部）而非进程间使用的,
        // 优点封装性,写到SinkChannel中的字节都能按照同样的顺序在SourceChannel上重现
        //场景:用来仅在同一个Java虚拟机内部传输数据
        //来辅助测试,一个单元测试框架可以将某个待测试的类连接到管道的“写”端并检查管道的“读”端出来的数据。它也可以将被测试的类置于通道的“读”端并将受控的测试数据写进其中。

        /*通道工具类：Channels*/






    }

}
