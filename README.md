# a-provider

![JitPack](https://img.shields.io/jitpack/v/github/rh-id/a-provider)
![Downloads](https://jitpack.io/v/rh-id/a-provider/week.svg)
![Downloads](https://jitpack.io/v/rh-id/a-provider/month.svg)
![Android CI](https://github.com/rh-id/a-provider/actions/workflows/gradlew-build.yml/badge.svg)

This is a simple Service Locator for Android projects that doesn't rely on annotations or "magic"


## Example Usage

This project support jitpack, in order to use this, you need to add jitpack to your project root build.gradle:
```
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io" }
        jcenter() // Warning: this repository is going to shut down soon
    }
}
```

Include this to your module dependency (module build.gradle)
```
dependencies {
    implementation 'com.github.rh-id:a-provider:v0.0.1'
}
```

Then you could proceed writing code,
First create root module as a root of the provider to provide services.

```
public class RootModule implements ProviderModule{
    @Override
    void provides(ProviderRegistry providerRegistry, Provider provider){
        // Register your services/components here or other ProviderModule
        providerRegistry.register(IService.class, () -> new ServiceImpl());
        providerRegistry.registerModule(new ProviderModuleA());
        // You could use registerLazy to lazy-load your services
        providerRegistry.registerLazy(IServiceA.class, ServiceAImpl::new);
        // You could use registerAsync to initialize your services in background thread
        providerRegistry.registerAsync(IServiceB.class,
                                        () -> new ServiceBImpl(provider.get(IServiceA.class)));
        // use registerFactory to load new instances everytime Provider.get() is invoked
        providerRegistry.registerFactory(MyPojo.class, () -> {
            MyPojo myPojo = new MyPojo();
            myPojo.setAge(99);
            myPojo.setName("Foo");
            return myPojo;
        });
        // OR use registerPool to load new instances everytime Provider.get() is invoked.
        providerRegistry.registerPool(MyPojo.class, () -> {
            MyPojo myPojo = new MyPojo();
            myPojo.setAge(99);
            myPojo.setName("Foo");
            return myPojo;
        });
    }

    @Override
    void dispose(Provider provider){
        // do something when this module is going to be disposed
    }
}
```

Initialize on your application for global access (example only)

```
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // store the instance in static value for global access
        Provider provider = Provider.createProvider(this, new RootModule());
        // example retrieve value
        IServiceA iServiceA = provider.get(IServiceA.class);
        MyPojo myPojo = provider.get(MyPojo.class);
    }
}
```
If you need to handle dispose event you could implement `ProviderDisposable` to your component/services
```
public class ServiceAImpl implements IServiceA, ProviderDisposable {
    @Override
    public void dispose(Context context){
    // anything to dispose, this will be called on Provide.dispose
    }
}
```
```
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Provider provider = Provider.createProvider(this, new RootModule());
        IServiceA iServiceA = provider.get(IServiceA.class);
        provider.dispose(); // ServiceAImpl.dispose(Context) will be called
    }
}
```

## Skipping same type

For integration testing purposes you could turn on `skipSameType`. This will make `providerRegistry`
ignore duplicate type during registration

Example production RootModule:

```
public class RootModule implements ProviderModule{
    @Override
    void provides(ProviderRegistry providerRegistry, Provider provider){
        providerRegistry.register(IService.class, () -> new ServiceImpl());
    }
}
```

Example test RootModule to be used:

```
public class TestRootModule extends RootModule{
    @Override
    void provides(ProviderRegistry providerRegistry, Provider provider){
        // register IService.class with test instance
        providerRegistry.register(IService.class, () -> new TestServiceImpl());

        providerRegistry.setSkipSameType(true); // enable
        // since skip is true, the IService.class from parent will not be registered again
        super.provides(providerRegistry, provider); 
        providerRegistry.setSkipSameType(false); // disable skip after done
    }
}
```

The configuration `providerRegistry.setSkipSameType(true);` can be useful on some circumstances such
as multiple android app flavors or configuration

## Example Projects

<ul>
<li>https://github.com/rh-id/a-news-provider</li>
<li>https://github.com/rh-id/a-flash-deck</li>
<li>https://github.com/rh-id/a-medic-log</li>
<li>https://github.com/rh-id/a-personal-stuff</li>
</ul>

## Support this project
Consider donation to support this project
<table>
  <tr>
    <td><a href="https://trakteer.id/rh-id">https://trakteer.id/rh-id</a></td>
  </tr>
</table>
