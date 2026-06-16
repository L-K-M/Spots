# Apple "Vehicle Motion Cues" — What It Is and How It Works

> A factual, source-based report. Throughout, this document distinguishes
> **what Apple officially stated** from **what reviewers / observers inferred**.
> Where the exact physical-to-visual mapping matters for re-implementation, the
> distinction is called out explicitly.
>
> Compiled 2026-06. Part of the [Spots](../../README.md) research set; see also
> [the science of motion sickness](02-the-science-of-motion-sickness.md) and the
> [Android implementation research](03-android-implementation-research.md).

---

## 1. What it is

**Vehicle Motion Cues** is an Apple accessibility feature that displays animated
dots around the edges of the screen to reduce motion sickness for **passengers**
using an iPhone or iPad (and, later, a Mac) in a moving vehicle.

### Apple's own description

Apple introduced the feature in a Newsroom press release on **15 May 2024**,
"Apple announces new accessibility features, including Eye Tracking" [1]. The
verbatim framing:

> "Motion sickness is commonly caused by a sensory conflict between what a person
> sees and what they feel, which can prevent some users from comfortably using
> iPhone or iPad while riding in a moving vehicle. With Vehicle Motion Cues,
> animated dots on the edges of the screen represent changes in vehicle motion to
> help reduce sensory conflict without interfering with the main content." [1]

And on the mechanism and controls:

> "Using sensors built into iPhone and iPad, Vehicle Motion Cues recognizes when
> a user is in a moving vehicle and responds accordingly. The feature can be set
> to show automatically on iPhone, or can be turned on and off in Control
> Center." [1]

### Timeline

- **Announced:** 15 May 2024 (annual pre-WWDC accessibility preview) [1].
- **Shipped on iPhone/iPad:** with **iOS 18 / iPadOS 18**, September 2024 [3][7].
- **Expanded to Mac:** announced 15 May 2025; shipped with **macOS Tahoe 26** in
  autumn 2025 [4]. (Pre-release coverage called the OS "macOS 16"; Apple later
  renamed the 2025 line "26".)
- **Refined in iOS 26 (2025):** added appearance customization and a "Dynamic"
  motion pattern (see §3) [8].

### The problem it addresses

Reading or watching a phone in a moving car is a classic motion-sickness trigger.
The eyes, fixed on a screen that appears stationary, report "no motion," while
the vestibular system (inner ear) reports the car's accelerations, braking and
turns. That mismatch — the **sensory-conflict** account of motion sickness —
produces nausea [1][2][6]. Vehicle Motion Cues *adds* a peripheral visual signal
that matches what the inner ear feels, closing the gap without forcing the user
to look up at the road or horizon [2][6]. The deeper science is covered in
[doc 02](02-the-science-of-motion-sickness.md).

---

## 2. Exactly how it works (the part that matters for re-implementation)

### 2a. Visual form and placement

- Small **animated dots** (rendered as circles) appear as a **semi-transparent
  overlay around the screen's edges/margins** — left and right edges and the
  top/bottom periphery [1][3][9].
- The overlay sits **on top of** whatever app you are using and **does not
  interfere with the main content**; that is Apple's explicit design constraint
  [1]. You keep reading/watching in the centre while the dots move in your
  peripheral vision.
- Apple does not publish an exact dot count. The iOS 26 customization options
  let you **increase the number of dots** and their visibility [8]. (For
  comparison, Google's parallel Android "Motion Cues" reportedly uses roughly
  **14 pairs** of dots [11] — a reference point, not Apple's number.)

### 2b. The motion-to-dot mapping

The dots move so that their on-screen optic flow **matches the apparent motion of
the world** as felt by the inner ear. The mapping consistently reported across
Apple's framing and multiple hands-on reviews:

| Physical event (vehicle) | Dot movement on screen | Source |
|---|---|---|
| **Accelerate forward** | Dots slide **down / backward** (toward bottom) | [9][10][12] |
| **Brake / decelerate** | Dots slide **up / forward** (toward top) | [9][10] |
| **Turn left** | Dots glide to the **right** | [9][10][12] |
| **Turn right** | Dots glide to the **left** | [9][10][12] |
| **Constant velocity (steady cruise)** | Dots **hold still** | [12] |

**Why "opposite" is correct.** Several outlets describe the dots as moving "in the
*opposite* direction" to the vehicle [10][12]. This is best understood as
**optic flow / vection**: when a car accelerates forward, the stationary world
*appears* to stream backward past the passenger; the dots reproduce that backward
streaming. On a left turn the world appears to swing right, so the dots swing
right [9][12].

**Acceleration, not velocity.** A second critical point: the dots respond to
**acceleration and changes of orientation, not constant velocity**. The
vestibular system is an *inertial* sensor — it senses change, not steady speed.
Accordingly the dots are most active during acceleration, braking and turning,
and **go still at constant speed** [12]. A faithful re-implementation must drive
dot motion from **measured linear acceleration and yaw rate**, not from GPS
speed. (Spots does exactly this — see the [plan](../../PLAN.md).)

### 2c. Sensors used

- **Apple's stated description:** "sensors built into iPhone and iPad" [1]. Apple
  does not enumerate them.
- **What reviewers consistently infer:** the **accelerometer** (forward/back
  acceleration and braking) and **gyroscope** (turn/yaw rate) [3][9], exposed on
  iOS through Core Motion. Some coverage speculates **GPS** may assist the "are
  we in a vehicle?" classification, but Apple has not confirmed that, and the
  *dot motion itself* is driven by inertial sensors, not GPS [3][9].

### 2d. Automatic detection and the three states

The feature has three states [3][9][10]:

- **On** — dots always shown.
- **Off** — never shown.
- **Automatic** — the device uses its sensors to detect that you are "riding in a
  car or other on-road vehicle," shows the dots automatically, and **hides them
  when the motion stops** [3]. This is the behaviour Apple highlights ("set to
  show automatically") [1].

Apple does not publish detection thresholds or how it disambiguates vehicle
motion from walking [9].

### 2e. Non-interference and intended posture

- The overlay is **content-agnostic** — it draws over any app and is not meant to
  obstruct reading/watching [1].
- It **works best when the user is seated facing forward**, because the dot
  mapping assumes a forward-facing frame of reference. Facing backward (e.g.
  rear-facing train seats) reduces effectiveness [4][9].

---

## 3. How to enable it

### Settings path (iPhone/iPad)

**Settings → Accessibility → Motion → Vehicle Motion Cues**, then choose
**On / Off / Automatic** [3][9].

### Control Center toggle

Control Center → **+** / "Add a Control" → under **Vision Accessibility**, add
**Vehicle Motion Cues** → tap it to toggle on demand [1][3].

### iOS 26 customization (2025)

Under **Settings → Accessibility → Motion → Vehicle Motion Cues → Customize
Appearance** [8]:

- **Color:** six colour options in addition to the default grayscale.
- **Visibility / density:** increase dot visibility and the number of dots.
- **"Dynamic" motion pattern:** displays the dots more organically/chaotically
  while still reflecting the overall sensed motion [8].

### Supported platforms

- **iPhone and iPad:** iOS 18 / iPadOS 18 and later (Sept 2024) [1][3][7].
- **Mac:** macOS Tahoe 26 and later (autumn 2025) [4]. Apple did not detail the
  Mac sensor mechanism; because most Macs lack motion sensors, the exact source
  of motion data on the Mac is **unconfirmed**.

---

## 4. The design rationale Apple gave

- **Core premise — sensory conflict:** motion sickness "is commonly caused by a
  sensory conflict between what a person sees and what they feel" [1]. The dots
  "represent changes in vehicle motion to help reduce sensory conflict" so the
  user can keep using the device **without looking at the road/horizon** and
  **without interfering with the main content** [1].
- **Peripheral vision:** the cues sit at the **edges** of the display.
  Peripheral vision is especially sensitive to motion, so an edge overlay can
  deliver the vestibular-matching signal while the fovea stays on the content
  [2][13]. (Apple's copy implies this; the explicit "peripheral vision detects
  motion" rationale is stated most clearly in the academic precedent — §6.)
- **Research basis:** Apple says the design draws on **"published, leading
  research"** but **named no specific study** [1][2]. The "sensory conflict"
  model it invokes is the dominant academic account of motion sickness [2][6].

---

## 5. Reception / hands-on

General tone: cautiously positive — "weird but it works for many people" — with
the universal caveat that **efficacy is individual and anecdotal**; no large
peer-reviewed validation of *Apple's specific* implementation exists yet [2][6].

- **Popular Science** [6] — tested during errands, reported feeling "a little less
  sick" with it on; notes it doesn't work for everyone, ties it to the CDC's
  description of sensory conflict, and links it to the 2019 academic precedent
  (§6).
- **Macworld** [9] — describes the behaviour precisely: "small dots in the
  margins … will move forward and back as the car slows down or speeds up, and
  side to side as the car turns," matching them to inner-ear sensations. Flags
  optimization for **forward-facing** passengers.
- **The Verge** (relayed) [10] — testers read/wrote on **winding mountain
  switchbacks** (a worst case) and found the dots reduced the urge to look out
  the window; noted the dots are noticeable at first but the brain adapts.
- **Car and Driver** [10] — confirms the directional mapping (left turn → dots
  right; accelerate → dots down).
- **MacRumors** [3][4][7] — the canonical how-to and announcement coverage;
  documented an early Settings-toggle bug (Control Center as workaround) and the
  Mac expansion.

**Reported limitations (reviewer-inferred, not Apple-stated):** effectiveness
varies by individual and trip [3][6][9]; optimized for forward-facing seating
[4][9]; dots can be distracting at first (mitigated by adaptation and the iOS 26
customization) [8][10]; and there is no independent clinical validation of
Apple's exact implementation [2][6].

---

## 6. Related Apple R&D, patents, and the academic precedent

### Academic precedent (not Apple, but directly relevant)

- **"Bubble Margin: Motion Sickness Prevention While Reading on Smartphones in
  Vehicles"** — Meschtscherjakov, Strumegger, Trösterer (University of
  **Salzburg**), **INTERACT 2019** [13]. The closest published antecedent: a
  semi-transparent overlay showing **"bubbles" at the screen margins**, driven by
  **smartphone sensors**, leaving the rest of the screen usable. A 10-participant
  on-road reading study showed *mitigating effects*. The paper explicitly
  leverages **peripheral vision's sensitivity to motion**. Popular Science draws
  the direct line from this work to Apple's feature [6][13]. This is essentially
  the design Apple productized — and the primary reference for Spots.

### Apple patents — the Mark Rober "Immersive Virtual Display" lineage

A separate, earlier R&D thread (VR/AR in autonomous vehicles), distinct from the
shipping overlay but sharing the same anti-nausea, motion-synchronization
principle:

- **"Immersive Virtual Display"** — US Patent Application **US20180089901A1**,
  granted as **US10643391B2**. Inventors: Mark B. Rober (lead) *et al.*; assignee
  Apple Inc.; filed 22 Sept 2017, granted 5 May 2020 [14][15]. A VR/AR system for
  vehicle passengers that **synchronizes the virtual content's motions and
  accelerations with the vehicle's actual motions and accelerations** to prevent
  the visual/vestibular mismatch, with an adjustable mapping ratio (e.g. 1:1) for
  motion-sensitive passengers [14][15][16].

The through-line: the Rober-era patents proposed **synchronizing rich VR/AR
content to vehicle dynamics**; the shipping Vehicle Motion Cues applies the same
"make the visuals match what the inner ear feels" principle in a **minimal,
content-agnostic 2-D dot overlay**.

### Industry parallel (a useful Android reference)

**Google's "Motion Cues" for Android** is a direct parallel: ~14 pairs of edge
dots that move with the vehicle, a manual toggle plus "Auto-enable when Driving,"
and customization (including a randomizer that changes dot shape/colour every few
seconds), expected around Android 16's later quarterly releases or Android 17
[11]. It is the closest existing Android-native reference for the concept.

---

## 7. Distilled requirements for an Android re-implementation

1. **Drive dot motion from inertial sensors, not GPS speed.** Map measured linear
   acceleration (forward/back, lateral) and gyroscope yaw rate to optic-flow-
   consistent dot velocity: forward accel → dots down; brake → dots up; left turn
   → dots right; right turn → dots left. Dots **settle to rest at constant
   velocity**.
2. **Edge/peripheral placement, semi-transparent, content-agnostic** — draw over
   arbitrary apps (a system overlay), keep the centre clear.
3. **Auto-detection:** combine a platform vehicle-activity signal with an
   inertial-energy threshold; show on entry to vehicle motion, hide when it
   stops. Offer **On / Off / Automatic**.
4. **Tunables (iOS 26 / Android parity):** dot count/density, opacity, colour, and
   an optional "dynamic / randomized" pattern.
5. **Reference designs:** the Salzburg "Bubble Margin" paper [13] for the
   validated core concept, and Google's Android Motion Cues [11] as a platform-
   native UX template.

How Spots realizes each of these is detailed in [PLAN.md](../../PLAN.md).

---

## References

1. Apple Newsroom, "Apple announces new accessibility features, including Eye
   Tracking," 15 May 2024 — <https://www.apple.com/newsroom/2024/05/apple-announces-new-accessibility-features-including-eye-tracking/>
2. Endurance, "Apple Unveils Motion Cues For Car Sickness" — <https://www.endurancewarranty.com/learning-center/tech/car-sickness-apple-unveils-vehicle-motion-cues/>
3. MacRumors (how-to), "iOS 18: Prevent Motion Sickness With Vehicle Motion Cues" — <https://www.macrumors.com/how-to/prevent-motion-sickness-vehicle-cues-ios/>
4. MacRumors, "Apple is Bringing a Useful iPhone Feature to the Mac," 15 May 2025 — <https://www.macrumors.com/2025/05/15/vehicle-motion-cues-coming-to-mac/>
5. Apple Support, "Use iPhone more comfortably while riding in a vehicle" (iph55564cb22) — <https://support.apple.com/guide/iphone/iph55564cb22/ios>
6. Popular Science, "Why you get carsick—and how an iPhone feature might help" — <https://www.popsci.com/diy/vehicle-motion-cues-iphone-carsickness/>
7. MacRumors, "Apple to Reduce Motion Sickness With Vehicle Motion Cues on iPhone," 15 May 2024 — <https://www.macrumors.com/2024/05/15/apple-reduce-motion-sickness-feature/>
8. 9to5Mac, "This hidden Vehicle Motion Cues setting solved my motion sickness" (iOS 26 customization), 3 Dec 2025 — <https://9to5mac.com/2025/12/03/hidden-vehicle-motion-cues-adjustment-ios-26/>
9. Macworld, "How Vehicle Motion Cues in iOS 18 can reduce motion sickness" — <https://www.macworld.com/article/2405722/ios-18-how-to-vehicle-motion-cues-reduce-motion-sickness.html>
10. AOL / Car and Driver (Gannon Burgett), "Apple Reveals 'Vehicle Motion Cues' Feature to Fight Carsickness" — <https://www.aol.com/apple-reveals-vehicle-motion-cues-202300572.html>
11. Android Authority / Android Police, "Google 'Motion Cues' for Android" — <https://www.androidauthority.com/android-17-motion-cues-rumor-3624642/>
12. Grokipedia, "Vehicle Motion Cues" (optic-flow / acceleration-vs-velocity description) — <https://grokipedia.com/page/Vehicle_Motion_Cues>
13. Meschtscherjakov, Strumegger & Trösterer, "Bubble Margin: Motion Sickness Prevention While Reading on Smartphones in Vehicles," INTERACT 2019 — <https://link.springer.com/chapter/10.1007/978-3-030-29384-0_39> (open copy: <https://inria.hal.science/hal-02544623/>)
14. Google Patents, "Immersive virtual display" US20180089901A1 / US10643391B2 — <https://patents.google.com/patent/US20180089901A1/en>
15. MacRumors, "Apple Still Pursuing VR-Based Vehicle Motion Sickness Solution With Contributions From Mark Rober," 13 Aug 2020 — <https://www.macrumors.com/2020/08/13/apple-motion-sickness-vr-patent/>
16. AppleInsider, "Ex-Apple inventor describes how his VR patent works to solve car motion sickness," 8 Aug 2022 — <https://appleinsider.com/articles/22/08/08/ex-apple-inventor-describes-how-his-vr-patent-works-to-solve-car-motion-sickness>

### Sourcing reliability

- **Apple-official (primary):** [1] (Newsroom) and [5] (support article
  iph55564cb22). The exact dot *direction* mapping per manoeuvre is **not spelled
  out in Apple's own words**; Apple only says dots "represent changes in vehicle
  motion." The manoeuvre-by-manoeuvre table in §2b is **reviewer-reported and
  physically consistent (optic flow)**, corroborated across [9][10][12].
- The two least-confirmed items are the precise dot **count** and the **Mac sensor
  mechanism**; both are flagged above.
