Feature: Obtener token OAuth2 - grant client_credentials

  # Se invoca via karate.call(...) desde common/headers.js, pasando el objeto "auth"
  # del servicio (tokenUrl, clientId, clientSecret, scope) como argumento de la llamada.
  # Karate expone las claves de ese mapa como variables de nivel superior en este feature.

  Scenario:
    Given url tokenUrl
    And header Content-Type = 'application/x-www-form-urlencoded'
    And form field grant_type = 'client_credentials'
    And form field client_id = clientId
    And form field client_secret = clientSecret
    And form field scope = scope
    When method post
    Then status 200
    * def token = response.access_token
