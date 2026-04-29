const nextJest = require('next/jest');

const createJestConfig = nextJest({ dir: './' });

/** @type {import('jest').Config} */
const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testEnvironment: 'jest-environment-jsdom',
  moduleNameMapper: {
    '^@commonServices/(.*)$': '<rootDir>/common/services/$1',
  },
  reporters: [
    'default',
    [
      'jest-junit',
      {
        outputDirectory: 'test-results',
        outputName: 'junit.xml',
      },
    ],
  ],
  collectCoverageFrom: [
    'utils/*.ts',
    'common/services/ApiClientService.ts',
    'common/services/ResponseStatusHandlingService.ts',
    'common/services/ToastService.ts',
    'modules/catalog/services/BrandService.ts',
    'modules/location/services/CountryService.ts',
    'modules/tax/services/TaxClassService.ts',
  ],
  coverageReporters: ['lcov', 'text', 'json-summary'],
  coverageDirectory: 'coverage',
  coverageThreshold: {
    global: {
      lines: 70,
      functions: 70,
      branches: 70,
      statements: 70,
    },
  },
};

module.exports = createJestConfig(customJestConfig);
