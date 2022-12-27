package br.puc_rio.tecgraf.maven.plugin.dependency.template;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

@Mojo(name = "dependency-template", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DependencyTemplateMojo extends AbstractMojo {

  protected static final String DEFAULT_TEMPLATE =
    "{{groupId}}:{{artifactId}}-{{version}}{{ifClassifier:-}}{{classifier}}.{{type}}";

  @Parameter(defaultValue = "${project}", readonly = true) protected MavenProject mavenProject;

  @Parameter protected String templateFile;

  @Parameter protected String dependencyTemplateFile;

  /**
   * If we should exclude transitive dependencies
   */
  @Parameter(defaultValue = "false") protected Boolean excludeTransitive;

  /**
   * Comma separated list of GroupId Names to exclude.
   */
  @Parameter protected List<String> excludeGroupIds;

  /**
   * Comma separated list of Artifact names to exclude.
   */
  @Parameter protected List<String> excludeArtifactIds;

  /**
   * Comma Separated list of Classifiers to exclude. Empty String indicates don't exclude anything (default).
   */
  @Parameter protected List<String> excludeClassifiers;

  /**
   * Comma Separated list of Types to exclude. Empty String indicates don't exclude anything (default).
   */
  @Parameter protected List<String> excludeTypes;

  @Parameter protected String outputFile;

  @Parameter protected String outputProperty;

  @Parameter(defaultValue = "UTF-8") protected String charset;

  @Parameter(defaultValue = "\n") protected String separator;

  @Override public void execute() throws MojoExecutionException {
    try {
      String dependencyTemplate = DependencyTemplateMojo.DEFAULT_TEMPLATE;
      if (this.dependencyTemplateFile != null) {
        final byte[] bytes = Files.readAllBytes(Paths.get(this.dependencyTemplateFile));
        dependencyTemplate = new String(bytes, this.charset);
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

      Collections.sort(artifacts);

      final StringJoiner dependencyJoiner = new StringJoiner(separator);

      for (Artifact artifact : artifacts) {
        String dependencyAsString = applyTemplate(artifact, dependencyTemplate);
        dependencyJoiner.add(dependencyAsString);
      }

      String template = "{{dependencies}}";
      if (this.templateFile != null) {
        final byte[] bytes = Files.readAllBytes(Paths.get(this.templateFile));
        template = new String(bytes, this.charset);
      }

      template = template.replace("{{dependencies}}", dependencyJoiner.toString());
      if (this.outputProperty != null) {
        System.setProperty(this.outputProperty, template);
      }
      if (this.outputFile != null) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputFile))) {
          writer.write(template);
        }
      }
    }
    catch (Exception e) {
      getLog().error(e);
      throw new MojoExecutionException(e);
    }
  }

  private String applyTemplate(Artifact artifact, String dependencyTemplate) {
    String str = dependencyTemplate;

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
