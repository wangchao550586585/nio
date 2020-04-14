package nio.buffers;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;

public class A_ByteExample {
    public static void main(String[] args) {

        /**基础*/
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        byteBuffer.put((byte) 'H').put((byte) 'E').put((byte) 'L').put((byte) 'L').put((byte) 'O');
        byteBuffer.put(0, (byte) 'M').put((byte) 'W');

        //上界属性设置为当前位置,将读取位置重置0
        //byteBuffer.limit(byteBuffer.position()).position(0);
        //同上,将能够继续添加的数据元素填充状态的缓冲区翻转成一个准备读出元素的释放状态
        byteBuffer.flip();
        //将位置设为0
        byteBuffer.rewind();

        //hasRemaining:是否达到缓冲区上界
        byte[] bytes = new byte[byteBuffer.limit()];
        for (int i = 0; byteBuffer.hasRemaining(); i++) {
            bytes[i] = byteBuffer.get();
        }
        String s = new String(bytes);
        System.out.println(s);

        //remaining:当前位置到上界剩余元素数目
        for (int i = 0; i < byteBuffer.remaining(); i++) {
            bytes[i] = byteBuffer.get();
        }
        s = new String(bytes);
        System.out.println(s);

        /*压缩,之后的移动到前面,前面的移动到后面,然后最新的position在后面的第一个元素上，上界属性被设置为容量的值,以便起到丢弃后面的数据*/
        byteBuffer.compact();

        //标记当前位置
        byteBuffer.mark();

        //position重新定位标记位置
        byteBuffer.reset();

        //批量移动,等价下面指定偏移量的方法
        //如果长度不够bytes,则异常
        byteBuffer.get(bytes);//填充bytes
        byteBuffer.get(bytes, 0, bytes.length);

        byteBuffer.get(bytes, 0, byteBuffer.remaining());
        /*批量读取*/
        while (byteBuffer.hasRemaining()) {
            int length = Math.min(byteBuffer.remaining(), bytes.length);
            byteBuffer.get(bytes, 0, length);
        }

        /*写入,如果缓冲区空间不足则异常*/
        byteBuffer.put(bytes);
        byteBuffer.put(bytes, 0, bytes.length);

        //缓冲区写入缓冲区
        ByteBuffer byteBuffer1 = ByteBuffer.allocate(20);
        byteBuffer.put(byteBuffer1);
        while (byteBuffer1.hasRemaining()) {
            byteBuffer.put(byteBuffer1.get());
        }

        CharBuffer allocate = CharBuffer.allocate(10);
        String str = "hellow";
        //三者等价
        allocate.put(str);
        allocate.put(str, 0, str.length());
        for (int i = 0; i < str.length(); i++) {
            allocate.put(str.charAt(i));
        }

        /**     创建缓冲区*/
        //从堆空间中分配了一个char型数组作为备份存储器来储存100个char变量
        CharBuffer allocate1 = CharBuffer.allocate(100);

        //创建一个缓冲区对象但是不分配任何空间来储存数据元素
        char[] chars = new char[100];
        CharBuffer wrap = CharBuffer.wrap(chars);
        //指定初始化位置和上界,position=12/limit=54,。
        // 这个缓冲区可以存取这个数组的全部范围；offset和length参数只是设置了初始的状态。
        // 调用使用上面代码中的方法创建的缓冲区中的clear()函数，然后对其进行填充，直到超过上界值，这将会重写数组中的所有元素
        wrap = CharBuffer.wrap(chars, 12, 42);

        //通过allocate()或者wrap()函数创建的缓冲区通常都是间接的。间接的缓冲区使用备份数组
        //hasArray()缓冲区是否有一个可存取的备份数组。
        //array()函数返回这个缓冲区对象所使用的数组存储空间的引用。

        //缓冲区是可读的话,它的备份数组将会是超出上界的,
        // 即使一个数组对象被提供给wrap()函数。调用array()函数或者arrayOffset()会抛出一个ReadOnlyBufferException异常，
        //通过其它的方式获得了对备份数组的存取权限，对这个数组的修改也会直接影响到这个只读缓冲区

        /*arrayOffset()，返回缓冲区数据在数组中存储的开始位置的偏移量（从数组头0开始计算）。
        如果使用了带有三个参数的版本的wrap()函数来创建一个缓冲区，对于这个缓冲区，arrayOffset()会一直返回0，
        如果切分了由一个数组提供存储的缓冲区，得到的缓冲区可能会有一个非0的数组偏移量。
        这个数组偏移量和缓冲区容量值会告诉您数组中哪些元素是被缓冲区使用的。*/

        CharBuffer.wrap(str);
        CharBuffer.wrap(str, 0, str.length());

        /**  复制缓冲区*/
        //创建了一个与原始缓冲区相似的新缓冲区。两个缓冲区共享数据元素，拥有同样的容量，但每个缓冲区拥有各自的位置，上界和标记属性。
        //对一个缓冲区内的数据元素所做的改变会反映在另外一个缓冲区上。这一副本缓冲区具有与原始缓冲区同样的数据视图。如果原始的缓冲区为只读，或者为直接缓冲区，新的缓冲区将继承这些属性
        CharBuffer allocate2 = CharBuffer.allocate(100);
        allocate2.position(2).limit(6).mark().position(5);
        CharBuffer duplicate = allocate2.duplicate();
        allocate2.clear();

        //生成只读视图
        CharBuffer charBuffer = allocate2.asReadOnlyBuffer();

        //分割缓冲区,创建一个从原始缓冲区的当前位置开始到剩余元素数量容量的新缓冲区,也会继承只读和直接属性
        //创建一个映射到数组位置12-20（9个元素）的buffer对象
        char[] myBuffer = new char[100];
        CharBuffer cb = CharBuffer.wrap(myBuffer);
        cb.position(12).limit(21);
        CharBuffer sliced = cb.slice();


        /** 字节缓存区*/
        /*字节顺序*/
        //获取和存放缓冲区内容函数对字节进行编码或解码的方式取决于ByteBuffer当前字节顺序的设定
        //如果数字数值的最高字节——big end（大端），位于低位地址，那么系统就是大端字节顺序(03 7F B4 C7)。如果最低字节最先保存在内存中，那么小端字节顺序(C7 B4 7F 03)。
        //ByteOrder:决定从缓冲区中存储或检索多字节数值时使用哪一字节顺序的常量
        //ByteOrder.nativeOrder():JVM运行的硬件平台的固有字节顺序
        //每个缓冲区类都具有一个能够通过调用order()查询的当前字节顺序设定。

        //字符顺序设定,其他缓冲类只能查看,且返回的结果为ByteOrder.nativeOrder()
        ByteBuffer allocate3 = ByteBuffer.allocate(10);
        allocate3.order(ByteOrder.BIG_ENDIAN);//默认ByteBuffer.BIG_ENDIAN

        //当创建一个ByteBuffer对象的视图,order()返回的数值就是视图被创建时其创建源头的ByteBuffer的字节顺序设定。
        //视图的字节顺序设定在创建后不能被改变，而且如果原始的字节缓冲区的字节顺序在之后被改变，它也不会受到影响

        /*直接缓冲区
        * 直接缓冲区被用于与通道和固有I/O例程交互。它们通过使用固有代码来告知操作系统直接释放或填充内存区域，对用于通道直接或原始存取的内存区域中的字节元素的存储尽了最大的努力
        * */
        /*非直接缓冲区：通过 allocate() 方法分配缓冲区，将缓冲区建立在 JVM 的内存中
        直接缓冲区：通过 allocateDirect() 方法分配直接缓冲区，将缓冲区建立在物理内存中。可以提高效率*/
        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(100);
        byteBuffer2.isDirect();

        /*视图缓冲区*/
        //这种视图对象维护它自己的属性，容量，位置，上界和标记，但是和原来的缓冲区共享数据元素
        //这个缓冲区是基础缓冲区的一个切分，由基础缓冲区的位置和上界决定。
        // 新的缓冲区的容量是字节缓冲区中存在的元素数量除以视图类型中组成一个数据类型的字节数。
        // 在切分中任一个超过上界的元素对于这个视图缓冲区都是不可见的
        CharBuffer charBuffer1 = byteBuffer2.asCharBuffer();

        /*数据元素视图*/
        /*每一种原始数据类型提供了存取的和转化的方法每一种原始数据类型提供了存取的和转化的方法 getChar/putChar */
        int value = byteBuffer2.getInt();//会提取position后4位按照字节排序组成int返回,提取字节位数,缓冲区不够,则异常
        value = byteBuffer2.order(ByteOrder.BIG_ENDIAN).getInt();

        /*存取无符号数据*/















    }
}
