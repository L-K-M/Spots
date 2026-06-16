# Spots — Vehicle Motion Cues for Android

> An open-source Android re-implementation of Apple's **Vehicle Motion Cues**:
> a translucent overlay of animated **spots** at the edges of the screen that move
> with your vehicle's real motion, to help reduce passenger motion sickness while
> you read or watch — in **any** app.

This document is the comprehensive plan: the research it rests on, the product
decisions, the technical architecture (including the motion math), and the
execution checklist. The science lives in [`docs/research/`](docs/research/); a
companion [`awesome.md`](awesome.md) tracks the post-build review and follow-ups.

> **Spots is an experimental comfort aid, not a medical device.** The evidence
> that visual cues reduce motion sickness is real but preliminary (see
> [doc 02 §4](docs/research/02-the-science-of-motion-sickness.md#4-evidence-that-congruent-visual-motion-cues-reduce-motion-sickness)).

---

## 1. Goal & product framing

The north star: a passenger opens whatever they like to do in a car — read,
message, browse — and Spots quietly paints **spots at the screen's edges that
move the way the world *appears* to move**. Forward acceleration streams the
spots down; braking lifts them up; a left turn slides them right. The centre of
the screen is untouched. When the car holds a steady speed, the spots go still.

### What the research tells us to build
(From [doc 01 §7](docs/research/01-apple-vehicle-motion-cues.md#7-distilled-requirements-for-an-android-re-implementation)
and [doc 02 §3](docs/research/02-the-science-of-motion-sickness.md#3-visually-induced-motion-sickness-vection-and-peripheral-vision).)

1. **Drive the cue from inertial sensors, not GPS speed** — the vestibular system
   senses *acceleration*, so the spots must respond to acceleration and yaw, and
   **settle to rest at constant velocity**.
2. **Peripheral placement** — the edges, because peripheral vision dominates the
   sense of self-motion. Keep the centre clear.
3. **Optic-flow mapping** — spots move *opposite* to the vehicle's motion vector,
   reproducing how the world streams past.
4. **Content-agnostic overlay** — it draws over any app.
5. **On / Off / Automatic** — auto-detect vehicle motion and show/hide.
6. **Tunable** — density, opacity, colour, sensitivity, and a "dynamic" pattern.

### Non-goals (v1)
- Not a medical device; no health claims beyond "may help, individually varies."
- No account, no network, no analytics, no ads. Everything is on-device.
- No driving/heads-up features — Spots is for **passengers**.

---

## 2. The science → design mapping

| Finding (see research docs) | Design consequence in Spots |
|---|---|
| Sensory conflict: eyes say "still," ears say "moving" | Add a congruent *visual* motion signal on the screen the user already looks at |
| Peripheral vision dominates self-motion perception | Place the spots at the **edges**, leave the centre clear |
| Optic flow: world appears to stream *opposite* to motion | forward accel → spots **down**; brake → **up**; left turn → spots **right** |
| Vestibular system senses *change*, not steady speed | Map **linear acceleration + yaw rate**; spots **settle** at constant velocity |
| ~0.2 Hz oscillation is most nauseogenic (ISO 2631) | Surface a frequency-weighted **"comfort meter"** so users see provocative motion |
| Vestibular latency (~10 ms) ≪ visual (~80 ms) | Keep the pipeline low-latency (50 Hz sensors, frame-synced draw) |
| Evidence is preliminary, effects modest | In-app honesty: "experimental comfort aid," link to the research |
| Susceptibility varies; some find dots distracting | Generous tunables; an in-app **live preview** to dial it in before a trip |

---

## 3. Architecture

Single-module app. All the **motion math is pure Kotlin** (no `android.*`
imports) so it runs under plain JVM unit tests; only the thin
sensor/overlay/service shells touch the framework.

```
ch.lkmc.spots
├── SpotsApp                       Application (notification channel)
├── MainActivity                   Compose host + navigation
│
├── motion/                        ── pure math + sensor shell ──
│   ├── Vec3, MotionMath           projection / decomposition helpers (pure)
│   ├── MotionDecomposer           linear-accel + gravity → long/lat/heave + screen axes (pure)
│   ├── SignalFilters              LowPass, HighPass, EMA, OneEuro (pure)
│   ├── MotionSample               immutable sample (long/lat/heave accel, yaw rate, t)
│   └── SensorMotionSource         SensorManager → Flow<MotionSample> (framework)
│
├── engine/                        ── the cue, pure ──
│   ├── CueMapping                 (long, lat, yaw) → optic-flow Offset (pure)
│   ├── Spring                     critically-damped spring integrator (pure)
│   ├── Dot, DotField              edge layout + per-dot spring state (pure)
│   └── MotionCueEngine            update(dt, sample) → drives the dots (pure)
│
├── detect/                        ── "are we moving?" ──
│   ├── VehicleMotionClassifier    sensors-only hysteresis state machine (pure)
│   └── ActivityRecognitionDetector  optional Play-services enhancement (framework, reflective)
│
├── overlay/
│   ├── DotsOverlayView            custom View, Choreographer-driven Canvas draw
│   └── OverlayService             specialUse FGS: window + sensors + engine + detector
│
├── comfort/
│   └── ComfortMeter               ISO-2631-style frequency-weighted MSDV estimate (pure)
│
├── data/
│   ├── SpotsPrefs                 enums + CueConfig data class (pure)
│   └── SettingsRepository         DataStore persistence (framework)
│
└── ui/
    ├── theme/                     Material 3 (light/dark, dynamic colour)
    ├── navigation/                NavHost + routes
    ├── components/                LivePreview, PermissionCard, sliders, swatches
    ├── home/                      status, big Start/Stop, live preview, permission flow
    ├── settings/                  mode, appearance, sensitivity, detection, advanced
    └── about/                     how it works + the science + disclaimer + licenses
```

### The motion math (the heart of it)

Inputs each tick: linear acceleration `a` (device frame, gravity removed),
gravity `g`, gyroscope `ω`, and the current display rotation.

1. **Decompose** (`MotionDecomposer`, pure): with `ĝ = g/|g|`,
   `a_h = a − (a·ĝ)ĝ`. The **forward axis** is device −Z projected onto the
   horizontal plane and normalized (robust to reading tilt); the **right axis** is
   device +X projected onto horizontal — both rotated by the display rotation so
   the cue matches the *screen* the user sees.
   `longitudinal = a_h·forward`, `lateral = a_h·right`, `heave = a·ĝ`,
   `yaw = ω about ĝ`.
2. **Filter** (`SignalFilters`, pure): a light low-pass on each channel removes
   sensor noise; a slow high-pass removes residual bias so the spots return to
   centre when motion stops.
3. **Map to optic flow** (`CueMapping`, pure): produce a 2-D flow vector in
   screen coordinates (+x right, +y down):
   - `flow.y = +k_long · longitudinal` → forward accel pushes spots **down**,
     brake pushes them **up**.
   - `flow.x = −k_lat · lateral − k_yaw · yawLeft` → a **left** turn (centripetal
     accel left, or leftward yaw) slides spots **right**; a right turn, left.
   - magnitudes are clamped; sensitivity scales `k_*`.
4. **Animate** (`Spring` + `MotionCueEngine`, pure): each dot's target offset is
   the flow vector scaled by its depth/parallax; a **critically-damped spring**
   (`ζ = 1`, no overshoot) advances each dot toward its target using the real
   frame `dt`. At constant velocity, flow → 0, springs relax, spots **settle**.
5. **Draw** (`DotsOverlayView`, framework): a `Choreographer` loop reads each
   dot's position and calls `Canvas.drawCircle`, advancing physics by the true
   `frameTimeNanos` delta. No allocation in the loop.

The full directional table and the optic-flow rationale are in
[doc 01 §2b](docs/research/01-apple-vehicle-motion-cues.md#2b-the-motion-to-dot-mapping).

### Key decisions
1. **Custom `View`, not Compose, for the overlay.** A `ComposeView` from a Service
   overlay crashes without manual tree-owner wiring; a `Choreographer`-driven
   `View` is simpler and more robust for "draw dozens of circles." Compose is used
   only for the in-app UI ([doc 03 §4](docs/research/03-android-implementation-research.md#4-rendering--animation)).
2. **`specialUse` foreground service**, sensors only — **no GPS, no Play-services
   hard dependency.** Auto-detect defaults to a **sensors-only** classifier so
   Spots runs fully on de-Googled / open-source Android. Activity Recognition is
   an optional, reflectively-loaded enhancement.
3. **Pure-Kotlin math.** Everything load-bearing (decomposition, filters, mapping,
   spring, detection, comfort meter) is `android`-free and unit-tested on the JVM
   — the only way to meaningfully test an app whose real validation needs a car.
4. **Privacy by construction.** No network permission at all; nothing leaves the
   device.

---

## 4. Screens

1. **Home** — a big **Start / Stop** that toggles the overlay; live **status**
   (off / armed-automatic / active); an inline **live preview** that runs the real
   engine off the phone's sensors so you can see the spots react by tilting/moving
   the device before you ever get in a car; and a **permission flow** (overlay,
   notifications, optional activity-recognition) shown only when needed.
2. **Settings**
   - **Mode:** Off / On / Automatic.
   - **Appearance:** dot count/density, size, opacity, colour (grayscale + a
     palette), and a **Dynamic** organic-motion pattern (the iOS 26 parity touch).
   - **Sensitivity:** how strongly spots respond; separate longitudinal / lateral
     weighting; "settle speed."
   - **Detection:** sensors-only vs Activity Recognition; sensitivity of
     auto-arm; auto-stop delay.
   - **Advanced:** show the **comfort meter**, edge selection (all / sides only),
     keep-screen-awake while active.
3. **About / How it works** — a plain-language explanation, the science summary
   with links into `docs/research/`, the **disclaimer**, version, and open-source
   licenses.

---

## 5. CI/CD, shell-script install & release

Mirrors the sibling **Kararead** / **release-tool** family conventions.

- **`.github/workflows/ci.yml`** on push/PR: JDK 17 + Android SDK + Gradle cache;
  `./gradlew lintDebug testDebugUnitTest assembleDebug`; upload the debug APK and
  lint/test reports as artifacts.
- **`.github/workflows/release.yml`** on tag `v*`: build the (debug-signed)
  release APK, rename to `spots-v<version>.apk`, generate notes, publish a GitHub
  Release with the APK **and** the `install.sh` one-liner installer attached.
- **`.github/dependabot.yml`** for Gradle + Actions, grouped (androidx / kotlin).
- **A checked-in `debug.keystore`** for reproducible CI signing.
- **`scripts/install.sh`** — local dev: `./gradlew installDebug` + launch on a
  connected device (the Kararead pattern).
- **`scripts/release.sh`** — a ~20-line stub that exports `RELEASE_*` config and
  `exec`s **`lkm-release`** (the shared [release-tool](https://github.com/L-K-M/release-tool)
  engine), `RELEASE_KIND="gradle-android"`.
- **`install.sh` (repo root) — the released shell-script installer.** A
  `curl … | bash`-able script that finds the latest GitHub Release, downloads
  `spots-v<version>.apk`, verifies it, and `adb install`s it to a connected
  device — with `--list`, `--version`, `--apk-only`, `--device` flags. This is the
  "release of shell scripts" deliverable: the installer ships as a release asset
  and is documented in the README.

---

## 6. Quality

- **JVM unit tests** for every pure module: `MotionMath`/`MotionDecomposer`
  (tilt-invariance, direction signs), `SignalFilters` (step response, cutoff),
  `CueMapping` (the full directional table — forward→down, brake→up, left→right,
  right→left, constant→still), `Spring` (critical damping: monotonic, no
  overshoot, settles), `DotField` (edge layout, counts), `VehicleMotionClassifier`
  (hysteresis: arms on sustained motion, disarms after quiet), `ComfortMeter`
  (0.2 Hz weighting), and `CueConfig` clamping. JUnit4 + `kotlin.test`.
- **`./gradlew lint` clean.**
- **README** with the value proposition, the science in brief, install
  instructions (shell one-liner + build-from-source), and a screenshots-of-intent
  section.

---

## 7. Execution checklist

- [x] Research: Apple feature, motion-sickness science, Android building blocks → `docs/research/`
- [x] PLAN.md (this file)
- [x] Gradle project, version catalog, wrapper, CI-ready debug signing
- [x] Theme, navigation, app scaffold
- [x] Pure motion math: Vec3/decomposer, filters, cue mapping, spring, dot field, engine
- [x] Sensor source + overlay view + foreground service
- [x] Vehicle detection (sensors-only + optional activity recognition)
- [x] Comfort meter
- [x] Settings (DataStore) + all three screens + live preview
- [x] Unit tests for every pure module
- [x] CI/CD + Dependabot + scripts + released `install.sh` + README
- [x] Build green (lint + tests + assembleDebug)
- [x] Review → `awesome.md` → implement the worthwhile items
