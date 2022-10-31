package com.provectus.kafka.ui.serdes.smile;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.fasterxml.jackson.dataformat.smile.SmileParser;
import com.provectus.kafka.ui.serde.api.DeserializeResult;
import com.provectus.kafka.ui.serde.api.PropertyResolver;
import com.provectus.kafka.ui.serde.api.Serde;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;


class SmileSerdeTest {

  private final PropertyResolver resolverMock = mock(PropertyResolver.class);

  private SmileSerde smileSerde;

  @BeforeEach
  void initSerde() {
    smileSerde = new SmileSerde();
    smileSerde.configure(resolverMock, null, null);
  }

  @ParameterizedTest
  @EnumSource
  void canBeAppliedToAnyTopic(Serde.Target target) {
    assertTrue(smileSerde.canDeserialize("test", target));
    assertTrue(smileSerde.canSerialize("test", target));
  }

  @ParameterizedTest
  @EnumSource
  void doesNoProvideSchemaDescription(Serde.Target target) {
    assertTrue(smileSerde.getSchema("test", target).isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{ \"name\": \"Clark Kent\",  \"age\": 35 }",
      "123",
      "123.123",
      "\"string json\"",
      "null"
  })
  void serializeAndDeserializeWorksInPair(String jsonString) {
    var serializer = smileSerde.serializer("test", Serde.Target.VALUE);
    byte[] serializedBytes = serializer.serialize(jsonString);

    var deserializer = smileSerde.deserializer("test", Serde.Target.VALUE);
    var deserializeResult = deserializer.deserialize(null, serializedBytes);

    assertEquals(DeserializeResult.Type.JSON, deserializeResult.getType());
    assertTrue(deserializeResult.getAdditionalProperties().isEmpty());
    assertJsonEquals(jsonString, deserializeResult.getResult());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{ \"name\": \"Clark Kent\",  \"age\": 35 }",
      "123",
      "123.123",
      "\"string json\"",
      "null"
  })
  void byDefaultPayloadStartsWithSmilePrefix(String json) {
    var serializer = smileSerde.serializer("test", Serde.Target.VALUE);
    byte[] serializedBytes = serializer.serialize(json);
    assertTrue(new String(serializedBytes).startsWith(":)"));
  }


  @Test
  void generatorAndParserFeaturesCanBeTunedViaConfig() {
    // do not write smile header
    when(resolverMock.getMapProperty("generator", SmileGenerator.Feature.class, Boolean.class))
        .thenReturn(Optional.of(Map.of(SmileGenerator.Feature.WRITE_HEADER, false)));

    // do not require smile header while parsing
    when(resolverMock.getMapProperty("parser", SmileParser.Feature.class, Boolean.class))
        .thenReturn(Optional.of(Map.of(SmileParser.Feature.REQUIRE_HEADER, false)));

    smileSerde.configure(resolverMock, null, null);

    String json = "{ \"name\": \"Clark Kent\",  \"age\": 35 }";

    var serializer = smileSerde.serializer("test", Serde.Target.VALUE);
    byte[] serializedBytes = serializer.serialize(json);
    // checking that smile header wasn't added
    assertFalse(new String(serializedBytes).startsWith(":)"));

    var deserializer = smileSerde.deserializer("test", Serde.Target.VALUE);
    var deserializeResult = deserializer.deserialize(null, serializedBytes);
    assertJsonEquals(json, deserializeResult.getResult());
  }

  private void assertJsonEquals(String expected, String actual) {
    var mapper = new JsonMapper();
    try {
      assertEquals(mapper.readTree(expected), mapper.readTree(actual));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}