/**
 * Arma las cabeceras de un request: las obligatorias (iguales para todos los
 * servicios) + la cabecera Authorization obtenida con OAuth2 client_credentials
 * (common/auth/client-credentials.feature). Es el unico tipo de auth soportado.
 *
 * Uso en el Background de cada feature de servicio:
 *   * configure headers = call read('classpath:common/headers.js') 'users'
 */
function(serviceName) {
  var services = karate.get('services');
  var mandatoryHeaders = karate.get('mandatoryHeaders') || {};
  var service = services[serviceName];
  if (!service) {
    karate.fail('Servicio no configurado en karate-config.js: ' + serviceName);
  }

  var headers = {};
  for (var key in mandatoryHeaders) {
    headers[key] = mandatoryHeaders[key];
  }

  var auth = service.auth || {};
  if (auth.type !== 'client_credentials') {
    karate.fail('Tipo de autenticacion no soportado: ' + auth.type + ' (solo se soporta client_credentials)');
  }

  var TokenCache = Java.type('runners.support.TokenCache');
  var cacheKey = serviceName + ':' + auth.type;

  var token = TokenCache.get(cacheKey);
  if (!token) {
    token = karate.call('classpath:common/auth/client-credentials.feature', auth).token;
    TokenCache.put(cacheKey, token);
  }

  headers['Authorization'] = 'Bearer ' + token;
  return headers;
}
