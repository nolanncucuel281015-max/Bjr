// Export du manuel HTML en PDF A4 avec le Chromium préinstallé (Playwright).
const { chromium } = require('playwright');
const path = require('node:path');

(async () => {
  const [, , input, output] = process.argv;
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('file://' + path.resolve(input), { waitUntil: 'networkidle' });
  await page.pdf({
    path: output, format: 'A4', printBackground: true, preferCSSPageSize: true,
    displayHeaderFooter: true,
    headerTemplate: '<div></div>',
    footerTemplate: '<div style="width:100%;font-size:7pt;color:#8a7a5a;font-family:Inter,sans-serif;padding:0 15mm;display:flex;justify-content:space-between"><span>Les Dimensions Oubliées — Manuel du joueur</span><span class="pageNumber"></span></div>',
  });
  await browser.close();
  console.log('PDF écrit :', output);
})();
