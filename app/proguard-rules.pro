-repackageclasses "qmhelper"

-keep class me.kofua.qmhelper.XposedInit {
    <init>();
}

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
  <fields>;
}

-keepclasseswithmembers class me.kofua.qmhelper.utils.DexHelper {
 native <methods>;
 long token;
 java.lang.ClassLoader classLoader;
}

-keepattributes RuntimeVisible*Annotations

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers class me.kofua.qmhelper.** implements java.io.Externalizable {
    static final long serialVersionUID;
}

#-keepclassmembers class me.kofua.qmhelper.MainActivity$Companion {
#    boolean isModuleActive();
#}

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}

-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

-allowaccessmodification
-overloadaggressively
