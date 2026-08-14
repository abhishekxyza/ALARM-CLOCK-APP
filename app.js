// Web Audio API Synthesizer Context
let audioCtx = null;
let audioInterval = null;
let isPreviewing = false;
let currentPreviewTone = null;

// Initialize Web Audio Context
function getAudioContext() {
  if (!audioCtx) {
    audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  }
  if (audioCtx.state === 'suspended') {
    audioCtx.resume();
  }
  return audioCtx;
}

// Tone Synthesizers
const synthTones = {
  classic: (ctx, time) => {
    // Pulse beep beep
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    
    osc.type = 'square';
    osc.frequency.setValueAtTime(880, time);
    
    gain.gain.setValueAtTime(0, time);
    gain.gain.linearRampToValueAtTime(0.5, time + 0.05);
    gain.gain.setValueAtTime(0.5, time + 0.15);
    gain.gain.linearRampToValueAtTime(0, time + 0.2);
    
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start(time);
    osc.stop(time + 0.25);
  },
  
  digital: (ctx, time) => {
    // Double retro tone
    const osc1 = ctx.createOscillator();
    const osc2 = ctx.createOscillator();
    const gain = ctx.createGain();
    
    osc1.type = 'sawtooth';
    osc1.frequency.setValueAtTime(1200, time);
    osc2.type = 'triangle';
    osc2.frequency.setValueAtTime(1500, time + 0.08);
    
    gain.gain.setValueAtTime(0, time);
    gain.gain.linearRampToValueAtTime(0.3, time + 0.02);
    gain.gain.setValueAtTime(0.3, time + 0.15);
    gain.gain.linearRampToValueAtTime(0, time + 0.18);
    
    osc1.connect(gain);
    osc2.connect(gain);
    gain.connect(ctx.destination);
    
    osc1.start(time);
    osc1.stop(time + 0.18);
    osc2.start(time + 0.08);
    osc2.stop(time + 0.18);
  },
  
  chime: (ctx, time) => {
    // Gentle ringing bells
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    
    osc.type = 'sine';
    osc.frequency.setValueAtTime(587.33, time); // D5
    osc.frequency.exponentialRampToValueAtTime(880, time + 0.1);
    
    gain.gain.setValueAtTime(0, time);
    gain.gain.linearRampToValueAtTime(0.4, time + 0.05);
    gain.gain.exponentialRampToValueAtTime(0.001, time + 0.8);
    
    osc.connect(gain);
    gain.connect(ctx.destination);
    
    osc.start(time);
    osc.stop(time + 0.8);
  },
  
  pulse: (ctx, time) => {
    // Sci-fi space pulse sweeps
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    
    osc.type = 'triangle';
    osc.frequency.setValueAtTime(200, time);
    osc.frequency.linearRampToValueAtTime(600, time + 0.3);
    
    gain.gain.setValueAtTime(0, time);
    gain.gain.linearRampToValueAtTime(0.4, time + 0.05);
    gain.gain.linearRampToValueAtTime(0, time + 0.3);
    
    osc.connect(gain);
    gain.connect(ctx.destination);
    
    osc.start(time);
    osc.stop(time + 0.3);
  }
};

// Play sound loop
function playToneLoop(toneName) {
  stopToneLoop();
  const ctx = getAudioContext();
  
  let loopCount = 0;
  const loopFunc = () => {
    const now = ctx.currentTime;
    // Trigger play logic
    if (toneName === 'classic') {
      synthTones.classic(ctx, now);
      synthTones.classic(ctx, now + 0.3);
    } else if (toneName === 'digital') {
      synthTones.digital(ctx, now);
      synthTones.digital(ctx, now + 0.25);
    } else if (toneName === 'chime') {
      synthTones.chime(ctx, now);
    } else if (toneName === 'pulse') {
      synthTones.pulse(ctx, now);
    }
  };
  
  // Initial play
  loopFunc();
  const intervalTime = toneName === 'chime' ? 1000 : toneName === 'pulse' ? 500 : 700;
  audioInterval = setInterval(loopFunc, intervalTime);
}

function stopToneLoop() {
  if (audioInterval) {
    clearInterval(audioInterval);
    audioInterval = null;
  }
}

// State Management
let alarms = JSON.parse(localStorage.getItem('alarms')) || [];
let activeRingingAlarm = null;
let editingAlarmId = null;

// DOM Elements
const timeDisplay = document.getElementById('current-time');
const ampmDisplay = document.getElementById('time-ampm');
const dateDisplay = document.getElementById('current-date');
const themeToggle = document.getElementById('theme-toggle');
const moonIcon = themeToggle.querySelector('.moon-icon');
const sunIcon = themeToggle.querySelector('.sun-icon');

const alarmsList = document.getElementById('alarms-list');
const emptyState = document.getElementById('empty-state');

const alarmModal = document.getElementById('alarm-modal');
const addAlarmBtn = document.getElementById('add-alarm-btn');
const modalClose = document.getElementById('modal-close');
const alarmForm = document.getElementById('alarm-form');
const deleteAlarmFormBtn = document.getElementById('delete-alarm-form-btn');
const modalTitle = document.getElementById('modal-title');

// Time Picker Elements
const hourSelect = document.getElementById('alarm-hour');
const minuteSelect = document.getElementById('alarm-minute');
const ampmSelect = document.getElementById('alarm-ampm');
const labelInput = document.getElementById('alarm-label');
const toneSelect = document.getElementById('alarm-tone');
const previewToneBtn = document.getElementById('preview-tone-btn');

// Ringing Overlay Elements
const ringingOverlay = document.getElementById('ringing-overlay');
const ringingTime = document.getElementById('ringing-time');
const ringingLabel = document.getElementById('ringing-label');
const dismissBtn = document.getElementById('dismiss-alarm-btn');
const snoozeBtn = document.getElementById('snooze-alarm-btn');

// Populate minutes select options (00 to 59)
function populateMinutes() {
  minuteSelect.innerHTML = '';
  for (let i = 0; i < 60; i++) {
    const val = i.toString().padStart(2, '0');
    const option = document.createElement('option');
    option.value = val;
    option.textContent = val;
    minuteSelect.appendChild(option);
  }
}

// Clock updates
function updateClock() {
  const now = new Date();
  
  // Format Date
  const dateOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  dateDisplay.textContent = now.toLocaleDateString('en-US', dateOptions);
  
  // Format Time
  let hours = now.getHours();
  const minutes = now.getMinutes().toString().padStart(2, '0');
  const seconds = now.getSeconds().toString().padStart(2, '0');
  const ampm = hours >= 12 ? 'PM' : 'AM';
  
  hours = hours % 12;
  hours = hours ? hours : 12; // 0 should be 12
  const formattedHours = hours.toString().padStart(2, '0');
  
  timeDisplay.textContent = `${formattedHours}:${minutes}:${seconds}`;
  ampmDisplay.textContent = ampm;

  // Check Alarms
  checkAlarms(formattedHours, minutes, ampm, now.getDay());
}

// Check if any alarm should trigger
function checkAlarms(hours, minutes, ampm, dayOfWeek) {
  if (activeRingingAlarm) return; // Already ringing
  
  const currentFormattedTime = `${hours}:${minutes} ${ampm}`;
  
  alarms.forEach(alarm => {
    if (!alarm.active) return;
    
    // Check time match
    const alarmTimeFormatted = `${alarm.hour}:${alarm.minute} ${alarm.ampm}`;
    if (alarmTimeFormatted === currentFormattedTime) {
      // Check repeat days or if this is a one-time alarm
      const hasRepeat = alarm.days && alarm.days.length > 0;
      if (hasRepeat && !alarm.days.includes(dayOfWeek.toString())) {
        return; // Scheduled for repeating, but not today
      }
      
      // Secondary check: ensure we don't trigger multiple times in the same minute
      const nowEpoch = Math.floor(Date.now() / 60000);
      if (alarm.lastTriggeredMinute === nowEpoch) {
        return;
      }
      
      alarm.lastTriggeredMinute = nowEpoch;
      
      // If it doesn't repeat, deactivate it
      if (!hasRepeat) {
        alarm.active = false;
      }
      
      saveAlarmsToStorage();
      triggerAlarm(alarm);
    }
  });
}

// Trigger Alarm Ringing State
function triggerAlarm(alarm) {
  activeRingingAlarm = alarm;
  ringingTime.textContent = `${alarm.hour}:${alarm.minute} ${alarm.ampm}`;
  ringingLabel.textContent = alarm.label || 'Wake Up!';
  ringingOverlay.classList.remove('hidden');
  
  // Play sound loop
  playToneLoop(alarm.tone || 'classic');
}

// Handle Dismiss Alarm
function dismissAlarm() {
  stopToneLoop();
  activeRingingAlarm = null;
  ringingOverlay.classList.add('hidden');
  renderAlarms();
}

// Handle Snooze Alarm (Snooze adds a temporary alarm for 5 minutes later)
function snoozeAlarm() {
  if (!activeRingingAlarm) return;
  
  stopToneLoop();
  
  // Calculate snooze time
  const now = new Date();
  const snoozeMinutes = 5;
  const snoozeTime = new Date(now.getTime() + snoozeMinutes * 60000);
  
  let snoozeHour = snoozeTime.getHours();
  const snoozeMin = snoozeTime.getMinutes().toString().padStart(2, '0');
  const snoozeAmPm = snoozeHour >= 12 ? 'PM' : 'AM';
  snoozeHour = snoozeHour % 12;
  snoozeHour = snoozeHour ? snoozeHour : 12;
  const snoozeHourStr = snoozeHour.toString().padStart(2, '0');
  
  // Create virtual snooze alarm (one-time)
  const snoozedAlarm = {
    id: 'snooze-' + Date.now(),
    hour: snoozeHourStr,
    minute: snoozeMin,
    ampm: snoozeAmPm,
    label: `Snooze: ${activeRingingAlarm.label || 'Alarm'}`,
    tone: activeRingingAlarm.tone,
    days: [],
    active: true
  };
  
  alarms.push(snoozedAlarm);
  saveAlarmsToStorage();
  
  activeRingingAlarm = null;
  ringingOverlay.classList.add('hidden');
  renderAlarms();
}

// Theme logic
function initTheme() {
  const savedTheme = localStorage.getItem('theme') || 'dark';
  document.documentElement.setAttribute('data-theme', savedTheme);
  updateThemeUI(savedTheme);
}

function updateThemeUI(theme) {
  if (theme === 'dark') {
    moonIcon.classList.remove('hidden');
    sunIcon.classList.add('hidden');
  } else {
    moonIcon.classList.add('hidden');
    sunIcon.classList.remove('hidden');
  }
}

themeToggle.addEventListener('click', () => {
  const currentTheme = document.documentElement.getAttribute('data-theme');
  const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', newTheme);
  localStorage.setItem('theme', newTheme);
  updateThemeUI(newTheme);
});

// Render List of Alarms
function renderAlarms() {
  alarmsList.innerHTML = '';
  
  // Filter out expired snooze alarms (that are not active anymore)
  // Let's keep active snooze alarms, and clean up inactive ones
  alarms = alarms.filter(alarm => {
    if (alarm.id.toString().startsWith('snooze') && !alarm.active) {
      return false;
    }
    return true;
  });
  
  if (alarms.length === 0) {
    emptyState.classList.remove('hidden');
    return;
  }
  
  emptyState.classList.add('hidden');
  
  alarms.forEach(alarm => {
    const card = document.createElement('div');
    card.className = `alarm-card ${alarm.active ? '' : 'inactive'}`;
    
    // Formatting repeat days
    const dayNames = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
    let repeatText = 'Once';
    if (alarm.days && alarm.days.length > 0) {
      if (alarm.days.length === 7) {
        repeatText = 'Every day';
      } else if (alarm.days.length === 5 && !alarm.days.includes('0') && !alarm.days.includes('6')) {
        repeatText = 'Weekdays';
      } else {
        repeatText = alarm.days.map(d => dayNames[parseInt(d)]).join(' ');
      }
    }
    
    card.innerHTML = `
      <div class="alarm-card-info">
        <div class="alarm-card-time-row">
          <span class="alarm-card-time">${alarm.hour}:${alarm.minute}</span>
          <span class="alarm-card-ampm">${alarm.ampm}</span>
        </div>
        <div class="alarm-card-label">${alarm.label || 'Alarm'}</div>
        <div class="alarm-card-days">${repeatText}</div>
      </div>
      <div class="alarm-card-controls">
        <label class="switch" aria-label="Toggle Alarm">
          <input type="checkbox" class="alarm-toggle" ${alarm.active ? 'checked' : ''}>
          <span class="slider"></span>
        </label>
      </div>
    `;
    
    // Event listener to open edit modal
    card.addEventListener('click', (e) => {
      // Don't trigger if clicked on toggle switch
      if (e.target.closest('.switch')) return;
      openEditModal(alarm);
    });
    
    // Event listener for toggle switch
    const toggleInput = card.querySelector('.alarm-toggle');
    toggleInput.addEventListener('change', () => {
      alarm.active = toggleInput.checked;
      saveAlarmsToStorage();
      renderAlarms();
    });
    
    alarmsList.appendChild(card);
  });
}

function saveAlarmsToStorage() {
  localStorage.setItem('alarms', JSON.stringify(alarms));
}

// Modal handling
function openAddModal() {
  editingAlarmId = null;
  modalTitle.textContent = 'New Alarm';
  deleteAlarmFormBtn.classList.add('hidden');
  alarmForm.reset();
  
  // Pre-fill with current hour/minute
  const now = new Date();
  let hr = now.getHours();
  const ampm = hr >= 12 ? 'PM' : 'AM';
  hr = hr % 12;
  hr = hr ? hr : 12;
  
  hourSelect.value = hr.toString().padStart(2, '0');
  minuteSelect.value = now.getMinutes().toString().padStart(2, '0');
  ampmSelect.value = ampm;
  toneSelect.value = 'classic';
  
  alarmModal.classList.remove('hidden');
}

function openEditModal(alarm) {
  editingAlarmId = alarm.id;
  modalTitle.textContent = 'Edit Alarm';
  deleteAlarmFormBtn.classList.remove('hidden');
  
  hourSelect.value = alarm.hour;
  minuteSelect.value = alarm.minute;
  ampmSelect.value = alarm.ampm;
  labelInput.value = alarm.label || '';
  toneSelect.value = alarm.tone || 'classic';
  
  // Uncheck/Check repeat days
  const checkboxes = alarmForm.querySelectorAll('input[name="days"]');
  checkboxes.forEach(cb => {
    cb.checked = alarm.days.includes(cb.value);
  });
  
  alarmModal.classList.remove('hidden');
}

function closeModal() {
  alarmModal.classList.add('hidden');
  stopTonePreview();
}

// Sound Preview Logic
function toggleTonePreview() {
  if (isPreviewing) {
    stopTonePreview();
  } else {
    isPreviewing = true;
    const tone = toneSelect.value;
    playToneLoop(tone);
    previewToneBtn.querySelector('.play-icon').classList.add('hidden');
    previewToneBtn.querySelector('.stop-icon').classList.remove('hidden');
  }
}

function stopTonePreview() {
  isPreviewing = false;
  stopToneLoop();
  previewToneBtn.querySelector('.play-icon').classList.remove('hidden');
  previewToneBtn.querySelector('.stop-icon').classList.add('hidden');
}

// Save/Submit Form Action
alarmForm.addEventListener('submit', (e) => {
  e.preventDefault();
  
  const hour = hourSelect.value;
  const minute = minuteSelect.value;
  const ampm = ampmSelect.value;
  const label = labelInput.value.trim();
  const tone = toneSelect.value;
  
  const selectedDays = [];
  const checkboxes = alarmForm.querySelectorAll('input[name="days"]:checked');
  checkboxes.forEach(cb => {
    selectedDays.push(cb.value);
  });
  
  if (editingAlarmId !== null) {
    // Update existing
    const alarm = alarms.find(a => a.id === editingAlarmId);
    if (alarm) {
      alarm.hour = hour;
      alarm.minute = minute;
      alarm.ampm = ampm;
      alarm.label = label;
      alarm.tone = tone;
      alarm.days = selectedDays;
      alarm.active = true;
      // Reset trigger lock
      delete alarm.lastTriggeredMinute;
    }
  } else {
    // Add new
    const newAlarm = {
      id: Date.now(),
      hour,
      minute,
      ampm,
      label,
      tone,
      days: selectedDays,
      active: true
    };
    alarms.push(newAlarm);
  }
  
  saveAlarmsToStorage();
  closeModal();
  renderAlarms();
});

// Delete Alarm Button Action (from inside edit modal)
deleteAlarmFormBtn.addEventListener('click', () => {
  if (editingAlarmId !== null) {
    alarms = alarms.filter(a => a.id !== editingAlarmId);
    saveAlarmsToStorage();
    closeModal();
    renderAlarms();
  }
});

// Setup Event Listeners
addAlarmBtn.addEventListener('click', openAddModal);
modalClose.addEventListener('click', closeModal);
previewToneBtn.addEventListener('click', toggleTonePreview);
dismissBtn.addEventListener('click', dismissAlarm);
snoozeBtn.addEventListener('click', snoozeAlarm);

// Start app
populateMinutes();
initTheme();
renderAlarms();
updateClock();
setInterval(updateClock, 1000);
