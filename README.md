<div align="center">

# Spots

**Vehicle Motion Cues for Android — gentle dots at the edges of your screen that
move with your car, to make reading as a passenger easier on your stomach.**

**Latest release:** v<!-- version -->0.1.0<!-- /version --> · [Download](https://github.com/L-K-M/Spots/releases/latest)

[![CI](https://github.com/L-K-M/Spots/actions/workflows/ci.yml/badge.svg)](https://github.com/L-K-M/Spots/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

> [!IMPORTANT]
> Spots is an **experimental comfort aid, not a medical device**. It may help
> reduce motion sickness for some people; results vary. It is an independent,
> open-source project and is not affiliated with Apple. **For passengers only —
> never use it while driving.**

> [!NOTE]
> LLM disclosure: Spots was built with substantial help from large language
> models, including the research in [`docs/research/`](docs/research/) and the
> implementation.

---

## What is this?

Reading or watching something on your phone in a moving car is a classic recipe
for motion sickness: your eyes see a stationary screen while your inner ear feels
the car accelerate, brake and turn. Your brain dislikes that mismatch — that's
the **sensory-conflict** explanation of motion sickness.

**Spots** is an open-source Android re-implementation of Apple's *Vehicle Motion
Cues*. It paints small, semi-transparent dots around the **edges** of your screen
that stream the way the world *appears* to move:

| The car… | …the spots |
|---|---|
| accelerates forward | flow **down** |
| brakes | rise **up** |
| turns left | slide **right** |
| turns right | slide **left** |
| holds a steady speed | go **still** |

Your peripheral vision — which is very sensitive to motion — gets a signal that
matches what your body feels, while the **centre of the screen stays clear** for
whatever you're doing, in **any** app.

The science (and the honest caveats about how strong the evidence is) is written
up in [`docs/research/`](docs/research/):

- [Apple's Vehicle Motion Cues — what it is and how it works](docs/research/01-apple-vehicle-motion-cues.md)
- [The science of motion sickness and visual motion cueing](docs/research/02-the-science-of-motion-sickness.md)
- [Android implementation research](docs/research/03-android-implementation-research.md)

## Install

### One line (recommended)

With a phone connected over USB (USB debugging on) and `adb` available, this
downloads the latest release APK, verifies its checksum, and installs it:

```bash
curl -fsSL https://github.com/L-K-M/Spots/releases/latest/download/install.sh | bash
```

Useful flags: `--version v0.1.0`, `--list`, `--device <serial>`, `--apk-only`.

### Or grab the APK

Download `spots-v<version>.apk` from the
[Releases](https://github.com/L-K-M/Spots/releases) page and install it.

### Or build from source

```bash
./scripts/install.sh          # build + install + launch the debug build
```

## First run

1. Open **Spots** and choose a mode: **Off**, **On**, or **Automatic**
   (auto-shows when it detects vehicle motion).
2. Grant **“Display over other apps”** when asked — Spots needs it to draw on top
   of other apps.
3. On Android 13+, allow the (silent) notification that runs while Spots is active.
4. Use the **live preview** on the home screen to tune the look and *feel the
   effect* — just tilt and move your phone — before your trip.

Everything happens **on-device**: no internet permission, no account, no
analytics, no ads. By default, vehicle detection is **sensors-only**, so Spots
works fully without Google Play services.

## How it's built

A `specialUse` foreground service owns a translucent, non-touchable overlay window
and a 50 Hz sensor pipeline. Linear acceleration + gravity + gyroscope are
decomposed into longitudinal / lateral / heave acceleration and yaw rate, mapped
to an **optic-flow** velocity, and used to stream the dots (drawn with a
`Choreographer`-driven `Canvas`). All of the motion math lives in pure,
`android`-free Kotlin and is covered by JVM unit tests. See [`PLAN.md`](PLAN.md).

## Releasing

`scripts/release.sh` is a thin stub over the shared
[release-tool](https://github.com/L-K-M/release-tool) engine:

```bash
scripts/release.sh 0.2.0 --push   # bump version, tag v0.2.0, push → CI publishes the Release
```

## License

[MIT](LICENSE) © Spots contributors.
