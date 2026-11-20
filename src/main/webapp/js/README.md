# Vendored JavaScript libraries

Third-party libraries in this directory are copied from their official release.
Don't edit them: replace the whole file with a new release and update this
table.

| File | Library | Version | Source | Integrity of the file |
|---|---|---|---|---|
| `clipboard-js/clipboard.min.js` | [clipboard.js](https://clipboardjs.com) (MIT) | 1.4.1 | `dist/clipboard.min.js` at the [`v1.4.1`](https://github.com/zenorocha/clipboard.js/tree/v1.4.1) tag of `zenorocha/clipboard.js`. That release was never published to npm, so there is no tarball to compare with | `sha256-7q4lOSXWHtemy/P35ZGUFrC5+olto3mVgKuMoFtZAu4=` |
| `clipboard-js/clipboard.js` | clipboard.js | 1.4.1 | `dist/clipboard.js` at the same tag. The only difference is line endings: the tag has 49 CRs, this file has none. Nothing loads this file | `sha256-eUp1Uc3EY8UzAo1EKyrZzOwT6FdCjQerAVrwCO9S4uo=` |

`clipboard-js/clipboard-events.js`, `clipboard-js/tooltips.js` and
`clipboard-js/clippy.svg` are Security Shepherd's own glue code, adapted from
the clipboard.js demo page. They aren't vendored libraries.

clipboard.js 1.4.1 has no known vulnerabilities in the [OSV](https://osv.dev)
database, but it dates from 2015. The current release is 2.0.11. The level pages
use only `new Clipboard(selector)`, the `success` and `error` events,
`clearSelection()` and the `data-clipboard-target` attribute, which 2.x keeps.

To check a file, compare its hash with the table, for example
`openssl dgst -sha256 -binary clipboard.min.js | base64`. To check where the
file came from, download `dist/clipboard.min.js` from the tag above and compare
the two.

jQuery, jQuery UI and the other libraries in this directory are being recorded
and upgraded under #957.
