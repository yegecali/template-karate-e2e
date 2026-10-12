Feature: Servicio Users - Consulta de usuario por ID

  Background:
    * url services.users.baseUrl
    * configure headers = call read('classpath:common/headers.js') 'users'

  @users @success
  Scenario Outline: Obtener usuario exitosamente - <description>
    * def userId = Number(userId)
    * def expectedStatus = Number(expectedStatus)
    Given path 'users', userId
    When method get
    Then match responseStatus == expectedStatus
    And match response.id == userId
    And match response.name == expectedName
    And match response.username == expectedUsername

    Examples:
      | read('classpath:data/' + karate.env + '/users-success.csv') |

  @users @failure
  Scenario Outline: Obtener usuario inexistente - <description>
    * def userId = Number(userId)
    * def expectedStatus = Number(expectedStatus)
    Given path 'users', userId
    When method get
    Then match responseStatus == expectedStatus
    And match response == {}

    Examples:
      | read('classpath:data/' + karate.env + '/users-failure.csv') |
