# Документация для игроков

**AuthCraft** — это плагин, который защищает ваш аккаунт на сервере Minecraft с помощью регистрации, входа и двухфакторной аутентификации (2FA). Эта документация объясняет, как использовать команды и функции плагина.

## Основные команды

- **/register <пароль>**
  - Регистрирует новый аккаунт.
  - Пароль должен быть длиной 8–16 символов, содержать заглавные и строчные буквы, цифры и не совпадать с именем пользователя.
  - Пример: `/register Password123`

- **/login <пароль>**
  - Выполняет вход в ваш аккаунт.
  - Пример: `/login Password123`

- **/changepassword <старый_пароль> <новый_пароль>**
  - Изменяет ваш пароль.
  - Новый пароль должен соответствовать тем же требованиям.
  - Пример: `/changepassword Password123 NewPassword456`

- **/logout**
  - Выходит из аккаунта, возвращая вас в режим аутентификации.
  - Пример: `/logout`

- **/2fa <действие>**
  - Управляет двухфакторной аутентификацией:
    - `/2fa enable TOTP`: Включает 2FA через приложение (например, Google Authenticator). Сканируйте QR-код в чате.
    - `/2fa enable TELEGRAM`: Включает 2FA через Telegram (требуется настройка сервера).
    - `/2fa enable VK`: Включает 2FA через VK (требуется настройка сервера).
    - `/2fa disable`: Отключает 2FA.
    - `/2fa verify <код>`: Подтверждает код 2FA после входа.
  - Пример: `/2fa enable TOTP`, затем `/2fa verify 123456`

## Как работает аутентификация

1. При входе на сервер вы попадаете в **лимбо-мир** (пустой мир с эффектами слепоты и замедления).
2. Используйте `/register <пароль>` (если новый игрок) или `/login <пароль>` (если уже зарегистрированы).
3. Если включена 2FA, введите код с помощью `/2fa verify <код>`.
4. После успешной аутентификации вы телепортируетесь в основной мир.

## Советы

- **Безопасность пароля**: Не используйте простые пароли или пароли, совпадающие с вашим именем.
- **2FA**: Включите двухфакторную аутентификацию для дополнительной защиты.
- **Сообщение об ошибках**: Если команда не работает, обратитесь к администратору сервера.

## Ответственность

Согласно [Пользовательскому соглашению](AGREEMENT.md), вы несете ответственность за использование нелицензионных копий Minecraft или подключение к серверам в оффлайн-режиме.

---

# Player Documentation (English)

**AuthCraft** is a plugin that secures your Minecraft server account through registration, login, and two-factor authentication (2FA). This documentation explains how to use the plugin’s commands and features.

## Main Commands

- **/register <password>**
  - Registers a new account.
  - Password must be 8–16 characters, include uppercase and lowercase letters, numbers, and not match your username.
  - Example: `/register Password123`

- **/login <password>**
  - Logs into your account.
  - Example: `/login Password123`

- **/changepassword <old_password> <new_password>**
  - Changes your password.
  - The new password must meet the same requirements.
  - Example: `/changepassword Password123 NewPassword456`

- **/logout**
  - Logs out of your account, returning you to the authentication state.
  - Example: `/logout`

- **/2fa <action>**
  - Manages two-factor authentication:
    - `/2fa enable TOTP`: Enables 2FA via an app (e.g., Google Authenticator). Scan the QR code in chat.
    - `/2fa enable TELEGRAM`: Enables 2FA via Telegram (requires server setup).
    - `/2fa enable VK`: Enables 2FA via VK (requires server setup).
    - `/2fa disable`: Disables 2FA.
    - `/2fa verify <code>`: Verifies a 2FA code after login.
  - Example: `/2fa enable TOTP`, then `/2fa verify 123456`

## How Authentication Works

1. Upon joining the server, you enter a **limbo world** (an empty world with blindness and slowness effects).
2. Use `/register <password>` (if new) or `/login <password>` (if registered).
3. If 2FA is enabled, enter the code with `/2fa verify <code>`.
4. After successful authentication, you teleport to the main world.

## Tips

- **Password Security**: Avoid simple passwords or those matching your username.
- **2FA**: Enable two-factor authentication for added security.
- **Report Issues**: If a command fails, contact the server administrator.

## Responsibility

Per the [User Agreement](AGREEMENT.md), you are responsible for using unlicensed Minecraft copies or connecting to servers in offline mode.