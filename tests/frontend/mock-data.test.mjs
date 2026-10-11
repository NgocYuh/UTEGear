import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import vm from 'node:vm';

const script = fs.readFileSync(new URL('../../src/main/resources/static/js/core/mock-data.js', import.meta.url), 'utf8');
const contract = fs.readFileSync(new URL('../../docs/api/mock/api-01.json', import.meta.url), 'utf8');
const served = fs.readFileSync(new URL('../../src/main/resources/static/js/mock/api-01.json', import.meta.url), 'utf8');
const fixtures = JSON.parse(contract);
const createClient = fetch => {
  const context = vm.createContext({ URL, structuredClone, location: new URL('https://utegear.test/preview'), fetch });
  vm.runInContext(script, context);
  return context.UTEGear.MockData;
};

test('served mock matches the merged contract exactly', () => assert.equal(served, contract));
test('loaded fixtures are independent copies and unknown keys fail', async () => {
  const client = createClient(async () => ({ok: true, json: async () => structuredClone(fixtures)}));
  const loaded = await client.load();
  loaded.profile.fullName = 'Changed';
  const read = client.get('profile');
  read.fullName = 'Also changed';
  assert.equal(client.get('profile').fullName, fixtures.profile.fullName);
  assert.throws(() => client.get('not-in-contract'), /Unknown API-01 fixture/);
});
test('unloaded, cross-origin and non-fixture reads fail without calling an API', async () => {
  let calls = 0;
  const client = createClient(async () => { calls++; });
  assert.throws(() => client.get('profile'), /Load mock data/);
  await assert.rejects(client.load('https://other.test/js/mock/api-01.json'), /same-origin/);
  await assert.rejects(client.load('/api/products'), /same-origin/);
  assert.equal(calls, 0);
});
test('failed or incomplete fixture responses are reported', async () => {
  await assert.rejects(createClient(async () => ({ok: false})).load(), /Không tải được/);
  await assert.rejects(createClient(async () => ({ok: true, json: async () => ({})})).load(), /Missing API-01 fixture/);
});
