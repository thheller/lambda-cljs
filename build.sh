#!/bin/bash

set -e

rm -rf dist
mkdir dist
npx shadow-cljs release lambda --config-merge '{:output-to "dist/index.js"}'
cp package.json package-lock.json dist
cd dist
npm install --omit=dev
rm package-lock.json
zip -r lambda.zip .
