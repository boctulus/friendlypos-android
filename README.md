# FriendlyPOS | Android Point of Sale

Esta app es un POS Android para una tienda de la app multi-tienda, backend de esta y escrita en NodeJS, servida desde distintos endpoints.

## Tecnologias

### Android App (Kotlin)

- **Kotlin** - Lenguaje de programación
- **Jetpack Compose** - UI moderna
- **Compose Material 3** - Sistema de diseño
- **ViewBinding / DataBinding** - Binding de vistas
- **Navigation Component** - Navegación
- **Lifecycle (LiveData, ViewModel)** - Ciclo de vida
- **Firebase** - Backend services:
  - Firebase Auth (autenticación)
  - Firestore (base de datos)
  - Firebase Storage (almacenamiento de archivos)
  - FCM (Push Notifications)
- **MinSDK 23** - Android 6.0+
- **TargetSDK 34** - Android 14

---

### Backend of API REST

El backend es una app en NodeJS que provee una API REST.

URL Base de la API REST se leera de `ApiConfig.kt`. Valor que por ejemplo contendra: "http://localhost:3001"

---

### Database

La base de datos vive en el backend en NodeJS y es PGSQL ('supabase').

Las distintas entidades (tablas) del backend en NodeJS estan listadas en el proyecto en NodeJS:

```
D:\nodejs\friendlypos_nodejs\config\databases.config.js
```

---

### Firebase

Firebase para:

- Autenticación (Firebase Auth)
- Push Notifications (FCM)

y nada mas.


## Que hace la app

Es un POS (Punto de Venta) de mano (Android).

Se debe integrar con servicio de **Facturación Electrónica (SII de Chile):** OpenFactura (Haulmer) para emisión de boletas y facturas electrónicas.

## Requisitos previos

- Node.js (v14 o superior)
- Cuenta en Firebase con Firestore y Authentication habilitados

# Notas

## Authentication

Para habilitar nuevos métodos de autenticación debe visitarse `https://console.firebase.google.com/project/{project-id}/authentication/providers` donde project-id sería "friendlypos-a53bd", "{this-app}-66e25", etc

Ej:
```
https://console.firebase.google.com/project/{this-app}-66e25/authentication/providers
```

## Crear usuario Admin

De ser necesario se pueden crear usuarios "admin" ejecutando desde la app en NodeJS (backend) el script siguiente:
```
node scripts/create_adm.js
```

O quizás más convenientemente:
```
npm run create-admin
```

Si se desea especificar correo o correo y contraseña puede consultar la ayuda:
```
npm run create-admin help
```

## Comandos CLI

Hay varios comandos "CLI" (para la terminal) que se pueden conocer usando la ayuda:
```
npm run cli help
```

## Base de datos

El proyecto utiliza los servicios de Firebase:

- **Firestore** - Base de datos NoSQL
- **Firebase Auth** - Autenticación de usuarios
- **Firebase Storage** - Almacenamiento de archivos

## Firestore

Se administra desde Firebase Console yendo a `https://console.firebase.google.com/project/{project-id}/firestore/databases/-default-/data` donde project-id sería "friendlypos-a53bd", "{this-app}-66e25", etc

Ej:
```
https://console.firebase.google.com/project/{this-app}-66e25/firestore/databases/-default-/data
```

## Firebase Storage

https://console.firebase.google.com/project/{project-id}/storage/{project-id}.firebasestorage.app

Ej:

https://console.firebase.google.com/project/{this-app}-66e25/storage/{this-app}-66e25.firebasestorage.app

Las reglas se ajustan desde:

https://console.firebase.google.com/project/{project-id}/storage/{project-id}.firebasestorage.app/rules

## API Endpoints

Todos los endpoints están disponibles desde una API REST de un SDK en PHP:

- `GET /health` - Health check
- `POST /dte/emit` - Emitir DTE
- `GET /dte/status/{token}` - Consultar estado
- `GET /taxpayer/{rut}` - Consultar contribuyente
- `GET /organization` - Información de organización

Solicitar documentación completa para más endpoints.

Para conocer la URL base de la API REST del SDK de OpenFactura leer debera leerse `ApiConfig.kt` y para la url contendra algo como "http://sr-haulmer.lan/api/openfactura"


---

## Documentación

- Doc propia de la app Android en `docs\`
- Doc del backend en NodeJS en `D:\nodejs\friendlypos_nodejs\docs\`
- Doc del SDK en PHP en `D:\laragon\www\sr-haulmer\docs\` y dentro de los packages correspondientes.