#!/bin/bash
java -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -jar target/ap_parser-1.0-SNAPSHOT-jar-with-dependencies.jar "$@"