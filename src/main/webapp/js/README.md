# Vendored JavaScript libraries

Third-party libraries in this directory are copied unmodified from their
official release. Don't edit them: replace the whole file with a new release
and update this table. The version is part of the file name, so a browser never
keeps using a cached copy of an older release.

| File | Library | Version | Source | Integrity of the file |
|---|---|---|---|---|
| `jquery-3.7.1.min.js` | [jQuery](https://jquery.com) | 3.7.1 | `dist/jquery.min.js` from the npm tarball `jquery@3.7.1` (tarball `sha512-m4avr8yL8kmFN8psrbFFFmB/If14iN5o9nw/NgnnM+kybDJpRsAynV2BsfpTYrTRysYUdADVD7CkUUizgkpLfg==`); identical to https://code.jquery.com/jquery-3.7.1.min.js | `sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=` |
| `jquery-ui-1.14.2.min.js` | [jQuery UI](https://jqueryui.com) | 1.14.2 | `dist/jquery-ui.min.js` from the npm tarball `jquery-ui@1.14.2` (tarball `sha512-1gSl7PUjyipa2adSr780Ujk16faicrV7PjPPzPtvWk7tTqBnsqp67NNV9jZK2+BIxUPXWSnIUU/LBCgwgGZE+Q==`); identical to https://code.jquery.com/ui/1.14.2/jquery-ui.min.js | `sha256-mblSWfbYzaq/f+4akyMhE6XELCou4jbkgPv+JQPER2M=` |
| `clipboard-js/clipboard.min.js` | [clipboard.js](https://clipboardjs.com) (MIT) | 1.4.1 | `dist/clipboard.min.js` at the [`v1.4.1`](https://github.com/zenorocha/clipboard.js/tree/v1.4.1) tag of `zenorocha/clipboard.js`. That release was never published to npm, so there is no tarball to compare with | `sha256-7q4lOSXWHtemy/P35ZGUFrC5+olto3mVgKuMoFtZAu4=` |
| `clipboard-js/clipboard.js` | clipboard.js | 1.4.1 | `dist/clipboard.js` at the same tag. The only difference is line endings: the tag has 49 CRs, this file has none. Nothing loads this file | `sha256-eUp1Uc3EY8UzAo1EKyrZzOwT6FdCjQerAVrwCO9S4uo=` |

To check a file, compare its hash with the table, for example
`openssl dgst -sha256 -binary jquery-3.7.1.min.js | base64`, and compare the
npm tarball with `npm view jquery@3.7.1 dist.integrity`. For clipboard.js,
download `dist/clipboard.min.js` from the tag above and compare the two.

jQuery UI is used only by `index.jsp` (module search autocomplete and the
slide effect on alerts). It needs no theme stylesheet: the one rule it relies
on, `.ui-helper-hidden-accessible`, is in `css/theCss.css`.

`clipboard-js/clipboard-events.js`, `clipboard-js/tooltips.js` and
`clipboard-js/clippy.svg` are Security Shepherd's own glue code, adapted from
the clipboard.js demo page. They aren't vendored libraries.

clipboard.js 1.4.1 has no known vulnerabilities in the [OSV](https://osv.dev)
database, but it dates from 2015. The current release is 2.0.11. The level pages
use only `new Clipboard(selector)`, the `success` and `error` events,
`clearSelection()` and the `data-clipboard-target` attribute, which 2.x keeps.

## Older copies still being replaced (#957)

| File | Library | Version | Still loaded by |
|---|---|---|---|
| `jquery.js` | jQuery | 1.7.1 | challenges, until they move to `jquery-3.7.1.min.js` |
| `tinysort.js` | TinySort | 1.3.25 | `scoreboard.jsp` |
