# LazyDrop Server

LazyDrop is a real-time, zero-setup file sharing platform that lets people instantly create secure drop sessions and transfer files across devices.

This repository contains the production backend powering LazyDrop — including session orchestration, real-time messaging, storage integration, and billing.

---

## ⚡ What is LazyDrop?

LazyDrop lets you:

- Create instant sharing sessions with a short code or QR
- Send files directly device-to-device via cloud relay
- Receive files in real time with zero accounts required
- Upgrade for higher limits and longer sessions

Built to feel like AirDrop — but works everywhere.

---

## 🧠 Platform Capabilities

- **Real-time sessions** with WebSocket broadcasting
- **Direct-to-storage uploads** using signed URLs
- **Guest & authenticated users**
- **Live participant tracking**
- **Plan-based limits (Free / Plus / Pro)**
- **Stripe-backed subscriptions**
- **Auto-expiring secure rooms**

---

## 🏗️ System Architecture

LazyDrop uses:

- Spring Boot (API + WebSocket Gateway)
- PostgreSQL (session & file metadata)
- Cloud Object Storage (file relay)
- JWT-based identity (guests & users)
- Event-driven WebSocket messaging
- Stripe for billing and entitlements

The backend is deployed as a containerized service and scaled horizontally.

---

## 🔴 Live Product

🌐 **Web App:** https://lazydrop.app  
📡 **API:** Private production service  
🔐 **Auth:** Supabase-backed  
💳 **Billing:** Stripe Checkout & Portal

This repository reflects the production codebase and is not intended to be run without the full platform configuration.

---

## 📸 Screenshots & Demo

(Coming soon)

---

## 👨🏽‍💻 Built By

Adebowale Adebayo  
Computer Science @ UNB  
Full-stack / Systems Engineer  
Founder of LazyDrop

---

## 🚧 Roadmap

- Native mobile clients
- Encrypted peer relay
- Multi-file streaming
- Session history
- Team workspaces
