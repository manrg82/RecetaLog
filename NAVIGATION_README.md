# Sistema de Navegación - RecetaLog

## Implementación Completada (Sin Fragments)

Se ha implementado el sistema de navegación de la aplicación con:

### 1. Top Bar (Barra Superior)
- **Logo**: Ubicado en el lado izquierdo (usa `@mipmap/logoapp`)
- **Nombre de la App**: Centrado ("RecetaLog")
- Color de fondo: Usa el color primario del tema
- Elevación de 4dp para efecto de sombra
- Altura mínima de 56dp con padding dinámico para edge-to-edge

### 2. Bottom Navigation (Barra de Navegación Inferior)
Tres opciones principales:
- **Inicio** (`navigation_home`): Menú principal/inicio
- **Mis Recetas** (`navigation_recipes`): Biblioteca de recetas
- **Perfil** (`navigation_profile`): Opciones/perfil del usuario

### Archivos Creados/Modificados

#### Nuevos Archivos:
1. `res/drawable/ic_home.xml` - Icono de inicio
2. `res/drawable/ic_recipes.xml` - Icono de recetas
3. `res/drawable/ic_profile.xml` - Icono de perfil
4. `res/menu/bottom_nav_menu.xml` - Menú de navegación inferior
5. `res/layout/view_home.xml` - Vista de inicio
6. `res/layout/view_recipes.xml` - Vista de biblioteca de recetas
7. `res/layout/view_profile.xml` - Vista de perfil/opciones

#### Archivos Modificados:
1. `res/layout/activity_main.xml` - Layout principal con top bar y bottom navigation
2. `res/values/strings.xml` - Strings para las opciones de navegación
3. `view/MainActivity.kt` - Configuración de la navegación con cambio de vistas

### Estructura del Layout

```
┌─────────────────────────────────┐
│  Top Bar                        │
│  [Logo]    RecetaLog            │
├─────────────────────────────────┤
│                                 │
│  Content Container              │
│  (Vistas intercambiables)       │
│                                 │
├─────────────────────────────────┤
│  Bottom Navigation              │
│  [Inicio] [Recetas] [Perfil]    │
└─────────────────────────────────┘
```

### Funcionamiento

La navegación funciona **sin Fragments**, usando Views inflados dinámicamente:

1. Cuando el usuario selecciona una opción en la bottom navigation
2. El `contentContainer` (FrameLayout) elimina todas las vistas anteriores
3. Se infla el layout correspondiente y se añade al container

```kotlin
private fun loadView(layoutResId: Int) {
    binding.contentContainer.removeAllViews()
    val view = LayoutInflater.from(this).inflate(layoutResId, binding.contentContainer, false)
    binding.contentContainer.addView(view)
}
```

### Vistas Disponibles

1. **view_home.xml**: Vista de inicio con mensaje de bienvenida
2. **view_recipes.xml**: Vista de biblioteca de recetas
3. **view_profile.xml**: Vista de perfil y opciones

Cada vista es un layout XML independiente que puedes personalizar completamente.

### Funcionalidad Actual

- ✅ Top bar con logo y nombre centrado
- ✅ Bottom navigation con 3 opciones
- ✅ Iconos personalizados para cada opción
- ✅ Navegación funcional cambiando vistas dinámicamente
- ✅ Soporte para edge-to-edge display
- ✅ Selección por defecto en "Inicio"
- ✅ 3 layouts de ejemplo para cada vista

### Próximos Pasos

Para personalizar cada vista:

1. **Edita los layouts XML** directamente:
   - `view_home.xml` - Personaliza la vista de inicio
   - `view_recipes.xml` - Añade RecyclerView para lista de recetas
   - `view_profile.xml` - Añade opciones de configuración

2. **Añade lógica específica** (opcional):
   - Puedes crear clases auxiliares para cada vista
   - O manejar todo directamente en MainActivity

### Ventajas de este Enfoque

- ✅ Más simple que usar Fragments
- ✅ Menos código de configuración
- ✅ Perfecto para vistas estáticas o semi-estáticas
- ✅ Fácil de entender y mantener
- ✅ No necesitas FragmentManager ni transacciones

### Personalización

Para cambiar colores, edita:
- `res/values/colors.xml` - Colores del tema
- `res/values/themes.xml` - Tema principal

Para cambiar textos:
- `res/values/strings.xml`

Para añadir contenido a las vistas:
- Edita directamente `view_home.xml`, `view_recipes.xml`, o `view_profile.xml`


