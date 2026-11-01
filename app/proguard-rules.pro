-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ViewModels are created reflectively by the default lifecycle factory.
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Compose runtime metadata used by tooling and lambdas.
-dontwarn androidx.compose.**
