# Cost Breakdown

## Infrastructure
| Component | Service | Monthly Cost |
|-----------|---------|--------------|
| VPS | Hetzner/OVH/DigitalOcean | $60–$100 |
| GPU/API Inference | AWS/GCP/Azure | $300–$600 |
| Object Storage | Backblaze/Wasabi/S3 (S3-compatible API) | $10–$30 |
| CDN | Cloudflare | $20–$30 |
| Load Balancer | NGINX/Traefik | Included in VPS |

## AI & Generative Services
| Service | Monthly Cost |
|---------|--------------|
| LLM API (OpenAI/Azure/Anthropic) | $100–$300 |
| Generative Design AI (Figma plugin/API) | $50–$150 |
| LangChain/Semantic Kernel | Free |

## Backend & Business Services
| Service | Monthly Cost |
|---------|--------------|
| Chargebee | ~$100/mo |
| PostgreSQL + Redis | $20–$50/mo |
| SMTP/Email (Zoho) | $10–$20/mo |
| SMS Gateway (Zoho/MSG91/TextLocal) | ₹0.15–₹0.25 per SMS |

## Frontend & Design
| Component | Cost |
|-----------|------|
| Flutter Build Tools | Free |
| Design System Repository | Included |
| Localization Hooks (Phase 2) | Included |

## Monitoring & Analytics
| Service | Monthly Cost | Phase |
|---------|-------------|-------|
| Prometheus + Grafana (self-hosted) | Included | MVP |
| Mixpanel | $50–$150/mo | Phase 2 |
| Sentry | ~$29/mo | MVP |

## India-First Integrations
| Service | Cost | Phase |
|---------|------|-------|
| Payment Gateway (Razorpay/PayU) | 2–3% per transaction | MVP |
| CRM (Zoho/Freshworks) | $50–$200/mo | Phase 2 |
| DigiLocker Plugin | Free | Phase 2 |

## Per-Agent-Run Cost Estimate

| Agent Type | Avg LLM Tokens | Cost per Run (USD) |
|-----------|---------------|-------------------|
| BA | 2,000 (input) + 500 (output) | ~$0.005–$0.01 |
| Developer | 4,000 (input) + 2,000 (output) | ~$0.02–$0.05 |
| Tester | 3,000 (input) + 1,000 (output) | ~$0.01–$0.03 |
| Compliance | 2,000 (input) + 500 (output) | ~$0.005–$0.01 |
| UI/UX | 3,000 (input) + 1,500 (output) | ~$0.015–$0.04 |
| Solutions Architect | 2,000 (input) + 800 (output) | ~$0.01–$0.02 |

**Average cost per agent run: ~$0.015–$0.03 USD** (assuming GPT-4-class model)

**Monthly cost estimate by tier:**
- Free (5 runs): ~$0.075–$0.15 (subsidized)
- Startup (50 runs): ~$0.75–$1.50
- Growth (200 runs): ~$3.00–$6.00
- Enterprise (500 runs): ~$7.50–$15.00

## Monthly Estimates
| Scale | Customers | Monthly Cost (USD) | Monthly Cost (INR) |
|-------|-----------|-------------------|--------------------|
| MVP | 10 | $500–$800 | ₹42k–₹67k |
| Scaling | 100+ | $1,200–$2,000 | ₹100k–₹167k |
| Enterprise | 500+ | $3,000–$5,000 | ₹250k–₹420k |
