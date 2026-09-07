# Security and Portfolio Sanitization Notes

This was an academic prototype and was not designed as a production security architecture.

For the public portfolio copy:

- The original course database credential was **removed**
- The original class server hostname was replaced with a local Android-emulator development address
- `local.properties`, IDE metadata, build outputs, and the original `.git` database were excluded
- Database configuration now uses environment variables

## Important prototype limitation
The archived project uses simplified username/password handling appropriate to a class prototype, not a production authentication system. A production version should use password hashing, secure session/token management, TLS, input validation, least-privilege database accounts, and secret management.

Do not deploy this repository publicly as a real authentication service without redesigning those areas.
