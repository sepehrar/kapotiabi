# Kapotiabi — Video Script

---

Hello. We are Team-23, also known as **külapoe filosoofid**, and this is our project — **Kapotiabi**.

Well, I think the best way to talk about this project is to tell you how I came up with this idea.

---

So imagine this: you live in Tallinn. Your car is due for regular maintenance, and on top of that, a few new issues have come up. You have a rough idea of what's wrong, so you start calling and writing to different repair shops for price quotes.

The first one comes back with €1,000. The second quotes €700. The third never responds at all.

After a week of waiting, you naturally go with the €700 option — it's the best you've got. The repairs get done, and everything seems fine. But then, almost by chance, you find out that a shop in a nearby town would have done the exact same job for €400.

Four hundred euros. For the same work.

That's a frustrating experience. And honestly — it's a pretty common one.

That's where **Kapotiabi** comes in.

---

## The Idea

Kapotiabi is a car repair marketplace built for Estonia. The core idea is simple: **you describe your car problem once, and repair shops come to you with offers.**

No more calling around. No more guessing. You post your job, set a deadline, and shops in your area submit real price estimates — which you then compare and choose from.

Oh, and — if there are any investors watching this — yes, of course it has AI. We use AI to help car owners better describe their problems, and to help repair shops analyze those descriptions and come up with competitive, realistic price estimates. Cutting-edge stuff. Totally not just a chatbot. We call it a *intelligent automotive diagnostics and dynamic pricing optimization engine*. You're welcome.

---

## How It Works

### For the car owner:

1. You describe what your car needs — whether that's an oil change, brake replacement, or something more complex.
2. You enter your car's details. If you're in Estonia, you can look up your car by license plate through the ARK registry — it auto-fills the make, model, year, and engine.
3. Your job request goes out to repair shops in your city or county.
4. Shops review it and submit offers — with a price breakdown, estimated duration, and parts quality tier (OEM or equivalent).
5. You compare the offers side by side, pick the one that works for you, and confirm the booking.
6. After the job is done, you leave a review.

And if you're not ready to register yet? You can still get a **guest estimate** — just describe your problem and your prefferedd location(s), and get a rough idea of what to expect. No account needed.

---

### For the repair shop:

1. You register your shop, set your service categories, and select your coverage area.
2. When a new job request comes in from your area, you get notified.
3. You review the request, prepare a detailed offer — with price, timeline, and parts breakdown — and submit it.
4. If the car owner accepts, the booking is confirmed. You schedule the appointment, do the job, and mark it complete.
5. Good work earns you good reviews, which helps you stand out in the marketplace.

---

## What We Built

Over the course of this project, our team of five built a full-stack marketplace from scratch:

- **Frontend** — built with Next.js 14 and Tailwind CSS. The interface is fast, clean, and fully multilingual. Car owners get a conversational search experience where they describe their problem in plain language.
- **Backend** — a RESTful API built with Spring Boot and Java 21. It handles authentication, job requests, offers, bookings, reviews, and car lookups — all documented with OpenAPI/Swagger.
- **Database** — PostgreSQL, managed with Flyway migrations for a clean, version-controlled schema.
- **Infrastructure** — the whole stack runs in Docker. One command gets the entire environment running locally.

---

## Roadmap

We're not stopping here. Here's where Kapotiabi is headed:

**Q2 2026 — MVP Complete**
Core marketplace functionality is live. Car owners can post jobs, shops can submit offers, bookings are confirmed, and reviews are in. The foundation is solid.

**Q3 2026 — AI Integration & Market Validation**
We deepen the AI layer — smarter problem summarization, more accurate price predictions, and better matching between job requests and the right shops. Alongside that, we begin real-world testing with early users and gather feedback from both sides of the marketplace.

**Q4 2026 — Iteration & Refinement**
We act on what we learn. User feedback drives product improvements, edge cases get ironed out, and the platform gets polished for scale. Performance, reliability, and trust are the priorities.

**Q1 2027 — Public Launch**
Kapotiabi opens to the Estonian market. Full go-to-market push — onboarding repair shops, acquiring early car owners, and establishing Kapotiabi as the default way to get your car fixed in Estonia.

---

## Closing

Kapotiabi started as a frustrating personal experience and became a product we're genuinely proud of. We built a two-sided marketplace — real users, real workflows, real code.

If you've ever been stuck on the phone trying to get a fair price for a car repair, well — this one's for you.

But none of this would exist without the people who built it. Let me introduce you to the dream team.

**Olga** — a woman with pixel-perfect frontend instincts. If it looks good, it's probably her doing.

**Mahdiyeh** — a woman so strong on the backend, she dreams in assembly. The API doesn't argue with her.

**Bohdan** — our AI expert. Not someone who just uses AI — someone who actually studied how to build it.

**Andrii** — a man who leaves no bug behind. Every edge case, every failure path, every off-by-one error: he finds them all.

And **Ingvar** — a man doing backend and his best to lead this group of wonderfully crazy talented people. Keyword: trying.

Thank you for watching.

