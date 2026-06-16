# Spots ProGuard / R8 rules.
#
# The app has no reflection-based serialization and no network layer, so the
# defaults from proguard-android-optimize.txt are sufficient. Classes referenced
# from AndroidManifest.xml (the Activity, Service and BroadcastReceiver) are kept
# automatically by AGP.

# Keep Compose tooling-friendly line numbers for readable release stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
