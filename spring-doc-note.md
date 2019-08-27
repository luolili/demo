#ConfigurableBeanFactory 
intro: 继承了HierarchicalBeanFactory接口和SingletonBeanRegistry。

HierarchicalBeanFactory的作用
- BeanFactory getParentBeanFactory(); 获取他的父亲facotry类，如果没有就返回null
- boolean containsLocalBean(String name);根据传入的bean名字，在当前的工厂里面找是否有这bean，
不会在他的父亲工厂类里面去找

SingletonBeanRegistry的作用
- void registerSingleton(String beanName, Object singletonObject); singletonObject表示的是已经完全初始化
好的一个单例对象；把这个对象放到beanName的名下。他的实现类在调用这个方法的时候应该加上同步访问的控制。
这里要求名字和对象都不能为空。

DefaultSingletonBeanRegistry：
- singletonObjects： bean name --> obj的ConcurrentHashMap结构。
在向map加入一条记录之前，先判断加入的记录在这个map里面是否已经存在。
在访问singletonObjects 的时候需要同步。

然后调用protected void addSingleton(String beanName, Object singletonObject)；
- 在加入新的记录的时候也需要同步singletonObjects。防止多个线程都来加入相同的bean.

1. FactoryBean与ObjectFactory2个接口的区别？
- ObjectFactory的getObject方法通常返回的是一个多例的对象,也会返回单例的对象； FactoryBean是一个SPI实例对象
- ObjectFactory捕获的是BeansException;FactoryBean是捕获Exception.
singletonFactories 里面装的就是ObjectFacory,用他来创建单例对象，并把创建出来的对象放进earlySingletonObjects。

2. spring获取单例对象的步骤？
方法： protected Object getSingleton(String beanName, boolean allowEarlyReference) 
- 从singletonObjects中根据名字获取单例对象，如果获得的对象不是空，则返回；
- 如果返回的是空，那么从ObjectFactory创建的对象的缓存earlySingletonObjects里面获取；
- 如果几个是空，那么根据名字从 singletonFactories 获取到生产这bean的 工厂，
- 如果工厂部位空，那么调用过程的getObject方法 获得单例对象， 把他赋值给返回值对象，把他放入 earlySingletonObjects
- 删除掉生产这bean的过程，因为下一次的bean可能会增加货减少一些属性，所以要删除原来的工厂；需要新的工厂来生产新的bean，

3. 在什么时候创建bean，放入缓存里面？
方法： public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) 
属性：singletonsCurrentlyInDestruction： 标识当前时间是否是在销毁单例的时间范围之内。
不能再销毁对象的时候，来创建货请求一个bean。
- 从 singletonObjects 根据名字获取单例对象；如果获得的对象是空，当前正在销毁对象销毁，就报错BeanCreationNotAllowedException。
- 打印日志：Creating shared instance of singleton bean
- 前置处理： beforeSingletonCreation(beanName);
- boolean newSingleton = false; 是否是新创建的单例。从传的参数 singletonFactory 里面获取单例对象，newSingleton 变为true.
- 在catch里面再次从 singletonObjects 获取单例对象
- 后置处理： 	afterSingletonCreation(beanName);
- addSingleton

