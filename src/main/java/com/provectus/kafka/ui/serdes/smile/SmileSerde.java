package com.provectus.kafka.ui.serdes.smile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.fasterxml.jackson.dataformat.smile.SmileParser;
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper;
import com.provectus.kafka.ui.serde.api.DeserializeResult;
import com.provectus.kafka.ui.serde.api.PropertyResolver;
import com.provectus.kafka.ui.serde.api.RecordHeaders;
import com.provectus.kafka.ui.serde.api.SchemaDescription;
import com.provectus.kafka.ui.serde.api.Serde;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class SmileSerde implements Serde {

  private static final JsonMapper JSON_MAPPER = new JsonMapper();

  private SmileMapper smileMapper;

  @Override
  public void configure(PropertyResolver serdeProperties,
                        PropertyResolver clusterProperties,
                        PropertyResolver appProperties) {
    SmileFactory factory = new SmileFactory();

    serdeProperties.getMapProperty("generator", SmileGenerator.Feature.class, Boolean.class)
        .ifPresent(featureState -> featureState.forEach(factory::configure));

    serdeProperties.getMapProperty("parser", SmileParser.Feature.class, Boolean.class)
        .ifPresent(featureState -> featureState.forEach(factory::configure));

    this.smileMapper = new SmileMapper(factory);
  }

  @Override
  public Optional<String> getDescription() {
    return Optional.empty();
  }

  @Override
  public Optional<SchemaDescription> getSchema(String topic, Target target) {
    return Optional.empty();
  }

  @Override
  public boolean canDeserialize(String topic, Target target) {
    return true;
  }

  @Override
  public boolean canSerialize(String topic, Target target) {
    return true;
  }

  @Override
  public Serializer serializer(String topic, Target target) {
    return new Serializer() {
      @Override
      public byte[] serialize(String inputString) {
        try {
          JsonNode jsonNode = JSON_MAPPER.readTree(inputString);
          return smileMapper.writeValueAsBytes(jsonNode);
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Serialization error", e);
        }
      }
    };
  }

  @Override
  public Deserializer deserializer(String topic, Target target) {
    return new Deserializer() {
      @Override
      public DeserializeResult deserialize(RecordHeaders recordHeaders, byte[] bytes) {
        try {
          return new DeserializeResult(
              smileMapper.readTree(bytes).toString(),
              DeserializeResult.Type.JSON,
              Collections.emptyMap());
        } catch (IOException e) {
          throw new RuntimeException("Deserialization error", e);
        }
      }
    };
  }
}
