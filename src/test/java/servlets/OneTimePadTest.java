package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class OneTimePadTest {

  private static String chars(char c, int count) {
    char[] arr = new char[count];
    Arrays.fill(arr, c);
    return new String(arr);
  }

  @Test
  void encryptDecrypt_roundTripWithDefaultKey() {
    String plaintext = "Hello, World!";
    String encrypted = OneTimePad.encrypt(plaintext);
    String decrypted = OneTimePad.decrypt(encrypted);
    assertEquals(plaintext, decrypted);
  }

  @Test
  void encryptDecrypt_roundTripWithCustomKey() {
    String plaintext = "Secret Message";
    String key = "MyCustomKey12345";
    String encrypted = OneTimePad.encrypt(plaintext, key);
    String decrypted = OneTimePad.decrypt(encrypted, key);
    assertEquals(plaintext, decrypted);
  }

  @Test
  void encrypt_producesBase64Output() {
    String encrypted = OneTimePad.encrypt("test");
    assertNotNull(encrypted);
    assertNotEquals("test", encrypted);
  }

  @Test
  void encrypt_deterministicForSameInput() {
    String first = OneTimePad.encrypt("same input");
    String second = OneTimePad.encrypt("same input");
    assertEquals(first, second);
  }

  @Test
  void encrypt_differentKeysProduceDifferentOutput() {
    String plaintext = "Hello";
    String enc1 = OneTimePad.encrypt(plaintext, "key1key1key1key1");
    String enc2 = OneTimePad.encrypt(plaintext, "key2key2key2key2");
    assertNotEquals(enc1, enc2);
  }

  @Test
  void encryptDecrypt_emptyString() {
    String encrypted = OneTimePad.encrypt("");
    String decrypted = OneTimePad.decrypt(encrypted);
    assertEquals("", decrypted);
  }

  @Test
  void encryptDecrypt_specialCharacters() {
    String plaintext = "Spëcial Chàrs: <>&\"'!@#$%^";
    String encrypted = OneTimePad.encrypt(plaintext);
    String decrypted = OneTimePad.decrypt(encrypted);
    assertEquals(plaintext, decrypted);
  }

  @Test
  void encryptDecrypt_longString() {
    String plaintext = chars('A', 1000);
    String encrypted = OneTimePad.encrypt(plaintext);
    String decrypted = OneTimePad.decrypt(encrypted);
    assertEquals(plaintext, decrypted);
  }

  @Test
  void decrypt_wrongKeyProducesGarbage() {
    String plaintext = "Hello";
    String encrypted = OneTimePad.encrypt(plaintext, "correctKey123456");
    String decrypted = OneTimePad.decrypt(encrypted, "wrongKey12345678");
    assertNotEquals(plaintext, decrypted);
  }
}
