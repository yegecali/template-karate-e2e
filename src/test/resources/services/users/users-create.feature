Feature: Servicio Users - Creacion de usuario (con campos hasheados, Base64 y cifrados)

  Background:
    * url services.users.baseUrl
    * configure headers = call read('classpath:common/headers.js') 'users'
    * def crypto = call read('classpath:common/crypto.js')
    * def rsaPublicKey = read('classpath:keys/demo-rsa-public-key.txt')
    * def rsaPrivateKey = read('classpath:keys/demo-rsa-private-key.txt')

  @users @success
  Scenario Outline: Crear usuario exitosamente - <description>
    * def expectedStatus = Number(expectedStatus)
    # password -> hash (nunca se envia en texto plano)
    * def passwordHash = crypto.sha256(password)
    # documentNumber -> Base64 (algunos servicios piden los campos codificados asi)
    * def documentNumberBase64 = crypto.base64Encode(documentNumber)
    # cardNumber -> cifrado con la llave publica del servicio (RSA)
    * def cardNumberEncrypted = crypto.encryptRSA(rsaPublicKey, cardNumber)

    Given path 'users'
    And request
      """
      {
        "name": "#(name)",
        "username": "#(username)",
        "email": "#(email)",
        "passwordHash": "#(passwordHash)",
        "documentNumberBase64": "#(documentNumberBase64)",
        "cardNumberEncrypted": "#(cardNumberEncrypted)"
      }
      """
    When method post
    Then match responseStatus == expectedStatus
    And match response.name == name
    And match response.username == username
    And match response.email == email
    # el hash enviado debe ser identico al recalculado (reproducibilidad del hash)
    And match response.passwordHash == passwordHash
    # el Base64 enviado se puede decodificar y recuperar el valor original
    And match response.documentNumberBase64 == documentNumberBase64
    And match crypto.base64Decode(response.documentNumberBase64) == documentNumber
    # el valor cifrado con la llave publica se puede descifrar con la llave privada (round-trip)
    And match response.cardNumberEncrypted == cardNumberEncrypted
    And match crypto.decryptRSA(rsaPrivateKey, response.cardNumberEncrypted) == cardNumber

    Examples:
      | read('classpath:data/' + karate.env + '/users-create.csv') |
