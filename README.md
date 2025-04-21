# AuthCraft

**AuthCraft** — это плагин аутентификации для серверов Minecraft (Spigot 1.16.5), обеспечивающий безопасную регистрацию, вход, двухфакторную аутентификацию (2FA) и управление данными игроков. Плагин находится в **бета-версии**, и некоторые функции могут работать нестабильно, так как проводится пользовательское тестирование. Мы приветствуем обратную связь через [Issues](https://github.com/HTTYDCraft/AuthCraft/issues) на GitHub.

## Основные возможности

- **Регистрация и вход**: Безопасное создание учетных записей с проверкой паролей.
- **Двухфакторная аутентификация**: Поддержка TOTP, Telegram и VK (VK требует внешней настройки).
- **База данных**: Поддержка SQLite и PostgreSQL с резервным копированием.
- **Шифрование**: Защита данных с помощью AES и хеширование паролей через BCrypt.
- **Лимбо-мир**: Игроки находятся в изолированном мире до аутентификации.
- **Ограничение соединений**: Защита от множественных подключений с одного IP.
- **Проверка Mojang**: Верификация лицензионных учетных записей.

## Установка

1. Скачайте `authcraft-<version>.jar` из [Releases](https://github.com/HTTYDCraft/AuthCraft/releases). (Временно недоступно, так как пока идет бета-тест - чтобы избежать ошибок, компилируйте проект сами с помощью любой IDE. После релиза скачать можно будет непосредственно из [Releases](https://github.com/HTTYDCraft/AuthCraft/releases))
2. Поместите JAR в папку `plugins` вашего сервера Spigot. (Сервер использует версию Spigot API 1.16.5 которая позволяет сделать максимальную совместимость с остальными. Если на какой то версии не работает - пожалуйста, сообщите об этом.)
3. Перезапустите сервер.
4. Настройте `config.yml` в папке `plugins/AuthCraft` (см. [Документацию для владельцев серверов](OWNERS.md)).
5. Протестируйте команды: `/register`, `/login`, `/2fa`.

## Документация

- [Документация для игроков](PLAYERS.md): Как использовать команды и 2FA.
- [Документация для владельцев серверов](OWNERS.md): Настройка плагина и базы данных.
- [Пользовательское соглашение](AGREEMENT.md): Ответственность игроков и владельцев серверов.

## Статус бета-версии

Это **бета-версия**, предназначенная для пользовательского тестирования. Некоторые функции, такие как полная интеграция VK-бота, могут быть ограничены. Сообщайте об ошибках или предложениях через [Issues](https://github.com/HTTYDCraft/AuthCraft/issues).

## Лицензия

MIT License. Подробности в [LICENSE](LICENSE).

---

# AuthCraft (English)

**AuthCraft** is an authentication plugin for Minecraft servers (Spigot 1.16.5), providing secure registration, login, two-factor authentication (2FA), and player data management. The plugin is in **beta**, and some features may not work as expected due to ongoing user testing. We welcome feedback via [Issues](https://github.com/HTTYDCraft/AuthCraft/issues) on GitHub.

## Key Features

- **Registration and Login**: Secure account creation with password validation.
- **Two-Factor Authentication**: Supports TOTP, Telegram, and VK (VK requires external setup).
- **Database**: Supports SQLite and PostgreSQL with backup functionality.
- **Encryption**: Data protection using AES and password hashing via BCrypt.
- **Limbo World**: Players are isolated until authenticated.
- **Connection Limiting**: Prevents multiple connections from the same IP.
- **Mojang Verification**: Validates licensed accounts.

## Installation

1. Download `authcraft-1.0.3.jar` from [Releases](https://github.com/HTTYDCraft/AuthCraft/releases).
2. Place the JAR in the `plugins` folder of your Spigot 1.16.5 server.
3. Restart the server.
4. Configure `config.yml` in the `plugins/AuthCraft` folder (see [Server Owners Documentation](OWNERS.md)).
5. Test commands: `/register`, `/login`, `/2fa`.

## Documentation

- [Player Documentation](PLAYERS.md): How to use commands and 2FA.
- [Server Owners Documentation](OWNERS.md): Plugin and database configuration.
- [User Agreement](AGREEMENT.md): Responsibilities of players and server owners.

## Beta Status

This is a **beta version** intended for user testing. Some features, such as full VK bot integration, may be limited. Report bugs or suggestions via [Issues](https://github.com/HTTYDCraft/AuthCraft/issues).

## License

MIT License. See [LICENSE](LICENSE) for details.