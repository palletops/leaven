//-------------------------------------------------------------------------------------------------
//
// I am a "runner" script for use with phantomjs and cemerick/clojurescript.test
// I handle the case where the cljsbuild setting is ':optimizations :none'
//
var path = require("path"),
  fs = require("fs"),
  args = process.argv.slice(4);
var thisName = process.argv[1].split('/').slice(-1);

var usage = [
   "",
   "Usage: nodejs " + thisName + " output-dir  output-to  [tweaks]",
   "",
   "Where:",
   "    - \"output-dir\" and \"output-to\" should match the paths you supplied ",
   "      to cljsbuild in the project.clj. (right next to \":optimizations :none\").",
   "    - [tweaks] is zero or more of either:",
   "        (1) an extra javascript file - e.g.  path/to/my-shim.js ",
   "        (2) arbitrary javascript code fragments. E.g. window.something=flag",
   "      These tweaks will be applied to the test page prior to load of test code."
   ].join("\n");

//-- Colors ---------------------------------------------------------------------------------------

function yellow(text) {
    return "\u001b[31m" + text + "\u001b[0m";
}

function red(text) {
    return "\u001b[33m" + text + "\u001b[0m";
}

function green(text) {
    return "\u001b[32m" + text + "\u001b[0m";
}


//-- Commandline  ---------------------------------------------------------------------------------

if (process.argv.length < 4)  {
    console.log(usage);
    process.exit(1);
}

// google base dir
var output_to =  process.argv[2];
if (output_to.slice(-1) != path.sep)    // we want a trailing '/'
    output_to = output_to + path.sep;
if (!fs.existsSync(output_to)) {
    console.log(red('\nError: output_to directory doesn\'t exist: '  + output_to))
    process.exit(1)
}

var googBasedir = path.join(process.cwd(), output_to, 'goog')
if (!fs.existsSync(googBasedir)) {
    console.log(red('\nError: goog directory doesn\'t exist: '  + googBasedir))
    process.exit(1)
}

// test file
var testFile = process.argv[3];    // output-to parameter. Eg  test.js
if (!fs.existsSync(testFile)) {
    console.log(red('\nError: test file doesn\'t exist: ' + testFile));
    process.exit(1)
}
var haveCljsTest = function () {
    return (typeof cemerick !== "undefined" &&
        typeof cemerick.cljs !== "undefined" &&
        typeof cemerick.cljs.test !== "undefined" &&
        typeof cemerick.cljs.test.run_all_tests === "function");
};

var failIfCljsTestUndefined = function () {
    if (!haveCljsTest()) {
        var messageLines = [
            "",
            "ERROR: cemerick.cljs.test was not required.",
            "",
            "You can resolve this issue by ensuring [cemerick.cljs.test] appears",
            "in the :require clause of your test suite namespaces.",
            "Also make sure that your build has actually included any test files.",
            "",
            "Also remember that Node.js can be only used with simple/advanced ",
            "optimizations, not with none/whitespace.",
            ""
        ];
        console.error(messageLines.join("\n"));
        process.exit(1);
    }
}

//-- Load Google Clojure ----------------------------------------------------------------------------
// global.CLOSURE_BASE_PATH = googBasedir;
// require('closure').Closure(global);
require(path.join(googBasedir, 'bootstrap', 'nodejs.js'))
//-- Handle Any Tweaks  -----------------------------------------------------------------------------

args.forEach(function (arg) {
    var file = path.join(process.cwd(), arg);
    if (fs.existsSync(file)) {
      try {
        // using eval instead of require here so that `this` is the "real"
        // top-level scope, not the module
        eval("(function () {" + fs.readFileSync(file, {encoding: "UTF-8"}) + "})()");
      } catch (e) {
        failIfCljsTestUndefined();
        console.log("Error in file: \"" + file + "\"");
        console.log(e);
      }
    } else {
      try {
        eval("(function () {" + arg + "})()");
      } catch (e) {
        console.log("Could not evaluate expression: \"" + arg + "\"");
        console.log(e);
      }
    }
});

//-- Load code into our test page  ----------------------------------------------------------------
goog.nodeGlobalRequire(testFile);

// This loop is where a lot of important work happens
// It will inject both the unittests and code-to-be-tested into the page
for(var namespace in goog.dependencies_.nameToPath)
    goog.require(namespace);    // will trigger CLOSURE_IMPORT_SCRIPT calls which injectJs into page

failIfCljsTestUndefined(); // check this before trying to call set_print_fn_BANG_

//-- Run the tests  -------------------------------------------------------------------------------
//
// All the code is now loaded into the test page. Time to test.
console.log("about to run tests")

cemerick.cljs.test.set_print_fn_BANG_(function(x) {
    // since console.log *itself* adds a newline
    var x = x.replace(/\n$/, "");
    if (x.length > 0) console.log(x);
});

var success = (function() {
    var results = cemerick.cljs.test.run_all_tests();
    cemerick.cljs.test.on_testing_complete(results, function () {
        process.exit(cemerick.cljs.test.successful_QMARK_(results) ? 0 : 1);
    });
})();
