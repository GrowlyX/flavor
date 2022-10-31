# flavor
A light-weight [kotlin](https://kotlinlang.org) IoC container and lifecycle management framework. 
- Flavor is supposed to be an easy-to-use alternative to [guice](https://github.com/google/guice). 
  - Flavor also incorperates several design elements from [HK2](https://javaee.github.io/hk2/), a DI framework.
- We use kotlin-exclusive features such as [reified types & inline functions](https://kotlinlang.org/docs/inline-functions.html) heavily.
  - Flavor has limited functionality for languages other than Kotlin.

## Features:
- **[Services:](https://github.com/GrowlyX/flavor/tree/master/src/main/kotlin/gg/scala/flavor/service)**
  - Built-in service locator for [@Service](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/service/Service.kt) classes.
  - Control lifecycle through the [@PostConstruct](https://docs.oracle.com/javaee/7/api/javax/annotation/PostConstruct.html) and [@PreDestroy](https://docs.oracle.com/javaee/7/api/javax/annotation/PreDestroy.html) annotations.
  - Create boilerplate binder modules and inherit them through [Flavor#inherit](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/Flavor.kt)
    - Use the [@Extract](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/inject/Extract.kt) annotation to extract constructed object instances from a FlavorBinderContainer.
- **[DI:](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/Flavor.kt)**
  - Allows for simple yet effective dependency injection in both objects & classes.
  - Similar to almost every other DI framework, fields are injected eagerly.
    - You can create a simple work-around to this by injecting a [lazy](https://kotlinlang.org/docs/delegated-properties.html#lazy-properties) delegate.
- **[Locating:](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/Flavor.kt#L51)**
  - Allows for a simple yet effective method to look for methods with specific annotations on startup. 

## Usage:
Creating a Flavor instance:
```kt
val flavor = Flavor.create<FlavorTest>(
    // This argument is optional
    FlavorOptions(logger = yourLogger)
)
```

Binding a class to an instance:
```kt
flavor.bind<SomeObject>()
    .annotated<Named> {
        it.value == "SomethingString"
    }
    .to(someObjectInstance)
```

Instantiating an injected class:
 - The closest constructor with all bound instances will be used.
```kt
flavor.injected<SomeObjectInjected>()
```

Binder modules (similar to Guice) can be created through a new `FlavorBinderContainer`:
```kt
@Override
fun bind()
{
   
}

flavor.inherit(container)
```

An example of a Flavor service:
```kt
@Service
// @IgnoreAutoScan - if you do not want Flavor to 
// automatically register this service
object SomeService
{
    @Inject 
    @Named("SomethingInjected")
    lateinit var something: String
    
    @Inject
    @Named("SomethingInjected")
    fun onInjectSomething(string: String)
    {
       // injection through methods
    }
    
    @PostConstruct
    fun postConstruct()
    {
        // this method is invoked once all 
        // fields have been injected!
    }
    
    @PreDestroy
    fun preDestroy()
    {
        // this method is invoked on your 
        // platform's shutdown!
    }
}
```

If you want to manually register/inject into a service/class:
```
flavor.inject(instance)
```

Your Flavor instance can be started and stopped using:
 - All services will be automatically located and constructed on startup.
```kt
flavor.startup()
flavor.close()
```

- **NOTE:** Flavor automatically iterates through each class in the base package of the registration class, registers any available service, and injects all fields which need injection.
```
gg.scala.flavor.FlavorTest - registration class
gg.scala.flavor - package which flavor will scan
```

*⚠️ There is currently no way to disable auto service locator functionality.*

## Compilation:
- Flavor is available on [jitpack.io](https://jitpack.io/#GrowlyX/flavor)

If you would like compile flavor yourself, use:
- Flavor will automatically install itself to your local maven repository.
```
./gradlew build
```
