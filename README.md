# flavor
A light-weight kotlin dependency injection & service management framework. 
- Flavor is supposed to be an easy-to-use alternative to [guice](https://github.com/google/guice) 
- We use kotlin-exclusive features such as [reified types & inline functions](https://kotlinlang.org/docs/inline-functions.html) heavily.
  - Due to this feature use, flavor will **NOT** work in java-only projects.
  - An update will be released if this changes in the future.

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
    // We will only inject this binder 
    // into singletons
    .scoped(InjectScope.SINGLETON)
    .annotated<Named> {
        it.value == "SomethingString"
    }
    .to(someObjectInstance)
```

Creating an injected class:
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
