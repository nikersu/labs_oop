@echo off
REM Скрипт для запуска Newman тестов в Windows
REM Убедитесь, что приложение Spring Boot запущено на порту 8080

echo ==========================================
echo Запуск Newman тестов для Labs OOP API
echo ==========================================

REM Проверка наличия Newman
where newman >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Newman не установлен. Установите его командой:
    echo npm install -g newman
    exit /b 1
)

REM Проверка наличия файлов
if not exist "LabsOOP.postman_collection.json" (
    echo Ошибка: файл коллекции LabsOOP.postman_collection.json не найден
    exit /b 1
)

if not exist "LabsOOP.postman_environment.json" (
    echo Ошибка: файл окружения LabsOOP.postman_environment.json не найден
    exit /b 1
)

REM Запуск тестов
echo Запуск тестов...
newman run LabsOOP.postman_collection.json ^
    -e LabsOOP.postman_environment.json ^
    --reporters cli,json ^
    --reporter-json-export newman-report.json

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo Все тесты прошли успешно!
    echo ==========================================
) else (
    echo.
    echo ==========================================
    echo Некоторые тесты не прошли. Код выхода: %ERRORLEVEL%
    echo ==========================================
)

exit /b %ERRORLEVEL%



