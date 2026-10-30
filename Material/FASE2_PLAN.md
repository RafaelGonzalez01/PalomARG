# PalomARG – FASE 2: Cuentas y Vinculación de Palomar

## Arquitectura general

```mermaid
flowchart TD
    APP["PalomargApp.java\n(entry point)"] --> LOGIN["LoginView\n(login / registro)"]
    LOGIN -->|auth exitoso| MAIN["MainView\n(vista principal con rol)"]
    MAIN -->|USUARIO| VISTAS["Mis Palomas · Mi Palomar · Carreras"]
    MAIN -->|MASTER| RECLAMOS["ReclamosView\n(aprobar / rechazar / fusionar)"]

    LOGIN --> AUTH["AuthService\n(BCrypt, sesión)"]
    LOGIN --> BUSQ["BusquedaColombofiloService\n(Jaccard + Jaro-Winkler)"]
    LOGIN --> RECLSVC["ReclamoService\n(crear / aprobar / rechazar)"]

    RECLSVC --> REPO_R["ReclamoPalomarRepository"]
    RECLSVC --> REPO_C["ColombofiloRepository"]
    BUSQ --> REPO_C
    AUTH --> REPO_U["UsuarioRepository"]
```

## Componentes a implementar

| # | Archivo | Estado |
|---|---------|--------|
| 1 | `pom.xml` – agregar BCrypt | ✅ Listo |
| 2 | `service/AuthService.java` | ✅ Listo |
| 3 | `service/BusquedaColombofiloService.java` | ✅ Listo |
| 4 | `service/ReclamoService.java` | ✅ Listo |
| 5 | `view/LoginView.java` | ✅ Listo |
| 6 | `view/ReclamosView.java` | ✅ Listo |
| 7 | `controller/MainController.java` (actualizado) | ✅ Listo |
| 8 | `view/MainView.java` (actualizado) | ✅ Listo |
| 9 | `PalomargApp.java` (actualizado) | ✅ Listo |
| 10 | `database/DatabaseManager.java` (usuario MASTER) | ✅ Listo |
| 11 | `css/styles.css` (estilos nuevos) | ✅ Listo |

> **`mvn compile` → BUILD SUCCESS** ✅

## Flujo de Registro

```mermaid
flowchart TD
    REG["Usuario se registra"] --> OPC{"¿Opción?"}
    OPC -->|Crear palomar nuevo| NUEVO["Crear Colombofilo nuevo\nVincular directo"]
    OPC -->|Buscar palomar existente| BUSCAR["Buscar con texto ≥3 chars\n(Jaccard + Jaro-Winkler)"]
    BUSCAR --> CANDS["Ver hasta 5 candidatos\nnombre · ciudad · # palomas"]
    CANDS --> ELEGIR["Usuario elige candidato"]
    ELEGIR --> RECLAMO["Crear ReclamoPalomar PENDIENTE"]
    RECLAMO --> ESPERA["Usuario ve: 'Esperando aprobación'\n(no puede editar esas palomas)"]
```

## Flujo de Aprobación MASTER

```mermaid
flowchart TD
    NOTIF["MASTER recibe notificación\n(reclamos pendientes + colombófilos sin usuario)"]
    NOTIF --> VER["Ver pantalla Reclamos"]
    VER --> DETALLE["Ver detalle: usuario + colombófilo candidato"]
    DETALLE --> ACCION{"¿Acción?"}
    ACCION -->|Aprobar| VINCULAR["Colombofilo.usuario = usuario\nGuardar nombre_web como alias\nEstado → APROBADO"]
    ACCION -->|Rechazar| MOTIVO["Ingresar motivo\nEstado → RECHAZADO"]
    ACCION -->|Fusionar| FUSION["Elegir colombófilo destino\nReasignar palomas + resultados\nEliminar origen"]
```

## Algoritmo de Búsqueda Difusa

```
score = 0.6 × Jaccard(tokens_query, tokens_candidato)
      + 0.4 × JaroWinkler(query_normalizado, nombre_candidato_normalizado)
      + 0.2  (si código [n] coincide)
      + 0.1  (si ciudad/localidad coincide)

Normalización:
  - minúsculas, sin tildes, espacios colapsados
  - quitar conectores: "y", "e", "hijos", "familia", "hnos", "de", "del", "los", "las"

Solo candidatos con id_usuario IS NULL
Devuelve hasta 5, ordenados por score desc
```

## Reglas de negocio críticas

> [!IMPORTANT]
> **Nunca vincular automáticamente.** Todo vínculo Colombofilo↔Usuario requiere aprobación explícita del MASTER.

> [!WARNING]
> **Conflicto de anillo**: si una paloma tiene dueño y llega una importación con el mismo anillo para otro colombófilo → NO sobreescribir → abrir reclamo.

> [!NOTE]
> **Fusión**: si el usuario creó un palomar manual y llega importación con el mismo colombófilo → ofrecer "fusionar" en lugar de duplicar.

## Credenciales por defecto (datos de ejemplo)

| Usuario | Password | Rol |
|---------|----------|-----|
| `master` | `master123` | MASTER |

