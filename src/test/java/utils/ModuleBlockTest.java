package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModuleBlockTest {

  @BeforeEach
  void resetState() {
    ModuleBlock.reset();
  }

  @Test
  void reset_clearsBlockerId() {
    ModuleBlock.blockerId = "someId";
    ModuleBlock.reset();
    assertEquals("", ModuleBlock.blockerId);
  }

  @Test
  void reset_clearsMessage() {
    ModuleBlock.setMessage("blocked");
    ModuleBlock.reset();
    assertEquals("", ModuleBlock.getBlockerMessage());
  }

  @Test
  void reset_disablesBlocker() {
    ModuleBlock.blockerEnabled = true;
    ModuleBlock.reset();
    assertFalse(ModuleBlock.blockerEnabled);
  }

  @Test
  void setAndGetMessage_roundTrip() {
    ModuleBlock.setMessage("Test Message");
    assertEquals("Test Message", ModuleBlock.getBlockerMessage());
  }

  @Test
  void getBlockerMessage_encodesHtml() {
    ModuleBlock.setMessage("<script>alert(1)</script>");
    String result = ModuleBlock.getBlockerMessage();
    assertFalse(result.contains("<script>"));
  }

  @Test
  void getBlockerMessage_encodesAmpersand() {
    ModuleBlock.setMessage("A & B");
    String result = ModuleBlock.getBlockerMessage();
    assertFalse(result.contains(" & "));
  }

  @Test
  void getBlockerMessage_emptyAfterReset() {
    ModuleBlock.setMessage("something");
    ModuleBlock.reset();
    assertEquals("", ModuleBlock.getBlockerMessage());
  }
}
