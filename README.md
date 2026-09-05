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

This library requires minSdk 21 (Android 5.0).

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

## Minification & Obfuscation (R8)

If you decide to enable minify and obfuscation (R8) in your app, **no manual ProGuard rules are required**. The published AAR bundles `consumerProguardFiles` (see `provider/consumer-rules.pro`) which is intentionally empty: the library performs no runtime reflection — services are registered and looked up through `Class` literals (`Class.getName()` comparison, `isAssignableFrom`, `isInstance`), which R8 rewrites consistently under shrinking and obfuscation. One caveat: do not enable `minifyEnabled true` on the library module itself — with no entry points R8 strips the entire public API, producing an empty classes.jar (verified); keep the library's release `minifyEnabled false` (the shipped default) and let consumer apps do the shrinking.

This is verified automatically by the `:r8-smoke` module: a harness app that consumes the library exactly like a real consumer, builds MINIFIED (`minifyEnabled true`), and runs instrumented tests against that minified build. Its `verifyR8Mapping` Gradle task parses the R8 `mapping.txt` on every release build and fails the build if obfuscation is silently disabled/weakened or if any `m.co.rh.id.aprovider.*` class stays identity-named.

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
