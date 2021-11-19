# a-provider

![Android CI](https://github.com/rh-id/a-provider/actions/workflows/gradlew-build.yml/badge.svg)

This is a simple Service Locator for Android projects that doesn't rely on annotations or "magic"


## Example Usage
For example production app see ![a-news-provider](https://github.com/rh-id/a-news-provider)

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
    void provides(Context context, ProviderRegistry providerRegistry, Provider provider){
        // Register your services/components here or other ProviderModule
        providerRegistry.register(IService.class, new ServiceImpl());
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
    }

    @Override
    void dispose(Context context, Provider provider){
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
        Provider provider = Provider.create(this, new RootModule());
        // example retrieve value
        IServiceA iServiceA = provider.get(IServiceA.class);
        MyPojo myPojo = provider.get(MyPojo.class);
        // if you need async get and perform operation
        provider.getAsyncAndDo(MyPojo.class, new ProviderAction<MyPojo>() {
            @Override
            public void onSuccess(MyPojo value) {
                // do something on main thread
            }

            @Override
            public void onError(Exception exception) {
                // handle error on main thread
            }
        });
    }
}
```
