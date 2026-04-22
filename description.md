

Skill Vault
A Rule-Driven Skill Economy with Evolution & Decay Modeling
Technical Specification Document
Version 1.0 | February 2026
 
Table of Contents
Right-click the TOC and select "Update Field" to refresh page numbers
1. Project Vision	1
2. Core Functional Modules	1
2.1 Skill Economy Engine (Barter & Credit System)	1
2.2 Skill Evolution & Decay Engine	1
2.3 Knowledge Decay Tracker	1
3. System Architecture	1
4. Database Schema	1
4.1 Users Table	1
4.2 Skills Table (Single Table Inheritance)	1
4.3 KnowledgeTopics Table	1
4.4 TradeSessions Table	1
4.5 Entity Relationships	1
5. Class Design & Implementation	1
6. API Specifications	1
6.1 User Controller	1
6.2 Skill Controller	1
6.3 Trade Controller	1
6.4 Knowledge Controller	1
6.5 WebSocket Endpoints	1
7. Frontend Specifications	1
8. Testing Strategy	1
8.1 Backend Testing	1
8.2 Frontend Testing	1
8.3 Example Test	1
9. Deployment Guide	1
10. Advanced Features	1
10.1 Authentication & Authorization	1
10.2 Real-time Notifications	1
10.3 Database Migration	1
10.4 Monitoring	1
10.5 Caching	1
10.6 API Documentation	1

 
1. Project Vision
Skill Vault is a full-stack system that models skills and knowledge as dynamic, evolving entities within a credit-based barter ecosystem. Unlike traditional learning platforms where skills are static entries, this system implements a state-transition architecture driven by rules and entropy modeling.
Core Capabilities:
•	Rewards teaching with credits that can be exchanged for learning sessions
•	Enables peer-to-peer skill barter between users in the ecosystem
•	Evolves skill strength based on usage patterns and peer validation
•	Applies decay to unused skills using sophisticated time-based logic
•	Tracks theoretical knowledge decay using forgetting-curve principles
•	Runs a background evolution engine that continuously updates system state
This is not CRUD. It is a state-transition system driven by rules and entropy modeling.
2. Core Functional Modules
2.1 Skill Economy Engine (Barter & Credit System)
Purpose:Implements a credit-based peer-to-peer skill exchange mechanism.
Credit Earning Rules:
•	Teaching a session: Credits earned based on session duration and rating
•	Receiving positive peer validation: Bonus credits for ratings above 4
Credit Spending Rules:
•	Booking a learning session: Credits deducted based on teacher's rate
Trade Flow:
0.	Student requests a skill session from a teacher
1.	Teacher accepts the request
2.	TradeSession entity is created with REQUESTED status
3.	On completion: Credits transferred, student rates teacher (1-5), skill evaluation triggered
TradeSession Entity (JPA):
@Entity
public class TradeSession {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sessionId;
    
    @ManyToOne
    private User teacher;
    
    @ManyToOne
    private User student;
    
    @ManyToOne
    private Skill skill;
    
    private double durationHours;
    private int rating; // 1-5
    
    @Enumerated(EnumType.STRING)
    private TradeStatus status; // REQUESTED -> ACCEPTED -> COMPLETED
    
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private double creditsTransferred;
}
2.2 Skill Evolution & Decay Engine
Purpose:Models skill growth and entropy over time using object-oriented design patterns.
Abstract Skill Design (JPA Inheritance):
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "skill_type")
public abstract class Skill {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID skillId;
    
    protected String name;
    protected double skillScore;         // 0-100
    protected double confidenceIndex;    // 0-100
    protected LocalDate lastUsed;
    protected int usageFrequency;
    
    @ManyToOne
    protected User owner;
    
    protected static final double DECAY_RATE = 0.5;
    
    public abstract void evaluate(double input);
    public abstract void applyDecay();
    
    public SkillLevel getLevel() {
        if (skillScore <= 30) return SkillLevel.BEGINNER;
        if (skillScore <= 70) return SkillLevel.INTERMEDIATE;
        return SkillLevel.ADVANCED;
    }
    
    public boolean isDecaying() {
        return confidenceIndex < 40.0;
    }
}
TechnicalSkill Implementation:
@Entity
@DiscriminatorValue("TECHNICAL")
public class TechnicalSkill extends Skill {
    private double practiceHours;
    private static final double GROWTH_FACTOR = 2.5;
    
    @Override
    public void evaluate(double hours) {
        this.practiceHours += hours;
        this.skillScore = Math.min(100, skillScore + (hours * GROWTH_FACTOR));
        this.confidenceIndex = Math.min(100, confidenceIndex + (hours * 0.5));
        this.usageFrequency++;
        this.lastUsed = LocalDate.now();
    }
    
    @Override
    public void applyDecay() {
        long daysSinceUse = ChronoUnit.DAYS.between(lastUsed, LocalDate.now());
        if (daysSinceUse > 7) {
            confidenceIndex -= daysSinceUse * DECAY_RATE;
            confidenceIndex = Math.max(0, confidenceIndex);
        }
    }
}
SoftSkill Implementation:
@Entity
@DiscriminatorValue("SOFT")
public class SoftSkill extends Skill {
    private static final double VALIDATION_WEIGHT = 3.0;
    
    @Override
    public void evaluate(double rating) {
        this.skillScore = Math.min(100, skillScore + (rating * VALIDATION_WEIGHT));
        this.confidenceIndex = Math.min(100, confidenceIndex + (rating * 0.8));
        this.usageFrequency++;
        this.lastUsed = LocalDate.now();
    }
    
    @Override
    public void applyDecay() {
        long daysSinceUse = ChronoUnit.DAYS.between(lastUsed, LocalDate.now());
        if (daysSinceUse > 14) { // Soft skills decay slower
            confidenceIndex -= daysSinceUse * (DECAY_RATE * 0.7);
            confidenceIndex = Math.max(0, confidenceIndex);
        }
    }
}
Skill State Levels:
Score Range	Level
0-30	Beginner
31-70	Intermediate
71-100	Advanced
Table 1: Skill Level Classification
Background Decay Service (Spring @Scheduled):
@Service
public class DecayEngine {
    
    @Autowired
    private SkillRepository skillRepository;
    
    @Autowired
    private KnowledgeRepository knowledgeRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Scheduled(fixedRate = 60000) // 1 minute interval
    @Transactional
    public void runDecayCycle() {
        scanAndUpdateSkills();
        scanAndUpdateKnowledge();
        generateAlerts();
    }
    
    private void scanAndUpdateSkills() {
        List<Skill> allSkills = skillRepository.findAll();
        allSkills.forEach(skill -> {
            double oldConfidence = skill.getConfidenceIndex();
            skill.applyDecay();
            if (skill.isDecaying() && oldConfidence >= 40) {
                notificationService.sendDecayAlert(skill.getOwner(), skill);
            }
        });
        skillRepository.saveAll(allSkills);
    }
}
2.3 Knowledge Decay Tracker
Purpose:Models forgetting curve for theoretical topics using exponential decay.
KnowledgeTopic Entity:
@Entity
public class KnowledgeTopic {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID topicId;
    
    private String title;
    private String subject;
    private double masteryLevel; // 0-100
    private LocalDate lastReviewed;
    private double decayRate; // Configurable per topic
    
    @Enumerated(EnumType.STRING)
    private RevisionStatus status;
    
    @ManyToOne
    private User owner;
    
    public void applyDecay() {
        long days = ChronoUnit.DAYS.between(lastReviewed, LocalDate.now());
        
        // Exponential decay model (Ebbinghaus)
        double decayFactor = Math.exp(-decayRate * days);
        this.masteryLevel = this.masteryLevel * decayFactor;
        
        if (this.masteryLevel < 40.0) {
            this.status = RevisionStatus.NEEDS_REVISION;
        }
    }
}
Decay Formula Options:
•	Linear Model: mastery -= (daysSinceLastReview * decayRate)
•	Exponential Model (Ebbinghaus): newMastery = oldMastery * e^(-k * days)
3. System Architecture
Technology Stack:
Category	Technology	Purpose
Backend	Java 17+	Core language
Framework	Spring Boot 3.x	Application framework
ORM	Spring Data JPA	Database operations
Database	PostgreSQL 14+	Relational storage
Frontend	React 18+	UI library
Build Tool	Maven 3.6+	Dependency management
Testing	JUnit 5, Jest	Unit & integration testing
Security	Spring Security + JWT	Authentication/Authorization
Real-time	WebSocket (STOMP)	Live notifications
Table 2: Technology Stack Overview
Backend Package Structure:
src/main/java/com/skillvault/
├── controller/          # REST API endpoints
│   ├── UserController.java
│   ├── SkillController.java
│   ├── TradeController.java
│   └── KnowledgeController.java
├── service/             # Business logic
│   ├── UserService.java
│   ├── SkillService.java
│   ├── TradeService.java
│   ├── CreditService.java
│   └── DecayEngine.java
├── repository/          # Spring Data JPA
│   ├── UserRepository.java
│   ├── SkillRepository.java
│   ├── TradeRepository.java
│   └── KnowledgeRepository.java
├── model/               # JPA Entities
│   ├── User.java
│   ├── Skill.java (abstract)
│   ├── TechnicalSkill.java
│   ├── SoftSkill.java
│   ├── KnowledgeTopic.java
│   └── TradeSession.java
├── dto/                 # Data Transfer Objects
├── config/              # Configuration classes
├── exception/           # Custom exceptions
└── security/            # JWT, filters
Frontend Structure (React):
src/
├── components/
│   ├── common/          # Header, Sidebar, Footer
│   ├── dashboard/       # CreditDisplay, DecayAlerts, SkillOverview
│   ├── skills/          # SkillList, SkillCard, SkillForm
│   ├── trades/          # TradeList, TradeRequestForm
│   └── knowledge/       # TopicList, TopicCard
├── pages/               # Dashboard, Skills, Knowledge, Trades
├── services/            # API services (Axios)
├── hooks/               # Custom React hooks
├── context/             # AuthContext, NotificationContext
└── utils/               # Constants, helpers
4. Database Schema
4.1 Users Table
Column	Type	Constraint	Description
user_id	UUID	PK	Unique identifier
username	VARCHAR(50)	UNIQUE	Display name
email	VARCHAR(100)	UNIQUE	Email address
password_hash	VARCHAR(255)		Encrypted password
credit_balance	DECIMAL(10,2)		Current credits
created_at	TIMESTAMP		Account creation
last_active	TIMESTAMP		Last activity
Table 3: Users Table Schema
4.2 Skills Table (Single Table Inheritance)
Column	Type	Constraint	Description
skill_id	UUID	PK	Unique identifier
user_id	UUID	FK	Owner reference
skill_name	VARCHAR(100)		Skill name
skill_type	VARCHAR(20)		TECHNICAL/SOFT
skill_score	DECIMAL(5,2)		Current score 0-100
confidence_index	DECIMAL(5,2)		Confidence 0-100
last_used	DATE		Last usage date
usage_frequency	INT		Total usage count
practice_hours	DECIMAL(5,2)		TechnicalSkill only
Table 4: Skills Table Schema
4.3 KnowledgeTopics Table
Column	Type	Constraint	Description
topic_id	UUID	PK	Unique identifier
user_id	UUID	FK	Owner reference
title	VARCHAR(200)		Topic title
subject	VARCHAR(100)		Subject category
mastery_level	DECIMAL(5,2)		Mastery 0-100
last_reviewed	DATE		Last review date
decay_rate	DECIMAL(5,4)		Decay rate constant
status	VARCHAR(20)		CURRENT/NEEDS_REVISION
Table 5: KnowledgeTopics Table Schema
4.4 TradeSessions Table
Column	Type	Constraint	Description
session_id	UUID	PK	Unique identifier
teacher_id	UUID	FK	Teacher reference
student_id	UUID	FK	Student reference
skill_id	UUID	FK	Skill reference
duration_hours	DECIMAL(4,2)		Session duration
credits_transferred	DECIMAL(10,2)		Credits exchanged
rating	INT		Student rating 1-5
status	VARCHAR(20)		REQUESTED/ACCEPTED/COMPLETED
created_at	TIMESTAMP		Request time
completed_at	TIMESTAMP		Completion time
Table 6: TradeSessions Table Schema
4.5 Entity Relationships
•	User 1:N Skill
•	User 1:N KnowledgeTopic
•	User 1:N TradeSession (as teacher)
•	User 1:N TradeSession (as student)
•	Skill 1:N TradeSession
•	Skill 1:N SkillHistory
5. Class Design & Implementation
OOP Concepts Demonstrated:
Concept	Implementation
Encapsulation	Private fields, getters/setters, DTOs
Abstraction	Abstract Skill class
Inheritance	TechnicalSkill, SoftSkill extend Skill
Polymorphism	Skill references pointing to child objects
Constructor Overloading	Multiple constructors in entities
Method Overloading	Overloaded service methods
Custom Exceptions	InvalidSkillException, SkillNotFoundException
Multithreading	@Scheduled DecayEngine
Collections	List, Set, Map usage in services
Comparable	Skill ranking and sorting
Table 7: OOP Concepts Implementation
User Entity Implementation:
@Entity
@Table(name = "users")
public class User implements Comparable<User> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    private double creditBalance = 100.0; // Initial credits
    
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Skill> skills = new ArrayList<>();
    
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<KnowledgeTopic> knowledgeTopics = new ArrayList<>();
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastActive;
    
    public void earnCredits(double amount) {
        this.creditBalance += amount;
    }
    
    public boolean spendCredits(double amount) {
        if (this.creditBalance >= amount) {
            this.creditBalance -= amount;
            return true;
        }
        return false;
    }
    
    @Override
    public int compareTo(User other) {
        return Double.compare(other.creditBalance, this.creditBalance);
    }
}
6. API Specifications
6.1 User Controller
GET    /api/users                    - List all users
GET    /api/users/{id}               - Get user by ID
POST   /api/users/register           - Register new user
POST   /api/users/login              - Authenticate user
PUT    /api/users/{id}               - Update user
GET    /api/users/{id}/skills        - Get user's skills
GET    /api/users/{id}/credits       - Get credit balance
6.2 Skill Controller
GET    /api/skills                   - List all skills (with filters)
GET    /api/skills/{id}              - Get skill by ID
POST   /api/skills                   - Create new skill
PUT    /api/skills/{id}              - Update skill
DELETE /api/skills/{id}              - Delete skill
POST   /api/skills/{id}/evaluate     - Evaluate skill with input
POST   /api/skills/{id}/apply-decay  - Manual decay trigger
GET    /api/skills/decaying          - Get all decaying skills
6.3 Trade Controller
GET    /api/trades                   - List all trade sessions
GET    /api/trades/{id}              - Get trade by ID
POST   /api/trades                   - Create trade request
PUT    /api/trades/{id}/accept       - Accept trade
PUT    /api/trades/{id}/complete     - Complete trade with rating
GET    /api/trades/user/{userId}     - Get user's trades
6.4 Knowledge Controller
GET    /api/knowledge                - List all topics
POST   /api/knowledge                - Create topic
PUT    /api/knowledge/{id}/review    - Mark as reviewed
GET    /api/knowledge/needs-revision - Get topics needing review
6.5 WebSocket Endpoints
/topic/alerts/{userId}                - Real-time decay alerts
/topic/trade-updates/{userId}         - Trade status updates
7. Frontend Specifications
Key React Components:
import React, { useEffect, useState } from 'react';
import { skillService } from '../services/skillService';
import { useWebSocket } from '../hooks/useWebSocket';

export const Dashboard = () => {
    const [skills, setSkills] = useState([]);
    const [decayingSkills, setDecayingSkills] = useState([]);
    const [credits, setCredits] = useState(0);
    const { subscribeToAlerts } = useWebSocket();
    
    useEffect(() => {
        loadDashboardData();
        subscribeToAlerts(handleNewAlert);
    }, []);
    
    const loadDashboardData = async () => {
        const [userSkills, balance] = await Promise.all([
            skillService.getUserSkills(),
            userService.getCreditBalance()
        ]);
        setSkills(userSkills);
        setDecayingSkills(userSkills.filter(s => s.confidenceIndex < 40));
        setCredits(balance);
    };
    
    return (
        <div className="dashboard">
            <CreditDisplay amount={credits} />
            <DecayAlerts skills={decayingSkills} />
            <SkillsOverview skills={skills} />
            <RecentActivity />
        </div>
    );
};
State Management:
•	React Context API for global state (auth, user)
•	React Query (TanStack Query) for server state
•	useState/useReducer for local state
•	WebSocket for real-time updates
UI Library:Tailwind CSS or Material-UI
Charts:Recharts for skill progress visualization
8. Testing Strategy
8.1 Backend Testing
Unit Tests (JUnit 5, Mockito):
•	UserServiceTest - credit transactions
•	SkillServiceTest - evolution, decay, level transitions
•	TradeServiceTest - state transitions, credit transfer
•	DecayEngineTest - scheduled operations
Integration Tests:
•	TradeControllerTest - complete trade flow
•	RepositoryTest - Spring Data JPA operations
•	WebSocketTest - real-time notifications
8.2 Frontend Testing
•	Jest - Unit testing for components
•	React Testing Library - Component integration
•	Cypress - End-to-end testing
8.3 Example Test
@SpringBootTest
public class SkillServiceTest {
    
    @Autowired
    private SkillService skillService;
    
    @Test
    void testSkillDecay() {
        User user = new User("testuser", "test@example.com", "password");
        TechnicalSkill skill = new TechnicalSkill("Java", user);
        skill.evaluate(10);
        
        assertEquals(25.0, skill.getSkillScore(), 0.01);
        
        skill.setLastUsed(LocalDate.now().minusDays(14));
        skill.applyDecay();
        
        assertTrue(skill.isDecaying());
        assertTrue(skill.getConfidenceIndex() < 50.0);
    }
}
9. Deployment Guide
Prerequisites:
•	Java JDK 17+
•	Node.js 18+
•	PostgreSQL 14+
•	Maven 3.6+
Backend Deployment Steps:
4.	Configure database connection in application.properties
5.	Run database migrations (Flyway/Liquibase)
6.	Build with Maven: mvn clean package
7.	Deploy JAR file to server
8.	Configure reverse proxy (Nginx)
Frontend Deployment Steps:
9.	Install dependencies: npm install
10.	Configure API endpoint in .env
11.	Build production bundle: npm run build
12.	Deploy static files to CDN or web server
10. Advanced Features
10.1 Authentication & Authorization
•	JWT token-based authentication
•	Spring Security configuration
•	Role-based access control (USER, ADMIN)
10.2 Real-time Notifications
•	WebSocket (STOMP) integration
•	Decay alerts pushed to client
•	Trade status updates
10.3 Database Migration
•	Flyway or Liquibase for schema versioning
10.4 Monitoring
•	Spring Boot Actuator
•	Micrometer metrics
•	Health checks
10.5 Caching
•	Redis for frequently accessed data
•	Cache eviction on skill updates
10.6 API Documentation
•	OpenAPI 3.0 (Swagger UI)
•	Auto-generated API documentation
 


Skill Vault
A Rule-Driven Skill Economy with Evolution & Decay Modeling
Technical Specification Document v1.0
© 2026 Skill Vault Project
