Feature: Servicio Posts - Consulta de post por ID

  Background:
    * url services.posts.baseUrl
    * configure headers = call read('classpath:common/headers.js') 'posts'

  @posts @success
  Scenario Outline: Obtener post exitosamente - <description>
    * def postId = Number(postId)
    * def expectedStatus = Number(expectedStatus)
    * def expectedUserId = Number(expectedUserId)
    Given path 'posts', postId
    When method get
    Then match responseStatus == expectedStatus
    And match response.id == postId
    And match response.userId == expectedUserId
    And match response.title == '#string'
    And match response.body == '#string'

    Examples:
      | read('classpath:data/' + karate.env + '/posts-success.csv') |

  @posts @failure
  Scenario Outline: Obtener post inexistente - <description>
    * def postId = Number(postId)
    * def expectedStatus = Number(expectedStatus)
    Given path 'posts', postId
    When method get
    Then match responseStatus == expectedStatus
    And match response == {}

    Examples:
      | read('classpath:data/' + karate.env + '/posts-failure.csv') |
