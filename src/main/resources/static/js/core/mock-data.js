(function (scope) {
  'use strict';
  const app = scope.UTEGear = scope.UTEGear || {};
  let payload;
  app.MockData = {
    async load(source = '/js/mock/api-01.json') {
      // Static contract fixtures only. This is not the production API client.
      const url = new URL(source, scope.location.href);
      if (url.origin !== scope.location.origin || !url.pathname.endsWith('/api-01.json')) {
        throw new TypeError('Expected a same-origin API-01 fixture');
      }
      const response = await fetch(url);
      if (!response.ok) throw new Error('Không tải được dữ liệu mẫu');
      const value = await response.json();
      for (const key of ['profile', 'categories', 'brands', 'productsPage', 'productsEmpty', 'validationError', 'unauthorized', 'forbidden']) {
        if (!Object.hasOwn(value, key)) throw new TypeError('Missing API-01 fixture: ' + key);
      }
      payload = value;
      return structuredClone(payload);
    },
    get(key) {
      if (!payload) throw new Error('Load mock data before reading fixtures');
      if (!Object.hasOwn(payload, key)) throw new RangeError('Unknown API-01 fixture');
      return structuredClone(payload[key]);
    }
  };
})(globalThis);
