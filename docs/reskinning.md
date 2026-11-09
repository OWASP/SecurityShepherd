# Reskinning

This page is for organisers who want Security Shepherd in their own branding: their logo, colours and name. Reskinning means changing files in the source and building your own WAR and image, so you need the setup from [Getting started](getting-started.md).

Changes made inside a running container are lost when the container is recreated, so always change the source.

## What to change

All of these are in `src/main/webapp/`:

| File | What it controls |
| --- | --- |
| `css/theCss.css` | The platform's pages: login, registration, the start page and menu, the scoreboard and setup. The `#header` rules set the banner, and `body` the page background. |
| `css/lessonCss/theCss.css` | The level pages, shown inside the start page. It's similar to `theCss.css` without the header and footer rules. Change its colours to match. |
| `css/theResponsiveCss.css` | Layout changes for narrow screens |
| `css/images/shepherd_and_sheep_v2.png` | The banner image at the top right of every platform page |
| `css/images/grass_tile_v2.jpeg` | The strip along the top of the page, behind the banner |
| `css/images/flavicon.jpg` | The browser tab icon |
| `css/images/lostSheep.jpg`, `serverError.jpg`, `loggedOutSheep.jpg` | The not-found, error and logged-out pages |
| `index.jsp`, `login.jsp`, `register.jsp`, `setup.jsp` | The `<h1>Security Shepherd</h1>` heading in the header, and each page's `<title>` |

Keep images at the same size as the originals for the best result, or adjust the `#header` rules in `theCss.css` to fit yours. If your banner already contains your name, remove or reposition the `<h1>` in the four pages, so the text doesn't sit on top of it.

The login page, the start page and **About Security Shepherd** also show a "Project Sponsors" section with four organisations' logos, from `sponsorshipMessage` in `src/main/java/utils/Analytics.java`. None of them sponsors the project today, and the section is being reassessed in [#1087](https://github.com/OWASP/SecurityShepherd/issues/1087). Until then, you may want to remove it from your version.

## Build and run your version

Rebuild the WAR and the Tomcat image, and recreate the container:

```bash
mvn -Pdocker clean package -DskipTests
docker compose up -d --build web
```

Reload the pages with your browser's cache cleared, because browsers keep stylesheets and images. The data volumes aren't touched, so players, classes and results are kept.

For your own Tomcat, build with `mvn clean package -DskipTests` and deploy the WAR, as in [Manual setup](manual-setup.md).

## Keeping your changes

Keep your reskin on a branch of your own fork, and merge each new release of Security Shepherd into it. Stylesheet and image changes rarely conflict with upstream changes. Changes to the JSPs can, so keep those to a minimum.
