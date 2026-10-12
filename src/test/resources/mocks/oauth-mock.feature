Feature: Mock local de un servidor OAuth2 (grant client_credentials)

  # Unicamente para que esta plantilla sea ejecutable sin un IDP real.
  # Responde cualquier POST a /oauth/token con un access_token de mentira.

  Scenario: pathMatches('/oauth/token') && methodIs('post')
    * def accessToken = 'mock-access-token-' + java.lang.System.currentTimeMillis()
    * def response = { access_token: '#(accessToken)', token_type: 'Bearer', expires_in: 3600 }
