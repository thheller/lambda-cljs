# AWS Lamba via CLJS

Answering [this post](https://clojureverse.org/t/lambda-function-in-clojurescript-how-where/9791/1) by showing an AWS Lambda example.

Clone repo and run
```
sh build.sh
```

This creates a `dist/lambda.zip`.

Deploy `dist/lambda.zip` to AWS. I'm sure you can do this via the command line somehow. I just created the AWS Lambda in their web interface and uploaded it manually for testing. I know next to nothing about AWS, just want to demonstrate the CLJS bits.

For this example I had to bump the default runtime timeout. 3sec didn't seem enough. The `@mozilla/readability` part seems to take about 5sec to run. 10sec timeout should be plenty. Don't know why it is so slow, it isn't in the CLJS bits.

Let's go over what `build.sh` does. You can use anything to perform these steps, but this seemed easiest to explain.

```
rm -rf dist
mkdir dist
```
Create an empty `dist` directory just so we don't copy files we don't need.

```
npx shadow-cljs release lambda --config-merge '{:output-to "dist/index.js"}'
```
Tell shadow-cljs to make a release build for the `:lambda` build. Using config-merge here, so it outputs the file directly to the `dist` folder. Could do this via the build config directly too.

```
cp package.json package-lock.json dist
cd dist
npm install --omit=dev
```

Ensure that all dependencies we may need are installed in the `dist` directory itself. the `--omit=dev` will not install `devDependencies`. Meaning that `shadow-cljs` itself won't be in the `dist` dir since it is not required. Makes the zip significantly smaller.

```
rm package-lock.json
zip -r lambda.zip .
```

Finally, create the zip file by zipping up the entire `dist` folder. You may also delete any additional files you do not need. So, just deleting the lock file as an example. It won't hurt anything if you keep it.

The build result here is a `2.5mb` zip file, which is mostly due to `node_modules` files. The CLJS output itself is 22kb zipped, 98kb unzipped.

Deployment via s3 buckets is also an option, but I'll leave you to figure out AWS specifics on your own.

## Development

For local development you can run `npx shadow-cljs watch lamba` or `npx shadow-cljs server` and using the http://localhost:9630/builds/lambda UI to start/stop the `watch`.

Executing `node test.js` runs the local output. Of course that is going to get limited quick. I don't know what options AWS provides for actually running lambda locally. You'll probably want to adjust the `event` parameter in `test.js` at some point. You won't get a `null` in a real lambda after all.

If the `server` is running the `build.sh` will also be significantly faster, since it doesn't need to start a new CLJS instance.

If you want a REPL use `npx shadow-cljs node-repl`, as the `:lambda` build has it disabled. Didn't want to have the node process linger after calling the handler via `test.js`.