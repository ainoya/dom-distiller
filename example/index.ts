// distill target url example script

import process from "process";
import fs from "fs";
import path from "path";
import puppeteer from "puppeteer";
import turndown from "turndown";

// get the target url from the command line arguments
const targetUrl = process.argv[2];

console.log(`Target URL: ${targetUrl}`);

async function scrapeAndDistill(url: string) {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  await page.goto(url, { waitUntil: "networkidle2" });

  const turndownService = new turndown();

  // load the DOM Distiller script
  const distillerScript = fs.readFileSync(
    path.resolve(__dirname, "../out/package/js/domdistiller.js"),
    "utf8"
  );
  await page.evaluate(distillerScript);

  // run the DOM Distiller script
  const distilledContent = await page.evaluate(() => {
    // @ts-ignore
    return org.chromium.distiller.DomDistiller.apply();
  });

  // console.log(distilledContent);
  const content = distilledContent[2][1];
  const markdown = turndownService.turndown(content);

  console.log(markdown);
  await browser.close();
}

scrapeAndDistill(targetUrl);
