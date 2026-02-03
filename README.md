# TSP Solver - Algoritmo Genético

## Qué es

Aplicación Android que resuelve el problema del vendedor viajero (TSP) mediante algoritmos genéticos. Permite definir puntos táctilmente en la pantalla y calcula la ruta óptima entre ellos utilizando técnicas evolutivas.

## Requisitos

- Android Studio Arctic Fox o superior
- JDK 11
- SDK de Android 29 o superior
- Gradle 8.0+

## Quickstart

### Instalar

1. Clona el repositorio:

```bash
git clone <url-del-repositorio>
cd algoritmo-genetico
```

1. Abre el proyecto en Android Studio

2. Espera a que Gradle sincronice las dependencias

### Configurar

No requiere configuración adicional. Los parámetros del algoritmo se ajustan desde la interfaz.

### Ejecutar

1. Conecta un dispositivo Android o inicia un emulador (API 29+)
2. Haz clic en "Run" en Android Studio
3. La aplicación se instalará y ejecutará automáticamente

## Configuración

Parámetros del algoritmo genético (ajustables desde la app):

- **Tamaño de población**: Número de individuos por generación (recomendado: 50-100)
- **Probabilidad de mutación**: Porcentaje de mutación en cada generación (recomendado: 0.01-0.05)
- **Número de generaciones**: Iteraciones del algoritmo (recomendado: 100-500)

## Uso

1. Toca la pantalla para agregar puntos (mínimo 11 puntos)
2. Configura los parámetros del algoritmo genético
3. Presiona el botón de acción para calcular la ruta óptima
4. La aplicación mostrará la mejor ruta encontrada
5. Puedes guardar rutas y consultarlas posteriormente

## Estructura

```txt
├── MainActivity.kt         # Pantalla principal con captura táctil
├── MainListar.kt          # Listado de rutas guardadas
├── Individuo.kt           # Clase que representa un cromosoma
├── Punto.kt               # Clase para coordenadas (x,y)
└── database.kt            # Gestión de base de datos SQLite
```

## Problemas comunes

**La aplicación no calcula la ruta**

- Verifica que hayas agregado al menos 11 puntos en la pantalla

**El algoritmo no converge**

- Aumenta el número de generaciones o el tamaño de población
- Ajusta la probabilidad de mutación (valores muy altos o muy bajos pueden afectar la convergencia)

**Error al guardar rutas**

- Asegúrate de que la aplicación tenga permisos de almacenamiento
- Verifica que hayas ejecutado el algoritmo antes de intentar guardar
