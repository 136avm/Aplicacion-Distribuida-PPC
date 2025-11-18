# Escenario distribuido en Red

Este proyecto consiste en la simulación de **tres servidores** y **un cliente** UDP. En este entorno, los servidores envían por broadcast mensajes sobre variables de control de calidad del aire como pueden ser la temperatura, humedad relativa...
El cliente a su vez, puede enviar **mensajes de control** para modificar el comportamiento de los servidores. Todos los mensajes van **serializados** con XML por defecto y con opción de cambiar a JSON.

---

# ▶️ Cómo Ejecutar el Proyecto

## 1️⃣ Clonar el repositorio

```bash
git clone https://github.com/136avm/Aplicacion-Distribuida-PPC
cd Aplicacion-Distribuida-PPC
```
## 2️⃣ Descargar la librería GSON

Descargar la librería para serializar/deserializar JSON desde [este enlace](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar).

## 3️⃣ Abrir proyecto en Eclipse y añadir la librería

1. Abrir el proyecto en Eclipse.
2. Click derecho sobre el proyecto, en la pestaña **Build Path** darle a la opción **Configure Build Path**.
3. En el apartado **Libraries**, darle click en **Classpath** y luego en el botón de **Add External JARs**.
4. Elegir el .jar donde lo hayamos guardado.
5. Ejecutar el `MainServidor.java` y el `ClienteGUI.java`.

---

# 💡 ACLARACIONES

Una vez ejecutado el cliente se pueden ver todos sus comandos escribiendo el comando `help` que nos mostrará la siguiente salida:
```
Comandos disponibles:
help -> muestra el panel de ayuda
mostrarServidores -> muestra los servidores disponibles
cambiarFrecuencia <nombreServidor> <frecuencia (ms)> -> cambia la frecuencia de entrega
stop <nombreServidor> -> detiene temporalmente el servidor
start <nombreServidor> -> reanuda el servidor detenido
stopAll -> detiene temporalmente todos los servidores
startAll -> reanuda todos los servidores detenidos
formato <nombreServidor> <XML/JSON> -> cambia el formato de envio del servidor
formatoAll <XML/JSON> -> cambia el formato de envio de los servidores
clear -> limpia la pantalla
salir -> cierra el programa
```

---


## ⚠️ IMPORTANTE

- Este proyecto está diseñado **exclusivamente para la práctica académica de la asignatura**, por lo que su estructura, configuraciones y variables están pensadas para un **entorno controlado**.   
- **No se incluye ninguna licencia**, por lo que **no está permitido copiar, distribuir o reutilizar este proyecto** sin autorización expresa.
