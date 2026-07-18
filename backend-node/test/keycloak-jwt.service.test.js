const assert = require('node:assert/strict');
const { generateKeyPairSync, sign } = require('node:crypto');
const { createServer } = require('node:http');
const { after, before, test } = require('node:test');

const ISSUER = 'http://issuer.test/realms/uav';
const AUDIENCE = 'uav-web';
const KID = 'test-signing-key';

const { privateKey, publicKey } = generateKeyPairSync('rsa', {
  modulusLength: 2048,
});
const jwk = publicKey.export({ format: 'jwk' });
Object.assign(jwk, { kid: KID, alg: 'RS256', use: 'sig' });

let server;
let service;

before(async () => {
  server = createServer((request, response) => {
    response.writeHead(200, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify({ keys: [jwk] }));
  });
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const address = server.address();

  process.env.AUTH_JWT_JWK_SET_URI = `http://127.0.0.1:${address.port}/jwks`;
  process.env.AUTH_JWT_ISSUER_URI = ISSUER;
  process.env.AUTH_JWT_AUDIENCE = AUDIENCE;
  const { KeycloakJwtService } = require(
    '../dist/auth/keycloak-jwt.service.js'
  );
  service = new KeycloakJwtService();
});

after(async () => {
  await new Promise((resolve, reject) => {
    server.close((error) => error ? reject(error) : resolve());
  });
});

test('accepts a valid operator token', async () => {
  const user = await service.verifyAccessToken(createToken({
    realm_access: { roles: ['OPERATOR'] },
  }));

  assert.equal(user.subject, 'user-001');
  assert.equal(user.username, 'pilot-one');
  assert.deepEqual(user.roles, ['OPERATOR']);
});

test('rejects a valid token without a business role as forbidden', async () => {
  await assert.rejects(
    service.verifyAccessToken(createToken({
      realm_access: { roles: ['offline_access'] },
    })),
    (error) => error.status === 403 && error.code === 'FORBIDDEN',
  );
});

test('rejects a token issued for another audience', async () => {
  await assert.rejects(
    service.verifyAccessToken(createToken({
      aud: 'another-client',
      realm_access: { roles: ['ADMIN'] },
    })),
    (error) => error.status === 401 && error.code === 'UNAUTHORIZED',
  );
});

function createToken(overrides = {}) {
  const now = Math.floor(Date.now() / 1000);
  const header = encode({ alg: 'RS256', typ: 'JWT', kid: KID });
  const payload = encode({
    sub: 'user-001',
    preferred_username: 'pilot-one',
    iss: ISSUER,
    aud: AUDIENCE,
    iat: now,
    nbf: now - 1,
    exp: now + 300,
    ...overrides,
  });
  const unsignedToken = `${header}.${payload}`;
  const signature = sign(
    'RSA-SHA256',
    Buffer.from(unsignedToken),
    privateKey,
  ).toString('base64url');
  return `${unsignedToken}.${signature}`;
}

function encode(value) {
  return Buffer.from(JSON.stringify(value)).toString('base64url');
}
