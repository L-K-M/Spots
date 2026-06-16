# awesome.md — a fresh review of Spots

A post-build, adversarial read of the first implementation: bugs, gaps, and a few
delightful ideas. The worthwhile items are then implemented (see the status tags).

**Legend:** 🐞 bug · 🔧 issue/cleanup · ✨ feature · 💡 idea/delight
**Status:** ⬜ open · 🟢 shipped · ⏸️ deferred (documented, not done)

---

## A. Bugs & correctness

- **A1 🐞 — Top/bottom dots flicker under forward/brake motion.** All dots share one
  flow velocity and wrap within their edge band. For the thin top/bottom bands,
  vertical (longitudinal) flow makes dots traverse the band height and recycle very
  quickly — a busy flicker. Fix: emphasise each band's **long axis** (sides stream
  vertically with longitudinal motion; top/bottom stream horizontally with
  lateral/yaw), damping the cross-axis component. **Status: 🟢 shipped** — per-edge
  axis weighting in `MotionCueEngine`.

- **A2 🐞 — Battery: 50 Hz sensors run even while parked in Automatic mode.** The
  detector needs only coarse motion energy, so running the accelerometer at
  `SENSOR_DELAY_GAME` while merely *armed* wastes power. Fix: sample at a slow rate
  while armed-and-waiting, and switch to the fast rate only once the overlay is
  actually showing. **Status: 🟢 shipped** — `SensorMotionSource.setRate()` + the
  service drops to `SENSOR_DELAY_UI` when not visible.

- **A3 🔧 — Choosing On/Automatic without the overlay permission silently arms a
  service that can't draw.** It burns a little battery with nothing on screen. Fix:
  only start the service once the overlay permission is held; otherwise prompt and
  let the resume-time re-check start it. **Status: 🟢 shipped** — gated in the
  Home `selectMode` flow.

- **A4 🔧 — The comfort meter only weights vertical heave.** ISO 2631 weights
  vertical most heavily but horizontal motion also contributes. Kept vertical-only
  for a simple, legible read-out; documented as an approximation. **Status: ⏸️**

## B. Features & gaps

- **B1 ✨ — No Quick Settings tile.** Apple toggles Vehicle Motion Cues from
  Control Center; Android's equivalent is a Quick Settings tile. Fix: a
  `TileService` that toggles Spots from the notification shade. **Status: 🟢
  shipped** — `SpotsTileService`, cycles Off↔last-on mode.

- **B2 ✨ — Rear-facing seats invert the cue.** Apple notes the feature assumes a
  forward-facing passenger; on a rear-facing train seat the longitudinal cue is
  backwards. Fix: a "seat faces backward" toggle that flips the longitudinal sign.
  **Status: 🟢 shipped** — `seatReversed` setting → `CueConfig.longitudinalSign`.

- **B3 ✨ / D1 💡 — Demo mode.** You can only feel the effect by moving the phone.
  A synthetic "test drive" that animates the dots on their own makes the live
  preview understandable indoors — and produces good screenshots. Fix: a Demo
  toggle that feeds the engine a scripted accelerate/turn/brake loop. **Status: 🟢
  shipped** — `DemoDrive` + a toggle on the preview.

- **B4 ✨ — Re-arm on boot.** Apple's Automatic is effectively always-on. A
  `BOOT_COMPLETED` receiver could re-arm Automatic mode. Deferred: it's a
  background-start/permission surface that many users won't want, and it's easy to
  add later. **Status: ⏸️**

## C. Engineering / project health

- **C1 🔧 — Lint warnings.** The first build had ~34 warnings (mostly unused
  imports and an unused parameter). Cleaned the trivial ones. **Status: 🟢 shipped**

- **C2 🔧 — Pure logic is well tested; the service/overlay aren't.** They need a
  device to be meaningful, so they're left to manual testing; the math (the part
  that's easy to get subtly wrong) has 50+ JVM tests. New pure code (seat sign,
  per-edge weighting, demo curve) gets tests too. **Status: 🟢 shipped** (tests
  added for the new pure logic).

## D. Delight & quirks

- **D2 💡 — Dot shape option.** Google's Android cue can randomise dot shape.
  Offer a filled vs. **hollow ring** style — a small, tasteful bit of character.
  **Status: 🟢 shipped** — `Appearance.hollow` + a Settings toggle.

- **D3 💡 — "Smooth road" calm.** When the comfort meter reads very low for a
  while, it could say something reassuring. Cute but noisy; left as an idea.
  **Status: ⏸️**

- **D4 💡 — Haptic-free by design.** Buzzing a nauseous passenger is the opposite
  of helpful; intentionally omitted. **Status: ⏸️ (won't do)**
