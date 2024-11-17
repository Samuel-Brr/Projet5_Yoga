# Yoga App !


# Backend Testing Guide

This document provides instructions for running backend tests and generating coverage reports for the Java Spring Boot application.

## Running Tests

### Running All Tests

To run all tests including unit tests and integration tests:

```bash
mvn test
```

### Running Specific Test Classes

To run tests from a specific test class:

```bash
mvn test -Dtest=ClassName
```

Example:
```bash
mvn test -Dtest=UserServiceTest
```

### Running Tests by Category

To run tests from a specific package:

```bash
mvn test -Dtest="com.openclassrooms.starterjwt.services.*Test"
```

## Test Coverage Reports

### Generating Coverage Reports

We use JaCoCo for code coverage analysis. To generate a coverage report:

```bash
mvn clean test jacoco:report
```

This command will:
1. Clean the project
2. Run all tests
3. Generate a detailed coverage report

### Viewing Coverage Reports

After generating the report, you can find the coverage results in:
- HTML format: `target/site/jacoco/index.html`
- XML format: `target/site/jacoco/jacoco.xml`
- CSV format: `target/site/jacoco/jacoco.csv`

The HTML report provides a user-friendly interface to:
- View overall project coverage
- Drill down into specific packages and classes
- See line-by-line coverage analysis

### Generating Coverage Reports Alternative

You can also generate a coverage report by right-clicking on the project in IntelliJ IDEA and selecting `Run 'All Tests' with Coverage`.