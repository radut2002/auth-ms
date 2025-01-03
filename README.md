# Authentication Microservices

**_A collection of microservices for 🚀-fast and secure JWT-based authentication._**

**🔑 Why?**

Many service architectures (microservice-style or not) need some kind of authentication. Especially when you have stateless services, password-based authentication against each service is cumbersome and often leads to security problems.
In those cases, **token-based authentication** is helpful. Authenticate your users with a secret (e.g. password) against the JWT service and then use only the trusted tokens generated from it to authenticate against the other services.
Normally you'd still have a user database with additional user details which you can consult _after_ the JWT service authenticated the user and provided valid tokens.

---

- [Features](#features)
- [Architecture](#architecture)
- [Examples and Usage](#examples-and-usage)
- [Build and Run](#build-and-run)
- [A Word on Security](#a-word-on-security)

## Features

- REST services with 3 main endpoints
    - `/register` for creating a user
    - `/login` for authenticating a user, i.e. creating tokens for a registered user
    - `/refresh` for creating a new access token from a refresh ("remember-me") token
- endpoints for updating stored users (username, secret, roles) and removing them and their current tokens
- password-based authentication against the JWT server
- secure (read more on [security](#a-word-on-security))
- scheduled auto-cleanup of expired tokens
- Fault tolerance (retry and fallback) for store-service calls
- `Server-Timing` headers in responses for timing and efficiency tracing
- lightweight and fast
- built with the [Quarkus](https://quarkus.io/) framework
    - ultra-efficient native (binary) builds possible (no JVM needed)
- ready-to-use Docker compose files

## Architecture

Each microservice has a single distinct responsibility for easier approachability and maintainability:

- **login-server**
    - registration, login, re-login and management of user credentials
    - routes user credentials to/from the `credentials-store` microservice
    - if the provided credentials match the ones stored in the credentials-store, it consults the `jwt-server` and returns the generated tokens to the caller
    - routes refresh tokens to the `token-store` to find a match and upon success, consults the jwt-server for a new token pair
- **jwt-server**
    - creates access and refresh tokens
    - consults the `token-store` microservice for storing and retrieving refresh tokens
    - the only instance with the private key for creating tokens; other services verify these tokens with the corresponding public key 
- **credentials-store**
    - stores and updates user credentials (username, id, secret (securely hashed), groups a user belongs to)
- **token-store**
    - stores refresh tokens
    - cleans up expired tokens

**Simplified data flows:**

![API data flow for register endpoint](./docs/img/flow_register.svg)

![API data flow for login endpoint](./docs/img/flow_login.svg)

![API data flow for refresh endpoint](./docs/img/flow_refresh.svg)

## Examples and Usage

Replace `$HOST` with the host name or IP address the service is running on.

ℹ️ For testing and debugging purposes you can access demo HTML forms for each microservice, e.g. `$HOST/register.html` or `$HOST/refresh.html`.

**Register and auto-login (create token pair):**
```
curl -XPOST \
     -H "Content-Type: application/json" \
     -d '{ "username": "john_doe", "secret": "secure-password" }' \
     http://$HOST/auth/register
```
Returns the JWT (access token), its expiration date and the user ID in the response body, and a refresh token in the `Set-Cookie` header.
This process stores the credentials securely in the database (service: _credentials-store_) as well as the refresh token (service: _token-store_).
Credentials are stored without any `roles`; you can add them by calling the appropriate REST endpoint, see below.

**Register only (no login):**
Append `?no-login` to the register URL. The response contains only the user ID (the _internal_ user ID, which you may use for updating the credentials, see below).

**Login:**
```
curl -XPOST \
     -H "Content-Type: application/json" \
     -d '{ "username": "john_doe", "secret": "secure-password" }' \
     http://$HOST/auth/login
```

**Refresh token:**
```
curl -XPOST \
     -H "Content-Type: application/json" \
     --cookie $REFRESH_COOKIE \
     http://$HOST/auth/refresh
```

Replace `$REFRESH_COOKIE` with the refresh token value you received from the login/register endpoint.

**Update credentials:**

Username/Secret:
```
curl -XPUT \
     -H "Content-Type: text/plain" \
     -d "new_password" \
     http://$HOST/auth/$USER_ID/secret
```
Replace `secret` with `username` to change the username. You need a valid access token of the appropriate user or a token with the `ROLE_ADMIN` role.

Groups:
```
curl -XPUT \
     -H "Content-Type: application/json" \
     -d '["ROLE_ADMIN", "ROLE_USER"]' \
     http://$HOST/auth/$USER_ID/groups
```
You need a valid access token with the `ROLE_ADMIN` role to change the groups.

## Build and Run

There are multiple ways for building and running these microservices.

- **[Build with Java and run inside JVM](#build-with-java-and-run-inside-jvm)**
    - ✅ Fastest compilation (some seconds),
    - ✅ neither GraalVM nor Docker required,
    - ❌ but JVM (Java 11+) required,
    - ❌ slower than the native build binary at runtime + higher memory consumption and
    - ❌ noticeable slower startup and shutdown times (ca. 1 s).
- **[Build and run native binary locally](#build-and-run-native-binary-locally)**
    - ✅ Best runtime performance, little memory usage, native to your platform,
    - ✅ fastest startup and shutdown (less than 0.1 s),
    - ✅ no JVM required at runtime, no Docker required.
    - ❌ GraalVM required for compilation,
    - ❌ native builds not portable to other operating systems (rebuild required),
    - ❌ long compilation times (1+ min.).
    - ⚠️ The database-dependent microservices still need a JVM runtime for the embedded H2 database (though you are free to replace these services with a custom implementation without this constraint, of course).
- **[Build and run native images inside Docker container](#build-and-run-native-images-inside-docker-container)**
    - ✅ Docker is single requirement, everything else happens inside container.
    - ✅ As portable as Docker is.
    - ✅ Other advantages of native images apply.
    - ✅ System isolation and management via Docker (compose) file.
    - ❌ Even longer compilation times (minutes) and
    - ❌ general overhead that comes with Docker.
- **[Build Docker-native images locally and run inside Docker container](#build-docker-native-images-locally-and-run-inside-docker-container)**
    - ✅ Once compiled locally, same advantages of previous method apply,
    - ✅ but faster compilation if you have the JVM/GraalVM/Maven tool-chain installed anyway.
    - ❌ Same downsides of previous method apply,
    - ❌ plus JVM/GraalVM needed for compilation.

### Build With Java and Run Inside JVM

#### Build

**With the build helper script (recommended):** Run the `build-microservices.sh` script from the root directory.
Without arguments, the `package` goal is assumed, so you'll end up with JAR files. You can pass any goal and argument to the script (e.g. `-DskipTests`).

ℹ️ _Note: The first time, you need to run the `build-shared-modules.sh` script which installs the shared modules into your local_ .m2 _directory._

**Or manually:** Build with Maven by running the `package` goal in every microservice directory. As shared modules in this project aren't distributed via a Maven repository, you'll need to `install` them locally first.

#### Run

Run all generated microservices from the `target/` directories of each microservice (`*-runner.jar`).

### Build and Run Native Binary Locally

#### Build

Same as JVM method but also pass `-Pnative` to the build script or Maven.

#### Run

Run all generated microservices from the `target/` directories of each microservice (`*-runner`).

ℹ️ _Note: Microservices which are not native-compatible (the `-store` services) still need to be run in a JVM (`java -jar *-runner.jar`)._

### Build and Run Native Images Inside Docker Container

See the [Quarkus Docs](https://quarkus.io/guides/building-native-image#creating-a-container-with-a-multi-stage-docker-build) on how to achieve such multi-stage Docker builds.

### Build Docker-native Images Locally and Run Inside Docker Container

First, make sure your Docker service is running. Then:

**With the build helper script (recommended):**
```
./build-microservices.sh package -Pnative -Dquarkus.native.container-build=true
```

This creates Docker-native builds of all microservices and runs `docker build` on all Dockerfiles.
After that, you can run all containers.

**Run with docker-compose (recommended):**
```
docker-compose up
```

The compose-file only exposes the login-server microservice, via port 8080.

See the `docker-compose.yml` file in the root directory for details.

**Or manually:**
```
docker run -i --rm -p $PORT:8080 $SERVICE
```
Replace `$PORT` with the local port you want to be forwarded to the container.
Replace `$SERVICE` with the microservice name (docker tag, as created by the previous command).
Remove the `--rm` flag if you want to persist the container between runs.

Without docker-compose, you also need to create a bridge network. See the [Docker Docs](https://docs.docker.com/network/bridge/) for instructions.

## A Word on Security

Obviously, even more so than performance, **security** is a major aspect of an authorization service. That's why the following measures were taken to secure the service and its users:

- the JWTs are **signed** (not encrypted as they hold no secret data)
- this repo comes with pre-generated keys and certificates
    - they are **RS256** (RSA signature with SHA-256) which is considered very secure
    - please generate your own keys and certificates; the existing ones are just for a quickstart
- all services relying on JWTs not only should check for a valid JWT (this is done automatically by Quarkus/Smallrye which we use here) but also the issuer (see the `mp.jwt.verify.issuer` property)
    - the microservices here are already configured this way
- the provided docker-compose file is configured in a way that only the login-server is exposed
    - the _jwt-server_ should never be reachable by any other service because it generates tokens for the data you hand in, no questions asked
- only the access token is returned in the HTTP response body; the long-living **refresh token** is returned as a `http-only` and `path`-restricted cookie
    - that way, it cannot be read via scripting (JavaScript) and is transmitted only to the `/auth` endpoints by the user agent (such cookies are very secure; browser storage is _not_)
- the access token has a short lifespan of just 15 minutes by default; refreshing that is simple by calling the appropriate endpoint
- very little data is stored; just the bare minimum to provide the feature set
    - use your own services to store more user data
- passwords are **hashed and salted with Bcrypt** before they reach the credentials-store service; the plaintext password provided to the login-server do not get send on to another service
    - there are no password policies; you may want to install policies in another service which e.g. requires a certain minimal character length for passwords; this service is not the right place for this
- access control is handled by _groups_: a user credentials entry has a set of groups which allow him/her to access different services
    - define your own groups for your applications
    - users created by the `/register` endpoint have no groups assigned
    - the `ROLE_ADMIN` is a group which is needed to assign groups to a user credentials entry; users (even with a valid access token) _cannot_ change their groups (otherwise they could gain more rights)
    - i.e. by using the normal API endpoints you cannot add admin users to your store; if you need any, you have to add them manually
