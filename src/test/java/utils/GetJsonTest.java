package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedReader;
import java.io.StringReader;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

class GetJsonTest {

  private BufferedReader readerOf(String content) {
    return new BufferedReader(new StringReader(content));
  }

  @Test
  void getJsonArrayFromPost_validArray() {
    JSONArray result = GetJson.getJssonArrayFromPost(readerOf("[1,2,3]"));
    assertEquals(3, result.length());
    assertEquals(1, result.getInt(0));
    assertEquals(2, result.getInt(1));
    assertEquals(3, result.getInt(2));
  }

  @Test
  void getJsonArrayFromPost_arrayOfObjects() {
    JSONArray result =
        GetJson.getJssonArrayFromPost(readerOf("[{\"name\":\"alice\"},{\"name\":\"bob\"}]"));
    assertEquals(2, result.length());
    assertEquals("alice", result.getJSONObject(0).getString("name"));
  }

  @Test
  void getJsonArrayFromPost_emptyArray() {
    JSONArray result = GetJson.getJssonArrayFromPost(readerOf("[]"));
    assertEquals(0, result.length());
  }

  @Test
  void getJsonArrayFromPost_malformedReturnsNull() {
    JSONArray result = GetJson.getJssonArrayFromPost(readerOf("not json"));
    assertNull(result);
  }

  @Test
  void getJsonArrayFromPost_emptyStringReturnsNull() {
    JSONArray result = GetJson.getJssonArrayFromPost(readerOf(""));
    assertNull(result);
  }

  @Test
  void getJsonArrayFromPost_multilineInput() {
    JSONArray result = GetJson.getJssonArrayFromPost(readerOf("[\n  1,\n  2\n]"));
    assertEquals(2, result.length());
  }
}
