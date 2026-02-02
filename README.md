# Project Job Aggregator System

An intelligent job aggregation system that automatically collects, filters, and notifies about Backend Java Junior and Internship positions via Telegram.

## Features

- **Multi-Source Aggregation**: Collects job postings from LinkedIn, JSearch API, and RSS feeds
- **Smart Filtering**: Automatically filters for Java Junior/Internship positions only
- **Automated Notifications**: Sends updates 5 times daily via Telegram (6AM, 10AM, 2PM, 6PM, 10PM)
- **Zero Maintenance**: Fully automated with scheduled execution
- **Persistent Storage**: PostgreSQL database for job tracking and deduplication

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2
- **Database**: PostgreSQL 15
- **Web Scraping**: Jsoup, Rome RSS Parser
- **APIs**: JSearch (RapidAPI), Adzuna, Google Gemini AI
- **Messaging**: Telegram Bot API
- **Deployment**: Docker, Docker Compose, GitHub Actions

## Quick Start

### Local Development

```bash
# Clone repository
git clone https://github.com/viniciustck/project-job-aggregator-system.git
cd project-job-aggregator-system

# Configure environment
cp .env.example .env
# Edit .env with your API keys

# Start services
docker-compose up -d

# View logs
docker logs job_hunter_app -f
```

### Production Deployment (VPS with Portainer)

See detailed deployment guide in `docs/deployment.md`

**Summary**:
1. Push to GitHub (GitHub Actions builds automatically)
2. Create Stack in Portainer using `docker-compose.prod.yml`
3. Configure environment variables
4. Deploy

## Configuration

### Required Environment Variables

```env
# Database
DB_PASSWORD=your_secure_password

# Telegram Bot
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_CHAT_ID=your_chat_id

# AI Processing
GEMINI_API_KEY=your_gemini_key
```

### Optional API Keys (Recommended for more results)

```env
# JSearch API (RapidAPI)
JSEARCH_API_KEY=your_jsearch_key

# Adzuna API
ADZUNA_APP_ID=your_app_id
ADZUNA_APP_KEY=your_app_key
```

## API Endpoints

```bash
# Manually trigger job notification
POST /api/jobs/notify-today

# Example
curl -X POST http://localhost:8080/api/jobs/notify-today
```

## Job Sources

| Source | Type | Status | Cost |
|--------|------|--------|------|
| LinkedIn | Web Scraping | Active | Free |
| JSearch API | REST API | Active | Free (with API key) |
| RSS Feeds | RSS Parser | Active | Free |
| Adzuna API | REST API | Configurable | Free (with API key) |

## Scheduling

- **Job Collection**: On application startup
- **Notifications**: 5 times daily at 6AM, 10AM, 2PM, 6PM, 10PM (server timezone)

## Development

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/job-aggregator-0.0.1-SNAPSHOT.jar
```

### Tests

```bash
mvn test
```

## Project Structure

```
project-job-aggregator-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/vinicius/jobhunter/
│   │   │       ├── client/          # External API clients
│   │   │       ├── controller/      # REST endpoints
│   │   │       ├── crawler/         # Job source crawlers
│   │   │       ├── dto/             # Data transfer objects
│   │   │       ├── model/           # JPA entities
│   │   │       ├── repository/      # Data access layer
│   │   │       └── service/         # Business logic
│   │   └── resources/
│   │       └── application.yml      # Application configuration
│   └── test/                        # Unit and integration tests
├── docker-compose.yml               # Local development
├── docker-compose.prod.yml          # Production deployment
├── Dockerfile                       # Container image definition
└── .github/workflows/               # CI/CD pipelines
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit changes (`git commit -m 'Add new feature'`)
4. Push to branch (`git push origin feature/new-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Roadmap

- [ ] Web dashboard for job management
- [ ] AI-powered salary analysis
- [ ] User-specific filter customization
- [ ] Multi-language support
- [ ] Analytics dashboard

## Support

For issues and questions, please open an issue on GitHub.

---

**Built with Spring Boot and modern Java technologies**
