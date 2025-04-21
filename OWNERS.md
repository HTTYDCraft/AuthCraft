# Документация для владельцев серверов

**AuthCraft** — это плагин аутентификации для серверов Minecraft (Spigot 1.16.5), обеспечивающий безопасное управление аккаунтами игроков. Эта документация описывает установку, настройку и администрирование плагина.

## Установка

1. Скачайте `authcraft-1.0.3.jar` из [Releases](https://github.com/HTTYDCraft/AuthCraft/releases).
2. Поместите JAR в папку `plugins` вашего сервера Spigot 1.16.5.
3. Перезапустите сервер. Папка `plugins/AuthCraft` и файлы `config.yml`, `messages.yml` будут созданы автоматически.
4. Настройте `config.yml` (см. ниже).
5. Проверьте логи в `plugins/AuthCraft/audit.log` на наличие ошибок.

## Конфигурация

Файл `config.yml` находится в `plugins/AuthCraft`. Основные параметры:


database:
  type: sqlite
  sqlite:
    file: authcraft.db
  postgresql:
    host: localhost
    port: 5432
    database: authcraft
    username: authcraft
    password: password
    pool_size: 10
telegram:
  token: your_telegram_bot_token
  admin_id: your_telegram_admin_id
vk:
  token: your_vk_bot_token
encryption:
  key: your_encryption_key


- **database.type**: `sqlite` или `postgresql`.
- **sqlite.file**: Путь к файлу базы данных SQLite.
- **postgresql**: Параметры подключения к PostgreSQL (хост, порт, имя базы, пользователь, пароль).
- **telegram/vk**: Токены для ботов 2FA (опционально). VK требует внешней настройки Long Poll API.
- **encryption.key**: Ключ AES для шифрования данных 2FA (оставьте пустым для автогенерации).

## Админ-команды

- **/authcraft backup**
  - Создает резервную копию базы данных.
  - Требуется право: `authcraft.admin`
  - Пример: `/authcraft backup`

- **/authcraft reload**
  - Перезагружает конфигурацию.
  - Пример: `/authcraft reload`

- **/authcraft stats**
  - Показывает статистику сервера (количество игроков, подключений и т.д.).
  - Пример: `/authcraft stats`

## Настройка базы данных

- **SQLite**: Не требует внешнего сервера. Убедитесь, что папка `plugins/AuthCraft` доступна для записи.
- **PostgreSQL**: Настройте сервер PostgreSQL и укажите параметры в `config.yml`. Проверьте доступность хоста и порта.
- **Резервное копирование**: Используйте `/authcraft backup` для создания копий базы данных.

## Настройка 2FA

- **TOTP**: Автоматически работает через QR-коды в чате.
- **Telegram**: Укажите `telegram.token` и `telegram.admin_id` в `config.yml`. Создайте бота через @BotFather.
- **VK**: Укажите `vk.token`. Для полной функциональности настройте Long Poll API или вебхуки (см. [VK API](https://vk.com/dev/bots_longpoll)).

## Логирование

- Логи хранятся в `plugins/AuthCraft/audit.log`.
- Проверяйте файл для диагностики ошибок (например, проблемы с базой данных или ботами).

## Ответственность

Согласно [Пользовательскому соглашению](AGREEMENT.md), владельцы серверов несут ответственность за:
- Использование нелицензионных копий Minecraft или оффлайн-режима.
- Обработку данных игроков в соответствии с местным законодательством (например, в России данные российских пользователей должны храниться на серверах в РФ).

## Поддержка

Если у вас есть вопросы, создайте тикет в [Issues](https://github.com/HTTYDCraft/AuthCraft/issues).

---

# Server Owners Documentation (English)

**AuthCraft** is an authentication plugin for Minecraft servers (Spigot 1.16.5), providing secure player account management. This documentation covers installation, configuration, and administration.

## Installation

1. Download `authcraft-1.0.3.jar` from [Releases](https://github.com/HTTYDCraft/AuthCraft/releases).
2. Place the JAR in the `plugins` folder of your Spigot 1.16.5 server.
3. Restart the server. The `plugins/AuthCraft` folder and `config.yml`, `messages.yml` files will be created.
4. Configure `config.yml` (see below).
5. Check logs in `plugins/AuthCraft/audit.log` for errors.

## Configuration

The `config.yml` file is located in `plugins/AuthCraft`. Key settings:


database:
  type: sqlite
  sqlite:
    file: authcraft.db
  postgresql:
    host: localhost
    port: 5432
    database: authcraft
    username: authcraft
    password: password
    pool_size: 10
telegram:
  token: your_telegram_bot_token
  admin_id: your_telegram_admin_id
vk:
  token: your_vk_bot_token
encryption:
  key: your_encryption_key


- **database.type**: `sqlite` or `postgresql`.
- **sqlite.file**: Path to the SQLite database file.
- **postgresql**: Connection details for PostgreSQL (host, port, database, username, password).
- **telegram/vk**: Tokens for 2FA bots (optional). VK requires external Long Poll API setup.
- **encryption.key**: AES key for encrypting 2FA data (leave empty to auto-generate).

## Admin Commands

- **/authcraft backup**
  - Creates a database backup.
  - Requires permission: `authcraft.admin`
  - Example: `/authcraft backup`

- **/authcraft reload**
  - Reloads the configuration.
  - Example: `/authcraft reload`

- **/authcraft stats**
  - Displays server statistics (player count, connections, etc.).
  - Example: `/authcraft stats`

## Database Setup

- **SQLite**: Requires no external server. Ensure `plugins/AuthCraft` is writable.
- **PostgreSQL**: Set up a PostgreSQL server and configure `config.yml`. Verify host/port accessibility.
- **Backup**: Use `/authcraft backup` to create database backups.

## 2FA Setup

- **TOTP**: Automatically works via QR codes in chat.
- **Telegram**: Set `telegram.token` and `telegram.admin_id` in `config.yml`. Create a bot via @BotFather.
- **VK**: Set `vk.token`. For full functionality, configure Long Poll API or webhooks (see [VK API](https://vk.com/dev/bots_longpoll)).

## Logging

- Logs are stored in `plugins/AuthCraft/audit.log`.
- Check the file for diagnostics (e.g., database or bot issues).

## Responsibility

Per the [User Agreement](AGREEMENT.md), server owners are responsible for:
- Using unlicensed Minecraft copies or offline mode.
- Processing player data in compliance with local laws (e.g., in Russia, Russian user data must be stored on servers within the country).

## Support

For questions, create a ticket in [Issues](https://github.com/HTTYDCraft/AuthCraft/issues).