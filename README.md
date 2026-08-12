# NVIDIA LLM Chat

Una aplicación Android premium, moderna y de alto rendimiento diseñada para interactuar con la **NVIDIA NIM API** (Chat Completions). Construida íntegramente con **Jetpack Compose**, ofrece una experiencia de usuario fluida, inmersiva y altamente configurable.

## 🚀 Características Principales

### 📸 Capacidades Multimodales
- **Soporte de Imágenes**: Envía fotos directamente al modelo para análisis visual, redimensionadas automáticamente para optimizar el rendimiento.
- **Procesamiento de PDF**: Adjunta documentos PDF; la app convierte automáticamente la primera página en imagen para que los modelos de visión puedan analizarla.
- **Archivos de Texto**: Importa archivos `.txt`, `.log` o código fuente directamente al chat como contexto.

### 🧠 Inteligencia y Persistencia
- **Historial Local (Room)**: Tus conversaciones se guardan automáticamente en una base de datos local cifrada, permitiendo retomar chats en cualquier momento.
- **Selector de Modelos con Buscador**: Explora el catálogo completo de modelos de NVIDIA (Llama 3.2, VILA, GLM, etc.) con filtrado en tiempo real.
- **Streaming de Respuestas**: Visualiza la generación de texto palabra por palabra con una latencia mínima.

### 🎨 Diseño y Personalización
- **Estética NVIDIA**: Interfaz forzada en modo oscuro (OLED friendly) con la identidad visual verde neón característica de NVIDIA.
- **Fondo de Partículas (Experimental)**: Una red neuronal animada de alto rendimiento que reacciona en tiempo real detrás del chat.
- **UI Flotante**: Barra de escritura y botones con diseño suspendido para una sensación de profundidad y modernidad.
- **Vista Previa de Imágenes**: Galería a pantalla completa integrada para examinar los adjuntos con detalle.

### 🛠️ Arquitectura y Optimización
- **Modularización Total**: Código organizado limpiamente en componentes, vistas y diálogos para facilitar el mantenimiento.
- **Optimización R8**: Aplicación extremadamente ligera y protegida contra ofuscación de modelos de datos mediante `@Keep`.
- **Internacionalización**: Todos los textos centralizados en recursos de strings para fácil localización.

## 🛠️ Stack Tecnológico

- **Kotlin + Jetpack Compose**: UI declarativa de última generación.
- **Room Database**: Persistencia de datos local robusta.
- **Corrutinas + Flow**: Gestión asíncrona avanzada para streaming SSE.
- **OkHttp + JSON**: Comunicación eficiente con la API de NVIDIA.
- **Coil**: Carga y gestión de imágenes optimizada.
- **PdfRenderer**: Procesamiento nativo de documentos PDF.

## 📦 Instalación y Configuración

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/ElJoker63/Chat_IA_NVIDEA.git
   ```
2. **Importar**: Abre el proyecto en **Android Studio (Ladybug o superior)**.
3. **Obtener API Key**: Regístrate y obtén tu clave gratuita en el [portal de NVIDIA Build](https://build.nvidia.com/).
4. **Configurar**: Al iniciar la app, introduce tu `nvapi-...` y la aplicación se autoconfigurará con los mejores parámetros para la familia Llama 3 / GLM.

---
*Desarrollado con ❤️ para la comunidad de IA y Android.*
