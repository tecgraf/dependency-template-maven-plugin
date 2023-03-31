package br.puc_rio.tecgraf.maven.plugin.dependency.template;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

/**
 * Maven plugin to get the list of project dependencies and apply it to templates.
 * The generated content can be stored in a file or a system property.
 * <p>
 * Two templates are used during the process: main and artifact templates.
 * The main template receives the formatted list of dependencies.
 * The artifact template is used to format each dependency from the list.
 * <p>
 * The plugin is configured to run by default in the "compile" maven phase.
 */
@Mojo(name = "dependency-template", defaultPhase = LifecyclePhase.COMPILE, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DependencyTemplateMojo extends AbstractMojo {

  protected static final String DEFAULT_ARTIFACT_TEMPLATE =
    "{{groupId}}:{{artifactId}}-{{version}}{{ifClassifier:-}}{{classifier}}.{{type}}";

  @Parameter(defaultValue = "${project}", readonly = true) protected MavenProject mavenProject;

  /**
   * Location of the file to override the main template's default value.
   */
  @Parameter protected String mainTemplateFile;

  /**
   * Location of the file to override the artifact template's default value.
   */
  @Parameter protected String artifactTemplateFile;

  /**
   * Whether to exclude transitive dependencies.
   */
  @Parameter(defaultValue = "false") protected Boolean excludeTransitive;

  /**
   * Comma separated list of groupId Names to exclude. Empty String indicates don't exclude anything.
   */
  @Parameter protected List<String> excludeGroupIds;

  /**
   * Comma separated list of artifact names to exclude. Empty String indicates don't exclude anything.
   */
  @Parameter protected List<String> excludeArtifactIds;

  /**
   * Comma separated list of classifiers to exclude. Empty String indicates don't exclude anything.
   */
  @Parameter protected List<String> excludeClassifiers;

  /**
   * Comma separated list of types to exclude. Empty String indicates don't exclude anything.
   */
  @Parameter protected List<String> excludeTypes;

  /**
   * Output file to store the generated content.
   */
  @Parameter protected String outputFile;

  /**
   * System property to store the generated content.
   */
  @Parameter protected String outputProperty;

  /**
   * Chartset used to read the template files.
   */
  @Parameter(defaultValue = "UTF-8") protected String charset;

  /**
   * Separator used between artifacts.
   */
  @Parameter() protected String separator;

  /**
   * Whether to add line break between artifacts.
   */
  @Parameter(defaultValue = "true") protected Boolean lineBreak;

  @Override public void execute() throws MojoExecutionException {
    try {
      String artifactTemplate = DependencyTemplateMojo.DEFAULT_ARTIFACT_TEMPLATE;
      if (this.artifactTemplateFile != null) {
        final byte[] bytes = Files.readAllBytes(Paths.get(this.artifactTemplateFile));
        artifactTemplate = new String(bytes, this.charset);
      }

      Set<?> unfilteredArtifacts;
      if (this.excludeTransitive) {
        unfilteredArtifacts = this.mavenProject.getDependencyArtifacts();
      }
      else {
        unfilteredArtifacts = this.mavenProject.getArtifacts();
      }

      final Set<String> excludeGroupIds = toSet(this.excludeGroupIds);
      final Set<String> excludeArtifactIds = toSet(this.excludeArtifactIds);
      final Set<String> excludeClassifiers = toSet(this.excludeClassifiers);
      final Set<String> excludeTypes = toSet(this.excludeTypes);


      List<Artifact> artifacts = new ArrayList<>();
      for (Object obj : unfilteredArtifacts) {
        Artifact artifact = (Artifact) obj;
        if (!excludeGroupIds.isEmpty() && excludeGroupIds.contains(artifact.getGroupId())) {
          continue;
        }
        if (!excludeArtifactIds.isEmpty() && excludeArtifactIds.contains(artifact.getArtifactId())) {
          continue;
        }
        if (!excludeTypes.isEmpty() && excludeTypes.contains(artifact.getType())) {
          continue;
        }
        if (!excludeClassifiers.isEmpty() && artifact.getClassifier() != null && excludeClassifiers.contains(
          artifact.getClassifier())) {
          continue;
        }

        artifacts.add(artifact);
      }

      getLog().info("Artifact list size: " + artifacts.size());

      Collections.sort(artifacts);

      String finalSeparator = "";
      if (this.separator != null) {
        finalSeparator += this.separator;
      }
      if (this.lineBreak) {
        finalSeparator += "\n";
      }

      final StringJoiner dependencyJoiner = new StringJoiner(finalSeparator);

      for (Artifact artifact : artifacts) {
        String dependencyAsString = applyArtifactTemplate(artifact, artifactTemplate);
        dependencyJoiner.add(dependencyAsString);
      }

      String mainTemplate = "{{artifacts}}";
      if (this.mainTemplateFile != null) {
        final byte[] bytes = Files.readAllBytes(Paths.get(this.mainTemplateFile));
        mainTemplate = new String(bytes, this.charset);
      }

      mainTemplate = mainTemplate.replace("{{artifacts}}", dependencyJoiner.toString());
      if (this.outputProperty != null) {
        System.setProperty(this.outputProperty, mainTemplate);
        getLog().info("Updated system property: " + this.outputProperty);
      }
      if (this.outputFile != null) {
        Files.createDirectories(Paths.get(this.outputFile).getParent());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputFile))) {
          writer.write(mainTemplate);
        }
        getLog().info("Generated file: " + this.outputFile);
      }
    }
    catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(e);
    }
  }

  private String applyArtifactTemplate(Artifact artifact, String template) {
    String str = template;

    str = str.replace("{{groupId}}", artifact.getGroupId());
    str = str.replace("{{artifactId}}", artifact.getArtifactId());
    str = str.replace("{{version}}", artifact.getVersion());
    str = str.replace("{{type}}", artifact.getType());
    str = str.replace("{{scope}}", artifact.getScope());

    String classifier = artifact.getClassifier() != null ? artifact.getClassifier() : "";
    str = str.replace("{{classifier}}", classifier);

    int firstIndex = str.indexOf("{{ifClassifier:");
    if (firstIndex > -1) {
      int lastIndex = str.indexOf("}}", firstIndex + 15);
      if (lastIndex > -1) {
        String content = str.substring(firstIndex + 15, lastIndex);
        String replacement = classifier.isEmpty() ? "" : content + artifact.getClassifier();
        str = str.replace("{{ifClassifier:" + content + "}}", replacement);
      }
    }
    return str;
  }

  private Set<String> toSet(List<String> list) {
    final Set<String> set = new HashSet<>();
    if (list != null) {
      for (String value : list) {
        if (value != null) {
          set.add(value.toLowerCase());
        }
      }
    }
    return set;
  }

}
