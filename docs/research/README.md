# Spots — Research

This folder is the **science-based, factual** research behind Spots, an
open-source Android re-implementation of Apple's *Vehicle Motion Cues*. Everything
here is sourced; each document separates **established science** from
**hypotheses** and from **preliminary results**, and separates **what Apple
officially stated** from **what observers inferred**.

| Document | What it covers |
|---|---|
| [01 — Apple's Vehicle Motion Cues](01-apple-vehicle-motion-cues.md) | What the feature is, the exact dot-motion mapping, the sensors, the settings, reception, the patents, and the academic precedent ("Bubble Margin") |
| [02 — The science of motion sickness](02-the-science-of-motion-sickness.md) | Sensory-conflict vs postural-instability theory, the vestibular system, why reading in a car makes you sick, VIMS / vection / peripheral vision, the evidence that visual cues help, and the quantitative facts (0.2 Hz peak, MSDV/ISO 2631, latency, susceptibility) |
| [03 — Android implementation research](03-android-implementation-research.md) | Motion sensors, overlay windows, foreground services, rendering/animation, vehicle detection, and the build toolchain — the technical building blocks |

## The one-paragraph summary

Motion sickness is commonly explained by **sensory conflict**: when a passenger
reads a phone, the eyes see stationary text (no motion) while the inner ear feels
the car accelerate, brake and turn. That mismatch produces nausea. **Peripheral
vision dominates the sense of self-motion**, so adding *congruent* motion to the
**edges** of the screen — dots that flow as the world would appear to flow — can
reduce the conflict **without making the user look away from their content**.
Apple's *Vehicle Motion Cues* (iOS 18, 2024) productized this idea, which traces
to the University of Salzburg's *Bubble Margin* study (2019). The supporting
evidence is **directionally consistent but still preliminary** (small samples,
modest effects). **Spots is an experimental comfort aid, not a medical device.**

## How the research maps to the build

The distilled requirements (doc 01 §7) and the technical building blocks (doc 03)
feed directly into [PLAN.md](../../PLAN.md), which turns them into a concrete
Android architecture: a translucent edge-dots overlay driven by inertial sensors,
mapped to optic flow, with the motion math kept in pure `android`-free Kotlin so
it is unit-tested on the JVM.
