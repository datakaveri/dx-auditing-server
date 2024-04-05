[![Jenkins Build](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520auditing-server%2520(v5.5.0)%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20auditing-server%20(v5.5.0)%20pipeline/lastBuild/)
[![Jenkins Tests](https://img.shields.io/jenkins/tests?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520auditing-server%2520(v5.5.0)%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20auditing-server%20(v5.5.0)%20pipeline/lastBuild/testReport/)
[![Jenkins Coverage](https://img.shields.io/jenkins/coverage/jacoco?jobUrl=https%3A%2F%2Fjenkins.iudx.io%2Fjob%2Fiudx%2520auditing-server%2520(v5.5.0)%2520pipeline%2F)](https://jenkins.iudx.io/job/iudx%20auditing-server%20(v5.5.0)%20pipeline/lastBuild/jacoco/)

# iudx-auditing-server
The Auditing Server is is [IUDX's](https://iudx.org.in) data access logging server. It logs API access information from various DX components. Additionally it logs the count and size of data consumed on every API call. This enables data providers to figure out who is consuming which resource of theirs. It also allows consumers to track how many times and how much they have consumed data of a certain resource.

## Features
- Asynchronous logging of API access information
- Immutable logs enabled by [immudb](https://immudb.io/) database
- Fast reads enabled by [PostgreSQL](https://www.postgresql.org/) database
- Logging of streaming data consumption
- Cache layer for IUDX catalogue server calls


## Get Started

### Prerequisite - Make configuration
Make a config file based on the template in `./configs/config-example.json` 
- Generate a certificate using Lets Encrypt or other methods
- Make a Java Keystore File and mention its path and password in the appropriate sections
- Modify the database url and associated credentials in the appropriate sections

### Docker based
1. Install docker and docker-compose
2. Clone this repo
3. Build the images 
   ` ./docker/build.sh`
4. Modify the `docker-compose.yml` file to map the config file you just created
5. Start the server in production (prod) or development (dev) mode using docker-compose 
   ` docker-compose up prod `


### Maven based
1. Install java 11 and maven
2. Use the maven exec plugin based starter to start the server 
   `mvn clean compile exec:java@auditing-server`

### Redeployer
A hot-swappable redeployer is provided for quick development 
`./redeploy.sh`

### Testing

### Unit tests
1. Run the tests using `mvn clean test checkstyle:checkstyle pmd:pmd`  
2. Reports are stored in `./target/`


## Contributing
We follow Git Merge based workflow 
1. Fork this repository
2. Create a new feature branch in your fork. Multiple features must have a hyphen separated name, or refer to a milestone name as mentioned in Github -> Projects  
4. Commit to your fork and raise a Pull Request with upstream


## License
[View License](./LICENSE)


