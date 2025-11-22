# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Lince PLUS is a software for systematic observation studies of sports and health. It's a JavaFX desktop application built with Spring Boot 3.5.5, Java 17, and Maven for behavioral video analysis and research.

## Build Commands

### Local Development
- **Build locally**: `./src/mvn-install-locally.sh` (equivalent to `mvn clean install -Plocal`)
- **Full build with installer**: `./src/build.sh` (requires INSTALL4J_LICENSE environment variable)
- **Run tests**: `mvn test` (from `src/` directory)
- **Run single test**: `mvn test -Dtest=YourTestClass` (from `src/` directory)
- **Run single test method**: `mvn test -Dtest=YourTestClass#methodName` (from `src/` directory)
- **Package without tests**: `mvn clean package -DskipTests`

### Version Management
- **Update version**: `./src/version.sh` (interactive script for version updates and git commits)

## Project Structure

This is a multi-module Maven project with the following modules:

- **lince-data**: Core data models, services, and business logic
- **lince-data-fx**: JavaFX-specific data components and adapters
- **lince-desktop**: Main JavaFX desktop application (`LinceAppFx` entry point)
- **lince-ai**: AI and statistical analysis components (includes agreement analysis using DKPro)
- **lince-math**: Mathematical calculations using Renjin R engine integration
- **lince-transcoding**: Video transcoding capabilities using FFmpeg

### Key Technologies
- **Frontend**: JavaFX 16 (desktop GUI framework)
- **Backend**: Spring Boot 3.5.5 with embedded web server
- **Build**: Maven multi-module project
- **Installer**: Install4J for cross-platform installers
- **Video Processing**: FFmpeg via JavaCV/ByteDeco
- **Statistics**: Renjin R engine for statistical computations
- **Testing**: JUnit 5 with Mockito

## Application Architecture

The application follows a layered architecture:
- **Desktop Layer** (`lince-desktop`): JavaFX controllers and UI components
  - `com.lince.observer.desktop.spring.controller`: Spring REST controllers (PageController, ComponentController, StreamingController)
  - JavaFX UI components and FXML-based views
- **Service Layer** (`lince-data`): Business logic services (AnalysisService, CategoryService, ProfileService, SessionService, SystemService)
- **Data Layer**: Spring Data repositories and entity models
- **External Integrations**: AI analysis, video transcoding, and R statistical computations

Main entry point: `com.lince.observer.desktop.LinceAppFx` → `LinceApp`

The application runs an embedded web server (random port) for REST API access and uses Spring Boot's actuator endpoints for monitoring.

## Development Environment

- **Java Version**: 17 (Liberica JDK recommended)
- **JavaFX**: Version 16 for macOS compatibility
- **Maven**: Standard Maven project structure (all Maven commands should be run from `src/` directory)
- **IDE**: Project includes IntelliJ IDEA configuration

## Dependencies

Key external dependencies:
- Spring Boot starter modules
- Jackson for JSON processing
- Apache Commons (IO, Lang3, Collections4, Math3)
- Guava for utilities
- MapStruct for object mapping
- Renjin for R integration
- FFmpeg for video processing
- Install4J for native installers

## Testing

- Unit tests use JUnit 5 and Mockito
- Test execution: `mvn test` (from `src/` directory)
- Tests are skipped in installer build (`-DskipTests`)
- The `lince-data-fx` module provides a test-jar with test utilities (includes `EmptyLinceApp` for testing)

## Build Profiles

- **default**: Includes Install4J plugin for building installers (active by default)
- **local**: Skips Install4J plugin execution for faster local builds (`mvn clean install -Plocal`)

## CI/CD

GitHub Actions workflow (`.github/workflows/maven.yml`):
- Triggers on pushes to `master` and `develop` branches
- Builds with Java 17 (Liberica distribution)
- Creates installers for Windows (.exe) and macOS (.dmg) for both ARM and x86
- Deploys to GitHub Packages on master branch (non-SNAPSHOT versions)
- Creates draft releases with installer artifacts

## Special Notes

- The project requires Install4J license for building installers
- R package integration is handled through Renjin (Java-based R implementation)
- Video transcoding uses FFmpeg with multiple adapter options (JavaCV, HumbleVideo)
- Application supports both local profiles and cloud deployment configurations
- The application uses a random server port (configured in `application.properties`) to avoid conflicts
- Internationalization is supported (English, Spanish, Catalan, German) via message properties files