# Solutions Architecture

## Integrations
- Payment: Razorpay (preferred), PayU; Stripe, PayPal (others as per customer's suggesion)
- SMS: Zoho (preferred), MSG91/TextLocal fallback

## Authentication Strategies
- Google/Facebook OAuth
- Mobile OTP
- Email + Password fallback
- DigiLocker KYC optional

## Database & Architecture
- PostgreSQL primary
- Redis + CDN cache
- Docker Compose MVP
- Kubernetes scaling

## India-first Solutions
- CRM: Zoho, Freshworks
- Cloud: Netcore, E2E Networks
- Compliance: DigiLocker, DPDP
- Monitoring: MoEngage, Hevo Data
