# flavor
A light-weight [kotlin](https://kotlinlang.org) dependency injection & lifecycle management framework. 
- Flavor is supposed to be an easy-to-use alternative to [guice](https://github.com/google/guice). 
  - Flavor also incorperates several design elements from [HK2](https://javaee.github.io/hk2/), a DI framework.
- We use kotlin-exclusive features such as [reified types & inline functions](https://kotlinlang.org/docs/inline-functions.html) heavily.
  - Due to this, flavor is only compatible with kotlin-exclusive projects.
    - I do not plan on adding support for other JVM languages.
- Hate my code? Despise annotations? Check out [depenject](https://github.com/devrawr/depenject) by [string](https://github.com/devrawr).
  - Depenject takes a delegate-based approach, rather than annotation-based. This is achieved by using Kotlin's [ReadWriteProperty](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.properties/-read-write-property/).
    - Unfortunately, the delegate based approach makes it unusable in languages other than kotlin.

## Features:
- **[Services:](https://github.com/GrowlyX/flavor/tree/master/src/main/kotlin/gg/scala/flavor/service)**
  - Searches all singletons (kotlin objects) within a specified package for objects marked with [@Service](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/service/Service.kt).
  - Control lifecycle through the [@Configure](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/service/Close.kt) and [@Close](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/service/Close.kt) annotations.
- **[DI:](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/Flavor.kt)**
  - Allows for simple yet effective dependency injection in both objects & classes.
  - Similar to almost every other DI framework, fields are injected eagerly.
    - You can create a simple work-around to this by injecting a [lazy](https://kotlinlang.org/docs/delegated-properties.html#lazy-properties) delegate.
- **[Listeners:](https://github.com/GrowlyX/flavor/blob/master/src/main/kotlin/gg/scala/flavor/Flavor.kt#L51)**
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

Instantiating an injected class (with optional parameters):
```kt
flavor.injected<SomeObjectInjected>(
    // These constructor parameters are not required.
    constructorParamOne, constructorParamTwo
)
```

An example of a Flavor service:
```kt
@Service
// @IgnoreAutoScan - if you do not want Flavor to 
// automatically register this service
object SomeService
{
    @Inject @Named("SomethingString")
    lateinit var something: String
    
    @Configure
    fun configure()
    {
        // this method is invoked once all 
        // fields have been injected!
    }
    
    @Close
    fun close()
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
```kt
flavor.startup()
flavor.close()
```

- **NOTE:** Flavor automatically iterates through each class in the base package of the registration class, registers any available service, and injects all fields which need injection.
```
gg.scala.flavor.FlavorTest - registration class
gg.scala.flavor - package which flavor will scan
```

*⚠️ There is currently no way to disable automatic scanning.*

## Compilation:
- Flavor is available on [jitpack.io](https://jitpack.io/#GrowlyX/flavor)

If you would like compile flavor yourself, use:
- Flavor will automatically install itself to your local maven repository.
```
./gradlew build
```
