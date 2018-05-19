# README

A trade off of running distributed applications is there are more
types of downstream provider failures that may impact the upstream
consumers.

## Failure Modes

Applications may fail in various ways:

1.  Hardware failures

2.  Network failures

3.  OS, Virtualization or Container software failures

4.  Application process failures

In our applications, we developers are on the hook to make sure
the application dependency and backing resource failures are handled
within the application.
But this means we must handle non-functional concerns, as well as the
business application concerns.

In Cloud orchestration architectures including Kubernetes and Cloud
Foundry, the Platform Operator role is responsible for hardening the
underlying platforms, but failures still may occur.
This is why we design our applications for *disposability*.

## Handling Failures with REST Applications

The scope of this example is to demonstrate failure detection of
application instances, and how Cloud Foundry can handle it.

This is where Spring Boot Actuator helps.
Actuator handles detection of backing resource dependencies in
our applications.

We will demonstrate how to use an Actuator to expose a custom
health check in a sample application.
Cloud Foundry will leverage the Actuator health checks to
dispose of unhealthy instances, and recover them.

## Prereqs

-   A Cloud Foundry account:
    -   Your account set up with `SpaceDeveloper` role
    -   Sufficient quota for 2 instances at 768M each
    -   A Pivotal Web Service account should be sufficient

-   JDK 8

-   Clone this project

-   Login to your Cloud Foundry account through `cf login`

## Review the Project

1.  The `HealthCheckExampleApplication` is a simple Spring Boot
    application.

1.  The `HelloController` will fail if an improper id is passed as
    part of a GET request.
    This is obviously not a realistic use case, but we want a
    deterministic why to induce failure when running on Cloud
    Foundry.

1.  The `HelloControllerHealthCheck` is an Actuator exposed health
    check that will detect a `HelloController` failure.

1.  Review the `application.properties`, the Actuator endpoint
    security is disabled for simplicity of running the example.

1.  Review the Cloud Foundry manifest `manifest.yml`.
    Note the HTTP health check and its associated endpoint
    configuration.
    Configure a unique route by filling in the `{unique id}` and one
    of your foundation domains as `{domain}`.

## Prep Steps

1.  From the project root, build the project:

    ```bash
    ./gradlew build
    ```

1.  Push the application:

    ```bash
    cf push
    ```

1.  Verify both app instances are running:

    ```bash
    cf app hello
    ```

1.  Review the app events:

    ```bash
    cf events hello
    ```

## Verify Happy Path

1.  Execute a successful GET request:

    ```bash
    curl -i http://{route from your manifest}/0
    ```

1.  Verify the Actuator health check:

    ```bash
    curl -i http://{route from your manifest}/health
    ```

    What do you see?
    You should see HTTP 200 and payload with STATUS of UP.

## Induce Process Failure

1.  Open a separate terminal window, and tail the application logs:

    ```bash
    cf logs hello
    ```

1.  Execute a failure mode GET request:

    ```bash
    curl -i http://{route from your manifest}/1
    ```

    You should see an HTTP 500 error.


1.  Verify the Actuator health check:

    ```bash
    curl -i http://{route from your manifest}/health
    ```

    What do you see?  You should see HTTP 503 and payload with
    STATUS of DOWN.

1.  Check state of the Cloud Foundry `hello` application.

    ```bash
    cf app hello
    ```

    You might need to refresh, given Cloud Foundry's 30 second
    health check interval.

1.  What states do you see an instance of the `hello` application?
    You might see a `crashed` state, followed by `starting`.
    You should see Cloud Foundry recovery of the instance in a
    `running` state.

1.  Review the app logs.
    If you find the logs verbose, search your terminal window by
    `HEALTH` search pattern.
    What do you see?
    You should see the following:

    ```bash
    ERR Failed to make HTTP request to '/health' on port 8080: received status code 503
    ```

1.  Review the application events:

    ```bash
    cf events hello
    ```

    What do you see?
    You should see an event for the application crash.

## Summary

This a simple example of how Cloud Foundry's health check mechanism
may be used to dispose of unhealthy instances.
Without an application layer health check, it may be possible that
an application instance process may be running, not healthy, and
causing cascading failures in upstream consumers.
