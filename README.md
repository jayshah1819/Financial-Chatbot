🧠 About the Project

This is a Java-based AI chatbot that acts as a financial assistant. I built it using:


Google's Gemini API for generating intelligent responses to user queries.

Alpha Vantage API for fetching real-time stock prices.
<img width="1268" alt="Screenshot 2025-06-12 at 7 40 51 PM" src="https://github.com/user-attachments/assets/20498462-1f1b-434b-90bd-628e650275a0" />

Key features I implemented:

Recognizes company names (e.g., "Apple") and maps them to their stock ticker (e.g., "AAPL").

Parses natural language to extract stock-related questions.

Handles API rate limiting with retry logic.

Provides general financial guidance across areas like personal finance, investments, crypto, and real estate.

All communication is through the terminal using
 Scanner, and HTTP requests are made using OkHttp. The chatbot interprets user input and intelligently decides whether to answer through Gemini or fetch stock data via Alpha Vantage.
