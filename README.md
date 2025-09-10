Cloud Share API (Backend)
This is the backend for the Cloud Share application, a REST API built with Spring Boot. This API is responsible for handling user authentication, file management (upload, download, delete), payment processing, and database management.

✨ Features
RESTful API: Clean and standard API endpoints to communicate with the frontend.

Secure Authentication: Secure user authentication using Clerk and JWT.

File Management: Efficiently store and manage files using MongoDB.

Payment Integration: Handle payments and subscriptions via the Razorpay SDK.

Webhook Support: Integrated webhook endpoint to handle real-time events from Clerk.

Centralized Configuration: Manage all application settings from a central properties file.

🛠️ Tech Stack
Framework: Spring Boot 3.5.5

Language: Java 21

Database: MongoDB

Security: Spring Security, JSON Web Tokens (JWT) via Clerk

Payments: Razorpay SDK

Build Tool: Apache Maven

Utilities: Lombok

🚀 Getting Started
Follow the steps below to set up and run the project on your local machine.

Prerequisites
Ensure you have the following software installed on your system:

Java Development Kit (JDK) 21

Apache Maven

MongoDB (running on the default port 27017)

The application will now be running on http://localhost:8080/api/v1.0.