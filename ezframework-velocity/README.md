# EzFramework - Velocity

Note: this module depends on the Velocity API at runtime and declares it as `provided`.

To run the module tests locally or in CI, activate the Maven profile that injects the Velocity API onto the test classpath:

```bash
# run tests for the velocity module (profile supplies velocity-api for tests)
mvn -f ezframework-velocity/pom.xml -Pwith-velocity-api test
```

In CI, enable the `with-velocity-api` profile so tests run successfully without bundling the API in the artifact.
