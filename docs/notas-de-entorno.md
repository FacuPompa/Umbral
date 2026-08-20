# Nota de entorno: rutas con caracteres no ASCII

El 19 de agosto de 2026, el comando `gradlew.bat build` del backend falló en la ruta original:

```text
E:\Carpetas\Programación\programacion solo\full\umbral\backend
```

El error ocurrió al ejecutar el test generado y fue:

```text
java.lang.ClassNotFoundException: com.umbral.UmbralApplicationTests
```

La clase de prueba sí había sido compilada. Se copió el mismo esqueleto, sin la carpeta `build/`, a una ruta temporal con caracteres ASCII y allí el mismo comando terminó correctamente:

```text
BUILD SUCCESSFUL
```

La única diferencia relevante fue la ruta. Por eso la evidencia apunta a un problema de classpath de Gradle en Windows al atravesar `Programación` (con `ó`).

No se eliminó el test ni se agregó configuración de Gradle como workaround. Esta nota registra el diagnóstico para decidir más adelante si el proyecto se mantiene en una ruta ASCII o si se evalúa una solución específica y verificable.
