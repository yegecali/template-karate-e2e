/**
 * Utilidades de hashing, Base64 y cifrado/descifrado RSA para usar en cualquier feature.
 *
 * Uso:
 *   * def crypto = call read('classpath:common/crypto.js')
 *   * def passwordHash = crypto.sha256(password)
 *   * def documentB64 = crypto.base64Encode(documentNumber)
 *   * def original = crypto.base64Decode(documentB64)
 *   * def publicKey = read('classpath:keys/demo-rsa-public-key.txt')
 *   * def cardEncrypted = crypto.encryptRSA(publicKey, cardNumber)
 */
function() {
  var Crypto = Java.type('runners.support.CryptoUtils');

  return {
    // Hashing (algoritmo en hex minuscula; usar 'MD5', 'SHA-1', 'SHA-256' o 'SHA-512')
    hash: function(algorithm, value) { return Crypto.hashHex(algorithm, value); },
    hashBase64: function(algorithm, value) { return Crypto.hashBase64(algorithm, value); },
    md5: function(value) { return Crypto.hashHex('MD5', value); },
    sha1: function(value) { return Crypto.hashHex('SHA-1', value); },
    sha256: function(value) { return Crypto.hashHex('SHA-256', value); },
    sha512: function(value) { return Crypto.hashHex('SHA-512', value); },

    // Base64
    base64Encode: function(value) { return Crypto.base64Encode(value); },
    base64Decode: function(value) { return Crypto.base64Decode(value); },

    // RSA: publicKeyPem/privateKeyPem son el contenido completo del archivo .pem/.txt
    encryptRSA: function(publicKeyPem, plainText) { return Crypto.encryptRSA(publicKeyPem, plainText); },
    decryptRSA: function(privateKeyPem, cipherTextBase64) { return Crypto.decryptRSA(privateKeyPem, cipherTextBase64); }
  };
}
