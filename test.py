import os
import sys
import json
import requests

# Configurar la salida estándar a UTF-8 para evitar errores de encoding en consolas Windows
if sys.stdout.encoding and sys.stdout.encoding.lower() != 'utf-8':
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

BASE_URL = "https://integrate.api.nvidia.com/v1"
API_KEY = os.environ.get("NVIDIA_API_KEY") or os.environ.get("NVAPI_KEY") or "TU_API_KEY_AQUI"

def get_headers():
    return {
        "Authorization": f"Bearer {API_KEY}",
        "Accept": "application/json",
        "Content-Type": "application/json"
    }

def inferir_capacidades(model_id):
    """
    Analiza el ID del modelo para inferir sus capacidades principales:
    - Visión (Multimodal)
    - Razonamiento (Reasoning / Thinking)
    - Código (Coding)
    - Guardrails / Seguridad (Safety)
    - Embeddings / Retrieval
    - Audio / Traducción / Voz
    """
    model_lower = model_id.lower()
    capacidades = []
    
    # Visión / Multimodal
    if any(k in model_lower for k in ["vision", "vl", "-vlm", "deplot", "kosmos", "neva", "vila", "clip"]):
        capacidades.append("📷 Visión / Multimodal")
    
    # Razonamiento / CoT
    if any(k in model_lower for k in ["reasoning", "reason", "thinking", "r1", "super"]):
        capacidades.append("🧠 Razonamiento Avanzado")
        
    # Código / Programación
    if any(k in model_lower for k in ["code", "coder", "starcoder", "embedcode", "codestral"]):
        capacidades.append("💻 Especializado en Código")
        
    # Embeddings / Búsqueda Semántica
    if any(k in model_lower for k in ["embed", "bge", "retriever", "parse"]):
        capacidades.append("🔍 Embeddings / RAG")
        
    # Seguridad / Guardrails
    if any(k in model_lower for k in ["guard", "safety", "reward"]):
        capacidades.append("🛡️ Seguridad / Guardrails")

    # Audio / Traducción
    if any(k in model_lower for k in ["riva", "translate", "audio", "omni"]):
        capacidades.append("🎙️ Audio / Traducción")
        
    # Si no coincide con palabras específicas pero es un modelo instruct/chat general
    if not capacidades:
        if "instruct" in model_lower or "chat" in model_lower:
            capacidades.append("💬 Chat / Instrucciones Generales")
        else:
            capacidades.append("🤖 Modelo Text/LLM General")
            
    return capacidades

def obtener_lista_modelos_clasificados():
    """Consulta los modelos y muestra su clasificación según sus capacidades."""
    url = f"{BASE_URL}/models"
    print(f"\n==========================================")
    print(f" 🔍 MODELOS DISPONIBLES Y SUS CAPACIDADES")
    print(f"==========================================")
    
    try:
        response = requests.get(url, headers=get_headers())
        if response.status_code == 200:
            data = response.json()
            modelos = data.get("data", [])
            print(f"[OK] Total de modelos: {len(modelos)}\n")
            
            for index, model in enumerate(modelos, 1):
                model_id = model.get("id", "Desconocido")
                owned_by = model.get("owned_by", "")
                caps = inferir_capacidades(model_id)
                caps_str = " | ".join(caps)
                print(f"{index:3d}. ID: {model_id:<48}")
                print(f"     Propietario: {owned_by:<15} | Capacidades: {caps_str}")
                print("-" * 80)
            
            return modelos
        else:
            print(f"[ERROR] {response.status_code}: {response.text}")
            return None
    except Exception as e:
        print(f"[EXCEPCION] {e}")
        return None

def probar_soporte_vision(model_id="meta/llama-3.2-11b-vision-instruct"):
    """Prueba si un modelo soporta imágenes en su estructura de mensajes."""
    url = f"{BASE_URL}/chat/completions"
    print(f"\n==========================================")
    print(f" 📷 PROBANDO CAPACIDAD DE VISIÓN: {model_id}")
    print(f"==========================================")
    
    # Imagen de prueba (1x1 pixel PNG transparente en base64)
    image_base64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
    
    payload = {
        "model": model_id,
        "messages": [
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": "¿Qué hay en esta imagen?"},
                    {"type": "image_url", "image_url": {"url": image_base64}}
                ]
            }
        ],
        "max_tokens": 100
    }
    
    try:
        response = requests.post(url, headers=get_headers(), json=payload)
        if response.status_code == 200:
            print("✅ El modelo ACEPTA entradas de VISIÓN (Multimodal).")
            res = response.json()
            print("Respuesta:", res["choices"][0]["message"]["content"])
        else:
            print(f"⚠️ Respuesta de la API ({response.status_code}): {response.text[:200]}")
    except Exception as e:
        print(f"[EXCEPCION] {e}")

if __name__ == "__main__":
    print("=== Análisis de Capacidades de Modelos en NVIDIA NIM ===")
    modelos = obtener_lista_modelos_clasificados()
    
    if API_KEY != "TU_API_KEY_AQUI":
        # Probar capacidad de visión en un modelo multimodal como ejemplo
        probar_soporte_vision("meta/llama-3.2-11b-vision-instruct")
