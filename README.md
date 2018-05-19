# README

## Overview

A trade off of running distributed applications is that downstream
provider failures may impact the upstream consumers.

If we were to run distributed applications with messaging integration
protocols, we could leverage features such as a combination of 
guaranteed delivery, expiry/timeout and/or retry as part of our
messaging integration solutions.
 
But in the case of REST applications running over HTTP, we do
not get any application level fault tolerance features as part of
the HTTP protocol.

The scope of this example is to demonstrate failure detection of
REST application instances, and how Cloud Foundry can handle it.

## Failure Modes

REST applications may fail in various ways:

1.  Hardware failures

2.  Network failures

3.  OS, Virtualization or Container software failures

4.  Application process failures

In Cloud Orchestation architectures including Kubernetes and Cloud
Foundry, the Platform Operator role is responsible for hardening the
underlying platforms, but failures still may occur.
This is why we design our applications for *disposability*.

In our applications, we developers are on the hook to make sure
the application dependency and backing resource failures are handled 
within the application.
But this means we must handle non-functional concerns, as well as the
business application concerns.

This is where Spring Boot helps, with Actuator.
Spring Boot and Actuator handles most of the pre-existing backing
resource integrations as dependencies in our applications.

We will demonstrate how to use custom health check logic to better
handle graceful failures in a sample application, and using Actuator
to expose it.
Cloud Foundry will leverage Actuator health checks to dispose of 
unhealthy instances, and recreate them.

## Prereqs

-   Cloud Foundry account, org and space:
    -   with you user set up with `SpaceDeveloper` role
    -   Sufficient quote for 2 instances at 768M each.
    -   A Pivotal Web Service account should be sufficient.

-   JDK 8

-   Clone this project.

-   Login to your Cloud Foundry account through `cf login`.

## Review the Project

1.  The `HealthCheckExampleApplication` is a simple Spring Boot
    application.

1.  The `HelloController` will fail if improper id is passed as part 
    of a GET request.
    This is obviously not a realistic use case, but we want a
    deterministic why to induce failure when running on Cloud Foundry.

1.  The `HelloControllerHealthCheck` is an Acutator exposed health
    check that will detect on the `HelloController` failure.

1.  Review the `application.properties`, the Actuator endpoint
    security is disabled for simplicity of running the example.

1.  Review the Cloud Foundry manifest `manifest.yml`.
    Note the HTTP health check and associated endpoint configuration.
    Configure a unique route by filling in the `{unique id}` and one 
    of your Foundation domains as `{domain}`.
 
## Prep Steps

1.  From the project root, build the project:

    ```bash
    ./gradlew build
    ```

1.  Push the application:

    ```bash
    cf push
    ```

1.  Verify both app instances running:

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
    