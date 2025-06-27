* **Code of Conduct**  
  Please ensure compliance by reviewing our [Code of Conduct](https://github.com/datakaveri/dx-auditing-server/blob/main/CODE_OF_CONDUCT.md).

* **PR Requirements Checklist** :clipboard:
  - [ ] The commit message aligns with our [contribution guidelines](https://github.com/datakaveri/dx-auditing-server/blob/main/CONTRIBUTING.md).
  - [ ] Tests have been added for all changes (bug fixes, features).
  - [ ] Documentation has been updated as necessary.
  - [ ] A separate branch is created for the changes (no direct commits to `main` or `master`).
  - [ ] No credentials or sensitive information is included in this commit.

* **Type of Change** :information_desk_person::scroll:  
  _(Bug fix, new feature, documentation update, refactor, chore, test, configuration, etc.)_  
  Briefly describe the changes made:

* **Issue Addressed** :wrench:  
  Fixes #(issue number, if applicable)

* **PR Checklist** :page_with_curl:
  - [ ] Relevant documentation has been updated.
  - [ ] Changes made in configuration files (`example-config`, `secrets/all-verticles-configs/config.json`) have been added to the [example-config](../example-configs/config-example.json).
  - [ ] Backend component changes (Elasticsearch, Postgres Flyway files, ImmuDB schema, RabbitMQ) have been communicated to the DX DevOps team by tagging [at]datakaveri/devops in a PR comment.
  - [ ] Comments added to clarify complex or critical code areas.
  - [ ] No new warnings are introduced with these changes.
  - [ ] Tests have been added or updated to verify functionality.
  - [ ] All existing and new unit tests pass locally.
  - [ ] PMD and Checkstyle issues have been resolved locally.
  - [ ] Reviewers have been assigned to the PR for feedback.
