# Arquitectura

El MVP usa un monorepo con tres servicios backend y un frontend Angular.

- `api-gateway`: entrada HTTP para Angular, CORS, validación JWT y reenvío hacia servicios internos.
- `auth-service`: autenticación, usuarios iniciales de desarrollo, BCrypt, JWT, Flyway y PostgreSQL.
- `student-service`: estructura inicial ejecutable para futuros módulos estudiantiles.
- `frontend/fmud-web`: Angular standalone, rutas lazy, Reactive Forms, Signals, guards e interceptores.

## Compatibilidad de versiones

- Spring Boot `4.1.0`: la documentación oficial indica Java 17 mínimo y compatibilidad hasta Java 26.
- Spring Cloud `2025.1.2`: release train compatible con Spring Boot `4.1.0`.
- Maven Wrapper `3.9.16`: versión actual recomendada de Maven y ejecutable con Java 25.
- Angular `22.0.7`: versión estable actual. Requiere Node `^22.22.3 || ^24.15.0 || ^26.0.0`, TypeScript `>=6.0.0 <6.1.0` y RxJS `^6.5.3 || ^7.4.0`.

El equipo local detectado tiene Node `25.6.0`, que no está en el rango oficial de Angular 22. Para ejecutar build/test del frontend use Node `26.x` o `24.15+`.

## Hexagonal

En `auth-service`, los controladores dependen de puertos de entrada, los casos de uso implementan esos puertos y la persistencia JPA implementa puertos de salida. El dominio no depende de Spring, JPA ni HTTP.

`student-service` mantiene la estructura de dominio/aplicación/infraestructura, pero no implementa módulos estudiantiles todavía por alcance.

## Datos

Cada servicio es dueño de sus datos. En desarrollo se usa una misma instancia PostgreSQL con bases separadas: `fmud_auth` y `fmud_student`.

No hay transacciones distribuidas en esta fase.
