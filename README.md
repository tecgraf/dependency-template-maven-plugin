[![](https://img.shields.io/maven-central/v/br.puc-rio.tecgraf/dependency-template-maven-plugin)](https://search.maven.org/artifact/br.puc-rio.tecgraf/dependency-template-maven-plugin)
[![](https://img.shields.io/badge/TECGRAF-PUC--RIO-lightgrey)](https://www.tecgraf.puc-rio.br/)

# dependency-template-maven-plugin

Maven plugin to get the list of project dependencies and apply it to templates.
The generated content can be stored in a file or a system property.

Two templates are used during the process: main and artifact templates.

The plugin is configured to run by default in the `compile` Maven phase.

## Main template

Receives the formatted list of dependencies.

Default value:
```
{{artifacts}}
```

## Artifact template

Used to format each dependency from the list.

Default value:
```
{{groupId}}:{{artifactId}}-{{version}}{{ifClassifier:-}}{{classifier}}.{{type}}
```

## Template tokens

| Token | Template | Description |
| --- | --- | --- |
| {{artifacts}} | Main | formatted list of dependencies |
| {{groupId}} | Artifact | group identifier |
| {{artifactId}} | Artifact | artifact identifier |
| {{version}} | Artifact | artifact version |
| {{ifClassifier:?}} | Artifact | value if classifier exists |
| {{classifier}} | Artifact | artifact classifier |
| {{type}} | Artifact | artifact type |

## Parameters

| Parameter | Description | Default value |
| --- | --- |---------------|
| mainTemplateFile | Location of the file to override the main template's default value. |               |
| artifactTemplateFile | Location of the file to override the artifact template's default value. |               |
| separator | Separator used between artifacts. | |
| lineBreak | Whether to add line break between artifacts. | true          |
| outputFile | Output file to store the generated content. |               |
| outputProperty | System property to store the generated content. |               |
| charset | Chartset used to read the template files. | UTF-8         |
| excludeTransitive | Whether to exclude transitive dependencies. | false         |
| excludeGroupIds | Comma separated list of groupId Names to exclude. Empty String indicates don't exclude anything. |               |
| excludeArtifactIds | Comma separated list of artifact names to exclude. Empty String indicates don't exclude anything. |               |
| excludeClassifiers | Comma separated list of classifiers to exclude. Empty String indicates don't exclude anything. |               |
| excludeTypes | Comma separated list of types to exclude. Empty String indicates don't exclude anything. |               |

## Example 1

pom.xml:
```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-numbers-angle</artifactId>
            <version>1.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>br.puc-rio.tecgraf</groupId>
                <artifactId>dependency-template-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>dependency-template</goal>
                        </goals>
                        <configuration>
                            <outputFile>target/output.txt</outputFile>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

generated file:

```
org.apache.commons:commons-numbers-angle-1.1.jar
org.apache.commons:commons-numbers-core-1.1.jar
```

## Example 2

pom.xml:
```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-numbers-angle</artifactId>
            <version>1.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>br.puc-rio.tecgraf</groupId>
                <artifactId>dependency-template-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>dependency-template</goal>
                        </goals>
                        <configuration>
                            <excludeTransitive>true</excludeTransitive>
                            <outputFile>target/output.txt</outputFile>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

generated file:

```
org.apache.commons:commons-numbers-angle-1.1.jar
```

## Example 3

artifact template file:

```
{{artifactId}}-{{version}}
```

main template file:

```
These are the project dependencies: {{artifacts}}
```

pom.xml:
```xml
    <dependencies>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-numbers-angle</artifactId>
            <version>1.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>br.puc-rio.tecgraf</groupId>
                <artifactId>dependency-template-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>dependency-template</goal>
                        </goals>
                        <configuration>
                            <mainTemplateFile>src/main/resources/templates/mainTemplate.txt</mainTemplateFile>
                            <artifactTemplateFile>src/main/resources/templates/artifactTemplate.txt</artifactTemplateFile>
                            <separator>,</separator>
                            <lineBreak>false</lineBreak>
                            <outputFile>target/output.txt</outputFile>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

generated file:

```
These are the project dependencies: commons-numbers-angle-1.1,commons-numbers-core-1.1
```



