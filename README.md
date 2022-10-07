# Smile serde plugin for kafka-ui

This is sample pluggable serde implementation for [kafka-ui](https://github.com/provectus/kafka-ui/).

This serde uses [Jackson library](https://github.com/FasterXML/jackson-dataformats-binary) as a [Smile](https://github.com/FasterXML/smile-format-specification) format parser/generator implementation. 

Jackson supports optimisations for Smile [encoding](https://github.com/FasterXML/jackson-dataformats-binary/blob/2.14/smile/src/main/java/com/fasterxml/jackson/dataformat/smile/SmileGenerator.java#L27) and [decoding](https://github.com/FasterXML/jackson-dataformats-binary/blob/2.14/smile/src/main/java/com/fasterxml/jackson/dataformat/smile/SmileParser.java#L20) that you can enable via serde configuration.

For sample serde usage and configuration please see docker-compose file [here](docker-compose/setup-example.yaml).