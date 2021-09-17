package m.co.rh.id.aprovider;

import android.content.Context;
import android.os.Handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import m.co.rh.id.aprovider.test.IServiceA;
import m.co.rh.id.aprovider.test.IServiceA1;
import m.co.rh.id.aprovider.test.IServiceB;
import m.co.rh.id.aprovider.test.ModuleA;
import m.co.rh.id.aprovider.test.MyPojo;
import m.co.rh.id.aprovider.test.ServiceAChildImpl;
import m.co.rh.id.aprovider.test.ServiceAImpl;
import m.co.rh.id.aprovider.test.ServiceAParentImpl;
import m.co.rh.id.aprovider.test.ServiceBImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProviderUnitTest {

    @Mock
    Context mockContext;

    @Mock
    Handler mockHandler;

    @Test
    public void singleton_registrationAndGet() {
        ServiceAImpl serviceA = new ServiceAImpl();
        Provider testProvider = Provider.createProvider(mockContext, new ProviderModule() {
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

    @Test(expected = IllegalArgumentException.class)
    public void singleton_registerSameClass() {
        ServiceAImpl serviceA = new ServiceAImpl();
        Provider.createProvider(mockContext, new ProviderModule() {
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
        Provider testProvider = Provider.createProvider(mockContext,
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
        Provider.createProvider(mockContext,
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
        Provider testProvider = Provider.createProvider(mockContext,
                moduleA);
        assertFalse(moduleA.isDisposed);
        testProvider.dispose();
        assertTrue(moduleA.isDisposed);

        // test another scenario where module is registered on root module
        final ModuleA registerModuleA = new ModuleA();
        testProvider = Provider.createProvider(mockContext,
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
        Provider testProvider = Provider.createProvider(mockContext,
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
        Provider testProvider = Provider.createProvider(mockContext,
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
        Provider testProvider = Provider.createProvider(mockContext,
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
        testProvider = Provider.createProvider(mockContext,
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
        Provider testProvider = Provider.createProvider(mockContext,
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
        testProvider = Provider.createProvider(mockContext,
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
        Provider testProvider = Provider.createProvider(mockContext,
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
    public void getAsyncAndDo_registrationAndGet() throws InterruptedException {
        when(mockHandler.post(any())).then(
                invocation -> {
                    Runnable r = invocation.getArgument(0);
                    r.run();
                    return true;
                }
        );
        Provider testProvider = new DefaultProvider(mockContext,
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
                }, mockHandler, Executors.newSingleThreadExecutor());

        CountDownLatch testCountDownLatch = new CountDownLatch(1);
        AtomicReference<Object> atomicReference = new AtomicReference<>();
        testProvider.getAsyncAndDo(MyPojo.class, new ProviderAction<MyPojo>() {
            @Override
            public void onSuccess(MyPojo value) {
                atomicReference.set(value);
                testCountDownLatch.countDown();
            }

            @Override
            public void onError(Exception exception) {
                atomicReference.set(exception);
                testCountDownLatch.countDown();
            }
        });
        testCountDownLatch.await();
        assertTrue(atomicReference.get() instanceof MyPojo);
        MyPojo myPojo = (MyPojo) atomicReference.get();
        assertNotNull(myPojo);
        assertEquals(myPojo.getAge(), 99);
        assertEquals(myPojo.getName(), "Foo");

        // exception scenario where IServiceA not exist
        CountDownLatch testCountDownLatch2 = new CountDownLatch(1);
        AtomicReference<Object> atomicReference2 = new AtomicReference<>();
        testProvider.getAsyncAndDo(IServiceA.class, new ProviderAction<IServiceA>() {
            @Override
            public void onSuccess(IServiceA value) {
                atomicReference2.set(value);
                testCountDownLatch2.countDown();
            }

            @Override
            public void onError(Exception exception) {
                atomicReference2.set(exception);
                testCountDownLatch2.countDown();
            }
        });
        testCountDownLatch2.await();
        assertNotNull(atomicReference2.get());
        assertTrue(atomicReference2.get() instanceof NullPointerException);
    }
}