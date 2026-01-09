# JSON para Probar Endpoints

## POST /api/usuarios/registro

```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@example.com",
  "password": "Password123",
  "telefono": "1123456789",
  "barrio": "Palermo",
  "ciudad": "Buenos Aires"
}
```

## POST /api/usuarios/login

```json
{
  "email": "juan.perez@example.com",
  "password": "Password123"
}
```

## POST /api/mascotas

```json
{
  "nombre": "Firulais",
  "tipo": "Perro",
  "raza": "Labrador",
  "color": "Dorado",
  "tamanio": "Grande",
  "descripcion": "Perro muy amigable con collar azul",
  "estadoMascota": "PERDIDA_PROPIA"
}
```

## POST /api/mascotas (con usuario asociado)

```json
{
  "nombre": "Firulais",
  "tipo": "Perro",
  "raza": "Labrador",
  "color": "Dorado",
  "tamanio": "Grande",
  "descripcion": "Perro muy amigable con collar azul",
  "estadoMascota": "PERDIDA_PROPIA",
  "usuarioId": 1
}
```

## POST /api/publicaciones

```json
{
  "descripcion": "Se busca mascota perdida en zona de Palermo",
  "fecha": "2025-11-14",
  "estadoPublicacion": "ACTIVA",
  "mascotaId": 1,
  "usuarioId": 1
}
```

## POST /api/avistamientos

```json
{
  "comentario": "Vi a esta mascota cerca del parque esta mañana",
  "fecha": "2025-11-14",
  "mascotaId": 1,
  "usuarioId": 1,
  "ubicacion": {
    "lat": -34.6037,
    "lng": -58.3816,
    "barrio": "Palermo",
    "direccion": "Av. Santa Fe 3000"
  }
}
```

## PUT /api/usuarios/{id}

```json
{
  "nombre": "Juan Carlos",
  "apellido": "Pérez González",
  "email": "juan.perez@example.com",
  "password": "Password123",
  "telefono": "1198765432",
  "barrio": "Recoleta",
  "ciudad": "Buenos Aires"
}
```

## PUT /api/mascotas/{id}

```json
{
  "nombre": "Firulais",
  "tipo": "Perro",
  "raza": "Labrador",
  "color": "Dorado",
  "tamanio": "Grande",
  "descripcion": "Perro encontrado - Ya está con su dueño",
  "estadoMascota": "ENCONTRADA",
  "usuarioId": 1
}
```

## PUT /api/publicaciones/{id}

```json
{
  "descripcion": "Mascota encontrada - Publicación cerrada",
  "fecha": "2025-11-14",
  "fechaCierre": "2025-11-14",
  "estadoPublicacion": "CERRADA",
  "mascotaId": 1,
  "usuarioId": 1
}
```

## PUT /api/avistamientos/{id}

```json
{
  "comentario": "Confirmado - La mascota fue encontrada aquí",
  "fecha": "2025-11-14",
  "mascotaId": 1,
  "usuarioId": 1,
  "ubicacion": {
    "lat": -34.6037,
    "lng": -58.3816,
    "barrio": "Palermo",
    "direccion": "Av. Santa Fe 3000"
  }
}
```

## PATCH /api/mascotas/{id}/estado

```json
{
  "estado": "ENCONTRADA"
}
```

## Estados válidos para EstadoMascota

PERDIDA_PROPIA
PERDIDA_AJENA
ENCONTRADA
BUSCANDO_DUEÑO
ADOPTADA

## Estados válidos para EstadoPublicacion

ACTIVA
CERRADA
PAUSADA

