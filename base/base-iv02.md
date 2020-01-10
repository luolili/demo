
char 类型变量为什么可以存一个中文汉字？

java 为了统一编码，选择了 unicode，而非某个特定的编码。
但是字符在 jvm 内部和外部有不同的表现形式；
内部都是 unicode,在外部 如文件系统 需要转换编码。

2.java 存在内存泄漏吗？

存在，如 hibernate 的一级缓存 里面的对象属于持久态，
GC 不会回收他们

3.静态的方法可以重写吗？

不能。
```
Person p = new Student()
p.eat();
```

静态类型：编译时候认为的对象属于 的 类型，Person
就是静态类型；

实际类型：运行时候真正的对象的类型：Student

方法的接受人：动态绑定所找到的执行个方法的对象：p

静态方法的调用是编译时候静态绑定的

4.组合（composition） 和 聚合（aggregation）的区别?

组合：人的手，身体等组成人，器官离开了人就没有了意义；聚合：一个班级有多个学生，一个学生离开了班级，他也可存活

5.为什么接口不允许 返回枚举？

在接口解析，会出现反序列化的异常。