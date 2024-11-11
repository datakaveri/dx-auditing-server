<p align="center">
<img src="./docs/cdpg.png" width="300">
</p>

We use a Git merge-based workflow. To get started, please refer to the [Git Pull Request Model](https://github.com/kailash/software-engineering/blob/master/git/pull-request-model.rst) document.

## Code Style
- To maintain consistent code quality, we use the Maven Checkstyle plugin. Learn more about it [here](https://maven.apache.org/plugins/maven-checkstyle-plugin/index.html).
- For code inspection and defect analysis, we use the PMD plugin to enforce best practices and remove common coding issues. More information is available [here](https://maven.apache.org/plugins/maven-pmd-plugin/).
- Generate PMD, Checkstyle, and Copy/Paste Detector (CPD) reports by running:
  ```
  mvn checkstyle:checkstyle pmd:pmd pmd:cpd
  ```
  Reports will be generated in the `./target/site` folder.
- To expedite Checkstyle issue resolution, install one of the following IDE plugins (for IntelliJ):
  - [google-java-format](https://github.com/google/google-java-format?tab=readme-ov-file)
  - [CheckStyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)

## About Commit Messages
The `git commit` command is used to save staged changes in the local repository.

To stage files:
```
git add <file>
```

To commit changes:
```
git commit -m "<subject>" -m "<description>"
```

### Commit Subject
- Keep the subject to 50 characters or less.
- Use imperative tone (e.g., `Change query` instead of `Changes made in the query` or `Changed the query`) to improve clarity and consistency.
- Use a short tag at the beginning to indicate the purpose: `[feat, fix, style, refactor, test, docs, chore]`.

### Commit Description
- Provide a clear and concise description, ideally no more than 72 characters.
- End punctuation is optional.
- Reference issues or pull requests when applicable.