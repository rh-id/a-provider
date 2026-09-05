package m.co.rh.id.aprovider.r8smoke;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Runs against the MINIFIED release APK (testBuildType 'release') to prove the
 * a-provider Service Locator survives R8 shrinking/obfuscation with its
 * intentionally EMPTY consumer-rules.pro.
 *
 * IMPORTANT: this test references ONLY SmokeResult, plus MainActivity.class
 * for ActivityScenario.launch (a manifest-kept entry point that keeps no
 * library classes). Referencing any other provider library class or smoke
 * service impl from the test APK would keep them through the app APK's R8 run
 * and falsify the experiment.
 */
@RunWith(AndroidJUnit4.class)
public class R8SmokeTest {

    private static final long AWAIT_TIMEOUT_SECONDS = 60;

    @Test
    public void serviceLocatorSurvivesR8Minification() throws Exception {
        ActivityScenario.launch(MainActivity.class);
        SmokeResult.awaitDone(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertNull("Unexpected exception(s) during the minified smoke run:\n"
                + SmokeResult.error, SmokeResult.error);
        assertTrue("Smoke suite did not finish within the timeout "
                + "(async poll stuck? registerAsync broken under R8?)", SmokeResult.done);

        // Scenario 1: exact-type lookup + singleton identity.
        assertTrue("exact-type register/get or singleton identity broke under R8 "
                + "(class merging swapping instances?)", SmokeResult.singletonIdentityOk);
        // Scenario 2: Class.isAssignableFrom interface lookup.
        assertTrue("interface lookup via Class.isAssignableFrom broke under R8 "
                + "(renamed type hierarchy mismatch?)",
                SmokeResult.interfaceLookupViaIsAssignableFromOk);
        // Scenario 3: Class.isInstance(value) lookup.
        assertTrue("lookup of an unregistered interface via Class.isInstance(value) broke "
                + "under R8 (instance-of checks remapped incorrectly?)",
                SmokeResult.instanceLookupViaIsInstanceOk);
        // Scenario 4: lazyGet / tryLazyGet.
        assertTrue("lazyGet did not return a working, deferred ProviderValue under R8 "
                + "(registerLazy became eager or its lambda was dropped)",
                SmokeResult.lazyGetProviderValueOk);
        assertTrue("tryLazyGet for an unregistered type did not yield null without throwing "
                + "under R8", SmokeResult.tryLazyGetUnregisteredReturnsNullOk);
        // Scenario 5: tryGet miss.
        assertTrue("tryGet for an unregistered type did not return null under R8",
                SmokeResult.tryGetUnregisteredReturnsNullOk);
        // Scenario 6: parent-class lookup feature.
        assertTrue("parent-class lookup (get on an unregistered child class whose parent "
                + "is registered) broke under R8 (Class.getName comparisons broken?)",
                SmokeResult.parentClassLookupOk);
        // Scenario 7: registerAsync.
        assertTrue("registerAsync service never became resolvable in the background poll "
                + "(executor submit or lambda stripped under R8?)",
                SmokeResult.asyncServiceResolvedOk);
        // Scenario 8: factory.
        assertTrue("registerFactory did not produce distinct instances under R8",
                SmokeResult.factoryDistinctInstancesOk);
        assertTrue("the previous factory instance was not disposed when replaced under R8 "
                + "(ProviderDisposable routing broken?)",
                SmokeResult.factoryPreviousInstanceDisposedOk);
        // Scenario 9: pool.
        assertTrue("registerPool did not produce distinct instances under R8",
                SmokeResult.poolDistinctInstancesOk);
        // Scenario 10/11: duplicate registration + skipSameType.
        assertTrue("duplicate registration did not throw IllegalArgumentException under R8 "
                + "(ProviderRegister equality/dedup broken?)",
                SmokeResult.duplicateRegistrationThrowsOk);
        assertTrue("setSkipSameType(true) did not suppress the duplicate registration "
                + "exception under R8", SmokeResult.skipSameTypeAllowsDuplicateOk);
        // Scenario 12: nested provider.
        assertTrue("nested provider did not traverse to its parent for parent-only types "
                + "under R8", SmokeResult.nestedProviderParentTraversalOk);
        assertTrue("nested provider lazyGet did not traverse to its parent under R8",
                SmokeResult.nestedProviderLazyTraversalOk);
        // Scenario 13: dispose semantics.
        assertTrue("ProviderDisposable.dispose(Context) was not invoked on provider.dispose() "
                + "under R8", SmokeResult.singletonDisposeCallbackRecorded);
        assertTrue("ProviderModule.dispose(Provider) was not invoked on provider.dispose() "
                + "under R8", SmokeResult.moduleDisposeCallbackRecorded);
        assertEquals("expected BOTH pool instances to receive exactly one dispose callback on "
                        + "provider.dispose() under R8",
                2, SmokeResult.POOL_DISPOSE_CALLBACK_COUNT.get());
        assertTrue("register after dispose() did not throw IllegalStateException under R8",
                SmokeResult.registerAfterDisposeThrowsOk);
        // Scenario 14: context.
        assertTrue("getContext() did not return the application context under R8",
                SmokeResult.contextOk);
    }
}
