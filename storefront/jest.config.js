const nextJest = require('next/jest')

const createJestConfig = nextJest({ dir: './' })

/** @type {import('jest').Config} */
const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testEnvironment: 'jest-environment-jsdom',
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/$1',
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
    '!utils/useDebounce.ts',
    'common/services/ApiClientService.ts',
    'modules/catalog/services/ToastService.ts',
    'modules/catalog/services/ProductService.ts',
    'modules/order/services/OrderService.ts',
    'modules/country/services/CountryService.ts',
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
}

module.exports = createJestConfig(customJestConfig)
