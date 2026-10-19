import fs from 'node:fs';
import path from 'node:path';

const BASE_URL = 'https://raw.githubusercontent.com/NSTechBytes/MonkeySounds/master/Sounds/Keyboard/Default';
const TARGET_DIR = path.resolve('src/main/resources/static/audio/switches');

const DOWNLOAD_CONFIG = {
    'blue': {
        folder: 'mx-blue',
        files: [
            'press_key1.mp3',
            'press_key2.mp3',
            'press_key3.mp3',
            'press_key4.mp3',
            'press_key5.mp3',
            'release.mp3'
        ]
    },
    'red': {
        folder: 'gateron-red-ink',
        files: [
            'press_key1.mp3',
            'press_key2.mp3',
            'press_key3.mp3',
            'press_key4.mp3',
            'press_key5.mp3',
            'press_space.mp3',
            'press_enter.mp3',
            'release_key.mp3'
        ]
    },
    'brown': {
        folder: 'mx-brown',
        files: [
            'press_key1.mp3',
            'press_key2.mp3',
            'press_key3.mp3',
            'press_key4.mp3',
            'press_key5.mp3',
            'press_space.mp3',
            'press_enter.mp3',
            'release_key.mp3'
        ]
    },
    'silver': {
        folder: 'mx-speed-silver',
        files: [
            'mx-speed-silver-1.wav',
            'mx-speed-silver-2.wav',
            'mx-speed-silver-3.wav',
            'mx-speed-silver-4.wav',
            'mx-speed-silver-5.wav',
            'mx-speed-silver-6.wav'
        ]
    },
    'silent': {
        folder: 'telios-v2',
        files: [
            'key1.wav',
            'key2.wav',
            'key3.wav',
            'key4.wav',
            'key5.wav',
            'key6.wav',
            'space.wav',
            'ent.wav'
        ]
    }
};

async function downloadFile(url, targetPath) {
    const res = await fetch(url);
    if (!res.ok) {
        throw new Error(`Failed to download ${url}: ${res.statusText}`);
    }
    const arrayBuffer = await res.arrayBuffer();
    fs.mkdirSync(path.dirname(targetPath), { recursive: true });
    fs.writeFileSync(targetPath, Buffer.from(arrayBuffer));
    console.log(`Saved: ${path.basename(targetPath)} (${arrayBuffer.byteLength} bytes)`);
}

async function main() {
    for (const [switchKey, config] of Object.entries(DOWNLOAD_CONFIG)) {
        console.log(`\n=== Downloading real audio for ${switchKey} (${config.folder}) ===`);
        for (const file of config.files) {
            const url = `${BASE_URL}/${config.folder}/${file}`;
            const targetPath = path.join(TARGET_DIR, switchKey, file);
            try {
                await downloadFile(url, targetPath);
            } catch (err) {
                console.warn(`Could not download ${file}:`, err.message);
            }
        }
    }
    console.log('\nAll mechanical switch audio samples downloaded!');
}

main();
