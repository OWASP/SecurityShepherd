# Setting up your tools

This page is for players. Security Shepherd's web levels run in your browser, but most of them are much easier with an intercepting proxy, and some can't be done without one.

## An intercepting proxy

An intercepting proxy sits between your browser and Security Shepherd. It shows you every request your browser sends and every response it gets, and lets you change a request before it's sent. Two free ones are:

- [ZAP](https://www.zaproxy.org/) (Zed Attack Proxy)
- [Burp Suite Community Edition](https://portswigger.net/burp/communitydownload)

Both can open a browser that's already set up to use them: **Quick Start → Manual Explore** in ZAP, and **Proxy → Intercept → Open browser** in Burp. That's the easiest way to start, because the proxy's own certificate is already trusted there.

If you'd rather use your own browser, set its proxy to the one ZAP or Burp is listening on (`127.0.0.1:8080` by default), and install the proxy's CA certificate in the browser, as the proxy's documentation describes.

## If Security Shepherd runs on your own machine

- **The certificate warning.** A local installation uses a self-signed certificate, so your browser warns about it the first time. Accept the warning for `localhost` only.
- **Proxying `localhost`.** Browsers send requests to `localhost` directly, without the proxy. The browsers ZAP and Burp open are set up to proxy it. In your own Firefox, set `network.proxy.allow_hijacking_localhost` to `true` in `about:config`. Or use your machine's network address in place of `localhost`.

## Your browser's developer tools

The browser's developer tools (F12 in most browsers) are useful alongside the proxy. You can read a page's HTML and JavaScript, look at its cookies, and run JavaScript in the console.

## What the levels need

- **Most web levels:** a browser and a proxy.
- **The CSRF challenges:** another player. A CSRF challenge is only completed when another logged-in player triggers the request you forged. Some of them also need a page you host somewhere the other player's browser can reach.
- **The mobile levels:** an Android emulator and the levels' apps. See [Mobile levels](mobile-levels.md).

## Don't use automated scanners

Don't point vulnerability scanners or brute-force tools at Security Shepherd. They slow the server down for everyone, they attack the platform as well as the levels, and repeated wrong result keys cost you points.
