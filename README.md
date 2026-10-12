# template-karate-e2e

Plantilla base para pruebas E2E/API con [Karate](https://github.com/karatelabs/karate), pensada para:

- Manejar **datos por ambiente** (`desarrollo`, `calidad`) en archivos **CSV**.
- Definir **respuestas esperadas** tanto para casos de **éxito** como de **fallo**.
- Soportar **N servicios** de forma aislada, filtrables por **tags**.
- Exigir **autenticación (token) y cabeceras obligatorias en todas las pruebas**, soportando
  autenticación por **OAuth2 `client_credentials`** (token `Bearer`) en todos los servicios.

La plantilla viene funcional "out of the box" (`mvn test` corre y pasa sin tocar nada):
usa la API pública [jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com) como
ejemplo de dos servicios (`users` y `posts`), y levanta un servidor OAuth2 **de mentira**
embebido (Karate mock) para que el flujo `client_credentials` sea real y verificable sin
depender de un IDP externo. Reemplaza esa configuración por tus APIs e IDP reales cuando
lo necesites (ver sección de autenticación más abajo).

## Estructura

```
pom.xml
src/test/java/runners/
  TestRunner.java          -> corre todos los features
  ServicesRunner.java      -> corre por servicio (tag @users, @posts, ...)
  SuiteRunner.java         -> corre por tipo de caso (@success, @failure)
  support/
    AuthMockSupport.java   -> levanta el mock local de OAuth2 (solo para la demo)
    TokenCache.java        -> cachea tokens en memoria durante todo el run
src/test/resources/
  karate-config.js         -> resuelve ambiente (karate.env), baseUrl, auth y headers por servicio
  logback-test.xml         -> logging de consola
  common/
    headers.js             -> arma headers obligatorios + header de autenticación
    auth/
      client-credentials.feature  -> grant OAuth2 client_credentials
  mocks/
    oauth-mock.feature      -> servidor OAuth2 de mentira (solo para que la demo corra sola)
  data/
    desarrollo/             -> CSV de datos para el ambiente "desarrollo"
      users-success.csv
      users-failure.csv
      users-create.csv
      posts-success.csv
      posts-failure.csv
    calidad/                -> mismos archivos, con datos propios de "calidad"
  services/
    users/
      users-get.feature
      users-create.feature
    posts/
      posts-get.feature
```

## Cómo funciona el multi-ambiente

`karate-config.js` lee la propiedad de sistema `karate.env` (default: `desarrollo`) y arma,
por ambiente, un mapa `services` con la config de cada servicio:

```js
services: {
  users: { baseUrl: '...', auth: { type: 'client_credentials', tokenUrl: '...', clientId: '...', clientSecret: '...', scope: '...' } },
  posts: { baseUrl: '...', auth: { type: 'client_credentials', tokenUrl: '...', clientId: '...', clientSecret: '...', scope: '...' } }
}
```

También expone `dataPath` (no usado directamente en `Examples`, ver nota abajo) y
`mandatoryHeaders` (cabeceras comunes a toda la suite).

Cada feature carga sus datos CSV con `read('classpath:data/' + karate.env + '/archivo.csv')`
directamente en el bloque `Examples`, por lo que el **mismo feature corre contra distintos
ambientes sin cambiar una sola línea**, solo cambiando `-Dkarate.env`.

> **Nota técnica:** el bloque `Examples` de un `Scenario Outline` se resuelve *antes* de que
> corra el `Background`, así que solo tiene visibilidad de `karate.env` y funciones globales
> (`read`, `karate.*`) — no de variables custom definidas en `karate-config.js` como
> `dataPath`. Por eso el `Examples` arma la ruta con `karate.env` directamente, mientras que
> dentro de los `Scenario` normales (`Background` en adelante) sí se puede usar `services.*`.

Para agregar un ambiente nuevo (ej. `produccion`):
1. Crear `src/test/resources/data/produccion/` con los mismos CSV que los otros ambientes.
2. Agregar su entrada (baseUrl + auth por servicio) en el objeto `environments` de
   `karate-config.js`.

## Cómo funciona el dataset CSV (éxito / fallo)

Por cada servicio hay CSV separados:

- `*-success.csv`: casos que deben responder con éxito (incluye el status y el body esperado).
- `*-failure.csv`: casos que deben fallar (status de error esperado).

Los valores de un CSV siempre llegan como **texto**, así que las columnas numéricas se
castean explícitamente al inicio del `Scenario Outline` con `Number(...)`:

```gherkin
Scenario Outline: Obtener usuario exitosamente - <description>
  * def userId = Number(userId)
  * def expectedStatus = Number(expectedStatus)
  Given path 'users', userId
  When method get
  Then match responseStatus == expectedStatus
  And match response.id == userId
```

Cada columna del CSV queda disponible como variable dentro del `Scenario Outline` sin
necesidad de `<...>` (Karate las inyecta directamente en el contexto); el texto `<description>`
en el nombre del escenario sí usa esa sintaxis, solo para que el reporte muestre un nombre
legible por caso.

## Autenticación: token + cabeceras obligatorias por servicio

Todas las pruebas pasan por `common/headers.js`, invocado desde el `Background` de cada
feature de servicio:

```gherkin
Background:
  * url services.users.baseUrl
  * configure headers = call read('classpath:common/headers.js') 'users'
```

`headers.js` arma, para el servicio indicado (`'users'`, `'posts'`, ...):

1. Las **cabeceras obligatorias** definidas una sola vez en `karate-config.js`
   (`mandatoryHeaders`: `Accept`, `Content-Type`, `X-App-Name`, `X-Environment`, etc.) —
   agrega o quita las que tu organización exija.
2. La **cabecera de autenticación**, según `auth.type` configurado para ese servicio:

   | `auth.type`          | Qué hace                                                        | Dónde está la lógica |
   |-----------------------|------------------------------------------------------------------|-----------------------|
   | `client_credentials`  | Pide un token OAuth2 (`grant_type=client_credentials`) y arma `Authorization: Bearer <token>` | `common/auth/client-credentials.feature` |

El **token se pide una sola vez por servicio durante todo el run** (no en cada escenario):
`headers.js` usa `runners/support/TokenCache.java` (un mapa en memoria) para cachear el
resultado la primera vez que un servicio necesita un token, y reutilizarlo en el resto de
escenarios/features de esa corrida.

Para agregar un servicio nuevo con un tipo de token ya soportado, **no hace falta tocar
código Java ni `headers.js`**: solo se agrega su entrada en `karate-config.js` (`services.<nombre>.auth`)
y su `Background` llama `call read('classpath:common/headers.js') '<nombre>'`.

Para agregar un tipo de autenticación nuevo (ej. un JWT firmado localmente, mTLS, etc.):
1. Crear `common/auth/<tipo>.feature`.
2. Agregar la rama correspondiente en `common/headers.js` (hoy solo se acepta `client_credentials`; otro tipo falla con `karate.fail`).

### Sobre el mock de OAuth2 incluido

`users` y `posts` están configurados con `auth.type = 'client_credentials'` apuntando, por defecto, a
`https://auth-desarrollo.example.com/oauth/token` — un dominio de ejemplo que **no existe**.
Para que la plantilla sea ejecutable sin depender de un IDP real, `runners/support/AuthMockSupport.java`
levanta (vía `@BeforeAll`) un mock local de Karate (`mocks/oauth-mock.feature`) que responde
cualquier `POST /oauth/token` con un `access_token` de mentira, y `karate-config.js` apunta
`tokenUrl` a ese mock automáticamente si lo detecta levantado (system property `mock.auth.url`).

**Para usar esto en un proyecto real:** reemplazá `tokenUrl`, `clientId`, `clientSecret` y
`scope` en `karate-config.js` por los de tu IDP real (Keycloak, Okta, Azure AD, Auth0, etc.),
y podés borrar `mocks/` y `AuthMockSupport` (o dejarlos para pruebas locales sin red).

## Cómo funciona el multi-servicio con tags

Cada feature se tagea con:

- `@<servicio>` (`@users`, `@posts`, ...) — para correr solo ese servicio.
- `@success` / `@failure` — para correr solo casos de éxito o solo de fallo.

Para sumar un servicio nuevo:
1. Crear `src/test/resources/services/<servicio>/` con sus `.feature`.
2. Crear sus CSV en `data/<env>/<servicio>-success.csv` y `-failure.csv`.
3. Agregar su config (`baseUrl` + `auth`) en `karate-config.js`.
4. Tagear los escenarios con `@<servicio>`.
5. (Opcional) agregar un método en `ServicesRunner.java` para tener un runner dedicado;
   `TestRunner.testAll()` y los runners de `SuiteRunner` ya lo recogen automáticamente
   porque escanean `classpath:services`.

## Comandos

Correr todo contra desarrollo (default):

```bash
mvn test
```

Correr todo contra calidad:

```bash
mvn test -Dkarate.env=calidad
```

Correr solo un servicio:

```bash
mvn test -Dkarate.env=calidad -Dtest=ServicesRunner#testUsers
```

Correr solo casos de fallo, en cualquier servicio:

```bash
mvn test -Dkarate.env=desarrollo -Dtest=SuiteRunner#testFailureCases
```

Filtrar por tags ad-hoc sin tocar código (combina cualquier tag):

```bash
mvn test -Dkarate.env=calidad -Dkarate.options="--tags @users --tags @success"
```

## Reportes

Karate genera reportes HTML en `target/karate-reports/karate-summary.html` después de cada
corrida.
