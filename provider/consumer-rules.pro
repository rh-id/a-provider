# a-provider consumer rules.
# Applied automatically to consumer apps via consumerProguardFiles.
#
# Intentionally EMPTY: the library performs no runtime reflection. All service
# lookups are done through Class literals, Class.isAssignableFrom/isInstance
# checks and Class.getName comparisons, all of which R8 handles correctly, so
# it is safe to shrink and obfuscate the library without keep rules.
# Services only need to stay reachable from the registration lambdas
# (ProviderModule.provides) as usual - the r8-smoke module in this repository
# proves that empirically on every release build.
