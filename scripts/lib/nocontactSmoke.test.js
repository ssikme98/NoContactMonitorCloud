const test = require('node:test');
const assert = require('node:assert/strict');

const { buildRouteBootstrapScript } = require('./nocontactSmoke');

test('route bootstrap script refreshes dynamic routes and redirects target path', () => {
  const script = buildRouteBootstrapScript('/survey/enterprise');

  assert.match(script, /dispatch\('GetInfo'\)/);
  assert.match(script, /dispatch\('GenerateRoutes'\)/);
  assert.match(script, /\$router\.replace\(targetPath\)/);
  assert.match(script, /sidebarRouters/);
  assert.match(script, /ready: root\.\$route\.path === targetPath/);
});
