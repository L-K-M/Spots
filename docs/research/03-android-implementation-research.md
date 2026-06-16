# Android Implementation Research

> The technical building blocks for re-implementing Vehicle Motion Cues on
> Android, with sources. Claims are tied to official `developer.android.com` /
> `source.android.com` / `kotlinlang.org` docs wherever possible. A handful of
> reference pages are JavaScript-rendered and would not yield text on fetch; those
> are corroborated from official change-logs and flagged.
>
> Compiled 2026-06. Feeds directly into [PLAN.md](../../PLAN.md).

---

## 1. Motion sensors

### Sensor types (from `Sensor`)

| Type | What it reports | Use in Spots |
|---|---|---|
| `TYPE_ACCELEROMETER` | total acceleration incl. gravity (m/s²) | fallback; gravity source via low-pass |
| `TYPE_LINEAR_ACCELERATION` | acceleration **with gravity removed** | **primary** longitudinal/lateral signal |
| `TYPE_GRAVITY` | gravity vector (device frame) | "down" reference for the world transform |
| `TYPE_GYROSCOPE` | angular velocity (rad/s) about device axes | **yaw rate** for turns |
| `TYPE_ROTATION_VECTOR` | fused orientation quaternion (uses magnetometer) | full orientation if available |
| `TYPE_GAME_ROTATION_VECTOR` | orientation **without** magnetometer (no yaw drift correction, immune to magnetic interference) | preferred orientation in a car (lots of metal) |

For driving the dot field, the load-bearing signals are **linear acceleration**
(forward/brake/lateral) and **gyroscope yaw rate** (turning). `TYPE_GRAVITY`
gives the "down" axis needed to separate horizontal vehicle acceleration from
device tilt.

### Registration, rates, batching

- `SensorManager.getDefaultSensor(type)` then
  `registerListener(listener, sensor, samplingPeriodUs[, maxReportLatencyUs])`.
- Rate constants: `SENSOR_DELAY_NORMAL` (~200 ms), `SENSOR_DELAY_UI` (~60 ms),
  `SENSOR_DELAY_GAME` (~20 ms), `SENSOR_DELAY_FASTEST` (0). Or pass explicit
  microseconds. **`SENSOR_DELAY_GAME` (~50 Hz) is the right balance** for smooth
  cues without excessive power draw.
- Continuous sampling above 200 Hz needs the `HIGH_SAMPLING_RATE_SENSORS`
  permission (not needed here).
- **No runtime permission** is required to read motion sensors.
- `SensorEvent.timestamp` is in **nanoseconds** on an unspecified but monotonic
  base; use **deltas** between events (don't assume a fixed period).
- **Battery:** the system does *not* disable sensors when the screen turns off;
  a registered listener keeps draining until you unregister. Register only while
  the overlay is active; unregister when it stops. A larger `maxReportLatencyUs`
  enables batching so the AP can sleep — but for a real-time 50 Hz cue we keep
  latency low.
  Source: <https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview>,
  <https://source.android.com/docs/core/sensors/batching>

### Device → world frame

The device sensor frame is fixed to the phone, not the screen or the world. To
separate "vehicle pushes me forward/sideways" from "I'm holding the phone at an
angle," decompose linear acceleration against gravity:

- `ĝ` = unit gravity (from `TYPE_GRAVITY`); `a` = linear acceleration (device
  frame).
- **Horizontal acceleration** `a_h = a − (a·ĝ)ĝ` (removes the vertical/heave
  component).
- **Forward axis** = the device's into-screen axis (−Z) projected onto the
  horizontal plane and normalized — robust to reading tilt: when the phone is
  vertical, −Z is horizontal and points forward; when tilted to read, its
  horizontal projection still points forward.
- **Right axis** = device +X projected onto the horizontal plane and normalized.
- **longitudinal** = `a_h · forwardAxis`; **lateral** = `a_h · rightAxis`;
  **heave** = `a · ĝ`.

For full orientation, `SensorManager.getRotationMatrix()` +
`remapCoordinateSystem()` map device axes to the world frame; the rotation-vector
sensors provide the quaternion. Spots uses the lighter gravity-projection
decomposition above (no magnetometer needed, robust to the magnetic noise of a
car) and rotates the screen axes by the current display rotation.
Source: <https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion>

### Separating gravity from linear acceleration

If `TYPE_LINEAR_ACCELERATION` / `TYPE_GRAVITY` are unavailable, a standard
**low-pass filter** isolates gravity from the raw accelerometer
(`gravity = α·gravity + (1−α)·sample`), and the **high-pass remainder**
(`sample − gravity`) is the linear acceleration. Spots ships this as a pure,
unit-tested fallback.

---

## 2. System overlay windows

- **`TYPE_APPLICATION_OVERLAY`** (constant `2038`) is the overlay window type for
  third-party apps, **added in API 26 (Android 8.0)**. The older types
  (`TYPE_PHONE`, `TYPE_SYSTEM_ALERT`, `TYPE_SYSTEM_OVERLAY`, …) can no longer be
  used by apps from API 26. Overlay windows always sit **below** critical system
  windows (status bar, IME). ⇒ **an overlay app effectively needs `minSdk 26`.**
  Source: <https://developer.android.com/about/versions/oreo/android-8.0-changes>
- **Permission `android.permission.SYSTEM_ALERT_WINDOW`** ("Display over other
  apps") is a **special/appop permission**, *not* grantable through the normal
  runtime `requestPermissions()` flow. The user must grant it via a Settings
  screen.
  - Check: `Settings.canDrawOverlays(context)` (API 23+).
  - Send the user there: `Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
    Uri.parse("package:$packageName"))`. **From Android 11 the `package:` data is
    ignored** and the user lands on the top-level list.
  Source: <https://developer.android.com/about/versions/11/privacy/permissions>
- **Drawing:** `windowManager.addView(view, params)` with
  `WindowManager.LayoutParams(type = TYPE_APPLICATION_OVERLAY, format =
  PixelFormat.TRANSLUCENT)`; update with `updateViewLayout`; remove with
  `removeView`.
- **Flags for a non-interactive dot overlay:**
  `FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE | FLAG_LAYOUT_IN_SCREEN` — never steals
  focus or the back button, and touches pass straight through to the app below.
  Use `MATCH_PARENT` size and edge-to-edge layout.
  Source: <https://developer.android.com/reference/android/view/WindowManager.LayoutParams>

---

## 3. Foreground service

- A persistent overlay that must survive while other apps are foregrounded should
  be owned by a **foreground `Service`** with a status-bar notification, started
  via `startForeground(id, notification, type)`.
- **Permissions:** `FOREGROUND_SERVICE` (normal, since API 28). **Android 14
  (API 34) makes a `foregroundServiceType` mandatory** plus the matching
  `FOREGROUND_SERVICE_*` permission, else `SecurityException` /
  `MissingForegroundServiceTypeException`.
- **Type choice:** a sensor + overlay app that reads only motion sensors (no GPS)
  fits **`specialUse`** with `FOREGROUND_SERVICE_SPECIAL_USE`, declared with a
  `<property android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE" …>`
  (reviewed at Play submission). If GPS is used, `location` +
  `FOREGROUND_SERVICE_LOCATION`. Spots uses **`specialUse`** (sensors only by
  default).
- **`POST_NOTIFICATIONS`** is a runtime permission from **Android 13 (API 33)**;
  request it at runtime. The FGS still runs if denied, but its notification is
  suppressed.
- **Android 15 (API 35):** `specialUse` and `location` are **not** subject to the
  6-hour `dataSync` limit, and *are* allowed from `BOOT_COMPLETED`. But the
  `SYSTEM_ALERT_WINDOW` background-start exemption narrowed: an app must hold the
  permission **and have a currently-visible `TYPE_APPLICATION_OVERLAY` window**
  to start an FGS from the background. Practical consequence: **add the overlay
  view first, then `startForeground`.**
  Sources: <https://developer.android.com/develop/background-work/services/fgs/service-types>,
  <https://developer.android.com/about/versions/14/changes/fgs-types-required>,
  <https://developer.android.com/about/versions/15/behavior-changes-15>

### Recommended permission set

`SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`,
`POST_NOTIFICATIONS`, and `ACTIVITY_RECOGNITION` (optional, for auto-detect).
Motion-sensor access needs no permission.

---

## 4. Rendering & animation

- **Per-frame budget:** ~16.7 ms at 60 fps (11.1 ms at 90, 8.3 ms at 120).
  Overrunning drops the whole frame → jank. **Don't assume 60 Hz** — drive motion
  off the actual `frameTimeNanos` delta.
  Sources: <https://developer.android.com/topic/performance/vitals/render>,
  <https://android-developers.googleblog.com/2020/04/high-refresh-rate-rendering-on-android.html>
- **Frame clock:** `Choreographer.postFrameCallback{ doFrame(frameTimeNanos) }`
  re-posts each frame; `frameTimeNanos` is a stable nanosecond base. (Compose's
  `withFrameNanos` is backed by the same `Choreographer`.)
- **For an overlay owned by a Service, a custom `View` with `onDraw` +
  `Choreographer` is simpler and more robust than Compose.** A `ComposeView`
  shown from a Service overlay **crashes** unless you wire up
  `setViewTreeLifecycleOwner` / `setViewTreeSavedStateRegistryOwner` /
  `ViewTreeViewModelStoreOwner`. **Spots uses a custom `View` for the overlay**
  (and Compose only for the normal in-app UI, where tree owners exist).
- **Smoothing / physics:** a **critically-damped spring** (`dampingRatio = 1`,
  no overshoot) is ideal for dots that settle without oscillating; an
  **exponential moving average** smooths noisy sensor targets. Numeric reference
  from AndroidX `SpringForce`: `STIFFNESS_LOW = 200`, `STIFFNESS_MEDIUM = 1500`;
  `DAMPING_RATIO_NO_BOUNCY = 1`. Spots implements its own tiny pure-Kotlin spring
  integrator so the physics is unit-testable on the JVM.
- **Avoid allocation in the draw loop:** allocate `Paint`, arrays and position
  buffers once; never `new` per dot per frame.
  Sources: <https://developer.android.com/develop/ui/views/animations/spring-animation>,
  <https://source.android.com/docs/core/graphics/arch-sv-glsv>

---

## 5. Detecting "in a vehicle"

- **Activity Recognition Transition API** (Play services
  `com.google.android.gms:play-services-location:21.3.0`): build
  `ActivityTransition`s for `DetectedActivity.IN_VEHICLE`
  (`ACTIVITY_TRANSITION_ENTER` / `_EXIT`), request via
  `ActivityRecognition.getClient(ctx).requestActivityTransitionUpdates(request,
  pendingIntent)`; receive with `ActivityTransitionResult.extractResult(intent)`.
  This is the battery-efficient, event-driven path.
- **Permission:** `android.permission.ACTIVITY_RECOGNITION` is a **runtime
  ("dangerous") permission from Android 10 (API 29)**; on API ≤ 28 declare the
  legacy `com.google.android.gms.permission.ACTIVITY_RECOGNITION`.
- **Speed alternative:** `FusedLocationProviderClient` + `Location.getSpeed()`
  (m/s; `hasSpeed()` guards it). Needs `ACCESS_FINE_LOCATION` and (background)
  `ACCESS_BACKGROUND_LOCATION`, plus the `location` FGS type — heavier.
- **Sensors-only fallback (no permission, no Play services):** classify "in
  motion" from sustained linear-acceleration energy / variance with hysteresis.
  Less reliable than Activity Recognition, but **permission-free and works on
  de-Googled devices** — important for an open-source app. **Spots ships the
  sensors-only detector as the default and treats Activity Recognition as an
  optional enhancement**, so it has zero hard dependency on Play services.
  Sources: <https://developer.android.com/guide/topics/location/transitions>,
  <https://developer.android.com/about/versions/10/privacy/changes>

---

## 6. Build / toolchain

Current-stable guidance (June 2026): Kotlin 2.4.0, AGP 9.2, Compose BOM
2026.05.x, Material 3 1.4, Play target-SDK requirement API 35 (Aug 2025) heading
to API 36 (Aug 2026).

**Spots deliberately pins the toolchain proven by the sibling Kararead repo
(AGP 8.7.3 / Kotlin 2.0.21 / Compose BOM 2024.12.01 / compileSdk 35, JDK 17)** for
a reliably green CI build, rather than the newest releases. `minSdk 26` is forced
by `TYPE_APPLICATION_OVERLAY`; `targetSdk 35` meets the current Play floor.
Sources: <https://developer.android.com/build/releases/gradle-plugin>,
<https://developer.android.com/google/play/requirements/target-sdk>,
<https://kotlinlang.org/docs/releases.html>

---

## 7. Putting it together (the Spots architecture in one paragraph)

A user grants **Display-over-other-apps**; a **`specialUse` foreground service**
adds a translucent, non-touchable **`TYPE_APPLICATION_OVERLAY`** view, then
registers **linear-acceleration + gravity + gyroscope** listeners at
`SENSOR_DELAY_GAME`. Each sensor batch is decomposed into **longitudinal / lateral
/ heave** acceleration and **yaw rate**, filtered, and fed to a pure-Kotlin
**cue engine** that maps them to an **optic-flow** vector and advances a
**critically-damped spring** per dot. A **`Choreographer`** loop draws the dots
with `Canvas.drawCircle` using `frameTimeNanos` deltas. A **sensors-only vehicle
detector** (with optional Activity Recognition) drives **Automatic** mode. All
the math lives in `android`-free classes so it runs under plain JVM unit tests.
See [PLAN.md](../../PLAN.md) §3 for the module map.

---

## Reference index (selected)

- Sensors overview & motion — developer.android.com/develop/sensors-and-location/sensors
- Overlay windows & 8.0 changes — developer.android.com/about/versions/oreo/android-8.0-changes
- Foreground service types — developer.android.com/develop/background-work/services/fgs/service-types
- Android 14 FGS types required — developer.android.com/about/versions/14/changes/fgs-types-required
- Android 15 behaviour changes — developer.android.com/about/versions/15/behavior-changes-15
- Notifications permission — developer.android.com/develop/ui/views/notifications/notification-permission
- Rendering / jank — developer.android.com/topic/performance/vitals/render
- High-refresh-rate rendering — android-developers.googleblog.com/2020/04/high-refresh-rate-rendering-on-android.html
- Spring animation / SpringForce — developer.android.com/develop/ui/views/animations/spring-animation
- Activity recognition transitions — developer.android.com/guide/topics/location/transitions
- Play services location 21.3.0 — developers.google.com/android/guides/setup
