# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-keep class me.pqpo.smartcropperlib.**{*;}
#-keep class android.support.v7.widget.** { *; }
-keep public class com.sicmagroup.tondi.Utilisateur extends com.orm.SugarRecord{*;}
-keep public class com.sicmagroup.tondi.Utilisateur extends com.orm.SugarApp{*;}

-keep public class com.sicmagroup.tondi.Versement extends com.orm.SugarRecord{*;}
-keep public class com.sicmagroup.tondi.Versement extends com.orm.SugarApp{*;}

-keep public class com.sicmagroup.tondi.Tontine extends com.orm.SugarRecord{*;}
-keep public class com.sicmagroup.tondi.Tontine extends com.orm.SugarApp{*;}

-keep public class com.sicmagroup.tondi.Synchronisation extends com.orm.SugarRecord{*;}
-keep public class com.sicmagroup.tondi.Synchronisation extends com.orm.SugarApp{*;}

-keep public class com.sicmagroup.tondi.Sim extends com.orm.SugarRecord{*;}
-keep public class com.sicmagroup.tondi.Sim extends com.orm.SugarApp{*;}

-keep public class com.sicmagroup.tondi.Retrait extends com.orm.SugarRecord{*;}
-keep public class com.sicmagroup.tondi.Retrait extends com.orm.SugarApp{*;}


-keep public class com.sicmagroup.tondi.Plainte extends com.orm.SugarRecord{*;}
-keep public class com.sicmagroup.tondi.Plainte extends com.orm.SugarApp{*;}

-keep public class com.sicmagroup.tondi.Cotis_Auto extends com.orm.SugarRecord{*;}
-keep public class com.sicmagroup.tondi.Cotis_Auto extends com.orm.SugarApp{*;}