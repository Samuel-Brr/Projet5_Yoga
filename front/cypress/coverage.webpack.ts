import * as path from 'path';

export default {
  module: {
    rules: [
      {
        test: /\.(js|ts)$/,
        loader: '@jsdevtools/coverage-istanbul-loader',
        options: {
          esModules: true,
          compact: false, // Helps with source maps
          produceSourceMap: true,
        },
        enforce: 'post',
        include: [
          path.join(__dirname, '..', 'src', 'app') // Focus on application code
        ],
        exclude: [
          /\.(e2e|spec|test)\.ts$/,
          /node_modules/,
          /(ngfactory|ngstyle)\.js/,
          /\.(routes|module|mock|const|config)\.ts$/,
          /index\.ts$/,
          /main\.ts$/,
          /environments/,
        ],
      },
    ],
  },
};
