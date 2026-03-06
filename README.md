# WeatherApp
A JavaFX weather app that shows current weather and 3-day forecast using OpenWeatherMap API.
Weather Information App

Description:
This JavaFX application provides real-time weather information using the OpenWeatherMap API.
Users can search weather by city name and view temperature, humidity, weather condition, and a short-term forecast.

Features:

Real-time weather data using OpenWeatherMap API

JavaFX graphical user interface

City input with validation

Temperature unit conversion (Celsius / Fahrenheit)

Weather forecast display

Search history with timestamps

History persistence using file storage

Error handling for invalid inputs and API failures

Technologies Used:

Java 25

JavaFX

OpenWeatherMap API

org.json library

How to Run:

Create a free account at OpenWeatherMap
 and generate your own API key.

Create a file named config.properties in the project root directory.

Add your API key in the file like this:

API_KEY=your_own_api_key_here

Make sure config.properties is listed in .gitignore to prevent exposing your key on GitHub.

Compile the program using javac with JavaFX and JSON library.

Run the program using java command with module-path.

Enter a city name (e.g., Kabul, London, Tehran).

Click "Search Weather" to view results.

Author: Mozhda Mohammadi
