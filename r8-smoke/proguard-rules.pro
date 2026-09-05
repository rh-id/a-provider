# Rules for the app (release) APK only. The library consumer-rules.pro under test is merged in
# automatically from provider/consumer-rules.pro via consumerProguardFiles.
#
# This app intentionally does NOT blanket-keep the library (m.co.rh.id.aprovider.**):
# the whole point of this harness is to let R8 shrink/obfuscate the library exactly like
# it would in a real consumer app, with only the shipped consumer rules protecting it -
# a-provider performs no reflection, so the consumer rules are intentionally empty and
# nothing here needs to keep the library.

# SmokeResult is the volatile result holder read from the test APK
# (R8SmokeTest asserts its flags). R8 only sees writes from the app side, so
# without an explicit keep the unused-read static fields could be removed,
# which would surface as NoSuchFieldError in the test APK after remapping.
-keep class m.co.rh.id.aprovider.r8smoke.SmokeResult { *; }

# NOTE: deliberately NO kotlin.** / androidx.tracing.Trace keeps here (unlike
# the a-navigator harness, where appcompat pulls kotlin/tracing into APP scope
# and the test-APK classes deduped against them need navigator-style identity
# keeps). This harness's app APK contains ZERO kotlin.*/androidx.tracing
# classes (verified in mapping.txt), and the test APK carries its own
# kotlin-stdlib from the androidTest-scope deps, so it is self-contained. IF
# app-scope kotlin or androidx.tracing deps are ever added here (e.g.
# appcompat), re-add the navigator-style keeps - see a-navigator
# r8-smoke/proguard-rules.pro.

