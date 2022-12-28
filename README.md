[![](https://img.shields.io/maven-central/v/br.puc-rio.tecgraf/dependency-template-maven-plugin)](https://search.maven.org/artifact/br.puc-rio.tecgraf/dependency-template-maven-plugin)
[![](https://img.shields.io/badge/TECGRAF-PUC--RIO-lightgrey)](https://www.tecgraf.puc-rio.br/)

# dependency-template-maven-plugin

Maven plugin to get the list of project dependencies and apply it to templates.
The generated content can be stored in a file or a system property.

Two templates are used during the process: main and artifact templates.

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
| --- | --- | --- |
| mainTemplateFile | Location of the file to override the main template's default value. |  |
| artifactTemplateFile | Location of the file to override the artifact template's default value. | |
| separator | Separator used between artifacts. | \n |
| outputFile | Output file to store the generated content. | |
| outputProperty | System property to store the generated content. | |
| charset | Chartset used to read the template files. | UTF-8 |
| excludeTransitive | Whether to exclude transitive dependencies. | false |
| excludeGroupIds | Comma separated list of groupId Names to exclude. Empty String indicates don't exclude anything. | |
| excludeArtifactIds | Comma separated list of artifact names to exclude. Empty String indicates don't exclude anything. | |
| excludeClassifiers | Comma separated list of classifiers to exclude. Empty String indicates don't exclude anything. | |
| excludeTypes | Comma separated list of types to exclude. Empty String indicates don't exclude anything. | |

## Example




