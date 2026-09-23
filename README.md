# HyrvoAI

## AI-Powered Company Helpdesk using Retrieval-Augmented Generation

HyrvoAI is an AI-powered company helpdesk and knowledge assistant designed to help employees and website visitors obtain information from company documents using natural language.

The system uses **Retrieval-Augmented Generation (RAG)** to retrieve relevant information from company documents and provide grounded AI-generated responses.

HyrvoAI supports authenticated employee conversations, administrator document management, multi-company data isolation, public document access, OAuth authentication, and an embeddable AI helpdesk widget for company websites.

---

## Project Team

This project was developed by:

- **Abhinav Mishra**
- **Aman Chauhan**
- **Akshay Kumar Dwivedi**

### Academic Project

This project was developed as a **B.Tech Computer Science and Engineering major project**.

---

# Table of Contents

- [Project Overview](#project-overview)
- [Problem Statement](#problem-statement)
- [Objectives](#objectives)
- [Key Features](#key-features)
- [How HyrvoAI Works](#how-hyrvoai-works)
- [RAG Pipeline](#rag-pipeline)
- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Application Modules](#application-modules)
- [Authentication](#authentication)
- [Role-Based Access Control](#role-based-access-control)
- [Multi-Tenant Architecture](#multi-tenant-architecture)
- [Document Management](#document-management)
- [Public AI Widget](#public-ai-widget)
- [Chat System](#chat-system)
- [Security](#security)
- [Database](#database)
- [Vector Search](#vector-search)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Prerequisites](#prerequisites)
- [Installation and Setup](#installation-and-setup)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Demo Account](#demo-account)
- [Limitations](#limitations)
- [Future Scope](#future-scope)
- [Conclusion](#conclusion)
- [Project Team](#project-team)

---

# Project Overview

Organizations maintain a large amount of information in documents such as:

- HR policies
- Leave policies
- Employee handbooks
- Company guidelines
- Department manuals
- Internal procedures
- Public company information

Finding specific information from these documents manually can be time-consuming.

HyrvoAI provides a conversational interface where users can simply ask questions instead of manually searching through documents.

For example:

> "What is the company's leave policy?"

HyrvoAI retrieves the relevant information from the company's document knowledge base and uses the retrieved content as context for the AI model.

This makes the generated response more relevant to the organization's actual documentation.

---

# Problem Statement

Traditional company helpdesks often require employees to:

1. Search through multiple documents.
2. Find the appropriate policy or manual.
3. Read large amounts of information.
4. Contact HR or administrators when the required information cannot be found.

General-purpose AI assistants also cannot automatically know a company's private policies and documentation.

HyrvoAI addresses this problem by combining:

- Document processing
- Semantic search
- Vector embeddings
- Retrieval-Augmented Generation
- Access control
- Company-level isolation
- Conversational AI

The result is an AI helpdesk capable of answering questions using company-specific knowledge.

---

# Objectives

The main objectives of HyrvoAI are:

- Build an AI-powered company knowledge assistant.
- Implement Retrieval-Augmented Generation.
- Store document embeddings using PostgreSQL and pgvector.
- Allow administrators to upload company documents.
- Allow users to ask questions about company documentation.
- Provide grounded answers using retrieved documents.
- Support employee authentication.
- Support Google OAuth authentication.
- Support LinkedIn OAuth authentication.
- Implement role-based access control.
- Support multiple companies.
- Isolate company data during retrieval.
- Support public and employee-only documents.
- Provide an embeddable public AI widget.
- Support anonymous public conversations.
- Implement basic rate limiting.
- Implement protection against common prompt-injection attempts.

---

# Key Features

## 1. AI Company Helpdesk

Users can ask questions in natural language about company documents.

---

## 2. Retrieval-Augmented Generation

HyrvoAI retrieves relevant document chunks before generating an answer.

This allows the AI response to be based on company-specific information.

---

## 3. Semantic Search

Documents are converted into vector embeddings and stored in PostgreSQL using pgvector.

Questions are compared against these embeddings to retrieve semantically relevant information.

---

## 4. Administrator Document Management

Administrators can:

- Upload documents
- Provide document titles
- Specify departments
- Specify document versions
- View uploaded documents
- Deactivate documents

---

## 5. Authentication

The system supports:

- Email/password authentication
- Google OAuth
- LinkedIn OAuth

---

## 6. Role-Based Access

HyrvoAI supports different user roles, including:

- Administrator
- Employee

Administrative functionality is protected using role-based authorization.

---

## 7. Multi-Company Support

Multiple companies can use the same HyrvoAI backend.

Company-specific filtering prevents documents from one company from being returned to another company's users.

---

## 8. Public and Employee Documents

Documents support two access levels:

```text
PUBLIC
EMPLOYEE
```
