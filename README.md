# BudgetMate - Premium AI Financial Management Application

BudgetMate is a native Android financial tracking application engineered with a high-end, dark-themed user interface (Deep Midnight Blue and Specular Metallic Gold). It blends double-entry ledger controls with real-time AI automation, camera OCR scanning, and live market telemetry.

---

## Core Features

* **Smart AI Chatbot & Logger**: Driven by the Google Gemini API. Parses natural language inputs (e.g., "Boba tea $5" or "Salary $2000") to automatically extract the amount, notes, and map the correct categories without manual form filling.
* **AI Receipt Scanner**: Built-in Optical Character Recognition (OCR) infrastructure deployed via the camera module to intercept, analyze, and pre-populate transaction data from physical bills.
* **Advanced Twin-Chart Analytics**:
    * *Grouped Bar Charts*: Custom layout configuration optimized to display 24 separate columns cleanly without viewport distortion.
    * *Cubic Bezier Line Charts*: Renders fluid cashflow velocity trendlines with real-time scalar node point values.
    * *Dual High-Contrast Pie Charts*: Discrete, mutually exclusive color palettes separating asset inflows and outflows for immediate readability.
    * *Behavioral Radar Charts*: Maps multivariate focus areas across core lifecycle categories (Food, Transport, Housing, Health, Entertainment, Shopping).
* **Live Market Telemetry**: Background workers polling live REST nodes to track top tech equities (FPT, VIC, VCB) and global indexes (S&P 500, NASDAQ, DOW JONES) using compact vector sparklines.
* **Local-First Security Layout**: Powered by a secure local SQLite sandboxed architecture via Room Persistence Library, ensuring all personal transactional records remain isolated on the device.
* **Document Export Engine**: Multi-threaded binary compilation of active time-series datasets into structured CSV and PDF documents saved directly to the system Downloads path.

---

## Project Directory Structure

```text
app/src/main/java/com/example/savemoneytime/
├── Intro/                           # Bootstrapping, Splash Screen & Onboarding
├── MainApplication/                 # Primary Core App Context
│   ├── Adapters/                    # Multi-view RecyclerView Binders (Chat, News, Transaction)
│   ├── FragmentInFragment/          # Sub-view tab structures (Expenses & Revenue Fragments)
│   ├── ViewModels/                  # LiveData state preservation layer
│   ├── HomeFragment.java            # US Format currency keypad logger
│   ├── ManageFragment.java          # Grouped historical data ledger with filtering pills
│   ├── StatsFragment.java           # Quad-Chart manager layout
│   └── MainActivity.java            # Root ViewPager2 Controller
├── database/                        # Room Persistence Tier (AppDatabase, ExpenseDao, RevenueDao)
├── model/                           # Primitive Schemas (TransactionItem, ExpenseEntity, RevenueEntity)
└── network/                         # Remote API REST Integration (Gemini, NewsApi, StockApi Nodes)
```
Tech Stack & Specifications

• Language: Native Java (Android SDK)
• Architecture Pattern: MVVM (Model-View-ViewModel)
• Data Persistence: Room Library Architecture / SQLite
• Network Tier: Retrofit 2 / OkHttp 3 Connection Interceptors
• Visualization: Custom configurations on top of MPAndroidChart engine
• Minimum SDK: API Level 26 (Android 8.0 Oreo)
• Target SDK: API Level 34 (Android 14)

Installation, Setup & Build Guides

Clone the Repository
Command: git clone https://github.com/danghaihoangwork-beep/BudgetMate.git

Configure Environment Access Tokens
Create a local.properties file in your root project directory and declare your functional API keys:

gemini.api.key
news.api.key
stock.api.key

License & Attribution
Distributed under private software development terms of ownership. Maintained by Dang Hoang (danghai.hoang.work@gmail.com).