#!/bin/bash
if type -p java; then
    _java=java
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$version" == "1.8.0_251" ]]; then
        java -Djava.library.path=bin -jar TGDragon.jar
    else
        echo Please install java 1.8.0_251 x64
    fi
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    _java="$JAVA_HOME/bin/java"
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$version" == "1.8.0_251" ]]; then
        java -Djava.library.path=bin -jar TGDragon.jar
    else
        echo Please install java 1.8.0_251 x64
    fi
else
    echo "no java"
fi