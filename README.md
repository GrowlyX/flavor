# flavor
A light-weight kotlin dependency injection & service management framework. 
- Flavor is based around [guice](https://github.com/google/guice)'s design.

## Usage:
Creating a Flavor instance:
```kt
val flavor = Flavor.create<FlavorTest>()
```

Binding a class to an instance:
```kt
flavor.bind<SomeObject>()
    .scoped(InjectScope.SINGLETON)
    .annotated<Named> {
        it.value == "horsie"
    }
    .to(someObjectInstance)
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
}
```

Your Flavor instance can be started using:
```kt
flavor.startup()
```

and stopped using:
```kt
flavor.close()
```

- NOTE: Flavor automatically iterates through each class in the base package of the registration class, registers any available service, and injects all fields which need injection.
```
gg.scala.flavor.FlavorTest - registration class
gg.scala.flavor - package which flavor will scan
```

**⚠️ There is currently no way to disable automatic scanning.**

## Compilation:
```
./gradlew build
```
