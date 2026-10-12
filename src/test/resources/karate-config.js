function fn() {
  var env = karate.env; // viene de -Dkarate.env en la linea de comandos
  if (!env) {
    env = 'desarrollo';
  }
  karate.log('karate.env ->', env);

  // URL del mock local de OAuth2 que levanta runners/support/AuthMockSupport
  // (solo para que esta plantilla corra de punta a punta sin un IDP real).
  // En un proyecto real: borrar este mock y poner el tokenUrl real abajo.
  var mockAuthUrl = karate.properties['mock.auth.url'];

  // ---------------------------------------------------------------------
  // Config por ambiente: baseUrl + credenciales OAuth2 (client_credentials)
  // por servicio. Todos los servicios se autentican con este flujo.
  // ---------------------------------------------------------------------
  var environments = {
    desarrollo: {
      services: {
        users: {
          baseUrl: 'https://jsonplaceholder.typicode.com',
          auth: {
            type: 'client_credentials',
            tokenUrl: (mockAuthUrl || 'https://auth-desarrollo.example.com') + '/oauth/token',
            clientId: 'users-dev-client-id',
            clientSecret: 'users-dev-client-secret',
            scope: 'users.read users.write'
          }
        },
        posts: {
          baseUrl: 'https://jsonplaceholder.typicode.com',
          auth: {
            type: 'client_credentials',
            tokenUrl: (mockAuthUrl || 'https://auth-desarrollo.example.com') + '/oauth/token',
            clientId: 'posts-dev-client-id',
            clientSecret: 'posts-dev-client-secret',
            scope: 'posts.read'
          }
        }
      }
    },
    calidad: {
      services: {
        users: {
          baseUrl: 'https://jsonplaceholder.typicode.com',
          auth: {
            type: 'client_credentials',
            tokenUrl: (mockAuthUrl || 'https://auth-calidad.example.com') + '/oauth/token',
            clientId: 'users-qa-client-id',
            clientSecret: 'users-qa-client-secret',
            scope: 'users.read users.write'
          }
        },
        posts: {
          baseUrl: 'https://jsonplaceholder.typicode.com',
          auth: {
            type: 'client_credentials',
            tokenUrl: (mockAuthUrl || 'https://auth-calidad.example.com') + '/oauth/token',
            clientId: 'posts-qa-client-id',
            clientSecret: 'posts-qa-client-secret',
            scope: 'posts.read'
          }
        }
      }
    }
  };

  var envConfig = environments[env];
  if (!envConfig) {
    karate.log('WARN: ambiente "' + env + '" no configurado en karate-config.js, usando "desarrollo"');
    envConfig = environments.desarrollo;
  }

  var config = {
    env: env,
    dataPath: 'classpath:data/' + env + '/',
    services: envConfig.services
  };

  // Cabeceras obligatorias para TODAS las pruebas. Se combinan con la cabecera
  // Authorization en common/headers.js
  config.mandatoryHeaders = {
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'X-App-Name': 'template-karate-e2e',
    'X-Environment': env
  };

  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 10000);

  return config;
}
