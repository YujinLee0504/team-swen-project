# U-Fund: YourLocalCupboard

Version 3.0
An online U-Fund system built in Java **21** and Angular **20**
  
## Team

- Nick Scioli
- Nathan Romine
- Giovanni Gakau
- Yujin Lee
- James Durante


## Prerequisites

- Java **21** (Make sure to have correct JAVA_HOME setup in your environment)
- Maven
- Angular
- Leaflet


## How to run it

1. Clone the repository and go to the root directory.
2. Open two terminals
3. In the first terminal, navigate to ufund-api and execute `mvn compile exec:java`
4. In the second terminal, navigate to ufund-ui and execute `ng serve`
5. If the previous instruction fails due to lack of local dependencies, it may help to run `npm install` once in ufund-ui
6. Open in your browser `http://localhost:4200/`

## Known bugs and disclaimers

Note: Hard coded admin password: abc123

1. If a need is deleted by the admin while in a helper's basket, it is not removed from the basket (there is no error, just that the pledge won't do anything)

## How to test it

The Maven build script provides hooks for run unit tests and generate code coverage
reports in HTML.

To run tests on all layers together do this:

1. Execute `mvn clean test jacoco:report`
2. Open in your browser the file at `PROJECT_API_HOME/target/site/jacoco/index.html`

To run tests on a single layer do this:

1. Execute `mvn clean test-compile surefire:test@layer jacoco:report@layer` where `layer` is one of `api`, `business`, `persistence`
2. Open in your browser the file at `PROJECT_API_HOME/target/site/jacoco/{api, business, persistence}/index.html`

To run tests on all the layers in isolation do this:

1. Execute `mvn exec:exec@tests-and-coverage`
2. To view the API layer tests open in your browser the file at `PROJECT_API_HOME/target/site/jacoco/api/index.html`
3. To view the Business layer tests open in your browser the file at `PROJECT_API_HOME/target/site/jacoco/business/index.html`
4. To view the Persistence layer tests open in your browser the file at `PROJECT_API_HOME/target/site/jacoco/persistence/index.html`

*(Consider using `mvn clean verify` to attest you have reached the target threshold for coverage)
  
  
## How to generate the Design documentation PDF

1. Access the `PROJECT_DOCS_HOME/` directory
2. Execute `mvn exec:exec@docs`
3. The generated PDF will be in `PROJECT_DOCS_HOME/` directory


## How to setup/run/test program 
1. Tester, first obtain the Acceptance Test plan
2. IP address of target machine running the app
3. Execute ________
4. ...
5. ...

## License

MIT License

See LICENSE for details.
