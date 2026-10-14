// Isolated H2 test launcher. Admin password exists only in child-process environment.
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { randomUUID } from 'node:crypto';
import { spawn, spawnSync } from 'node:child_process';
const output = path.resolve(process.argv[2] || 'target/fe-live');
const cache = process.env.QA_MAVEN_CACHE || path.join(os.homedir(), '.m2', 'repository');
const jwtLifetime = Number(process.env.QA_JWT_EXPIRATION_MS || 3600000);
if (!Number.isSafeInteger(jwtLifetime) || jwtLifetime < 1000) throw Error('Invalid QA JWT lifetime');
const realMedia = process.env.QA_MEDIA_REAL === '1';
const cloudKeys = ['CLOUDINARY_CLOUD_NAME', 'CLOUDINARY_API_KEY', 'CLOUDINARY_API_SECRET'];
if (realMedia && (process.env.QA_CLOUDINARY_CONFIRMED !== '1' || cloudKeys.some(key => !process.env[key]))) {
    throw Error('Real media checks require confirmed Cloudinary QA environment variables');
}
const serverEnvironment = { ...process.env };
if (realMedia) serverEnvironment.SPRING_APPLICATION_JSON = JSON.stringify({
    'cloudinary.cloud-name': process.env.CLOUDINARY_CLOUD_NAME,
    'cloudinary.api-key': process.env.CLOUDINARY_API_KEY,
    'cloudinary.api-secret': process.env.CLOUDINARY_API_SECRET,
    'cloudinary.folder': 'utegear-qa/' + randomUUID()
});
fs.mkdirSync(output, { recursive: true });
const password = randomUUID() + 'Ab1!', adminEmail = 'qa-admin@example.test';
const helper = path.join(output, 'QaHash.java');
fs.writeFileSync(helper, 'import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; class QaHash { public static void main(String[] args) { System.out.print(new BCryptPasswordEncoder().encode(System.getenv("QA_ADMIN_PASSWORD"))); } }');
const cp = [path.join(cache, 'org/springframework/security/spring-security-crypto/6.3.10/spring-security-crypto-6.3.10.jar'), path.join(cache, 'org/springframework/spring-jcl/6.1.21/spring-jcl-6.1.21.jar')].join(path.delimiter);
const encoded = spawnSync('java', ['--class-path', cp, helper], { encoding: 'utf8', env: { ...process.env, QA_ADMIN_PASSWORD: password } });
if (encoded.status !== 0 || !encoded.stdout.trim().startsWith('$2')) throw Error('Cannot encode ephemeral QA password; verify QA_MAVEN_CACHE and JDK');
const fullChecklist = process.env.QA_RUN_CHECKLIST === '1';
let sql = fs.readFileSync(fullChecklist ? 'tests/frontend/fixtures/qa05-test.sql' : 'tests/frontend/fixtures/admin-media-test.sql', 'utf8');
sql += `\nINSERT INTO roles(id,name) VALUES(101,'ADMIN');\nINSERT INTO users(id,full_name,email,password_hash,enabled,created_at,updated_at) VALUES(900,'QA Admin','${adminEmail}','${encoded.stdout.trim()}',true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);\nINSERT INTO user_roles(user_id,role_id) VALUES(900,101);\n`;
const seed = path.join(output, 'seed.sql'); fs.writeFileSync(seed, sql);
const log = fs.openSync(path.join(output, 'server.log'), 'w');
const cli = `--server.port=18081 --server.address=127.0.0.1 --spring.datasource.url=jdbc:h2:mem:qa_media;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.flyway.enabled=false --spring.config.additional-location=file:src/test/resources/application-test.properties --spring.jpa.hibernate.ddl-auto=create-drop --spring.jpa.defer-datasource-initialization=true --spring.sql.init.mode=always --spring.sql.init.data-locations=file:${seed.replaceAll('\\','/')} --app.websocket.allowed-origins=http://127.0.0.1:18081 --jwt.expiration=${jwtLifetime}`;
const command = `& .\\mvnw.cmd -B spring-boot:run '-Dspring-boot.run.useTestClasspath=true' '-Dspring-boot.run.profiles=test' '-Dspring-boot.run.arguments=${cli.replaceAll("'", "''")}'`;
let server;
try {
    let inUse = false;
    try { await fetch('http://127.0.0.1:18081/', { signal: AbortSignal.timeout(1000) }); inUse = true; } catch {}
    if (inUse) throw Error('Test port 18081 already in use; no existing process was stopped');
    server = spawn('powershell.exe', ['-NoProfile', '-Command', command], { stdio: ['ignore', log, log], env: serverEnvironment });
    let ready = false;
    for (let i = 0; i < 120; i++) {
        if (server.exitCode != null) throw Error('Test server startup failed; inspect local server.log');
        try { const response = await fetch('http://127.0.0.1:18081/api/products/100', { signal: AbortSignal.timeout(1000) }); if (response.ok) { ready = true; break; } } catch {}
        await new Promise(resolve => setTimeout(resolve, 500));
    }
    if (!ready) throw Error('Test server not ready within timeout');
    console.log('Isolated H2 server ready; running real browser checks.');
    const suiteEnvironment = { ...process.env, QA_ADMIN_EMAIL: adminEmail, QA_ADMIN_PASSWORD: password };
    let checklistExit = 0;
    if (fullChecklist) {
        const checklist = spawn(process.execPath, ['tests/frontend/qa05-browser.mjs', 'http://127.0.0.1:18081', 'http://127.0.0.1:9238', path.join(output, 'checklist')], { stdio: 'inherit', env: suiteEnvironment });
        checklistExit = await new Promise(resolve => checklist.on('exit', code => resolve(code ?? 1)));
    }
    const suite = spawn(process.execPath, ['tests/frontend/admin-media-browser.mjs', 'http://127.0.0.1:18081', 'http://127.0.0.1:9238', path.join(output, 'browser')], { stdio: 'inherit', env: suiteEnvironment });
    const mediaExit = await new Promise(resolve => suite.on('exit', code => resolve(code ?? 1)));
    process.exitCode = checklistExit === 1 || mediaExit === 1 ? 1 : mediaExit;
    if (fullChecklist) {
        const checklistReport = JSON.parse(fs.readFileSync(path.join(output, 'checklist/report.json'), 'utf8'));
        const mediaReport = JSON.parse(fs.readFileSync(path.join(output, 'browser/report.json'), 'utf8'));
        const otherIncomplete = checklistReport.steps.some(step => step.step !== 8 && step.status !== 'PASS');
        const status = checklistExit === 1 || mediaExit === 1 ? 'FAIL'
            : otherIncomplete || !realMedia || mediaReport.status !== 'PASS' ? 'BLOCKED' : 'PASS';
        fs.writeFileSync(path.join(output, 'qa05-summary.json'), JSON.stringify({ status,
            checklist: checklistReport, media: mediaReport,
            step8VerifiedByRealMediaSuite: realMedia && mediaReport.status === 'PASS',
            businessReviewRequired: true }, null, 2));
        process.exitCode = status === 'FAIL' ? 1 : status === 'BLOCKED' ? 2 : 0;
    }
} catch (error) { console.error(error.message); process.exitCode = 1; }
finally {
    if (server?.pid && server.exitCode == null) spawnSync('taskkill', ['/PID', String(server.pid), '/T', '/F'], { stdio: 'ignore' });
    fs.closeSync(log);
    // Remove only the temporary credential hash/source files created by this launcher.
    for (const file of [seed, helper]) if (fs.existsSync(file)) fs.unlinkSync(file);
}
