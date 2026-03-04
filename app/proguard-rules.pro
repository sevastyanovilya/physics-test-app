# Правила ProGuard для PhysicsTest
# Сохраняем Room-сущности
-keep class com.physics.tutor.data.db.entity.** { *; }
# Сохраняем ZXing
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
