# Conexión a la API del modelo GLM (NVIDIA)

Documento de referencia con la información necesaria para conectarse al modelo de inteligencia artificial GLM ofrecido a través de la plataforma de NVIDIA (NVIDIA Integrate API). Describe **qué** hay que enviar, **dónde** y **para qué** sirve cada parte, sin depender de un lenguaje o librería concretos.

---

## 1. Endpoint (URL base)

| Concepto | Valor |
|----------|--------|
| **URL base** | `https://integrate.api.nvidia.com/v1` |
| **Ruta del chat** | `/chat/completions` |
| **URL completa** | `https://integrate.api.nvidia.com/v1/chat/completions` |

**Qué significa:** es la dirección del servidor de NVIDIA que expone modelos de IA con un formato compatible con la API de *chat completions* (estilo OpenAI). Todas las peticiones de conversación se hacen a esa ruta mediante **HTTP POST**.

---

## 2. Autenticación

| Concepto | Valor / forma |
|----------|----------------|
| **Tipo** | Bearer token |
| **Cabecera HTTP** | `Authorization: Bearer <API_KEY>` |
| **Prefijo típico de la clave** | `nvapi-…` |

**Qué significa:** NVIDIA exige una clave de API personal para identificar quién hace la petición y aplicar cuotas/permisos. La clave se envía en la cabecera `Authorization` con el esquema *Bearer* (la palabra `Bearer`, un espacio y la clave).

**Recomendación de seguridad:** no compartir la clave en repositorios públicos ni en capturas. Si se filtra, hay que rotarla en el panel de NVIDIA.

---

## 3. Modelo a invocar

| Concepto | Valor |
|----------|--------|
| **Identificador del modelo** | `z-ai/glm-5.2` |

**Qué significa:** indica **qué** modelo de IA debe procesar el mensaje. El formato `organización/nombre-versión` es el identificador oficial en la plataforma de NVIDIA. En este caso, el modelo es **GLM 5.2** (familia GLM, proveedor/ruta `z-ai`).

---

## 4. Formato de la petición

### Método y cabeceras habituales

| Elemento | Valor / uso |
|----------|-------------|
| **Método HTTP** | `POST` |
| **Content-Type** | `application/json` |
| **Accept** (si se usa streaming) | `text/event-stream` (opcional pero recomendable) |
| **Cuerpo** | JSON con los campos descritos abajo |

### Cuerpo JSON (conceptos)

La petición es un objeto JSON con, al menos:

#### `model` (obligatorio)
- **Valor de referencia:** `"z-ai/glm-5.2"`
- **Significado:** elige el motor de IA (ver sección 3).

#### `messages` (obligatorio)
- **Formato:** lista (array) de mensajes.
- **Cada mensaje** tiene:
  - `role`: quién habla. Valores habituales:
    - `"user"` — el humano / la app
    - `"assistant"` — la respuesta del modelo
    - `"system"` — instrucciones globales (opcional, si el endpoint lo permite)
  - `content`: texto del mensaje.
- **Significado:** el historial de la conversación. Se envía en orden cronológico. Para chatear de verdad, suele incluirse todo el hilo (mensajes previos de usuario y asistente) + el nuevo mensaje del usuario.

#### `temperature` (opcional; valor de referencia: `1`)
- **Rango típico:** 0 a ~2 (según el proveedor).
- **Significado:** controla la **aleatoriedad** de la respuesta.
  - Cerca de **0**: más determinista, repetible y “conservador”.
  - Cerca de **1 o más**: más variado y creativo.
- En la configuración de referencia se usa **1** (creatividad / diversidad media-alta).

#### `top_p` (opcional; valor de referencia: `1`)
- **Rango típico:** 0 a 1.
- **Significado:** *nucleus sampling*. El modelo solo considera el conjunto de tokens cuya probabilidad acumulada suma hasta `top_p`.
  - **1**: casi no se restringe el vocabulario por este criterio (máxima amplitud).
  - Valores bajos: respuestas más acotadas y predecibles.
- Junto con `temperature`, define el estilo de muestreo; no siempre hace falta tocar ambos a la vez.

#### `max_tokens` (opcional; valor de referencia: `16384`)
- **Significado:** límite máximo de **tokens de salida** que el modelo puede generar en una sola respuesta.
- Un *token* es un trozo de texto (palabra, subpalabra, signo). Más tokens = respuestas potencialmente más largas y mayor coste/uso de cuota.
- **16384** es un techo alto: permite respuestas largas; el modelo puede devolver menos.

#### `seed` (opcional; valor de referencia: `42`)
- **Significado:** semilla numérica para intentar hacer el muestreo **reproducible** (misma entrada + mismos parámetros → salida más similar entre ejecuciones, si el backend lo respeta).
- No todos los proveedores garantizan reproducibilidad perfecta con streaming o en clústeres distribuidos; aun así, se puede enviar si se desea.

#### `stream` (valor de referencia: `true`)
- **Significado:**
  - `true`: la respuesta llega **poco a poco**, en fragmentos (útil para mostrar texto en tiempo real en un chat).
  - `false`: se espera a que el modelo termine y se recibe la respuesta **completa** de una vez.
- Con streaming, el cuerpo de la respuesta suele ser un flujo tipo **Server-Sent Events (SSE)**.

---

## 5. Ejemplo conceptual del cuerpo de la petición

```json
{
  "model": "z-ai/glm-5.2",
  "messages": [
    { "role": "user", "content": "Hola, ¿qué puedes hacer?" }
  ],
  "temperature": 1,
  "top_p": 1,
  "max_tokens": 16384,
  "seed": 42,
  "stream": true
}
```

Para un chat con contexto, `messages` crecería, por ejemplo:

```json
"messages": [
  { "role": "user", "content": "Hola" },
  { "role": "assistant", "content": "¡Hola! ¿En qué te ayudo?" },
  { "role": "user", "content": "Explícame los tokens" }
]
```

---

## 6. Respuesta con streaming (`stream: true`)

### Cómo se recibe
El servidor no devuelve un único JSON final de golpe, sino **líneas de eventos**. Muchas de ellas tienen la forma:

```text
data: { ... JSON del fragmento ... }
```

y al terminar suele aparecer:

```text
data: [DONE]
```

### Qué hay que leer de cada fragmento
Cada evento útil suele traer un objeto con un array `choices`. En la práctica de referencia:

1. Comprobar que existan `choices`.
2. Tomar el primer elemento: `choices[0]`.
3. Leer el objeto `delta` (el **cambio** de ese fragmento).
4. Si `delta.content` tiene texto, ese trozo se **concatena** a la respuesta del asistente.

**Qué significa:** la respuesta del modelo se reconstruye uniendo todos los `content` de los `delta` en orden. Así se puede ir pintando el mensaje en la UI mientras llega.

### Si no usas streaming (`stream: false`)
Normalmente la respuesta completa viene en un JSON donde el texto está en algo equivalente a:

`choices[0].message.content`

(en lugar de `delta.content` de cada trozo).

---

## 7. Resumen rápido para implementar en cualquier lenguaje

1. **POST** a `https://integrate.api.nvidia.com/v1/chat/completions`.
2. Cabecera **`Authorization: Bearer <tu_clave_nvapi>`**.
3. Cabecera **`Content-Type: application/json`**.
4. Cuerpo JSON con:
   - modelo `z-ai/glm-5.2`
   - historial en `messages`
   - `temperature: 1`, `top_p: 1`, `max_tokens: 16384`, `seed: 42`, `stream: true` (según la configuración de referencia)
5. Si streaming: leer el flujo línea a línea, parsear cada `data: …`, acumular `choices[0].delta.content` hasta `data: [DONE]`.
6. Mostrar o guardar la respuesta del asistente como un nuevo mensaje con rol `assistant` para el siguiente turno.

---

## 8. Tabla resumen de valores de referencia

| Aspecto | Valor | Significado breve |
|---------|--------|-------------------|
| URL base | `https://integrate.api.nvidia.com/v1` | Servidor NVIDIA Integrate |
| Endpoint | `POST …/chat/completions` | Completar conversación |
| Autenticación | Bearer + clave `nvapi-…` | Acceso autorizado |
| Modelo | `z-ai/glm-5.2` | GLM 5.2 en NVIDIA |
| temperature | `1` | Creatividad / aleatoriedad |
| top_p | `1` | Sin recorte agresivo de vocabulario |
| max_tokens | `16384` | Tope de longitud de salida |
| seed | `42` | Semilla de reproducibilidad |
| stream | `true` | Respuesta por fragmentos en tiempo real |

---

## 9. Notas útiles

- **Internet obligatorio:** la app o cliente debe poder salir a internet hacia el dominio de NVIDIA.
- **Errores HTTP:** códigos 401/403 suelen indicar clave inválida o sin permiso; 429, límite de uso; 5xx, problema del servidor.
- **Coste y límites:** `max_tokens` alto no obliga a gastar ese máximo, pero permite respuestas muy largas; conviene acotarlo si solo se necesitan respuestas cortas.
- **Esta configuración** reproduce la intención de conectarse al mismo servicio y modelo definidos en la fuente de referencia del proyecto; los valores numéricos se pueden ajustar según la experiencia de chat deseada.
