# Brainworm

## 1. If you had the ability to know which food is rotten, would you get it? 
Our API scrapes the FDA database, so that it finds recalls to certain foods. If a user wants to type in a specific word, the API would come out with a recall or not. Who would use it? People who are paranoid, as well as people who want to be safe. If anyone wants to know which foods are recalled, the API and app would notify them. In case, a user buys a lot of this food, the user can get notified on that specific food recall. It could save them from getting sick, or even worse. 

## 2. Resources
| Resource | Key fields | Relationships |
|---|---|---|
| User | id, user, isAdmin | User tracks many RegistryItems |
| RegistryItem | id, product, batch | RegistryItems has many RecalledFood |
| RecalledFood | id, product, contaminant, recallDate, manuDate, batch | RecallFood has one RegistryItem

## 3. ER sketch
Tables, primary and foreign keys, and cardinality. Edit this Mermaid diagram (it renders on GitHub;
try changes at https://mermaid.live):

```mermaid
erDiagram
    USER ||--o{ REGISTRY_ITEM : owns
    REGISTRY_ITEM ||--o{ RECALLED_FOOD : triggers
    USER {
        bigint id PK
        string user UK
        boolean isAdmin
    }
    REGISTRY_ITEM {
        bigint id PK
        bigint user_id FK
        string product
        string batch "nullable"
    }
    RECALLED_FOOD {
        bigint id PK
        string product
        string contaminant
        date recallDate
        date manuDate
        string batch "nullable"
    }
```

## 4. Endpoints
| Verb | Path | Auth | Purpose |
|---|---|---|---|
| GET | /api/v1/users/me | user | retrieves current user's password and email |
| GET | /api/v1/registry?page=0&size=20 | user | list monitored foods |
| GET | /api/v1/registry/{itemId} | user | retrieves details for a single monitored food | 
| GET | /api/v1/recalls?sort=recallDate,desc | user | lists active food recalls for user's foods | 
| GET | /api/v1/recalls/{recallId} | user | views details of a specific alert | 
| POST | /api/v1/registry | user | adds a new food item |
| PUT | /api/v1/registry/{itemId} | user | replaces a monitored food's details |
| DELETE | /api/v1/registry/{itemId} | user | removes food item from monitoring | 
| GET | /api/v1/users | admin | lists all users in system |
| GET | /api/v1/users/{userId} | admin | lists a specific user |
| PATCH | /api/v1/users/{userId} | admin | updates a user | 
| DELETE | /api/v1/users/{userId} | admin | completely deletes a user | 

## 5. Technical choices
- **Database host:** 
- **OAuth2 provider:** 
- **Repo layout:** We'll split the API into two repositories. The API repository will be the backend, to which will store all the API routes, the database, as well as any other backend. The App repository will be utilized for Android Studio. It will cover Kotlin, and everything the frontend has to offer. 

## 6. Risks


## 7. Team and Sprint 1
Who owns what in Sprint 1. Link your Project board and Sprint 1 milestone.
