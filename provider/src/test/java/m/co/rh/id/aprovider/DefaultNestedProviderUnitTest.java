package m.co.rh.id.aprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

import android.content.Context;
import android.os.Handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import m.co.rh.id.aprovider.test.IServiceA;
import m.co.rh.id.aprovider.test.IServiceA1;
import m.co.rh.id.aprovider.test.IServiceB;
import m.co.rh.id.aprovider.test.ModuleA;
import m.co.rh.id.aprovider.test.MyPojo;
import m.co.rh.id.aprovider.test.ServiceAChildImpl;
import m.co.rh.id.aprovider.test.ServiceAImpl;
import m.co.rh.id.aprovider.test.ServiceAParentImpl;
import m.co.rh.id.aprovider.test.ServiceBImpl;
import m.co.rh.id.aprovider.test.disposable.DisposableRegisterAsyncService;
import m.co.rh.id.aprovider.test.disposable.DisposableRegisterFactoryService;
import m.co.rh.id.aprovider.test.disposable.DisposableRegisterLazyService;
import m.co.rh.id.aprovider.test.disposable.DisposableRegisterService;

@RunWith(MockitoJUnitRunner.class)
public class DefaultNestedProviderUnitTest {

    @Mock
    Context mockContext;

    @Mock
    Handler mockHandler;

    @Test
    public void getProviderRegistryUsingGet() {
        ServiceAImpl serviceA = new ServiceAImpl();
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        // leave blank
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        assertSame(testProvider.get(ProviderRegistry.class), testProvider);
        assertSame(testProvider.lazyGet(ProviderRegistry.class).get(), testProvider);
        assertSame(testProvider.tryLazyGet(ProviderRegistry.class).get(), testProvider);
    }

    @Test
    public void singleton_registrationAndExactGet() {
        // testing real case scenario where you need both ExecutorService & ScheduledExecutorService
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.register(ScheduledExecutorService.class, scheduledExecutorService);
                        providerRegistry.register(ExecutorService.class, executorService);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        assertSame(testProvider.get(ExecutorService.class), executorService);
        assertSame(testProvider.get(ScheduledExecutorService.class), scheduledExecutorService);
        assertSame(testProvider.tryGet(ExecutorService.class), executorService);
        assertSame(testProvider.tryGet(ScheduledExecutorService.class), scheduledExecutorService);
        assertSame(testProvider.lazyGet(ExecutorService.class).get(), executorService);
        assertSame(testProvider.lazyGet(ScheduledExecutorService.class).get(), scheduledExecutorService);
        assertSame(testProvider.tryLazyGet(ExecutorService.class).get(), executorService);
        assertSame(testProvider.tryLazyGet(ScheduledExecutorService.class).get(), scheduledExecutorService);
    }

    @Test
    public void singleton_registrationAndGet() {
        ServiceAImpl serviceA = new ServiceAImpl();
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.register(IServiceA.class, serviceA);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        IServiceA serviceAFromProvider = testProvider.get(IServiceA.class);
        assertSame(serviceAFromProvider, serviceA);

        // if the implementation implements another interface, allow to get it
        IServiceA1 serviceA1FromProvider = testProvider.get(IServiceA1.class);
        assertSame(serviceA1FromProvider, serviceA);

        // allow to get using the actual implementation class or its parent class
        ServiceAImpl serviceAImplFromProvider = testProvider.get(ServiceAImpl.class);
        assertSame(serviceAImplFromProvider, serviceA);
        ServiceAParentImpl serviceAParentImplFromProvider = testProvider.get(ServiceAParentImpl.class);
        assertSame(serviceAParentImplFromProvider, serviceA);

        // test to ensure ServiceAChildImpl not found since the implementation is the parent of ServiceAChildImpl
        ServiceAChildImpl serviceAChildImplFromProvider = testProvider.tryGet(ServiceAChildImpl.class);
        assertNull(serviceAChildImplFromProvider);
    }

    @Test
    public void singleton_registrationAndLazyGet() {
        ServiceAImpl serviceA = new ServiceAImpl();
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.register(IServiceA.class, serviceA);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        IServiceA serviceAFromProvider = testProvider.lazyGet(IServiceA.class).get();
        assertSame(serviceAFromProvider, serviceA);

        // if the implementation implements another interface, allow to get it
        IServiceA1 serviceA1FromProvider = testProvider.lazyGet(IServiceA1.class).get();
        assertSame(serviceA1FromProvider, serviceA);

        // allow to get using the actual implementation class or its parent class
        ServiceAImpl serviceAImplFromProvider = testProvider.lazyGet(ServiceAImpl.class).get();
        assertSame(serviceAImplFromProvider, serviceA);
        ServiceAParentImpl serviceAParentImplFromProvider = testProvider.lazyGet(ServiceAParentImpl.class).get();
        assertSame(serviceAParentImplFromProvider, serviceA);

        // service B is not registered, should throw null pointer upon invocation
        assertThrows(NullPointerException.class, () -> testProvider.lazyGet(IServiceB.class));

        // test to ensure ServiceAChildImpl not found since the implementation is the parent of ServiceAChildImpl
        ServiceAChildImpl serviceAChildImplFromProvider = testProvider.tryGet(ServiceAChildImpl.class);
        assertNull(serviceAChildImplFromProvider);
    }

    @Test
    public void singleton_registrationAndLazyGetAndTryLazyGetDifference() {
        ServiceAImpl serviceA = new ServiceAImpl();
        DefaultNestedProvider testProvider = (DefaultNestedProvider)
                Provider.createNestedProvider("test",
                        null, mockContext, new ProviderModule() {
                            @Override
                            public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                                // leave blank
                            }

                            @Override
                            public void dispose(Context context, Provider provider) {
                                // nothing to dispose
                            }
                        });

        assertThrows(NullPointerException.class, () -> testProvider.get(IServiceA.class));
        assertThrows(NullPointerException.class, () -> testProvider.lazyGet(IServiceA.class));
        ProviderValue<IServiceA> tryLazyGet = testProvider.tryLazyGet(IServiceA.class);
        assertNotNull(tryLazyGet);

        // the value is null but not throwing null pointer exception
        assertNull(tryLazyGet.get());

        ServiceAImpl serviceA1 = new ServiceAImpl();
        testProvider.register(IServiceA.class, serviceA);

        // after register the get should contain value
        assertSame(tryLazyGet.get(), serviceA);

        assertSame(testProvider.get(IServiceA1.class), serviceA);
        assertSame(testProvider.lazyGet(IServiceA1.class).get(), serviceA);
        assertSame(testProvider.tryLazyGet(IServiceA1.class).get(), serviceA);

    }

    @Test(expected = IllegalArgumentException.class)
    public void singleton_registerSameClass() {
        ServiceAImpl serviceA = new ServiceAImpl();
        Provider.createNestedProvider("test",
                null, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.register(IServiceA.class, serviceA);
                        providerRegistry.register(IServiceA.class, new ServiceAImpl());
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
    }

    @Test
    public void module_registrationAndGet() {
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerModule(new ModuleA());
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {

                    }
                });

        IServiceA serviceAFromProvider = testProvider.get(IServiceA.class);
        assertNotNull(serviceAFromProvider);

        // if the implementation implements another interface, allow to get it
        IServiceA1 serviceA1FromProvider = testProvider.get(IServiceA1.class);
        assertNotNull(serviceA1FromProvider);

        // allow to get using the actual implementation class or its parent class
        ServiceAImpl serviceAImplFromProvider = testProvider.get(ServiceAImpl.class);
        assertNotNull(serviceAImplFromProvider);
        ServiceAParentImpl serviceAParentImplFromProvider = testProvider.get(ServiceAParentImpl.class);
        assertNotNull(serviceAParentImplFromProvider);

        // test to ensure ServiceAChildImpl not found since the implementation is the parent of ServiceAChildImpl
        ServiceAChildImpl serviceAChildImplFromProvider = testProvider.tryGet(ServiceAChildImpl.class);
        assertNull(serviceAChildImplFromProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void module_registrationMultipleSameModule() {
        // the exception was caused by duplicate services, NOT cause by multiple module instances
        Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerModule(new ModuleA());
                        providerRegistry.registerModule(new ModuleA());
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
    }

    @Test
    public void module_registrationAndDispose() {
        ModuleA moduleA = new ModuleA();
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                moduleA);
        assertFalse(moduleA.isDisposed);
        testProvider.dispose();
        assertTrue(moduleA.isDisposed);

        // test another scenario where module is registered on root module
        final ModuleA registerModuleA = new ModuleA();
        testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerModule(registerModuleA);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
        assertFalse(registerModuleA.isDisposed);
        testProvider.dispose();
        assertTrue(registerModuleA.isDisposed);
    }

    @Test
    public void lazySingleton_registrationAndGet() {
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerLazy(
                                IServiceA.class, ServiceAImpl::new);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
        IServiceA serviceAFromProvider1 = testProvider.get(IServiceA.class);
        assertNotNull(serviceAFromProvider1);
        IServiceA serviceAFromProvider2 = testProvider.get(IServiceA.class);
        assertSame(serviceAFromProvider1, serviceAFromProvider2);

        // This is lazy singleton, unable to check if implementation class implements other interface
        // since the singleton might not be initialized yet
        IServiceA1 serviceA1FromProvider = testProvider.tryGet(IServiceA1.class);
        assertNull(serviceA1FromProvider);
        ServiceAImpl serviceAImplFromProvider = testProvider.tryGet(ServiceAImpl.class);
        assertNull(serviceAImplFromProvider);
        ServiceAParentImpl serviceAParentImplFromProvider = testProvider.tryGet(ServiceAParentImpl.class);
        assertNull(serviceAParentImplFromProvider);
        ServiceAChildImpl serviceAChildImplFromProvider = testProvider.tryGet(ServiceAChildImpl.class);
        assertNull(serviceAChildImplFromProvider);
    }

    /**
     * Special test if decide to have global ExecutorService in project
     */
    @Test
    public void lazySingleton_executorService_registrationAndGet() {
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerLazy(
                                ExecutorService.class, Executors::newSingleThreadExecutor);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
        ExecutorService executorServiceFromProvider1 = testProvider.get(ExecutorService.class);
        assertNotNull(executorServiceFromProvider1);
        // ExecutorService extends Executor, must be able to get instance if using parent class
        Executor executorFromProvider1 = testProvider.get(Executor.class);
        assertSame(executorServiceFromProvider1, executorFromProvider1);
    }

    @Test
    public void lazySingleton_dependencyInversionPattern() {
        /*
         There are 2 interface IServiceA and IServiceB with its respective implementations ServiceAImpl and ServiceBImpl.
         ServiceBImpl depends on IServiceA NOT the implementation.
         The provider support this inversion pattern by using LazySingletonProviderRegister
         */
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerLazy(
                                IServiceB.class,
                                () -> new ServiceBImpl(provider.get(IServiceA.class)));
                        providerRegistry.registerLazy(
                                IServiceA.class, ServiceAImpl::new);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
        // try to get IServiceB first to check if it works
        IServiceB serviceBFromProvider1 = testProvider.get(IServiceB.class);
        assertNotNull(serviceBFromProvider1);
        IServiceB serviceBFromProvider2 = testProvider.get(IServiceB.class);
        assertSame(serviceBFromProvider1, serviceBFromProvider2);

        // then try to get IServiceA
        IServiceA serviceAFromProvider1 = testProvider.get(IServiceA.class);
        assertNotNull(serviceAFromProvider1);
        IServiceA serviceAFromProvider2 = testProvider.get(IServiceA.class);
        assertSame(serviceAFromProvider1, serviceAFromProvider2);

        // test if both services ar same instance
        assertSame(serviceBFromProvider1.getIServiceA(), serviceAFromProvider1);
        assertSame(serviceBFromProvider2.getIServiceA(), serviceAFromProvider1);

        // Another similar scenario, only get IServiceA first then get IServiceB
        testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerLazy(
                                IServiceB.class,
                                () -> new ServiceBImpl(provider.get(IServiceA.class)));
                        providerRegistry.registerLazy(
                                IServiceA.class, ServiceAImpl::new);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
        serviceAFromProvider1 = testProvider.get(IServiceA.class);
        assertNotNull(serviceAFromProvider1);
        serviceAFromProvider2 = testProvider.get(IServiceA.class);
        assertSame(serviceAFromProvider1, serviceAFromProvider2);

        serviceBFromProvider1 = testProvider.get(IServiceB.class);
        assertNotNull(serviceBFromProvider1);
        serviceBFromProvider2 = testProvider.get(IServiceB.class);
        assertSame(serviceBFromProvider1, serviceBFromProvider2);

        // test if both services ar same instance
        assertSame(serviceBFromProvider1.getIServiceA(), serviceAFromProvider1);
        assertSame(serviceBFromProvider2.getIServiceA(), serviceAFromProvider1);
    }

    @Test
    public void lazyFuture_dependencyInversionPattern() {
        /*
         There are 2 interface IServiceA and IServiceB with its respective implementations ServiceAImpl and ServiceBImpl.
         ServiceBImpl depends on IServiceA NOT the implementation.
         The provider support this inversion pattern by using LazySingletonProviderRegister
         */
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerAsync(
                                IServiceB.class,
                                () -> new ServiceBImpl(provider.get(IServiceA.class)));
                        providerRegistry.registerAsync(
                                IServiceA.class, ServiceAImpl::new);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
        // try to get IServiceB first to check if it works
        IServiceB serviceBFromProvider1 = testProvider.get(IServiceB.class);
        assertNotNull(serviceBFromProvider1);
        IServiceB serviceBFromProvider2 = testProvider.get(IServiceB.class);
        assertSame(serviceBFromProvider1, serviceBFromProvider2);

        // then try to get IServiceA
        IServiceA serviceAFromProvider1 = testProvider.get(IServiceA.class);
        assertNotNull(serviceAFromProvider1);
        IServiceA serviceAFromProvider2 = testProvider.get(IServiceA.class);
        assertSame(serviceAFromProvider1, serviceAFromProvider2);

        // test if both services ar same instance
        assertSame(serviceBFromProvider1.getIServiceA(), serviceAFromProvider1);
        assertSame(serviceBFromProvider2.getIServiceA(), serviceAFromProvider1);

        // Another similar scenario, only get IServiceA first then get IServiceB
        testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerLazy(
                                IServiceB.class,
                                () -> new ServiceBImpl(provider.get(IServiceA.class)));
                        providerRegistry.registerLazy(
                                IServiceA.class, ServiceAImpl::new);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
        serviceAFromProvider1 = testProvider.get(IServiceA.class);
        assertNotNull(serviceAFromProvider1);
        serviceAFromProvider2 = testProvider.get(IServiceA.class);
        assertSame(serviceAFromProvider1, serviceAFromProvider2);

        serviceBFromProvider1 = testProvider.get(IServiceB.class);
        assertNotNull(serviceBFromProvider1);
        serviceBFromProvider2 = testProvider.get(IServiceB.class);
        assertSame(serviceBFromProvider1, serviceBFromProvider2);

        // test if both services ar same instance
        assertSame(serviceBFromProvider1.getIServiceA(), serviceAFromProvider1);
        assertSame(serviceBFromProvider2.getIServiceA(), serviceAFromProvider1);
    }

    @Test
    public void factory_registrationAndGet() {
        Provider testProvider = Provider.createNestedProvider("test",
                null, mockContext,
                new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerFactory(MyPojo.class, () -> {
                            MyPojo myPojo = new MyPojo();
                            myPojo.setAge(99);
                            myPojo.setName("Foo");
                            return myPojo;
                        });
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // leave blank
                    }
                });
        MyPojo myPojo1 = testProvider.get(MyPojo.class);
        assertNotNull(myPojo1);
        assertEquals(myPojo1.getAge(), 99);
        assertEquals(myPojo1.getName(), "Foo");

        MyPojo myPojo2 = testProvider.get(MyPojo.class);
        assertNotNull(myPojo2);
        assertEquals(myPojo2.getAge(), myPojo1.getAge());
        assertEquals(myPojo2.getName(), myPojo1.getName());

        // because same factory,
        // both have same value and might be equals but not same instance
        assertNotSame(myPojo1, myPojo2);
    }

    @Test
    public void disposableComponents_registerAndDisposeWithoutGet() throws InterruptedException {
        DisposableRegisterService registerService = Mockito.mock(DisposableRegisterService.class);
        DisposableRegisterAsyncService registerAsyncService = Mockito.mock(DisposableRegisterAsyncService.class);
        DisposableRegisterFactoryService registerFactoryService = Mockito.mock(DisposableRegisterFactoryService.class);
        DisposableRegisterLazyService registerLazyService = Mockito.mock(DisposableRegisterLazyService.class);
        ProviderModule providerModule = new ProviderModule() {
            @Override
            public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                providerRegistry.register(
                        DisposableRegisterService.class,
                        registerService
                );
                providerRegistry.registerAsync(DisposableRegisterAsyncService.class,
                        () -> registerAsyncService
                );
                // Register factory always return same instance for testing purposes
                // dispose should not be called since "get" is not called
                providerRegistry.registerFactory(
                        DisposableRegisterFactoryService.class,
                        () -> registerFactoryService
                );
                providerRegistry.registerLazy(
                        DisposableRegisterLazyService.class,
                        () -> registerLazyService
                );
            }

            @Override
            public void dispose(Context context, Provider provider) {
                // leave blank
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        threadPoolExecutor.prestartAllCoreThreads();
        Provider testProvider = new DefaultNestedProvider("test",
                null, mockContext,
                providerModule, threadPoolExecutor);
        testProvider.dispose();
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS);

        Mockito.verify(registerService, Mockito.times(1))
                .dispose(eq(mockContext));
        Mockito.verify(registerAsyncService, Mockito.times(1))
                .dispose(eq(mockContext));
        Mockito.verify(registerFactoryService, Mockito.never())
                .dispose(eq(mockContext));
        // Since we didn't try to "Provider.get"
        // it should not be disposed since the instance was never instantiated
        Mockito.verify(registerLazyService, Mockito.never())
                .dispose(eq(mockContext));
    }

    @Test
    public void disposableComponents_registerGetAndThenDispose() throws InterruptedException {
        DisposableRegisterService registerService = Mockito.mock(DisposableRegisterService.class);
        DisposableRegisterAsyncService registerAsyncService = Mockito.mock(DisposableRegisterAsyncService.class);
        DisposableRegisterLazyService registerLazyService = Mockito.mock(DisposableRegisterLazyService.class);
        ProviderModule providerModule = new ProviderModule() {
            @Override
            public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                providerRegistry.register(
                        DisposableRegisterService.class,
                        registerService
                );
                providerRegistry.registerAsync(DisposableRegisterAsyncService.class,
                        () -> registerAsyncService
                );
                providerRegistry.registerFactory(
                        DisposableRegisterFactoryService.class,
                        () -> Mockito.mock(DisposableRegisterFactoryService.class)
                );
                providerRegistry.registerLazy(
                        DisposableRegisterLazyService.class,
                        () -> registerLazyService
                );
            }

            @Override
            public void dispose(Context context, Provider provider) {
                // leave blank
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        threadPoolExecutor.prestartAllCoreThreads();
        Provider testProvider = new DefaultNestedProvider("test",
                null, mockContext,
                providerModule, threadPoolExecutor);
        testProvider.get(DisposableRegisterService.class);
        testProvider.get(DisposableRegisterAsyncService.class);
        testProvider.get(DisposableRegisterLazyService.class);

        // test factory before dispose
        DisposableRegisterFactoryService disposableRegisterFactoryService1 =
                testProvider.get(DisposableRegisterFactoryService.class);
        Mockito.verify(disposableRegisterFactoryService1, Mockito.never()).dispose(mockContext);

        DisposableRegisterFactoryService disposableRegisterFactoryService2 =
                testProvider.get(DisposableRegisterFactoryService.class);
        assertNotSame(disposableRegisterFactoryService1, disposableRegisterFactoryService2);

        // after new instance is created, previous instance should trigger dispose
        Mockito.verify(disposableRegisterFactoryService1, Mockito.times(1))
                .dispose(mockContext);
        Mockito.verify(disposableRegisterFactoryService2, Mockito.never())
                .dispose(mockContext);

        testProvider.dispose();
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(5, TimeUnit.SECONDS);

        Mockito.verify(registerService, Mockito.times(1))
                .dispose(eq(mockContext));
        Mockito.verify(registerAsyncService, Mockito.times(1))
                .dispose(eq(mockContext));
        Mockito.verify(registerLazyService, Mockito.times(1))
                .dispose(eq(mockContext));

        // the previous instance still same, shouldn't trigger dispose again
        Mockito.verify(disposableRegisterFactoryService1, Mockito.times(1))
                .dispose(mockContext);
        // latest instance will trigger dispose
        Mockito.verify(disposableRegisterFactoryService2, Mockito.times(1))
                .dispose(mockContext);
    }

    // -------------BELOW HERE ARE TEST SPECIFIC TO NESTED PROVIDER CASES WITH PARENT------------------
    @Test
    public void singletonParent_registrationAndExactGet() {
        // testing real case scenario where you need both ExecutorService & ScheduledExecutorService
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Provider rootProvider = Provider.createProvider(mockContext, new ProviderModule() {
            @Override
            public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                providerRegistry.register(ScheduledExecutorService.class, scheduledExecutorService);
            }

            @Override
            public void dispose(Context context, Provider provider) {
                // nothing to dispose
            }
        });
        Provider testProvider = Provider.createNestedProvider("test",
                rootProvider, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.register(ExecutorService.class, executorService);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        assertSame(testProvider.get(ExecutorService.class), executorService);
        assertSame(testProvider.get(ScheduledExecutorService.class), scheduledExecutorService);
        assertSame(testProvider.tryGet(ExecutorService.class), executorService);
        assertSame(testProvider.tryGet(ScheduledExecutorService.class), scheduledExecutorService);
        assertSame(testProvider.lazyGet(ExecutorService.class).get(), executorService);
        assertSame(testProvider.lazyGet(ScheduledExecutorService.class).get(), scheduledExecutorService);
        assertSame(testProvider.tryLazyGet(ExecutorService.class).get(), executorService);
        assertSame(testProvider.tryLazyGet(ScheduledExecutorService.class).get(), scheduledExecutorService);
    }

    @Test
    public void factoryParent_registrationAndExactGet() {
        // testing real case scenario where you need both ExecutorService & ScheduledExecutorService
        Provider rootProvider = Provider.createProvider(mockContext, new ProviderModule() {
            @Override
            public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                providerRegistry.registerFactory(IServiceA.class,
                        () -> Mockito.mock(IServiceA.class));
            }

            @Override
            public void dispose(Context context, Provider provider) {
                // nothing to dispose
            }
        });
        Provider testProvider = Provider.createNestedProvider("test",
                rootProvider, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerFactory(IServiceB.class,
                                () -> Mockito.mock(IServiceB.class));
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        IServiceA serviceA1 = testProvider.get(IServiceA.class);
        IServiceA serviceA2 = testProvider.get(IServiceA.class);
        assertNotSame(serviceA1, serviceA2);
        IServiceB serviceB1 = testProvider.get(IServiceB.class);
        IServiceB serviceB2 = testProvider.get(IServiceB.class);
        assertNotSame(serviceB1, serviceB2);
    }

    @Test
    public void singletonParent_registrationAndExactGet_withParentDependency() {
        Provider rootProvider = Provider.createProvider(mockContext, new ProviderModule() {
            @Override
            public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                providerRegistry.registerLazy(IServiceA.class, ServiceAImpl::new);
            }

            @Override
            public void dispose(Context context, Provider provider) {
                // nothing to dispose
            }
        });
        Provider testProvider = Provider.createNestedProvider("test",
                rootProvider, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerLazy(IServiceB.class,
                                () -> new ServiceBImpl(provider.get(IServiceA.class)));
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        IServiceB serviceB = testProvider.get(IServiceB.class);
        IServiceA serviceA = testProvider.get(IServiceA.class);
        assertNotNull(serviceA);
        assertNotNull(serviceB);
    }

    @Test
    public void singletonParent_registrationAndExactGet_withParentDependencyAndParentNestedProvider() {
        Provider rootProvider = Provider.createNestedProvider("test", null,
                mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerLazy(IServiceA.class, ServiceAImpl::new);
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });
        Provider testProvider = Provider.createNestedProvider("test",
                rootProvider, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerLazy(IServiceB.class,
                                () -> new ServiceBImpl(provider.get(IServiceA.class)));
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        IServiceB serviceB = testProvider.get(IServiceB.class);
        IServiceA serviceA = testProvider.get(IServiceA.class);
        assertNotNull(serviceA);
        assertNotNull(serviceB);
    }

    @Test
    public void singletonParent_registerAsyncAndExactGet_withParentDependency() {
        Provider rootProvider = Provider.createProvider(mockContext, new ProviderModule() {
            @Override
            public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                providerRegistry.registerAsync(IServiceA.class, ServiceAImpl::new);
            }

            @Override
            public void dispose(Context context, Provider provider) {
                // nothing to dispose
            }
        });
        Provider testProvider = Provider.createNestedProvider("test",
                rootProvider, mockContext, new ProviderModule() {
                    @Override
                    public void provides(Context context, ProviderRegistry providerRegistry, Provider provider) {
                        providerRegistry.registerAsync(IServiceB.class,
                                () -> new ServiceBImpl(provider.get(IServiceA.class)));
                    }

                    @Override
                    public void dispose(Context context, Provider provider) {
                        // nothing to dispose
                    }
                });

        IServiceB serviceB = testProvider.get(IServiceB.class);
        IServiceA serviceA = testProvider.get(IServiceA.class);
        assertNotNull(serviceA);
        assertNotNull(serviceB);
    }
}