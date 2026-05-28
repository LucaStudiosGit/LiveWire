// Chair Tai Chi — gentle seated practice.
// Self-contained: no build step, no network, no dependencies.

const EXERCISES = [
  {
    id: "centering",
    name: "Centering breath",
    cue: "Sit tall. Drop the shoulders. Breathe in through the nose, out through the mouth.",
    detail: "Place the hands palm-down on the thighs. Notice the chair beneath you, the floor under your feet.",
    seconds: 60,
    rep: "Slow breaths — about 4 seconds in, 4 seconds out.",
    figure: "centering",
  },
  {
    id: "shoulder-rolls",
    name: "Shoulder rolls",
    cue: "Circle the shoulders backward, slowly and softly.",
    detail: "Eight circles back, then eight forward. Let the chest open as the shoulders move.",
    seconds: 60,
    rep: "8 back × 8 forward",
    figure: "shoulders",
  },
  {
    id: "neck",
    name: "Neck release",
    cue: "Drop the right ear toward the right shoulder. Then the left. Then chin to chest.",
    detail: "Move with the breath. Never force — go only where the stretch feels gentle.",
    seconds: 75,
    rep: "Hold each side for two breaths.",
    figure: "neck",
  },
  {
    id: "opening",
    name: "Opening the chest",
    cue: "Raise both arms forward, then out to the sides as you breathe in. Lower as you breathe out.",
    detail: "Lift only as high as the shoulders. Imagine parting a curtain of warm air.",
    seconds: 90,
    rep: "8 slow rounds",
    figure: "open",
    tier: "full",
  },
  {
    id: "cloud-hands",
    name: "Cloud hands (seated)",
    cue: "Hands float in front of the chest. The waist turns gently, right then left, hands trailing.",
    detail: "The hands stay soft. Picture moving through still water — slow, even, weightless.",
    seconds: 120,
    rep: "Side to side, 12 turns",
    figure: "clouds",
    tier: "full",
  },
  {
    id: "sparrow",
    name: "Grasp the sparrow's tail",
    cue: "Right hand reaches forward as if cradling something light. Draw it back to the chest. Repeat with the left.",
    detail: "The shoulder stays low. The breath leads the hand — in as it returns, out as it reaches.",
    seconds: 120,
    rep: "6 each side",
    figure: "sparrow",
    tier: "long",
  },
  {
    id: "knees",
    name: "Knee lifts",
    cue: "Lift the right knee a few inches, lower. Then the left. Keep the back tall.",
    detail: "Hold the chair lightly if balance asks for it. Move smoothly, no bouncing.",
    seconds: 90,
    rep: "10 lifts each side",
    figure: "knees",
    tier: "long",
  },
  {
    id: "ankles",
    name: "Ankle circles",
    cue: "Extend the right foot. Draw slow circles with the toes. Switch sides.",
    detail: "Eight circles one way, eight the other, each foot.",
    seconds: 75,
    rep: "8 × 4 directions",
    figure: "ankles",
    tier: "full",
  },
  {
    id: "settle",
    name: "Gathering qi",
    cue: "Hands rise up the centerline, palms up, then press gently down past the face and chest.",
    detail: "As if collecting warm light overhead and bringing it down to rest at the belly.",
    seconds: 60,
    rep: "6 gatherings",
    figure: "gather",
  },
  {
    id: "closing",
    name: "Closing stillness",
    cue: "Hands rest on the lower belly. Breathe naturally. Let everything settle.",
    detail: "Notice warmth, weight, quiet. There is nothing to do here.",
    seconds: 45,
    rep: "Slow, easy breaths.",
    figure: "centering",
  },
];

// Figures: small SVGs evoking each movement. Kept abstract on purpose.
const FIGURES = {
  centering: `
    <svg viewBox="0 0 200 200">
      <circle class="breath" cx="100" cy="100" r="60" fill="none" stroke="currentColor" stroke-width="2" opacity="0.5"/>
      <circle cx="100" cy="100" r="22" fill="currentColor" opacity="0.9"/>
      <line x1="100" y1="40" x2="100" y2="160" stroke="currentColor" stroke-width="1.5" opacity="0.35"/>
    </svg>`,
  shoulders: `
    <svg viewBox="0 0 200 200">
      <circle cx="100" cy="60" r="14" fill="currentColor"/>
      <path d="M60 95 Q100 80 140 95" stroke="currentColor" stroke-width="3" fill="none" stroke-linecap="round"/>
      <path class="breath" d="M50 110 Q100 130 150 110" stroke="currentColor" stroke-width="2" fill="none" opacity="0.55"/>
      <rect x="78" y="100" width="44" height="80" rx="14" fill="currentColor" opacity="0.85"/>
    </svg>`,
  neck: `
    <svg viewBox="0 0 200 200">
      <circle class="breath" cx="115" cy="60" r="16" fill="currentColor"/>
      <path d="M115 76 Q100 95 105 130" stroke="currentColor" stroke-width="3" fill="none"/>
      <rect x="70" y="120" width="60" height="60" rx="14" fill="currentColor" opacity="0.85"/>
      <path d="M40 80 Q90 50 140 60" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.4"/>
    </svg>`,
  open: `
    <svg viewBox="0 0 200 200">
      <circle cx="100" cy="60" r="14" fill="currentColor"/>
      <path class="breath" d="M40 110 Q70 70 100 95 Q130 70 160 110" stroke="currentColor" stroke-width="3" fill="none" stroke-linecap="round"/>
      <rect x="78" y="100" width="44" height="80" rx="14" fill="currentColor" opacity="0.85"/>
    </svg>`,
  clouds: `
    <svg viewBox="0 0 200 200">
      <circle cx="100" cy="55" r="12" fill="currentColor"/>
      <path class="breath" d="M50 110 Q80 90 100 110 Q120 130 150 110" stroke="currentColor" stroke-width="3" fill="none" stroke-linecap="round"/>
      <ellipse cx="65" cy="108" rx="14" ry="7" fill="currentColor" opacity="0.5"/>
      <ellipse cx="135" cy="112" rx="14" ry="7" fill="currentColor" opacity="0.5"/>
      <rect x="78" y="100" width="44" height="80" rx="14" fill="currentColor" opacity="0.85"/>
    </svg>`,
  sparrow: `
    <svg viewBox="0 0 200 200">
      <circle cx="100" cy="60" r="14" fill="currentColor"/>
      <path d="M100 75 L170 110" stroke="currentColor" stroke-width="3" fill="none" stroke-linecap="round"/>
      <circle class="breath" cx="170" cy="110" r="8" fill="currentColor"/>
      <path d="M100 75 L60 115" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" opacity="0.4"/>
      <rect x="78" y="100" width="44" height="80" rx="14" fill="currentColor" opacity="0.85"/>
    </svg>`,
  knees: `
    <svg viewBox="0 0 200 200">
      <circle cx="100" cy="55" r="12" fill="currentColor"/>
      <rect x="78" y="70" width="44" height="60" rx="14" fill="currentColor" opacity="0.85"/>
      <path class="breath" d="M85 130 Q70 150 80 180" stroke="currentColor" stroke-width="5" fill="none" stroke-linecap="round"/>
      <path d="M115 130 Q130 150 120 180" stroke="currentColor" stroke-width="5" fill="none" stroke-linecap="round" opacity="0.55"/>
    </svg>`,
  ankles: `
    <svg viewBox="0 0 200 200">
      <rect x="80" y="50" width="40" height="70" rx="14" fill="currentColor" opacity="0.85"/>
      <line x1="100" y1="120" x2="100" y2="160" stroke="currentColor" stroke-width="4" stroke-linecap="round"/>
      <ellipse class="breath" cx="100" cy="170" rx="34" ry="10" fill="none" stroke="currentColor" stroke-width="2"/>
      <circle cx="100" cy="170" r="6" fill="currentColor"/>
    </svg>`,
  gather: `
    <svg viewBox="0 0 200 200">
      <circle cx="100" cy="55" r="12" fill="currentColor"/>
      <path class="breath" d="M100 70 V150" stroke="currentColor" stroke-width="3" fill="none"/>
      <path d="M70 95 Q100 70 130 95" stroke="currentColor" stroke-width="2" fill="none" opacity="0.6"/>
      <path d="M60 130 Q100 110 140 130" stroke="currentColor" stroke-width="2" fill="none" opacity="0.45"/>
      <circle cx="100" cy="150" r="10" fill="currentColor" opacity="0.85"/>
    </svg>`,
};

// ---- State ----------------------------------------------------------------

const state = {
  list: [],
  idx: 0,
  remaining: 0,
  total: 0,
  tickHandle: null,
  paused: false,
  pace: 1,
  sound: true,
  voice: false,
};

// ---- Element refs ---------------------------------------------------------

const $ = (id) => document.getElementById(id);
const els = {
  welcome: $("welcome"),
  session: $("session"),
  done: $("done"),
  start: $("start-btn"),
  restart: $("restart-btn"),
  end: $("end-btn"),
  next: $("next-btn"),
  prev: $("prev-btn"),
  pause: $("pause-btn"),
  name: $("exercise-name"),
  cue: $("exercise-cue"),
  detail: $("exercise-detail"),
  timeLeft: $("time-left"),
  stepCounter: $("step-counter"),
  progress: $("progress-fill"),
  figure: $("figure"),
  repHint: $("rep-hint"),
  pace: $("pace"),
  routine: $("routine"),
  totalMinutes: $("total-minutes"),
  sound: $("sound-toggle"),
  voice: $("voice-toggle"),
  exerciseCard: document.querySelector(".exercise-card"),
};

// ---- Audio ----------------------------------------------------------------

let audioCtx = null;
function chime(kind = "tick") {
  if (!state.sound) return;
  try {
    audioCtx ??= new (window.AudioContext || window.webkitAudioContext)();
    const now = audioCtx.currentTime;
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.type = "sine";
    osc.frequency.value = kind === "end" ? 528 : kind === "start" ? 440 : 660;
    gain.gain.setValueAtTime(0.0001, now);
    gain.gain.exponentialRampToValueAtTime(0.18, now + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + 1.2);
    osc.connect(gain).connect(audioCtx.destination);
    osc.start(now);
    osc.stop(now + 1.3);
  } catch {
    /* audio is best-effort */
  }
}

function speak(text) {
  if (!state.voice || !("speechSynthesis" in window)) return;
  try {
    window.speechSynthesis.cancel();
    const u = new SpeechSynthesisUtterance(text);
    u.rate = 0.95;
    u.pitch = 1.0;
    u.volume = 0.85;
    window.speechSynthesis.speak(u);
  } catch {
    /* speech is best-effort */
  }
}

// ---- Flow -----------------------------------------------------------------

function buildList(routine) {
  if (routine === "short") return EXERCISES.filter((e) => !e.tier);
  if (routine === "long") return EXERCISES;
  return EXERCISES.filter((e) => e.tier !== "long");
}

function totalMinutes(list, pace) {
  const secs = list.reduce((s, e) => s + e.seconds / pace, 0);
  return Math.round(secs / 60);
}

function show(screen) {
  for (const s of [els.welcome, els.session, els.done]) s.classList.remove("active");
  screen.classList.add("active");
}

function fmt(s) {
  const m = Math.floor(s / 60);
  const r = Math.max(0, Math.floor(s % 60));
  return `${m}:${r.toString().padStart(2, "0")}`;
}

function renderExercise() {
  const ex = state.list[state.idx];
  els.name.textContent = ex.name;
  els.cue.textContent = ex.cue;
  els.detail.textContent = ex.detail;
  els.repHint.textContent = ex.rep || "";
  els.stepCounter.textContent = `${state.idx + 1} / ${state.list.length}`;
  els.figure.innerHTML = FIGURES[ex.figure] || FIGURES.centering;
  els.timeLeft.textContent = fmt(state.remaining);
  const done = state.list.slice(0, state.idx).reduce((s, e) => s + e.seconds / state.pace, 0);
  const pct = (done / state.total) * 100;
  els.progress.style.width = pct + "%";
}

function startExercise(i) {
  if (i >= state.list.length) return finish();
  if (i < 0) i = 0;
  state.idx = i;
  const ex = state.list[i];
  state.remaining = Math.round(ex.seconds / state.pace);
  renderExercise();
  chime("start");
  speak(ex.name + ". " + ex.cue);
  resumeTicker();
}

function tick() {
  if (state.paused) return;
  state.remaining -= 1;
  if (state.remaining <= 0) {
    chime("end");
    startExercise(state.idx + 1);
    return;
  }
  els.timeLeft.textContent = fmt(state.remaining);
  const ex = state.list[state.idx];
  const elapsed = Math.round(ex.seconds / state.pace) - state.remaining;
  const exShare = ex.seconds / state.pace / state.total;
  const base =
    state.list.slice(0, state.idx).reduce((s, e) => s + e.seconds / state.pace, 0) /
    state.total;
  els.progress.style.width = (base + exShare * (elapsed / (ex.seconds / state.pace))) * 100 + "%";
}

function resumeTicker() {
  clearInterval(state.tickHandle);
  state.tickHandle = setInterval(tick, 1000);
}

function pauseTicker() {
  clearInterval(state.tickHandle);
  state.tickHandle = null;
}

function setPaused(p) {
  state.paused = p;
  els.pause.textContent = p ? "Resume" : "Pause";
  els.exerciseCard.classList.toggle("paused", p);
  if (p) pauseTicker();
  else resumeTicker();
}

function finish() {
  pauseTicker();
  chime("end");
  speak("Practice complete.");
  show(els.done);
  els.progress.style.width = "100%";
}

function endSession() {
  pauseTicker();
  show(els.welcome);
}

// ---- Wiring ---------------------------------------------------------------

function recomputeMinutesPreview() {
  const list = buildList(els.routine.value);
  const pace = parseFloat(els.pace.value);
  els.totalMinutes.textContent = totalMinutes(list, pace);
}

els.routine.addEventListener("change", recomputeMinutesPreview);
els.pace.addEventListener("change", recomputeMinutesPreview);

els.start.addEventListener("click", () => {
  state.pace = parseFloat(els.pace.value);
  state.sound = els.sound.checked;
  state.voice = els.voice.checked;
  state.list = buildList(els.routine.value);
  state.total = state.list.reduce((s, e) => s + e.seconds / state.pace, 0);
  state.paused = false;
  els.pause.textContent = "Pause";
  els.exerciseCard.classList.remove("paused");
  show(els.session);
  startExercise(0);
});

els.restart.addEventListener("click", () => show(els.welcome));
els.end.addEventListener("click", endSession);
els.pause.addEventListener("click", () => setPaused(!state.paused));
els.next.addEventListener("click", () => {
  pauseTicker();
  startExercise(state.idx + 1);
});
els.prev.addEventListener("click", () => {
  pauseTicker();
  startExercise(state.idx - 1);
});

els.sound.addEventListener("change", () => (state.sound = els.sound.checked));
els.voice.addEventListener("change", () => {
  state.voice = els.voice.checked;
  if (!state.voice && "speechSynthesis" in window) window.speechSynthesis.cancel();
});

// Keyboard convenience
window.addEventListener("keydown", (e) => {
  if (!els.session.classList.contains("active")) return;
  if (e.key === " ") { e.preventDefault(); setPaused(!state.paused); }
  if (e.key === "ArrowRight") { pauseTicker(); startExercise(state.idx + 1); }
  if (e.key === "ArrowLeft")  { pauseTicker(); startExercise(state.idx - 1); }
});

// Pause when the tab is hidden so the timer matches what the user sees.
document.addEventListener("visibilitychange", () => {
  if (document.hidden && !state.paused && els.session.classList.contains("active")) {
    setPaused(true);
  }
});

recomputeMinutesPreview();
