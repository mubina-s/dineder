# Architecture Overview

Dine-Der is a client/server Android application.

## Android frontend
- Java
- XML layouts
- Android Studio / Gradle
- Volley for HTTP requests
- WebSocket clients for live updates
- RecyclerView-based lists and cards

## Spring Boot backend
- Java 17
- Spring Web
- Spring WebSocket
- Spring Data JPA
- MySQL
- Swagger / OpenAPI
- REST controllers, services, repositories, entities, and DTOs

## Main data concepts
The project design includes users, restaurants, cuisines, images, sessions, session participants, votes, session preferences, session results, and app reviews.

## Real-time flows
WebSockets support features such as:
- Ready-up / lobby status
- App review updates
- Voting updates

## High-level flow
User -> Android App -> REST / WebSocket -> Spring Boot -> MySQL
