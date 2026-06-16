# The Science of Motion Sickness and Visual Motion Cueing

> A literature review of the sensory, vestibular and perceptual mechanisms behind
> motion sickness, and the evidence that congruent peripheral visual motion cues
> can reduce it. Written to be **factual** and to **separate established science
> from hypotheses and from preliminary results**.
>
> Compiled 2026-06. Part of the [Spots](../../README.md) research set; see also
> [Apple's Vehicle Motion Cues](01-apple-vehicle-motion-cues.md).

---

## 1. Sensory-conflict theory and its competitors

### 1.1 The sensory-conflict / neural-mismatch framework

The dominant explanatory framework for motion sickness is **sensory-conflict
theory**, also called **sensory rearrangement** or **neural mismatch theory**.
Its modern formulation is attributed to **Reason & Brand (1975)** in *Motion
Sickness* (Academic Press) and elaborated by **Reason (1978)**.

The core claim: motion sickness arises whenever the motion signals from the three
spatial senses — **vision, the vestibular system (inner ear) and non-vestibular
proprioception** — are *mutually inconsistent*, and inconsistent with what the
brain *expects* from prior experience [1][2]. Reason & Brand distinguished two
broad categories of provocative conflict:

- **Intersensory (visual–vestibular) conflict** — e.g. the eyes signal a
  stationary world while the vestibular system signals motion (reading in a car),
  or vice-versa (a stationary observer watching a moving scene, as in a cinema or
  a VR headset).
- **Intrasensory (canal–otolith) conflict** — a mismatch *within* the vestibular
  system, e.g. between the angular-motion signal from the semicircular canals and
  the linear/gravitational signal from the otoliths.

A crucial refinement by **Reason (1978)**: the brain does not simply compare one
sensory channel against another; it compares incoming afference against an
**internally generated expectation** — a "neural store" of the expected sensory
consequences of self-motion. This is rooted in **von Holst & Mittelstaedt's
efference-copy / reafference** principle: the brain predicts the sensory feedback
its own motor commands should produce, and a discrepancy between predicted and
actual feedback generates a **mismatch signal** [3]. Modern computational models
(observer-theory / Kalman-filter models, e.g. Bos & Bles) formalize this as an
accumulating, "leaky-integrator" mismatch signal that builds toward a nausea
threshold over minutes [3][4] — which is why provocative motion usually produces
symptoms only after a latency of several minutes.

**Status of the evidence.** Sensory-conflict theory is the most widely cited and
clinically useful framework, and it makes correct qualitative predictions (looking
at the horizon reduces carsickness; reading aggravates it). It is frequently
criticized, however, for being **hard to falsify** and **non-quantitative** in its
original form: it explains symptoms post hoc but does not, by itself, predict
*who* will get sick or *how badly*. It also does not localize a neural "mismatch
detector," though the brainstem (vestibular nuclei, area postrema) and cerebellum
are implicated [2][5].

### 1.2 The competing postural-instability theory

The principal alternative is the **postural-instability theory** of **Riccio &
Stoffregen (1991)**, *Ecological Psychology* 3(3):195–240 [6]. From a Gibsonian
"ecological" perspective, they argued sickness is caused **not by sensory conflict
but by prolonged postural instability** — situations in which the animal lacks (or
has not yet learned) a strategy to keep control of its posture. On this view,
sensory conflict is neither necessary nor sufficient; instability of the action
system is the proximal cause.

| | Sensory conflict (Reason & Brand) | Postural instability (Riccio & Stoffregen) |
|---|---|---|
| **Proximal cause** | Mismatch between sensed and expected motion | Loss of postural/action control |
| **Locus** | Perceptual/CNS comparison process | Animal–environment action system |
| **Key prediction** | Sickness follows sensory rearrangement | Postural instability *precedes* sickness |
| **Falsifiability** | Often criticized as weak | Makes a testable temporal-ordering claim |

The strongest support for the postural theory is its **temporal-precedence**
prediction — that measurable postural instability appears *before* symptoms.
**Stoffregen & Smart (1998)** and **Smart, Stoffregen & Bardy (2002)**, *Human
Factors* 44(3):451–465, reported exactly this in a moving-room paradigm (N = 13),
with increased body sway *prior* to sickness onset and discriminant analysis
predicting who became sick [7][8].

**Status of the evidence.** The postural-precedence findings replicate but come
from **small samples** and largely **visually induced (optical) paradigms** rather
than real vehicle motion. Many researchers now treat the two theories as
**complementary** rather than rivals — physical conflict can itself destabilize
posture — describing different levels of the same phenomenon.

### 1.3 The evolutionary (toxin-detector) hypothesis — context

A separate question is *why* motion sickness causes **nausea and vomiting**.
**Treisman (1977)**, *Science* 197:493–495, proposed the widely cited
**"toxin-detector" hypothesis**: because many neurotoxins disrupt visual–
vestibular–proprioceptive coordination, the brain evolved to read *any* such
sensorimotor derangement as evidence of poisoning and to trigger emesis
defensively. Motion sickness is, on this view, an evolutionary "false alarm" of
an anti-poison defence routed through the **area postrema** [9]. It is plausible
and frequently cited but, like all adaptationist accounts, difficult to test and
criticized as a "just-so story" [10].

---

## 2. The vestibular system and why reading in a car makes you sick

### 2.1 Anatomy and physiology

The **vestibular system** of the inner ear is the body's inertial motion sensor.
It has two functional subsystems [11][12]:

- **The three semicircular canals** detect **angular (rotational) acceleration**
  of the head — pitch, roll and yaw. Each lies in a roughly orthogonal plane;
  rotation deflects the **endolymph**, bending the **cupula** and the **hair
  cells**. Because the fluid mechanics integrate acceleration, the canals
  effectively signal **head angular velocity** over the physiological range.
- **The two otolith organs — utricle and saccule** — detect **linear
  acceleration and head orientation relative to gravity**. The utricle is roughly
  horizontal (horizontal linear acceleration and tilt); the saccule roughly
  vertical (vertical acceleration, e.g. cresting a hill, and gravity). Their hair
  cells are weighted by calcium-carbonate crystals (**otoconia**); inertial drag
  on these during linear acceleration deflects the hair cells. A fundamental
  ambiguity follows from Einstein's equivalence principle: the otoliths **cannot
  distinguish a linear acceleration from a head tilt** — both produce the same
  shear. Resolving it requires combining otolith, canal and visual signals.

The brain fuses these vestibular signals with **vision** and
**proprioception** in the **vestibular nuclei and cerebellum** to estimate head
and body motion and orientation [2][5][13].

### 2.2 Timing: the vestibular system is much faster than vision

Of practical relevance to any visual mitigation: the **vestibulo-ocular reflex
(VOR)**, a minimal **three-neuron arc**, has a latency of only **~5–15 ms**
[14][15], whereas **visually mediated** eye movements and motion perception run
**~70–100+ ms** [14]. The inner ear "knows" about a bump or turn well before
vision registers it. This asymmetry is one motivation for an **anticipatory**
visual cue — one that signals motion the instant it is felt, helping the slower
visual channel catch up.

### 2.3 The passenger-reading scenario

The canonical visual–vestibular conflict is **reading a phone or book as a car
passenger** [12][16]:

- The **eyes** fixate text that is **stationary relative to the head** (the phone
  moves with the passenger). The visual scene signals **zero self-motion** — a
  stable retinal image.
- The **vestibular system** (otoliths sensing braking, acceleration and road
  undulation; canals sensing cornering) signals **continuous accelerations**.

These two signals are **mutually contradictory** and both conflict with learned
expectation; under sensory-conflict theory the mismatch accumulates toward the
nausea threshold. The everyday corroboration: **looking out at the scenery or the
horizon usually relieves the symptom**, because the optic flow of the external
world now agrees with the vestibular signal [16][17]. **Drivers are largely
immune** because they **anticipate** their own accelerations (they issue the motor
commands and watch the road), so predicted and actual feedback agree — a direct
prediction of the efference-copy account.

> **This is precisely the gap Vehicle Motion Cues / Spots target:** restore a
> congruent visual motion signal *on the screen the passenger is already looking
> at*, so they don't have to look away from their content.

---

## 3. Visually induced motion sickness, vection and peripheral vision

### 3.1 VIMS and vection

**Visually induced motion sickness (VIMS)** is motion sickness provoked by
**visual motion alone**, in a physically stationary observer — the class behind
cybersickness, simulator sickness and "cinerama sickness" [18][19]. It is the
mirror image of the carsick passenger: here the eyes signal motion while the
vestibular system signals stillness.

VIMS is closely linked to **vection** — the **illusion of self-motion** from a
moving visual field (the classic "is my train moving or the next one?"). Imaging
shows vection engages visual-motion areas **MT+/V5 and V6**, the multisensory
**VIP**, and the **parieto-insular vestibular cortex (PIVC)** — i.e. visual
self-motion is processed partly by vestibular cortical machinery [18][20].

**An honest nuance:** vection and VIMS are correlated but **dissociable**. Vection
is generally *necessary background* for VIMS but **not sufficient** — several
studies find no significant correlation between vection strength and sickness
severity, and people can experience strong vection without nausea [19][21]. "More
vection = more sickness" is an oversimplification.

### 3.2 Peripheral vision, optic flow and rest frames

The **peripheral visual field** is disproportionately important for the sense of
self-motion, for two well-supported reasons:

- **Optic flow** — the field-wide expansion/translation during self-motion — is a
  primary cue to heading and self-velocity, and the periphery carries much of it.
- **Background dominance / the rest-frame hypothesis** — the large, peripheral
  "background" structure is preferentially treated as the stationary reference
  against which self-motion is judged, while small foreground objects are treated
  as movable. A stable peripheral background therefore strongly signals "I am not
  moving."

Consequently, **forced eccentric gaze and peripheral stimulation increase both
vection and VIMS** — one study found accumulated sickness ratings ~20% higher
under forced eccentric gaze [22]. **This is the key theoretical lever for a
cueing device:** if peripheral vision dominates the sense of self-motion, then
*adding congruent motion to the periphery* (while the central field stays on
stationary text) should reduce the visual–vestibular conflict for a reading
passenger **without requiring them to look away**. This is exactly the rationale
for edge-placed dots.

---

## 4. Evidence that congruent visual motion cues reduce motion sickness

This is the section most directly relevant to a cueing feature. The evidence is
**directionally consistent but, individually, modest and preliminary.**

### 4.1 Earth-fixed reference frames / artificial horizon

- **Hemmerich, Keshavarz & Hecht (2020)**, *Frontiers in Virtual Reality*
  1:582095 — "Visually Induced Motion Sickness on the Horizon." Within-subjects,
  **N = 22** (8 further participants dropped out from severe sickness — a
  selection caveat). Four conditions (no cue / moving horizon / **Earth-fixed
  horizon** / fixation cross). **Only the Earth-fixed horizon significantly
  reduced VIMS** (F(3,63) = 2.87, p = 0.043). A static *point* did **not** help —
  a clear indication of *Earth-horizontal* is needed. Authors' own caveats:
  overall sickness was low, exposures brief, carryover possible — the benefit is
  real but **modest** [17].
- Earlier simulator/aircraft work on artificial-horizon displays reports similarly
  modest reductions, one cabin-motion study citing a ~1.6× reduction when an
  Earth-fixed frame moved opposite to cabin motion [23][24].

**Bottom line:** Earth-fixed references reliably produce **statistically
significant but modest** VIMS reductions, dependent on conveying genuine
**gravitational/Earth-horizontal** information.

### 4.2 Peripheral "moving margin" reading aids — the direct precedent

- **Meschtscherjakov, Strumegger & Trösterer (2019)** — "**Bubble Margin: Motion
  Sickness Prevention While Reading on Smartphones in Vehicles**," *INTERACT
  2019*, LNCS 660–677, DOI 10.1007/978-3-030-29384-0_39. A **University of
  Salzburg** semi-transparent **Android overlay** showing **moving "bubbles" in
  the screen margins** that shift with the vehicle's **linear acceleration**,
  leaving the reading area free. A **within-subjects pilot in real driving
  (N = 10)** with a reading task showed **mitigating effects** [25][26].
  **Limitations:** small (N = 10) pilot, single route, self-report — promising
  proof-of-concept, not definitive. (Apple's Vehicle Motion Cues productized a
  very similar idea; **Spots is an open re-implementation of this lineage**.)
- Related: **MotionReader** (peripheral "visual acceleration cues" for e-readers)
  and VR studies adding peripheral cues in cars report comparable directional
  benefits [22][27].

### 4.3 The 2024 systematic review of in-vehicle visual cueing

- **Emond, Bohrmann & Zare (2024)** — "Will visual cues help alleviating motion
  sickness in automated cars? A review article," *Ergonomics* 67(6):772–800; DOI
  10.1080/00140139.2023.2286187; **PMID 37981841** [28][29]. Conclusions:
  - **Visual cues can mitigate motion sickness** for particular in-vehicle
    configurations.
  - Each cue type was **more effective in the peripheral field than in central
    vision** — directly supporting the peripheral-dominance rationale of §3.2.
  - Most promising near-term: **interactive screens and ambient (peripheral)
    lighting**.
  - **Honest caveats:** effects on **situational awareness are unclear**; the
    literature is **heterogeneous**, making effect sizes hard to pool; much
    evidence is from **simulators and small samples**.

**Overall assessment.** The convergent evidence — Earth-fixed frames, peripheral
reading aids and the 2024 review — **consistently points the same way**:
congruent visual motion, especially in the periphery, reduces motion sickness.
But the individual studies have **small samples (often N = 10–25), brief
exposures, self-report outcomes and heterogeneous methods**, and effects are
**modest** rather than large. The field still lacks large, standardized, on-road
randomized trials. **Any product in this space — Spots included — should state
plainly that it is built on a well-motivated but still preliminary evidence base,
and is not a medical device.**

---

## 5. Key quantitative and physiological facts

### 5.1 The ~0.2 Hz nauseogenic peak

The most robust quantitative fact in the field: **low-frequency oscillation around
0.2 Hz is maximally nauseogenic**, falling off at lower and higher frequencies.

- **Golding, Mueller & Gresty (2001)**, *Aviat Space Environ Med* 72(3):188–192 —
  N = 12 susceptible subjects, 0.1/0.2/0.4 Hz at constant 1.0 m/s² peak. **0.2 Hz
  was clearly worst** (12/12 reached moderate nausea vs 8/12 and 7/12; mean
  time-to-endpoint 11.2 min at 0.2 Hz vs 18.0 and 20.2) [30][31].
- **Vertical (heave) oscillation is more provocative than horizontal** at these
  frequencies. This underlies the frequency weighting in **BS 6841** and
  **ISO 2631-1**, which weight motion most heavily near 0.2 Hz and roll off above
  ~0.5 Hz.
- **Lawther & Griffin (1987)** derived the **Motion Sickness Dose Value (MSDV)**
  from >20,000 ferry passengers: frequency-weighted r.m.s. acceleration predicts
  **vomiting incidence** roughly linearly, predicted % ≈ **⅓ × MSDV** [32][33].

The 0.2 Hz peak matters for road vehicles because typical **low-frequency
motions** (gentle stop-go, road undulation, gentle cornering) fall squarely in
this provocative band. Spots' analysis surfaces this band in its **"sickness
meter"** diagnostic (an ISO-2631-style frequency-weighted reading) — see the
[plan](../../PLAN.md).

### 5.2 Vestibular vs visual latency (recap)

- VOR / vestibular reflex latency: **~5–15 ms** (three-neuron arc) [14][15].
- Visually mediated eye movements / motion perception: **~70–100+ ms** [14].
- Implication: the vestibular channel reports motion far faster than vision,
  motivating *low-latency, anticipatory* visual cues.

### 5.3 Individual susceptibility

Susceptibility varies enormously: **roughly a third or more of people are notably
susceptible**, a minority essentially immune [34][35].

- **Age:** very low under ~2 years; **rises in childhood and peaks ~6–12** (often
  cited around age 9); declines through adolescence and adulthood; over-50s
  generally least susceptible [34][35].
- **Sex:** **females report it more often and more severely** (one population
  study: ~27% vs ~17%), modulated by hormonal state [36]; effect sizes vary and
  the mechanism is not fully established.
- **Habituation:** repeated exposure produces **adaptation** in roughly half of
  susceptible individuals [36] — the basis for graded desensitization and the
  reduced susceptibility of sailors, pilots and astronauts. Adaptation is the
  behavioural correlate of the brain updating its "neural store."
- **Other correlates:** **migraine** (especially vestibular migraine) is strongly
  associated — indeed *carsickness while reading is highly predictive of
  vestibular migraine* [16][37]; there is a heritable component and associations
  with field-dependence [34][35][38].

---

## 6. Summary of evidential strength

- **Well established:** vestibular anatomy/physiology and the visual–vestibular
  conflict of reading-while-riding; the ~0.2 Hz nauseogenic peak and the
  MSDV / ISO 2631 framework; the large vestibular-vs-visual latency gap; the broad
  pattern of susceptibility; that an Earth-fixed / peripheral congruent visual cue
  produces a *statistically significant* reduction in sickness.
- **Mainstream but contested / qualitative:** sensory-conflict theory as the
  *mechanism* (powerful and predictive but hard to falsify; complements postural-
  instability theory).
- **Promising but preliminary:** the *magnitude* and real-world durability of
  phone-based peripheral motion-cue aids (Bubble Margin N = 10; horizon studies
  N ≈ 22; heterogeneous methods; few large on-road RCTs). Effects are consistently
  **positive but modest**.

**What this means for Spots.** The design rests on solid, well-understood
mechanisms (peripheral optic flow, congruent visual–vestibular signalling), and
on a directionally consistent but still-preliminary evidence base. Spots is an
**experimental comfort aid, not a medical device**, and says so in-app.

---

## References

1. Reason, J.T. & Brand, J.J. (1975). *Motion Sickness*. Academic Press.
2. Reason, J.T. (1978). Motion sickness adaptation: a neural mismatch model. *J. R. Soc. Med.* 71(11):819–829 — <https://journals.sagepub.com/doi/pdf/10.1177/014107687807101109>
3. Computational model of motion sickness (efference copy, leaky integration), *Front. Syst. Neurosci.* (2021) — <https://www.frontiersin.org/articles/10.3389/fnsys.2021.634604/full>
4. Validating models of sensory conflict for motion-sickness prediction, *Biol. Cybern.* (2023) — <https://pmc.ncbi.nlm.nih.gov/articles/PMC10258185/>
5. Neuroanatomy, vestibular pathways. *StatPearls*, NCBI — <https://www.ncbi.nlm.nih.gov/books/NBK557380/>
6. Riccio, G.E. & Stoffregen, T.A. (1991). An ecological theory of motion sickness and postural instability. *Ecol. Psychol.* 3(3):195–240 — <https://www.tandfonline.com/doi/abs/10.1207/s15326969eco0303_2>
7. Stoffregen, T.A. & Smart, L.J. (1998). Postural instability precedes motion sickness. *Brain Res. Bull.* — <https://www.sciencedirect.com/science/article/abs/pii/S0361923098001026>
8. Smart, Stoffregen & Bardy (2002). VIMS predicted by postural instability. *Human Factors* 44(3):451–465. PMID 12502162 — <https://pubmed.ncbi.nlm.nih.gov/12502162/>
9. Treisman, M. (1977). Motion sickness: an evolutionary hypothesis. *Science* 197(4302):493–495 — <https://www.science.org/doi/10.1126/science.301659>
10. Oman, C.M. (2012). Are evolutionary hypotheses for motion sickness "just-so" stories? *J. Vestib. Res.* — <https://pubmed.ncbi.nlm.nih.gov/23000611/>
11. Vestibular system anatomy. Kenhub — <https://www.kenhub.com/en/library/anatomy/the-vestibular-system>
12. Vestibular system: function & anatomy. Cleveland Clinic — <https://my.clevelandclinic.org/health/body/vestibular-system>
13. Cullen, K.E. (2012). The vestibular system: multimodal integration and encoding of self-motion. *Trends Neurosci.* — <https://pmc.ncbi.nlm.nih.gov/articles/PMC4000483/>
14. VOR vs visual latency — <https://brianwerner.substack.com/p/the-need-for-speed-why-latency-is-crucial-in-vestibular-rehabilitation>
15. Vestibulo-ocular reflex testing overview (three-neuron arc, latency). Medscape — <https://emedicine.medscape.com/article/1836134-overview>
16. Motion sickness whilst reading as a passenger predicts vestibular migraine, *Front. Neurol.* (2024) — <https://www.frontiersin.org/articles/10.3389/fneur.2024.1426081/full>
17. Hemmerich, Keshavarz & Hecht (2020). Visually Induced Motion Sickness on the Horizon. *Front. Virtual Real.* 1:582095 — <https://www.frontiersin.org/articles/10.3389/frvir.2020.582095/full>
18. Optic-flow-selective cortical regions associated with vection. PMC — <https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4459088/>
19. Vection does not necessitate VIMS, *Displays/Applied Ergonomics* (2018) — <https://www.sciencedirect.com/science/article/abs/pii/S0141938218300477>
20. Vection as main contributor to MS from visual yaw rotation. PMC — <https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5381945/>
21. Comparing displays for enhancing vection, *Front. Psychol.* (2015) — <https://www.frontiersin.org/articles/10.3389/fpsyg.2015.00713/full>
22. Eye movement, vection and motion sickness with foveal vs peripheral vision (forced eccentric gaze ≈ +20%) — <https://www.researchgate.net/publication/10718786>
23. Artificial Horizon Effects on Motion Sickness and Performance — <https://www.researchgate.net/publication/225072864>
24. A visual display enhancing comfort by counteracting airsickness, *Displays* (2010) — <https://www.sciencedirect.com/science/article/abs/pii/S0141938210000818>
25. Meschtscherjakov, Strumegger & Trösterer (2019). Bubble Margin. *INTERACT 2019*, 660–677 — <https://link.springer.com/chapter/10.1007/978-3-030-29384-0_39> (open: <https://inria.hal.science/hal-02544623/>)
26. Popular Science summary of carsickness, peripheral cues, "Bubble Margin" — <https://www.popsci.com/diy/vehicle-motion-cues-iphone-carsickness/>
27. MotionReader: Visual Acceleration Cues for Passenger E-Reader Motion Sickness — <https://www.researchgate.net/publication/320362609>
28. Emond, Bohrmann & Zare (2024). Will visual cues help…? *Ergonomics* 67(6):772–800. PMID 37981841 — <https://pubmed.ncbi.nlm.nih.gov/37981841/>
29. Same review (publisher) — <https://www.tandfonline.com/doi/abs/10.1080/00140139.2023.2286187>
30. Golding, Mueller & Gresty (2001). 0.2 Hz motion-sickness maximum. *Aviat Space Environ Med* 72(3):188–192. PMID 11277284 — <https://pubmed.ncbi.nlm.nih.gov/11277284/>
31. Same (ResearchGate) — <https://www.researchgate.net/publication/12056741>
32. Lawther & Griffin (1987). Prediction of motion-sickness incidence (MSDV; basis of BS 6841 / ISO 2631) — <https://pure.mpg.de/rest/items/item_3008626_2/component/file_3008627/content>
33. Motion sickness (vibration / MSDV / ISO 2631 overview). ILO Encyclopaedia — <https://www.iloencyclopaedia.org/part-vi-16255/vibration/item/790-motion-sickness>
34. Motion sickness. *StatPearls*, NCBI — <https://www.ncbi.nlm.nih.gov/books/NBK539706/>
35. Golding, J.F. (2006). Motion sickness susceptibility. *Auton. Neurosci.* — <https://www.sciencedirect.com/science/article/abs/pii/S1566070206002128>
36. Sharma & Aparna (1997). Prevalence and correlates of susceptibility. PMID 9492893 — <https://pubmed.ncbi.nlm.nih.gov/9492893/>
37. Motion-sickness reading → vestibular-migraine prediction, *Front. Neurol.* (2024) — <https://www.frontiersin.org/articles/10.3389/fneur.2024.1426081/full>
38. Keshavarz, Andrievskaia & Berti (2025). Individual-difference factors in VIMS and vection. PMID 41147534 — <https://pubmed.ncbi.nlm.nih.gov/41147534/>

### Notes

- A few primary sources (Bubble Margin full text; Smart/Stoffregen 2002 full text;
  the *Ergonomics* publisher page) were paywalled; their key facts (authors,
  venue, N, design, findings) were confirmed via PubMed / Springer / Inria
  metadata and abstracts. Where a number comes from an abstract or pilot, that is
  stated.
- The review **distinguishes established science** (vestibular physiology, the
  0.2 Hz peak, MSDV/ISO 2631, latency, susceptibility) **from hypotheses**
  (sensory-conflict vs postural-instability; toxin-detector) **and from
  preliminary mitigation evidence** (small-N, modest, heterogeneous).
