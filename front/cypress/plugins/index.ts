/**
 * @type {Cypress.PluginConfig}
 */
 import * as registerCodeCoverageTasks from '@cypress/code-coverage/task';

 export default (on, config) => {
   registerCodeCoverageTasks(on, config);

   // Additional configuration for better source maps
   on('before:browser:launch', (browser, launchOptions) => {
     if (browser.name === 'chrome' && browser.isHeadless) {
       launchOptions.args.push('--disable-gpu');
       return launchOptions;
     }
   });

   return config
 };
