/**
 * UTEGear Virtual Switch Tester & Web Audio Acoustic Synthesizer
 * Feature: Interactive Sound Test & Virtual Switch Tester (YT-05)
 * Dual-Engine: Real Studio Mechanical Audio Samples + Physical Acoustic Fallback
 * Dynamic Product Form Factors & Colorways:
 * - 75% Exploded with Rotary Knob (AULA F75, VGN, etc.)
 * - 80% Tenkeyless (TKL)
 * - 100% Fullsize (AULA F108, Akko 5108)
 * - 65% Compact Mini (Razer Reclusa)
 */
window.UTEGear = window.UTEGear || {};

function hexToRgb(hex) {
    if (!hex) return '225, 29, 72';
    const clean = hex.replace('#', '');
    const num = parseInt(clean, 16);
    if (isNaN(num)) return '225, 29, 72';
    const r = (num >> 16) & 255;
    const g = (num >> 8) & 255;
    const b = num & 255;
    return `${r}, ${g}, ${b}`;
}

UTEGear.SwitchTester = {
    audioCtx: null,
    activeSwitch: null,
    profiles: [],
    isMuted: false,
    audioBuffers: {},
    currentLayout: 'LAYOUT_75',
    currentTheme: 'CUSTOM',

    AUDIO_MANIFEST: {
        'BLUE_CLICKY': {
            folder: 'blue',
            press: ['press_key1.mp3', 'press_key2.mp3', 'press_key3.mp3', 'press_key4.mp3', 'press_key5.mp3'],
            release: 'release.mp3'
        },
        'RED_LINEAR': {
            folder: 'red',
            press: ['press_key1.mp3', 'press_key2.mp3', 'press_key3.mp3', 'press_key4.mp3', 'press_key5.mp3'],
            space: 'press_space.mp3',
            enter: 'press_enter.mp3',
            release: 'release_key.mp3'
        },
        'BROWN_TACTILE': {
            folder: 'brown',
            press: ['press_key1.mp3', 'press_key2.mp3', 'press_key3.mp3', 'press_key4.mp3', 'press_key5.mp3'],
            space: 'press_space.mp3',
            enter: 'press_enter.mp3',
            release: 'release_key.mp3'
        },
        'SILVER_SPEED': {
            folder: 'silver',
            press: [
                'mx-speed-silver-1.wav',
                'mx-speed-silver-2.wav',
                'mx-speed-silver-3.wav',
                'mx-speed-silver-4.wav',
                'mx-speed-silver-5.wav',
                'mx-speed-silver-6.wav'
            ]
        },
        'SILENT_RED': {
            folder: 'silent',
            press: ['key1.wav', 'key2.wav', 'key3.wav', 'key4.wav', 'key5.wav', 'key6.wav'],
            space: 'space.wav',
            enter: 'ent.wav'
        }
    },

    initAudioContext() {
        if (!this.audioCtx) {
            const AudioContextClass = window.AudioContext || window.webkitAudioContext;
            if (AudioContextClass) {
                this.audioCtx = new AudioContextClass();
            }
        }
        if (this.audioCtx && this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }
    },

    /**
     * Preloads real studio-recorded mechanical switch audio into memory buffers
     */
    async preloadSwitchAudio(code) {
        if (!code || this.audioBuffers[code] || !this.AUDIO_MANIFEST[code]) return;
        this.initAudioContext();
        if (!this.audioCtx || typeof fetch === 'undefined') return;

        const manifest = this.AUDIO_MANIFEST[code];
        const ctx = this.audioCtx;
        const entry = { press: [], space: null, enter: null, release: null };
        this.audioBuffers[code] = entry;

        const loadBuffer = async (filename) => {
            if (!filename) return null;
            try {
                const res = await fetch(`/audio/switches/${manifest.folder}/${filename}`);
                if (!res.ok) return null;
                const arrayBuf = await res.arrayBuffer();
                if (typeof ctx.decodeAudioData === 'function') {
                    if (ctx.decodeAudioData.length === 1) {
                        return await ctx.decodeAudioData(arrayBuf);
                    }
                    return await new Promise((resolve, reject) => ctx.decodeAudioData(arrayBuf, resolve, reject));
                }
                return null;
            } catch (e) {
                return null;
            }
        };

        try {
            const pressPromises = (manifest.press || []).map(f => loadBuffer(f));
            const [pressResults, spaceBuf, enterBuf, releaseBuf] = await Promise.all([
                Promise.all(pressPromises),
                loadBuffer(manifest.space),
                loadBuffer(manifest.enter),
                loadBuffer(manifest.release)
            ]);
            entry.press = pressResults.filter(Boolean);
            entry.space = spaceBuf;
            entry.enter = enterBuf;
            entry.release = releaseBuf;
        } catch (e) {
            // Fallback will handle playback
        }
    },

    async preloadAllSwitchAudio() {
        if (!this.profiles || this.profiles.length === 0) return;
        for (const profile of this.profiles) {
            this.preloadSwitchAudio(profile.code);
        }
    },

    /**
     * Play Mechanical Keystroke Sound
     * Priority: 1. Real Recorded Mechanical Audio Samples (0ms latency)
     *           2. Physical Acoustic Synthesizer Fallback
     */
    playMechanicalSound(profile, isStabilizer = false, isRelease = false, keyCode = '') {
        if (this.isMuted) return;
        this.initAudioContext();
        if (!this.audioCtx) return;

        const ctx = this.audioCtx;
        const code = profile?.code || 'RED_LINEAR';
        const buffers = this.audioBuffers[code];

        // -------------------------------------------------------------
        // ENGINE 1: REAL RECORDED MECHANICAL AUDIO SAMPLES (Exact 100% Reality)
        // -------------------------------------------------------------
        if (buffers && (buffers.press.length > 0 || buffers.release)) {
            let bufferToPlay = null;

            if (isRelease) {
                bufferToPlay = buffers.release;
            } else if (keyCode === 'Space' || (isStabilizer && keyCode.includes('Space'))) {
                bufferToPlay = buffers.space || (buffers.press.length > 0 ? buffers.press[0] : null);
            } else if (keyCode === 'Enter' || keyCode === 'NumpadEnter') {
                bufferToPlay = buffers.enter || (buffers.press.length > 0 ? buffers.press[Math.floor(Math.random() * buffers.press.length)] : null);
            } else if (buffers.press.length > 0) {
                const idx = Math.floor(Math.random() * buffers.press.length);
                bufferToPlay = buffers.press[idx];
            }

            if (bufferToPlay) {
                try {
                    const source = ctx.createBufferSource();
                    source.buffer = bufferToPlay;

                    // Micro-tonal organic variation (+/- 2.5%) for 100% human realistic feel
                    const pitchOffset = 0.975 + Math.random() * 0.05;
                    const finalPitch = (keyCode === 'Space' && !buffers.space) ? pitchOffset * 0.82 : pitchOffset;
                    if (source.playbackRate && source.playbackRate.setValueAtTime) {
                        source.playbackRate.setValueAtTime(finalPitch, ctx.currentTime);
                    }

                    const gain = ctx.createGain();
                    const volVariation = 0.92 + Math.random() * 0.16;
                    if (gain.gain && gain.gain.setValueAtTime) {
                        gain.gain.setValueAtTime(volVariation, ctx.currentTime);
                    }

                    source.connect(gain);
                    gain.connect(ctx.destination);
                    source.start(0);
                    return;
                } catch (e) {
                    // Fall through to physical synthesizer fallback
                }
            }
        }

        // -------------------------------------------------------------
        // ENGINE 2: PHYSICAL ACOUSTIC SYNTHESIS ENGINE (Acoustic Modeling Fallback)
        // -------------------------------------------------------------
        const now = ctx.currentTime;
        const randPitch = 0.96 + Math.random() * 0.08;
        const randVol = 0.92 + Math.random() * 0.16;

        if (isRelease) {
            const releaseLen = Math.floor(ctx.sampleRate * 0.018);
            const releaseBuf = ctx.createBuffer(1, releaseLen, ctx.sampleRate);
            const out = releaseBuf.getChannelData(0);
            for (let i = 0; i < releaseLen; i++) {
                out[i] = (Math.random() * 2 - 1) * Math.exp(-i / (releaseLen * 0.22));
            }

            const releaseSrc = ctx.createBufferSource();
            releaseSrc.buffer = releaseBuf;

            const releaseFilter = ctx.createBiquadFilter();
            releaseFilter.type = 'bandpass';
            releaseFilter.frequency.setValueAtTime((isStabilizer ? 1400 : 2600) * randPitch, now);
            releaseFilter.Q.setValueAtTime(2.2, now);

            const releaseGain = ctx.createGain();
            const relVol = (code === 'SILENT_RED' ? 0.015 : 0.08) * randVol;
            releaseGain.gain.setValueAtTime(relVol, now);
            releaseGain.gain.exponentialRampToValueAtTime(0.0001, now + 0.02);

            releaseSrc.connect(releaseFilter);
            releaseFilter.connect(releaseGain);
            releaseGain.connect(ctx.destination);

            releaseSrc.start(now);
            return;
        }

        // Bottom-out plate clack
        const impactDur = isStabilizer ? 0.045 : (code === 'SILENT_RED' ? 0.018 : 0.032);
        const impactSamples = Math.floor(ctx.sampleRate * impactDur);
        const impactBuf = ctx.createBuffer(1, impactSamples, ctx.sampleRate);
        const impactData = impactBuf.getChannelData(0);
        for (let i = 0; i < impactSamples; i++) {
            const decay = Math.exp(-i / (impactSamples * 0.18));
            impactData[i] = (Math.random() * 2 - 1) * decay;
        }

        const impactSrc = ctx.createBufferSource();
        impactSrc.buffer = impactBuf;

        const plateFilter = ctx.createBiquadFilter();
        const bodyFilter = ctx.createBiquadFilter();

        if (code === 'SILENT_RED') {
            plateFilter.type = 'lowpass';
            plateFilter.frequency.setValueAtTime(750 * randPitch, now);
            plateFilter.Q.setValueAtTime(1.0, now);
            bodyFilter.type = 'lowpass';
            bodyFilter.frequency.setValueAtTime(450 * randPitch, now);
        } else if (code === 'BLUE_CLICKY') {
            plateFilter.type = 'bandpass';
            plateFilter.frequency.setValueAtTime((isStabilizer ? 1600 : 2400) * randPitch, now);
            plateFilter.Q.setValueAtTime(3.0, now);
            bodyFilter.type = 'peaking';
            bodyFilter.frequency.setValueAtTime(480 * randPitch, now);
            bodyFilter.gain?.setValueAtTime?.(4, now);
        } else if (code === 'BROWN_TACTILE') {
            plateFilter.type = 'bandpass';
            plateFilter.frequency.setValueAtTime((isStabilizer ? 1200 : 1850) * randPitch, now);
            plateFilter.Q.setValueAtTime(2.2, now);
            bodyFilter.type = 'peaking';
            bodyFilter.frequency.setValueAtTime(420 * randPitch, now);
            bodyFilter.gain?.setValueAtTime?.(5, now);
        } else if (code === 'SILVER_SPEED') {
            plateFilter.type = 'bandpass';
            plateFilter.frequency.setValueAtTime(2200 * randPitch, now);
            plateFilter.Q.setValueAtTime(2.5, now);
            bodyFilter.type = 'peaking';
            bodyFilter.frequency.setValueAtTime(550 * randPitch, now);
            bodyFilter.gain?.setValueAtTime?.(3, now);
        } else {
            plateFilter.type = 'bandpass';
            plateFilter.frequency.setValueAtTime((isStabilizer ? 950 : 1650) * randPitch, now);
            plateFilter.Q.setValueAtTime(1.8, now);
            bodyFilter.type = 'peaking';
            bodyFilter.frequency.setValueAtTime((isStabilizer ? 220 : 380) * randPitch, now);
            bodyFilter.gain?.setValueAtTime?.(7, now);
        }

        const impactGain = ctx.createGain();
        const baseImpactVol = (code === 'SILENT_RED' ? 0.08 : (isStabilizer ? 0.36 : 0.28)) * randVol;
        impactGain.gain.setValueAtTime(baseImpactVol, now);
        impactGain.gain.exponentialRampToValueAtTime(0.0001, now + impactDur);

        impactSrc.connect(plateFilter);
        plateFilter.connect(bodyFilter);
        bodyFilter.connect(impactGain);
        impactGain.connect(ctx.destination);
        impactSrc.start(now);

        if (code === 'BLUE_CLICKY') {
            const snapLen = Math.floor(ctx.sampleRate * 0.016);
            const snapBuf = ctx.createBuffer(1, snapLen, ctx.sampleRate);
            const snapData = snapBuf.getChannelData(0);
            for (let i = 0; i < snapLen; i++) {
                snapData[i] = (Math.random() * 2 - 1) * Math.exp(-i / (snapLen * 0.12));
            }

            const snapSrc = ctx.createBufferSource();
            snapSrc.buffer = snapBuf;

            const clickFilter1 = ctx.createBiquadFilter();
            clickFilter1.type = 'bandpass';
            clickFilter1.frequency.setValueAtTime(4600 * randPitch, now);
            clickFilter1.Q.setValueAtTime(7.5, now);

            const clickFilter2 = ctx.createBiquadFilter();
            clickFilter2.type = 'peaking';
            clickFilter2.frequency.setValueAtTime(5800 * randPitch, now);
            clickFilter2.gain?.setValueAtTime?.(8, now);

            const clickGain = ctx.createGain();
            clickGain.gain.setValueAtTime(0.42 * randVol, now);
            clickGain.gain.exponentialRampToValueAtTime(0.0001, now + 0.016);

            snapSrc.connect(clickFilter1);
            clickFilter1.connect(clickFilter2);
            clickFilter2.connect(clickGain);
            clickGain.connect(ctx.destination);
            snapSrc.start(now);
        }

        if (code !== 'SILENT_RED') {
            const cavityOsc = ctx.createOscillator();
            const cavityGain = ctx.createGain();
            cavityOsc.type = 'sine';

            const thockFreq = (isStabilizer ? 115 : (code === 'RED_LINEAR' ? 240 : 310)) * randPitch;
            const thockDur = isStabilizer ? 0.09 : 0.055;

            cavityOsc.frequency.setValueAtTime(thockFreq, now);
            cavityOsc.frequency.exponentialRampToValueAtTime(thockFreq * 0.6, now + thockDur);

            const thockVol = (isStabilizer ? 0.28 : (code === 'RED_LINEAR' ? 0.18 : 0.10)) * randVol;
            cavityGain.gain.setValueAtTime(thockVol, now);
            cavityGain.gain.exponentialRampToValueAtTime(0.0001, now + thockDur);

            cavityOsc.connect(cavityGain);
            cavityGain.connect(ctx.destination);
            cavityOsc.start(now);
            cavityOsc.stop(now + thockDur);
        }
    },

    build3DKeycaps(container) {
        if (!container) return;
        container.querySelectorAll('.vkey').forEach(keyBtn => {
            if (keyBtn.querySelector('.vkey-3d-body')) return;
            const body3d = document.createElement('span');
            body3d.classList.add('vkey-3d-body');
            body3d.setAttribute('aria-hidden', 'true');

            const front = document.createElement('span');
            front.classList.add('vkey-skirt', 'skirt-front');
            const back = document.createElement('span');
            back.classList.add('vkey-skirt', 'skirt-back');
            const left = document.createElement('span');
            left.classList.add('vkey-skirt', 'skirt-left');
            const right = document.createElement('span');
            right.classList.add('vkey-skirt', 'skirt-right');
            const stem = document.createElement('span');
            stem.classList.add('vkey-stem');

            body3d.append(front, back, left, right, stem);
            keyBtn.append(body3d);
        });
    },

    triggerReactiveRipple(keyButton, container = document.querySelector('[data-switch-tester]')) {
        if (!keyButton || !container) return;
        const chassis = container.querySelector('[data-keyboard-chassis]');
        if (!chassis) return;

        // Calculate relative key position for radial LED underglow burst
        if (typeof keyButton.getBoundingClientRect === 'function' && typeof chassis.getBoundingClientRect === 'function') {
            const cRect = chassis.getBoundingClientRect();
            const kRect = keyButton.getBoundingClientRect();
            if (cRect.width > 0 && cRect.height > 0) {
                const relX = Math.round(((kRect.left + kRect.width / 2 - cRect.left) / cRect.width) * 100);
                const relY = Math.round(((kRect.top + kRect.height / 2 - cRect.top) / cRect.height) * 100);
                chassis.style.setProperty('--reactive-x', `${Math.max(5, Math.min(95, relX))}%`);
                chassis.style.setProperty('--reactive-y', `${Math.max(5, Math.min(95, relY))}%`);
            }
        }
        chassis.style.setProperty('--reactive-plate-opacity', '0.96');

        keyButton.classList.add('reactive-trail');
        const prevKey = keyButton.previousElementSibling;
        const nextKey = keyButton.nextElementSibling;
        if (prevKey && prevKey.classList?.contains('vkey')) prevKey.classList.add('reactive-neighbor');
        if (nextKey && nextKey.classList?.contains('vkey')) nextKey.classList.add('reactive-neighbor');

        setTimeout(() => {
            keyButton.classList.remove('reactive-trail');
            if (prevKey && prevKey.classList?.contains('vkey')) prevKey.classList.remove('reactive-neighbor');
            if (nextKey && nextKey.classList?.contains('vkey')) nextKey.classList.remove('reactive-neighbor');
            chassis.style.setProperty('--reactive-plate-opacity', '0');
        }, 620);
    },

    triggerKey(keyButton, isRealKey = false) {
        if (!keyButton) return;
        keyButton.classList.add('active');
        const isStab = keyButton.dataset.stab === 'true';
        const keyCode = keyButton.dataset.key || '';
        this.playMechanicalSound(this.activeSwitch, isStab, false, keyCode);
        this.triggerReactiveRipple(keyButton);

        const display = document.querySelector('[data-key-hit-display]');
        if (display) {
            const keyName = keyButton.getAttribute('aria-label') || keyButton.textContent.trim();
            display.textContent = 'Phím vừa gõ: ' + keyName;
        }

        setTimeout(() => {
            keyButton.classList.remove('active');
            // Play top-out release clack
            this.playMechanicalSound(this.activeSwitch, isStab, true, keyCode);
        }, 110);
    },

    switchLayout(layoutCode, container = document.querySelector('[data-switch-tester]')) {
        if (!container) return;
        const chassis = container.querySelector('[data-keyboard-chassis]');
        if (!chassis) return;

        chassis.classList.remove('layout-75', 'layout-tkl', 'layout-fullsize', 'layout-compact_65', 'layout-96');
        const code = (layoutCode || 'LAYOUT_96').toUpperCase();
        this.currentLayout = code;

        if (code === 'LAYOUT_75') {
            chassis.classList.add('layout-75');
        } else if (code === 'FULLSIZE') {
            chassis.classList.add('layout-fullsize');
        } else if (code === 'LAYOUT_96' || code === '96%' || code === 'COMPACT_96' || code === '1800') {
            chassis.classList.add('layout-96');
        } else if (code === 'COMPACT_65') {
            chassis.classList.add('layout-compact_65');
        } else {
            chassis.classList.add('layout-tkl');
        }

        const badge = container.querySelector('[data-layout-badge]');
        if (badge && !badge.textContent) {
            badge.textContent = 'Dạng bàn phím: ' + code;
        }
    },

    applyAppearance(app, container, productName = '') {
        if (!container || !app) return;
        const chassis = container.querySelector('[data-keyboard-chassis]');
        if (!chassis) return;

        // 1. Set Layout Form Factor Class
        this.switchLayout(app.layoutType || 'LAYOUT_96', container);

        // 2. Set Dynamic Colorway Custom Properties
        if (app.caseColor) chassis.style.setProperty('--kb-case-color', app.caseColor);
        if (app.alphaColor) chassis.style.setProperty('--kb-alpha-color', app.alphaColor);
        if (app.alphaTextColor) chassis.style.setProperty('--kb-alpha-text', app.alphaTextColor);
        if (app.modColor) chassis.style.setProperty('--kb-mod-color', app.modColor);
        if (app.modTextColor) chassis.style.setProperty('--kb-mod-text', app.modTextColor);
        if (app.accentColor) chassis.style.setProperty('--kb-accent-color', app.accentColor);
        if (app.accentTextColor) chassis.style.setProperty('--kb-accent-text', app.accentTextColor);

        // 2b. Specific Keycap Types (Esc, Enter, Space, Arrow) & Brand Aesthetics
        const name = ((productName || '') + ' ' + (app.layoutDisplayName || '')).toLowerCase();
        if (name.includes('g515') || name.includes('logitech') || name.includes('g pro') || name.includes('g915') || name.includes('g512')) {
            // Logitech Gaming Aesthetics (Lilac Purple Arrow Cluster, Purple G Logo on Esc, Dual Knobs)
            chassis.classList.add('brand-logitech');
            chassis.style.setProperty('--kb-arrow-color', '#8b5cf6');
            chassis.style.setProperty('--kb-arrow-text', '#ffffff');
            chassis.style.setProperty('--kb-space-color', app.alphaColor || '#18181c');
            chassis.style.setProperty('--kb-enter-color', app.modColor || '#18181c');
            chassis.style.setProperty('--kb-esc-color', '#8b5cf6');
        } else {
            chassis.classList.remove('brand-logitech');
            const isAmberOrangeAccent = (app.accentColor && (app.accentColor.toLowerCase() === '#f59e0b' || app.accentColor.toLowerCase() === '#d97706'))
                || name.includes('f75') || name.includes('aula') || name.includes('cam') || name.includes('orange') || name.includes('reaper');

            if (isAmberOrangeAccent) {
                const accent = app.accentColor || '#f59e0b';
                const accentText = app.accentTextColor || '#18181b';
                chassis.style.setProperty('--kb-arrow-color', accent);
                chassis.style.setProperty('--kb-arrow-text', accentText);
                chassis.style.setProperty('--kb-space-color', accent);
                chassis.style.setProperty('--kb-space-text', accentText);
                chassis.style.setProperty('--kb-enter-color', accent);
                chassis.style.setProperty('--kb-enter-text', accentText);
                chassis.style.setProperty('--kb-esc-color', accent);
                chassis.style.setProperty('--kb-esc-text', accentText);
            } else {
                if (app.arrowColor) chassis.style.setProperty('--kb-arrow-color', app.arrowColor);
                if (app.spaceColor) chassis.style.setProperty('--kb-space-color', app.spaceColor);
                if (app.enterColor) chassis.style.setProperty('--kb-enter-color', app.enterColor);
                if (app.escColor) chassis.style.setProperty('--kb-esc-color', app.escColor);
            }
        }

        // 3. Update Product Model Badge
        const badge = container.querySelector('[data-layout-badge]');
        if (badge) {
            badge.textContent = app.layoutDisplayName || ('Dạng bàn phím: ' + this.currentLayout);
        }

        // 4. Primary Interactive Rotary Knob
        const knob = container.querySelector('[data-knob]');
        if (knob && !knob.dataset.bound) {
            knob.dataset.bound = 'true';
            let rotation = 0;
            knob.addEventListener('click', () => {
                rotation = (rotation + 30) % 360;
                knob.style.transform = `rotate(${rotation}deg)`;
                this.playMechanicalSound(this.activeSwitch, false, false, 'KeyR');
                const display = document.querySelector('[data-key-hit-display]');
                if (display) display.textContent = 'Núm xoay Volume: ' + rotation + '°';
            });
        }

        // 4b. Secondary Interactive Rotary Knob (for dual-knob gaming boards)
        const knob2 = container.querySelector('[data-knob-2]');
        if (knob2 && !knob2.dataset.bound) {
            knob2.dataset.bound = 'true';
            let rotation2 = 0;
            knob2.addEventListener('click', () => {
                rotation2 = (rotation2 + 30) % 360;
                knob2.style.transform = `rotate(${rotation2}deg)`;
                this.playMechanicalSound(this.activeSwitch, false, false, 'KeyR');
                const display = document.querySelector('[data-key-hit-display]');
                if (display) display.textContent = 'Núm xoay RGB: ' + rotation2 + '°';
            });
        }
    },

    updatePurchaseSwitchUI(profile) {
        if (!profile) return;
        // Sync inline purchase box pills
        document.querySelectorAll('[data-purchase-switch-pill]').forEach(btn => {
            const isActive = btn.dataset.switchCode === profile.code;
            btn.classList.toggle('active', isActive);
            btn.setAttribute('aria-checked', String(isActive));
        });

        const selectedBadge = document.querySelector('[data-selected-switch-badge]');
        if (selectedBadge && this.purchaseSwitchConfirmed) {
            selectedBadge.textContent = `Đã chọn: ${profile.name} (${profile.actuationForceG}g)`;
            selectedBadge.className = 'badge rounded-pill bg-success text-white px-2 py-1';
        }
    },

    selectSwitch(profile, container) {
        this.activeSwitch = profile;

        // Trigger preloading of this switch's audio samples
        if (profile?.code) {
            this.preloadSwitchAudio(profile.code);
        }

        // Update active pill button and colors
        if (container) {
            container.querySelectorAll('[data-switch-pill]').forEach(btn => {
                const isActive = btn.dataset.switchCode === profile.code;
                btn.classList.toggle('active', isActive);
                btn.setAttribute('aria-checked', String(isActive));
                if (isActive && profile.colorHex) {
                    btn.style.setProperty('--active-glow-color', profile.colorHex);
                    btn.style.backgroundColor = profile.colorHex;
                    btn.style.borderColor = profile.colorHex;
                    btn.style.color = '#ffffff';
                } else {
                    btn.style.backgroundColor = '';
                    btn.style.borderColor = '';
                    btn.style.color = '';
                }
            });

            // Update active glow and rgb color
            const chassis = container.querySelector('[data-keyboard-chassis]');
            if (chassis && profile.colorHex) {
                chassis.style.setProperty('--active-glow-color', profile.colorHex);
                chassis.style.setProperty('--active-rgb', hexToRgb(profile.colorHex));
            }

            // Update description
            const descEl = container.querySelector('[data-active-switch-desc]');
            if (descEl) {
                descEl.textContent = `${profile.name} • Lực nhấn: ${profile.actuationForceG}g • Điểm nhận: ${profile.actuationPointMm}mm • ${profile.soundCharacter}`;
            }
        }

        this.updatePurchaseSwitchUI(profile);

        // Sync to product variant select if matched
        const variantSelect = document.querySelector('[data-detail-variant]');
        if (variantSelect && profile.matchedVariantIds && profile.matchedVariantIds.length > 0) {
            const targetId = String(profile.matchedVariantIds[0]);
            if (variantSelect.value !== targetId) {
                const targetOption = Array.from(variantSelect.options).find(opt => opt.value === targetId);
                if (targetOption) {
                    variantSelect.value = targetId;
                    variantSelect.dispatchEvent(new Event('change', { bubbles: true }));
                }
            }
        }
    },

    initPurchaseSwitchFlow(container) {
        const purchaseBox = document.querySelector('[data-purchase-switch-box]');
        const purchasePills = document.querySelector('[data-purchase-switch-pills]');
        const selectedBadge = document.querySelector('[data-selected-switch-badge]');

        if (purchaseBox && purchasePills && this.profiles.length > 0) {
            purchaseBox.hidden = false;
            purchasePills.replaceChildren();

            for (const profile of this.profiles) {
                const pill = document.createElement('button');
                pill.type = 'button';
                pill.className = 'purchase-switch-pill';
                pill.dataset.purchaseSwitchPill = '';
                pill.dataset.switchCode = profile.code;
                pill.setAttribute('role', 'radio');
                pill.setAttribute('aria-checked', String(this.activeSwitch?.code === profile.code));
                if (this.activeSwitch?.code === profile.code) pill.classList.add('active');

                const dot = document.createElement('span');
                dot.className = 'pill-dot';
                dot.style.backgroundColor = profile.colorHex || '#e11d48';

                const text = document.createElement('span');
                text.textContent = `${profile.name} (${profile.actuationForceG}g)`;

                pill.append(dot, text);
                pill.addEventListener('click', () => {
                    this.purchaseSwitchConfirmed = true;
                    this.selectSwitch(profile, container);
                    this.playMechanicalSound(profile, false, false, 'KeyA');
                    if (selectedBadge) {
                        selectedBadge.textContent = `Đã chọn: ${profile.name} (${profile.actuationForceG}g)`;
                        selectedBadge.className = 'badge rounded-pill bg-success text-white px-2 py-1';
                    }
                    if (purchaseBox) {
                        purchaseBox.classList.remove('shake-highlight');
                    }
                });
                purchasePills.append(pill);
            }
        }

        // Intercept Buy Now, Add to Cart, and Wishlist clicks on keyboard products when switch is not yet selected
        const buyNowBtn = document.querySelector('[data-detail-buy-now]');
        const addCartBtn = document.querySelector('[data-detail-add]');
        const wishlistBtn = document.querySelector('.product-purchase-actions [data-action="toggle-wishlist"]')
            || document.querySelector('[data-action="toggle-wishlist"]');

        const bindActionInterceptor = (btn) => {
            if (!btn || btn.dataset.switchInterceptBound) return;
            btn.dataset.switchInterceptBound = 'true';
            btn.addEventListener('click', (e) => {
                if (this.purchaseSwitchConfirmed) return;
                e.preventDefault();
                e.stopImmediatePropagation();

                if (selectedBadge) {
                    selectedBadge.textContent = '⚠️ Vui lòng chọn Switch ở đây trước!';
                    selectedBadge.className = 'badge rounded-pill bg-danger text-white px-2 py-1';
                }
                if (purchaseBox) {
                    purchaseBox.classList.remove('shake-highlight');
                    void purchaseBox.offsetWidth;
                    purchaseBox.classList.add('shake-highlight');
                    if (typeof purchaseBox.scrollIntoView === 'function') {
                        purchaseBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                    }
                }
            }, true);
        };

        bindActionInterceptor(buyNowBtn);
        bindActionInterceptor(addCartBtn);
        bindActionInterceptor(wishlistBtn);
    },

    async runAutoDemo(container) {
        const demoKeys = ['KeyU', 'KeyT', 'KeyE', 'KeyG', 'KeyE', 'KeyA', 'KeyR', 'Space', 'KeyP', 'KeyR', 'KeyO', 'Enter'];
        for (const code of demoKeys) {
            const keyEl = container.querySelector(`[data-key="${code}"]`);
            if (keyEl) {
                this.triggerKey(keyEl);
            }
            await new Promise(r => setTimeout(r, 160));
        }
    },

    updateCompareTable(panel, profileA, profileB) {
        const tbody = panel.querySelector('[data-compare-table-body]');
        if (!tbody) return;

        panel.querySelector('[data-table-switch-title="A"]').textContent = profileA?.name || 'Mẫu A';
        panel.querySelector('[data-table-switch-title="B"]').textContent = profileB?.name || 'Mẫu B';

        const rows = [
            { label: 'Loại cảm giác gõ', a: profileA?.switchType, b: profileB?.switchType },
            { label: 'Lực nhấn kích hoạt', a: `${profileA?.actuationForceG} g`, b: `${profileB?.actuationForceG} g` },
            { label: 'Điểm nhận tín hiệu', a: `${profileA?.actuationPointMm} mm`, b: `${profileB?.actuationPointMm} mm` },
            { label: 'Tổng hành trình', a: `${profileA?.totalTravelMm} mm`, b: `${profileB?.totalTravelMm} mm` },
            { label: 'Mức độ âm thanh', a: profileA?.noiseLevel, b: profileB?.noiseLevel },
            { label: 'Đặc tính âm thanh', a: profileA?.soundCharacter, b: profileB?.soundCharacter },
            { label: 'Điều kiện thu âm', a: profileA?.recordingCondition, b: profileB?.recordingCondition }
        ];

        tbody.replaceChildren();
        for (const row of rows) {
            const tr = document.createElement('tr');
            const th = document.createElement('th');
            th.scope = 'row';
            th.className = 'text-start text-secondary';
            th.textContent = row.label;

            const tdA = document.createElement('td');
            tdA.textContent = row.a || '—';
            tdA.className = 'fw-medium';

            const tdB = document.createElement('td');
            tdB.textContent = row.b || '—';
            tdB.className = 'fw-medium';

            tr.append(th, tdA, tdB);
            tbody.append(tr);
        }
    },

    async init() {
        const container = document.querySelector('[data-switch-tester]');
        if (!container) return;

        const productId = container.dataset.productId;
        if (!productId) return;

        try {
            const data = await UTEGear.API.get(`/api/products/${encodeURIComponent(productId)}/switch-sounds`, { public: true });
            if (!data || !data.supported || !data.profiles || data.profiles.length === 0) {
                container.hidden = true;
                return;
            }

            container.hidden = false;
            this.profiles = data.profiles;

            const disclaimerEl = container.querySelector('[data-switch-disclaimer]');
            if (disclaimerEl && data.disclaimer) {
                disclaimerEl.textContent = '* ' + data.disclaimer;
            }

            // Build True 3D Sculpted Keycaps (5-face pyramid frustum + stem)
            this.build3DKeycaps(container);

            // Render Switch Pills (Both inline and theater mode toolbar)
            const inlinePills = container.querySelector('[data-switch-pills]');
            const theaterPills = container.querySelector('[data-theater-switch-pills]');
            const pillsContainers = [inlinePills, theaterPills].filter(Boolean);

            pillsContainers.forEach(pillsContainer => {
                pillsContainer.replaceChildren();

                for (const profile of this.profiles) {
                    const btn = document.createElement('button');
                    btn.type = 'button';
                    btn.className = 'switch-pill-btn';
                    btn.dataset.switchPill = '';
                    btn.dataset.switchCode = profile.code;
                    btn.setAttribute('role', 'radio');
                    btn.setAttribute('aria-checked', 'false');

                    const dot = document.createElement('span');
                    dot.className = 'switch-pill-color-dot';
                    dot.style.backgroundColor = profile.colorHex || '#e11d48';

                    const label = document.createElement('span');
                    label.textContent = `${profile.name} (${profile.actuationForceG}g)`;

                    btn.append(dot, label);
                    btn.addEventListener('click', () => {
                        this.selectSwitch(profile, container);
                        this.playMechanicalSound(profile, false, false, 'KeyA');
                    });
                    pillsContainer.append(btn);
                }
            });

            // Find default switch profile
            const initialCode = data.defaultSwitchCode || this.profiles[0].code;
            const initialProfile = this.profiles.find(p => p.code === initialCode) || this.profiles[0];
            this.selectSwitch(initialProfile, container);

            // Initialize Purchase Switch Selection Box & Pre-Action Modal Confirmation
            this.initPurchaseSwitchFlow(container);

            // Preload audio samples for all profiles in the background
            this.preloadAllSwitchAudio();

            // Apply Dynamic Product Form Factor & Colorway Appearance
            if (data.appearance) {
                this.applyAppearance(data.appearance, container, data.productName);
            } else {
                const fallbackAppearance = {
                    layoutType: data.layoutType || 'TKL',
                    caseColor: '#18181b',
                    alphaColor: '#1c3252',
                    alphaTextColor: '#f1f5f9',
                    modColor: '#0f172a',
                    modTextColor: '#94a3b8',
                    accentColor: '#f59e0b',
                    accentTextColor: '#000000',
                    layoutDisplayName: 'Layout: 80% TKL (87 phím)'
                };
                this.applyAppearance(fallbackAppearance, container, data.productName);
            }

            // Layout Switcher Buttons (if present)
            container.querySelectorAll('[data-layout-btn]').forEach(btn => {
                btn.addEventListener('click', () => {
                    const targetLayout = btn.dataset.layoutBtn;
                    if (targetLayout) {
                        this.switchLayout(targetLayout, container);
                    }
                });
            });

            // RGB Mode Selector & Visual Status Indicator
            const rgbSelect = container.querySelector('[data-rgb-mode]');
            const rgbBadge = container.querySelector('[data-rgb-status-badge]');
            const chassis = container.querySelector('[data-keyboard-chassis]');
            const RGB_MODE_META = {
                'per-key': {
                    label: '💡 LED: Xuyên Phím Tinh Tế (Per-Key Glow)',
                    bg: 'rgba(56, 189, 248, 0.16)',
                    color: '#38bdf8',
                    border: 'rgba(56, 189, 248, 0.5)'
                },
                'rgb-wave': {
                    label: '🌈 LED: Sóng Cầu Vồng Chuyển Động',
                    bg: 'linear-gradient(90deg, rgba(255,0,85,0.22), rgba(0,229,255,0.22))',
                    color: '#a5f3fc',
                    border: 'rgba(0, 229, 255, 0.5)'
                },
                'reactive-glow': {
                    label: '⚡ LED: Sáng Bùng Nổ Khi Gõ (Reactive)',
                    bg: 'rgba(244, 63, 94, 0.22)',
                    color: '#fda4af',
                    border: 'rgba(244, 63, 94, 0.55)'
                },
                'cyber-cyan': {
                    label: '❄️ LED: Xanh Băng Cyberpunk Nhịp Thở',
                    bg: 'rgba(0, 240, 255, 0.22)',
                    color: '#67e8f9',
                    border: 'rgba(0, 240, 255, 0.6)'
                },
                'rgb-off': {
                    label: '🌑 LED: Đã Tắt Đèn Nền',
                    bg: 'rgba(100, 116, 139, 0.2)',
                    color: '#94a3b8',
                    border: 'rgba(100, 116, 139, 0.4)'
                }
            };

            const theaterRgbSelect = container.querySelector('[data-theater-rgb-mode]');

            if ((rgbSelect || theaterRgbSelect) && chassis) {
                const pName = ((data.productName || '') + ' ' + (data.appearance?.layoutDisplayName || '')).toLowerCase();
                if (pName.includes('hello kitty') || pName.includes('không led') || pName.includes('non-led')) {
                    if (rgbSelect) rgbSelect.value = 'rgb-off';
                    if (theaterRgbSelect) theaterRgbSelect.value = 'rgb-off';
                }
                const updateRgbClass = (source) => {
                    const mode = source?.value || rgbSelect?.value || theaterRgbSelect?.value || 'per-key';
                    if (rgbSelect) rgbSelect.value = mode;
                    if (theaterRgbSelect) theaterRgbSelect.value = mode;

                    chassis.classList.remove('per-key', 'rgb-wave', 'reactive-glow', 'cyber-cyan', 'rgb-off');
                    chassis.classList.add(mode);
                    const meta = RGB_MODE_META[mode] || RGB_MODE_META['per-key'];
                    if (rgbBadge) {
                        rgbBadge.textContent = meta.label;
                        rgbBadge.style.background = meta.bg;
                        rgbBadge.style.color = meta.color;
                        rgbBadge.style.borderColor = meta.border;
                    }
                };
                updateRgbClass(rgbSelect);
                if (rgbSelect) rgbSelect.addEventListener('change', () => updateRgbClass(rgbSelect));
                if (theaterRgbSelect) theaterRgbSelect.addEventListener('change', () => updateRgbClass(theaterRgbSelect));
            }

            // 3D Orbit Mode & Auto-Reset Controller
            const toggle3dBtn = container.querySelector('[data-3d-toggle]');
            const reset3dBtn = container.querySelector('[data-3d-reset]');
            const icon3d = container.querySelector('[data-3d-icon]');
            const text3d = container.querySelector('[data-3d-text]');
            const wrapper = container.querySelector('[data-keyboard-interactive]');

            let is3dMode = false;
            let rotX = 20;
            let rotY = 0;
            let isDragging = false;
            let hasDragged = false;
            let lastDragEndTime = 0;
            let startMouseX = 0;
            let startMouseY = 0;

            const applyTransform = (smooth = false) => {
                if (!chassis) return;
                chassis.style.transition = smooth ? 'transform 0.4s cubic-bezier(0.2, 0.8, 0.2, 1)' : 'none';
                if (is3dMode) {
                    chassis.style.transform = `rotateX(${rotX}deg) rotateY(${rotY}deg)`;
                } else {
                    chassis.style.transform = 'none';
                }
            };

            const resetCameraToDefault = (smooth = true) => {
                rotX = 20;
                rotY = 0;
                applyTransform(smooth);
                if (reset3dBtn) reset3dBtn.style.display = is3dMode ? 'inline-flex' : 'none';
            };

            this.resetView = () => resetCameraToDefault(true);

            if (toggle3dBtn && wrapper) {
                toggle3dBtn.addEventListener('click', (e) => {
                    e?.stopPropagation?.();
                    is3dMode = !is3dMode;
                    toggle3dBtn.setAttribute('aria-pressed', String(is3dMode));
                    wrapper.classList.toggle('mode-3d-active', is3dMode);

                    if (is3dMode) {
                        toggle3dBtn.classList.replace('btn-outline-info', 'btn-info');
                        if (text3d) text3d.textContent = 'Chế độ: 3D Xoay 360°';
                        if (icon3d) icon3d.className = 'bi bi-arrow-repeat me-1';
                        if (reset3dBtn) reset3dBtn.style.display = 'inline-flex';
                        // Showcase 3D isometric angle immediately so case walls & depth are visible
                        rotX = 28;
                        rotY = -16;
                        applyTransform(true);
                    } else {
                        toggle3dBtn.classList.replace('btn-info', 'btn-outline-info');
                        if (text3d) text3d.textContent = 'Chế độ: 2.5D Chuẩn';
                        if (icon3d) icon3d.className = 'bi bi-box me-1';
                        if (reset3dBtn) reset3dBtn.style.display = 'none';
                        applyTransform(true);
                    }
                });

                if (reset3dBtn) {
                    reset3dBtn.addEventListener('click', (e) => {
                        e?.stopPropagation?.();
                        resetCameraToDefault(true);
                    });
                }

                // Drag to rotate in 3D Mode (strictly ignore clicks on buttons/status bar/keys)
                wrapper.addEventListener('pointerdown', (e) => {
                    if (!is3dMode) return;
                    if (e.target.closest('.vkey') || e.target.closest('button') || e.target.closest('a') || e.target.closest('.virtual-keyboard-status') || e.target.closest('.theater-header-bar')) {
                        return;
                    }
                    isDragging = true;
                    hasDragged = false;
                    startMouseX = e.clientX;
                    startMouseY = e.clientY;
                    wrapper.setPointerCapture?.(e.pointerId);
                });

                wrapper.addEventListener('pointermove', (e) => {
                    if (!isDragging || !is3dMode) return;
                    const deltaX = e.clientX - startMouseX;
                    const deltaY = e.clientY - startMouseY;
                    if (Math.abs(deltaX) > 1 || Math.abs(deltaY) > 1) {
                        hasDragged = true;
                        lastDragEndTime = Date.now();
                    }
                    startMouseX = e.clientX;
                    startMouseY = e.clientY;

                    rotY += deltaX * 0.5;
                    rotX -= deltaY * 0.5;
                    rotX = Math.max(-180, Math.min(180, rotX)); // Full 360-degree pitch & yaw inspection including bottom plate
                    applyTransform(false);
                });

                const stopDrag = (e) => {
                    if (isDragging) {
                        if (hasDragged) {
                            lastDragEndTime = Date.now();
                        }
                        if (e && wrapper.releasePointerCapture) {
                            try { wrapper.releasePointerCapture(e.pointerId); } catch (_) {}
                        }
                    }
                    isDragging = false;
                };
                wrapper.addEventListener('pointerup', stopDrag);
                wrapper.addEventListener('pointercancel', stopDrag);
            }

            // Fullscreen Theater Mode Controller
            const fullscreenBtn = container.querySelector('[data-tester-fullscreen]');
            const inlineTheaterBtn = container.querySelector('[data-theater-trigger-inline]');
            const theaterCloseBtn = container.querySelector('[data-theater-close]');
            const theaterBackdrop = container.querySelector('[data-theater-backdrop]');
            const fsIcon = container.querySelector('[data-fullscreen-icon]');
            const fsText = container.querySelector('[data-fullscreen-text]');

            let isTheaterMode = false;

            const setTheaterMode = (active) => {
                isTheaterMode = active;
                if (fullscreenBtn) {
                    fullscreenBtn.setAttribute('aria-pressed', String(active));
                    if (active) {
                        fullscreenBtn.classList.replace('btn-outline-dark', 'btn-dark');
                        if (fsIcon) fsIcon.className = 'bi bi-fullscreen-exit me-1';
                        if (fsText) fsText.textContent = 'Thoát toàn màn hình';
                    } else {
                        fullscreenBtn.classList.replace('btn-dark', 'btn-outline-dark');
                        if (fsIcon) fsIcon.className = 'bi bi-arrows-fullscreen me-1';
                        if (fsText) fsText.textContent = 'Toàn màn hình';
                    }
                }

                if (wrapper) {
                    wrapper.classList.toggle('theater-mode', active);
                }
                if (theaterBackdrop) {
                    theaterBackdrop.classList.toggle('d-none', !active);
                    theaterBackdrop.classList.toggle('active', active);
                }
                if (typeof document !== 'undefined' && document.body) {
                    document.body.classList.toggle('switch-tester-theater-open', active);
                }
            };

            this.toggleTheaterMode = () => setTheaterMode(!isTheaterMode);
            this.closeTheaterMode = () => setTheaterMode(false);
            this.isTheaterActive = () => isTheaterMode;

            if (fullscreenBtn) {
                fullscreenBtn.addEventListener('click', (e) => {
                    e?.stopPropagation?.();
                    setTheaterMode(!isTheaterMode);
                });
            }
            if (inlineTheaterBtn) {
                inlineTheaterBtn.addEventListener('click', (e) => {
                    e?.stopPropagation?.();
                    setTheaterMode(true);
                });
            }
            if (theaterCloseBtn) {
                theaterCloseBtn.addEventListener('click', (e) => {
                    e?.stopPropagation?.();
                    setTheaterMode(false);
                });
            }
            if (theaterBackdrop) {
                theaterBackdrop.addEventListener('click', () => {
                    if (hasDragged || (Date.now() - lastDragEndTime < 400)) {
                        hasDragged = false;
                        return;
                    }
                    setTheaterMode(false);
                });
            }
            if (wrapper) {
                wrapper.addEventListener('click', (e) => {
                    // Never exit theater mode if user just finished dragging/rotating in 3D!
                    if (hasDragged || (Date.now() - lastDragEndTime < 400)) {
                        hasDragged = false;
                        return;
                    }
                    // Only exit if user intentionally single-clicked the empty backdrop outside the keyboard
                    if (isTheaterMode && e.target === wrapper) {
                        setTheaterMode(false);
                    }
                });
            }

            // Mute Buttons (Both inline and theater toolbar)
            const muteBtn = container.querySelector('[data-tester-mute]');
            const muteIcon = container.querySelector('[data-tester-mute-icon]');
            const muteText = container.querySelector('[data-tester-mute-text]');
            const theaterMuteBtn = container.querySelector('[data-theater-mute]');
            const theaterMuteIcon = container.querySelector('[data-theater-mute-icon]');
            const theaterMuteText = container.querySelector('[data-theater-mute-text]');

            const updateMuteUI = () => {
                const label = this.isMuted ? 'Âm thanh: Tắt' : 'Âm thanh: Bật';
                const iconClass = this.isMuted ? 'bi bi-volume-mute-fill me-1' : 'bi bi-volume-up-fill me-1';
                if (muteBtn) {
                    muteBtn.setAttribute('aria-pressed', String(this.isMuted));
                    muteBtn.classList.toggle('btn-outline-danger', this.isMuted);
                    muteBtn.classList.toggle('btn-outline-secondary', !this.isMuted);
                }
                if (theaterMuteBtn) {
                    theaterMuteBtn.setAttribute('aria-pressed', String(this.isMuted));
                    theaterMuteBtn.classList.toggle('btn-danger', this.isMuted);
                    theaterMuteBtn.classList.toggle('btn-outline-light', !this.isMuted);
                }
                if (muteIcon) muteIcon.className = iconClass;
                if (theaterMuteIcon) theaterMuteIcon.className = iconClass;
                if (muteText) muteText.textContent = label;
                if (theaterMuteText) theaterMuteText.textContent = label;
            };

            const toggleMute = () => {
                this.isMuted = !this.isMuted;
                updateMuteUI();
            };

            if (muteBtn) muteBtn.addEventListener('click', toggleMute);
            if (theaterMuteBtn) theaterMuteBtn.addEventListener('click', toggleMute);

            // Auto Demo Buttons (Both inline and theater toolbar)
            const autoDemoBtn = container.querySelector('[data-tester-auto-demo]');
            const theaterAutoDemoBtn = container.querySelector('[data-theater-auto-demo]');
            const handleAutoDemo = () => {
                [autoDemoBtn, theaterAutoDemoBtn].forEach(b => { if (b) b.disabled = true; });
                this.runAutoDemo(container).finally(() => {
                    [autoDemoBtn, theaterAutoDemoBtn].forEach(b => { if (b) b.disabled = false; });
                });
            };
            if (autoDemoBtn) autoDemoBtn.addEventListener('click', handleAutoDemo);
            if (theaterAutoDemoBtn) theaterAutoDemoBtn.addEventListener('click', handleAutoDemo);

            // (Compare Button handled below in comparison panel setup)

            // Virtual Keys Mouse / Touch Events
            container.querySelectorAll('.vkey').forEach(keyBtn => {
                keyBtn.addEventListener('pointerdown', (e) => {
                    e.preventDefault();
                    if (is3dMode && (Math.abs(rotY) > 8 || rotX < 10 || rotX > 30)) {
                        resetCameraToDefault(true);
                    }
                    this.triggerKey(keyBtn);
                });
            });

            // Physical Keyboard Listeners (Real-time Press down & Release clack + Auto-Reset)
            window.addEventListener('keydown', (e) => {
                if (['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)) return;
                
                // Escape key exits Fullscreen Theater Mode
                if ((e.key === 'Escape' || e.code === 'Escape') && isTheaterMode) {
                    setTheaterMode(false);
                    return;
                }

                if (is3dMode && (Math.abs(rotY) > 8 || rotX < 10 || rotX > 30)) {
                    resetCameraToDefault(true);
                }
                const keyEl = container.querySelector(`[data-key="${e.code}"]`);
                if (keyEl && !keyEl.classList.contains('active')) {
                    keyEl.classList.add('active');
                    const isStab = keyEl.dataset.stab === 'true';
                    this.playMechanicalSound(this.activeSwitch, isStab, false, e.code);
                    this.triggerReactiveRipple(keyEl, container);

                    const display = document.querySelector('[data-key-hit-display]');
                    if (display) {
                        const keyName = keyEl.getAttribute('aria-label') || keyEl.textContent.trim();
                        display.textContent = 'Phím vừa gõ: ' + keyName;
                    }
                }
            });

            window.addEventListener('keyup', (e) => {
                if (['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)) return;
                const keyEl = container.querySelector(`[data-key="${e.code}"]`);
                if (keyEl) {
                    keyEl.classList.remove('active');
                    const isStab = keyEl.dataset.stab === 'true';
                    this.playMechanicalSound(this.activeSwitch, isStab, true, e.code);
                }
            });

            // Sync from Product Detail Variant Select
            const variantSelect = document.querySelector('[data-detail-variant]');
            if (variantSelect) {
                variantSelect.addEventListener('change', () => {
                    const selectedValId = Number(variantSelect.value);
                    const matchedProfile = this.profiles.find(p => p.matchedVariantIds?.includes(selectedValId));
                    if (matchedProfile && this.activeSwitch?.code !== matchedProfile.code) {
                        this.selectSwitch(matchedProfile, container);
                    }
                });
            }

            // Setup A/B Comparison Panel
            const compareToggleBtn = container.querySelector('[data-tester-toggle-compare]');
            const comparePanel = container.querySelector('[data-compare-panel]');
            const closeCompareBtn = container.querySelector('[data-close-compare]');
            const selectA = container.querySelector('[data-compare-select="A"]');
            const selectB = container.querySelector('[data-compare-select="B"]');

            if (compareToggleBtn && comparePanel && selectA && selectB) {
                selectA.replaceChildren();
                selectB.replaceChildren();

                this.profiles.forEach((p, idx) => {
                    const optA = new Option(p.name, p.code, false, idx === 0);
                    const optB = new Option(p.name, p.code, false, idx === Math.min(1, this.profiles.length - 1));
                    selectA.add(optA);
                    selectB.add(optB);
                });

                if (this.profiles.length > 0) {
                    selectA.value = this.profiles[0].code;
                    selectB.value = this.profiles[Math.min(1, this.profiles.length - 1)].code;
                }

                const refreshCompare = () => {
                    const pA = this.profiles.find(p => p.code === selectA.value) || this.profiles[0];
                    const pB = this.profiles.find(p => p.code === selectB.value) || this.profiles[1] || this.profiles[0];
                    this.updateCompareTable(comparePanel, pA, pB);
                };

                selectA.addEventListener('change', refreshCompare);
                selectB.addEventListener('change', refreshCompare);

                container.querySelector('[data-play-compare="A"]')?.addEventListener('click', async () => {
                    const pA = this.profiles.find(p => p.code === selectA.value);
                    if (!pA) return;
                    for (let i = 0; i < 3; i++) {
                        this.playMechanicalSound(pA, i === 2, false, i === 2 ? 'Space' : 'KeyA');
                        await new Promise(r => setTimeout(r, 160));
                    }
                });

                container.querySelector('[data-play-compare="B"]')?.addEventListener('click', async () => {
                    const pB = this.profiles.find(p => p.code === selectB.value);
                    if (!pB) return;
                    for (let i = 0; i < 3; i++) {
                        this.playMechanicalSound(pB, i === 2, false, i === 2 ? 'Space' : 'KeyA');
                        await new Promise(r => setTimeout(r, 160));
                    }
                });

                const theaterCompareBtn = container.querySelector('[data-theater-toggle-compare]');
                const toggleCompareAction = () => {
                    const willShow = comparePanel.hidden;
                    comparePanel.hidden = !willShow;
                    compareToggleBtn.setAttribute('aria-pressed', String(willShow));
                    if (theaterCompareBtn) theaterCompareBtn.setAttribute('aria-pressed', String(willShow));
                    if (willShow) {
                        refreshCompare();
                    }
                };

                compareToggleBtn.addEventListener('click', toggleCompareAction);
                if (theaterCompareBtn) theaterCompareBtn.addEventListener('click', toggleCompareAction);

                closeCompareBtn?.addEventListener('click', () => {
                    comparePanel.hidden = true;
                    compareToggleBtn.setAttribute('aria-pressed', 'false');
                    if (theaterCompareBtn) theaterCompareBtn.setAttribute('aria-pressed', 'false');
                });
            }

        } catch (error) {
            console.warn('Switch tester initialization skipped:', error.message);
            container.hidden = true;
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    UTEGear.SwitchTester.init();
});
