package nio.selector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class A_Selector {
    public static void main(String[] args) throws IOException {
        //选择器:选择器类管理着一个被注册的通道集合的信息和它们的就绪状态。
        //SelectableChannel:实现通道的可选择性所需要的公共方法,所有支持就绪检查的通道类的父类
        //SelectionKey:选择键封装了特定的通道与特定的选择器的注册关系
        //一个通道可以被注册到多个选择器上，但对每个选择器而言只能被注册一次。
        //调用可选择通道的register( )方法会将它注册到一个选择器上,通道需要非阻塞状态，否则异常
        SocketChannel channel1 = SocketChannel.open();
        SocketChannel channel2 = SocketChannel.open();
        SocketChannel channel3 = SocketChannel.open();
        Selector selector = Selector.open();
        //register():第二个参数表示选择器在检查通道就绪状态时需要关心的操作的比特掩码,有四种被定义的可选择操作：读(read)，写(write)，连接(connect)和接受(accept)。设置不支持的操作,抛异常
        SelectionKey selectionKey1 = channel1.register(selector, SelectionKey.OP_READ);
        SelectionKey selectionKey2 = channel2.register(selector, SelectionKey.OP_WRITE);
        SelectionKey selectionKey3 = channel3.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        int readyCount = selector.select(1000);//将线程置于睡眠状态,直到感兴趣的事情中的操作中的一个发生或者10秒钟的时间过去
        selector.close();//释放它可能占用的资源并将所有相关的选择键设置为无效
        selector.isOpen();//选择器是否处于被打开的状态
        channel1.validOps();//获取特定的通道所支持的操作集合

        //在更新interest集合为指定的值的同时，将返回与之前相同的选择键,后续的注册都只是简单地将与之前的注册关系相关的键进行更新
        //通道不会在键被取消的时候立即注销。直到下一次操作发生为止
        //将一个通道注册到一个相关的键已经被取消的选择器上，而通道仍然处于被注册的状态的时候,CancelledKeyException
        //isRegistered( )方法来检查一个通道是否被注册到任何一个选择器上,存在延迟。
        //keyFor( ):返回与该通道和指定的选择器相关的键

        /*SelectionKey*/
        //一个SelectionKey对象包含两个以整数形式进行编码的比特掩码：一个用于指示那些通道/选择器组合体所关心的操作(instrest集合)，另一个表示通道准备好要执行的操作（ready集合)
        //cancel( ):取消注册关系
        //isValid( ):检查它是否仍然表示一种有效的关系
        //interestOps( ):当前的interest集合,带参数的可修改编码比特掩码,可注册时指定。所有更改将会在select( )的下一个调用中体现出来
        //readyOps( ):获取相关的通道的已经就绪的操作,ready集合是interest集合的子集,表示了interest集合中从上次调用select( )以来已经就绪的那些操作

/*      当键被取消时，它将被放在相关的选择器的已取消的键的集合里。注册不会立即被取消，但键会立即失效。
            当再次调用select( )方法时（或者一个正在进行的select()调用结束时），已取消的键的集合中的被取消的键将被清理掉，并且相应的注销也将完成。
            通道会被注销，而新的SelectionKey将被返回。
        当通道关闭时，所有相关的键会自动取消
        当选择器关闭时，所有被注册到该选择器的通道都将被注销，并且相关的键将立即被无效化（取消）。一旦键被无效化，调用它的与选择相关的方法就将抛出CancelledKeyException。*/

        //与键关联的通道是否就绪,就绪后,读取数据,写入缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        if ((selectionKey1.readyOps() & SelectionKey.OP_READ) != 0) {//等价isReadable()
            byteBuffer.clear();
            ((SocketChannel) selectionKey1.channel()).read(byteBuffer);
            byteBuffer.flip();
        }
       /* 测试就绪状态,受操作系统影响,SelectionKey对象包含的ready集合与最近一次选择器对所注册的通道所作的检查相同。而每个单独的通道的就绪状态会同时改变。
                1:测试比特掩码来检查这些状态
                2:isReadable( )，isWritable( )，isConnectable( )， 和isAcceptable( )*/

        //attach/:在键上放置一个“附件”，可在后面获取它
        //attachment:获取附件对象
        //register(Selector sel, int ops, Object att):参数3就是attach
        //如果选择键的存续时间很长，但附加的对象不应该存在那么长时间，记得在完成后清理附件。否则，您附加的对象将不能被垃圾回收，您将会面临内存泄漏问题。

        /*Selector*/
        //使用内部的已取消的键的集合来延迟注销，是一种防止线程在取消键时阻塞，并防止与正在进行的选择操作冲突的优化。
        // 清理已取消的键，并在选择操作之前和之后立即注销通道，可以消除它们可能正好在选择的过程中执行的潜在棘手问题。这是另一个兼顾健壮性的折中方案。
        //keys:已注册的键的集合,与选择器关联的已经注册的键的集合
        //selectedKeys:已选择的键的集合,已注册的键的集合的子集。这个集合的每个成员都是相关的通道被选择器（在前一个选择操作中）判断为已经准备好的，并且包含于键的interest集合中的操作
        //cancel:已取消的键的集合,已注册的键的集合的子集，这个集合包含了cancel( )方法被调用过的键（这个键已经被无效化），但它们还没有被注销
        //select:无限阻塞,至少有一个注册时,选择器的选择键就会被更新，并且每个就绪的通道的ready集合也将被更新。返回值将会是已经确定就绪的通道的数目。
        //         可设置超时时间,
        //selectNow:非阻塞
        //wakeup( ):使线程从被阻塞的select( )方法中退出
        //close( ):与选择器相关的通道将被注销，而键将被取消。
        //interrupt( ):睡眠中的线程的interrupt( )方法被调用，它的返回状态将被设置。如果被唤醒的线程之后将试图在通道上执行I/O操作，通道将立即关闭，然后线程将捕捉到一个异常。
    }
}
