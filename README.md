# 🚗 ODAK - Real-Time Driver Drowsiness Tracking System

ODAK is an advanced, AI-powered safety ecosystem developed as a graduation project supported by **TÜBİTAK 2209-B**. The system focuses on preventing accidents by detecting driver fatigue and drowsiness through real-time facial expression analysis.

![Kotlin](https://img.shields.io/badge/Mobile-Kotlin_Native-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![ASP.NET Core](https://img.shields.io/badge/Backend-ASP.NET_Core_MVC-512BD4?style=for-the-badge&logo=dotnet&logoColor=white)
![Python](https://img.shields.io/badge/AI-Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![SignalR](https://img.shields.io/badge/RealTime-SignalR-orange?style=for-the-badge)

## 🌟 Project Architecture & Workflow

The project is built on a tripartite architecture, ensuring seamless communication between AI, mobile, and web layers:

1.  **AI Layer (Computer Vision):** A CNN-based model (YOLOv8/TensorFlow) trained to detect drowsiness signs using **EAR** (Eye Aspect Ratio) and **MAR** (Mouth Aspect Ratio) metrics.
2.  **Mobile App (The Edge):** Developed with **Kotlin Native**. It processes camera frames, detects anomalies, and sends real-time alerts to the backend via **SignalR**.
3.  **Web Dashboard:** An **ASP.NET Core MVC** management panel where fleet managers can monitor driver statistics and receive instant fatigue alerts.

## 🚀 Key Features

- **Real-Time Detection:** Instant drowsiness and fatigue analysis using MediaPipe and custom AI models.
- **SignalR Integration:** Zero-latency data synchronization between the driver and the control center.
- **Comprehensive Logging:** Every fatigue event is logged into a SQL Server database for historical analysis.
- **Local AI Processing:** Optimized mobile performance with TFLite integration.

## 👨‍💻 Developers

This project was developed by a dedicated team of engineers:

- **Emre Dönmez** - [GitHub](https://github.com/Emrefire) | [LinkedIn](https://linkedin.com/in/emredönmez41)
- **Mustafa Aslan** - [GitHub](https://github.com/mustafaaslan1) | [LinkedIn](https://www.linkedin.com/in/aslanmustafa33/)

## 🏗️ Folder Structure

- `/AI-Model`: Python training scripts, datasets, and model conversion (TFLite) logic.
- `/Mobile-App`: Native Android application built with Kotlin.
- `/Web-Dashboard`: ASP.NET Core MVC project including SignalR Hubs and Management UI.
- `script.sql`: Database schema and initial data to set up the environment.

## 🛠️ Tech Stack

- **Mobile:** Kotlin, CameraX, Retrofit, SignalR Client.
- **Backend:** .NET 8, SignalR Hubs, Entity Framework Core.
- **AI:** Python, OpenCV, MediaPipe, TensorFlow/Keras.
- **Database:** MSSQL Server.

---
*Supported by TÜBİTAK 2209-A Program.*
