# NVIDIA LLM API Chat

Una aplicación Android moderna y ligera diseñada para interactuar con la API de NVIDIA LLM (Chat Completions) de forma sencilla y eficiente. Construida con **Jetpack Compose** y siguiendo las últimas prácticas de desarrollo Android.

## 🚀 Características

- **Configuración Simplificada**: Inicia en segundos introduciendo solo tu API Key. La aplicación se encarga de configurar los endpoints y parámetros óptimos automáticamente.
- **Selector de Modelos Inteligente**: Explora y busca entre la amplia variedad de modelos ofrecidos por NVIDIA (como Llama 3, GLM, etc.) mediante un selector con buscador integrado.
- **Chat en Tiempo Real**: Soporte completo para *streaming* de respuestas, permitiendo ver la generación del texto palabra por palabra.
- **Soporte Markdown**: Visualización enriquecida de respuestas, incluyendo bloques de código, listas, negritas y más.
- **Interfaz Moderna**: Diseño basado en **Material 3**, con soporte para temas dinámicos y una experiencia de usuario fluida.
- **Gestión de Historial**: Limpia el chat o cambia de configuración fácilmente desde el menú principal.

## 🛠️ Tecnologías Utilizadas

- **Kotlin**: Lenguaje principal de desarrollo.
- **Jetpack Compose**: Toolkit moderno para la construcción de la interfaz de usuario declarativa.
- **Material 3**: Sistema de diseño de Google para una estética moderna.
- **Corrutinas y Flow**: Para la gestión de tareas asíncronas y el flujo de datos del streaming.
- **OkHttp**: Cliente HTTP robusto para la comunicación con la API.
- **ViewModel**: Arquitectura recomendada para la separación de lógica y UI.

## 📦 Instalación y Uso

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/ElJoker63/Chat_IA_NVIDEA.git
   ```
2. **Abrir en Android Studio**: Importa el proyecto y deja que Gradle sincronice las dependencias.
3. **Obtener API Key**: Consigue tu clave de acceso en el [portal de NVIDIA Build](https://build.nvidia.com/).
4. **Ejecutar**: Lanza la aplicación en tu dispositivo o emulador, introduce tu API Key y ¡comienza a chatear!
