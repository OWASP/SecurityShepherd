# Running a CTF

This guide is for organisers running Security Shepherd as a capture-the-flag event, for a class, a team or a conference. It assumes you have an installation running and have logged in as `admin`, as described in [Getting started](getting-started.md).

Most settings are in the **Admin** menu on the left, and [Admin menu reference](admin-menu.md) describes every page in it. Changes take effect immediately, and players may need to reload the page to see them.

## Before you start: secure the deployment

The Docker Compose setup is a quick start, with well-known default passwords and a self-signed certificate. Before other people use it, follow [Deploying into production](deploying-into-production.md): change the database password and the admin password, install your own certificate, and run it on a network that's only for the event.

## Choose the levels

All levels start closed. On the start page, **Configure Shepherd** has buttons to open all levels, only the web levels or only the mobile levels, and to close everything. To get back to the start page, reload the page.

- **Open Web App Levels Only** is the usual choice. The mobile levels need players to set up Android tooling.
- **Enable Unsafe Levels** opens levels that make the server vulnerable. Use it only on an isolated server.
- **Admin → Module Management → Open and Close Modules** and **Open or Close by Category** let you choose individual levels or whole topics.

Some levels need more than a browser and a proxy, so check them before you open them:

- The CSRF challenges are only completed when *another* logged-in player triggers the forged request, so players have to persuade someone else to visit their page. Some of them also need the player to host a page on a server they control.
- The unsafe levels expose the server, as above.

Try the levels yourself first, with cheat sheets enabled for administrators, so that you can help players who get stuck.

## Choose a layout

**Admin → Module Management → Change Module Layout** decides how levels are presented:

| Layout | How it works | Good for |
|---|---|---|
| **CTF Mode** | Players see one uncompleted level at a time, starting with the easiest. Difficulty rises slowly and moves between topics. | Guided training, where everyone follows the same path |
| **Open Floor Mode** | Every open level is available, grouped by vulnerability category, with the lessons first. | Self-paced learning, where players explore topics |
| **Tournament Mode** | Every open level is available, grouped into difficulty bands from easiest to hardest. | Competitions |

The page shows the current mode, with a button to switch to each of the others:

![The Change Module Layout page: Current Mode: Open Floor, with an Enable CTF Mode button, and the Module Management menu open on the left](assets/images/change-module-layout.png)

[How Shepherd works](how-shepherd-works.md#finding-a-level) shows what players see in each layout.

**Admin → Module Management → Module Block Setup** stops players from going past a chosen level and shows them a message of your choosing. Use it to pause an event, for example to teach a topic before the levels that need it, and remove the block when you're ready.

## Players, classes and admins

Group the players of one event into a class, so they share a scoreboard. The options are under **Admin → User Management**.

1. **Create Class** for the event.
2. **Set Default Player Class**, so that everyone who registers joins that class automatically. Use **Assign Players to Class** for accounts that already exist.
3. **Create New Admin** for each organiser, or **Upgrade Player to Admin** for an existing account.

Also under User Management:

- **Add Players** creates an account in a class you choose, if you don't want open registration.
- **Reset Password**, **Suspend Player** and **Undo Suspend Player** deal with problem accounts.
- **Add / Deduct Player Points** lets you award points by hand, for example for helping other players.

## Registration, scoreboard and feedback

These options are under **Admin → Configuration**.

- **Open/Close Registration.** Open it when you're ready for players to sign up. Players have to reload the login page to see the registration link. Consider closing registration once everyone has an account, so players can't create a second account to complete the CSRF challenges on their own.
- **Scoreboard Configuration.** Make the scoreboard public, show it to logged-in users or to admins only, or turn it off. **Enable Class Specific Scoreboard** shows players only their own class, which keeps the event's scoreboard separate from anyone else on the server.
- **Cheat Sheet Management.** Cheat sheets show how to complete each level. Enable them for administrators so you can help players. Enabling them for all users gives the answers away.
- **Feedback Configuration.** Makes players submit a feedback form before a level is marked as complete. It's useful for collecting what students understood. Read the feedback under **Admin → Module Management → View Feedback**, and follow each class's progress under **View Progress**.

![The Get Progress page for the class 2026 Autumn CTF: each player's name with a bar showing how many modules they've completed](assets/images/admin-view-progress.png)

## Scoring

![The scoreboard: players ranked by score, each with a bar showing their points and their gold, silver and bronze medal counts](assets/images/scoreboard.png)

- Each level is worth a fixed number of points, and harder levels are worth more.
- In each class, the first five players to complete a level get a bonus on top: 5 points for the first, 4 for the second, down to 1.
- The first three also win a gold, silver or bronze medal on the scoreboard.
- Wrong result keys cost nothing at first. After a player's 40th wrong submission, each further wrong key takes 10% off their score. This discourages guessing keys with automated tools.

You can add your own awards with **Add / Deduct Player Points**.

## Timing the event

Security Shepherd can open the levels at a set time, freeze the scoreboard for the last part of the event, and close the levels at the end. There's no page for this in the Admin menu: set the times in the database, as described in [Event times](configuration.md#event-times).

Back up the database before the event, and again afterwards if you want to keep the results. See [Backups and restore](backups.md).

## Rules for players

Publish rules before the event. A starting point:

- **Stay ethical.** The levels are for education only. Don't use what you learn against systems you're not authorised to test.
- **Attack the levels, not the platform.** The login, registration, result key submission, scoreboard and admin pages are out of scope. If you find a vulnerability in the platform itself, report it to the organisers instead of using it.
- **No automated scanners.** They slow the server down for everyone, and repeated wrong result keys cost points.
- **Help each other.** Explain concepts and share techniques, but don't share answers.

## Things to know

- **XSS levels.** These levels decide whether an XSS attack worked by checking the submitted input for known attack patterns, not by detecting that a script ran. Occasionally a payload that does pop an alert isn't recognised. If a player shows you one that works, help them find a variant the level accepts.
- **Reskinning.** To use your own logo, colours and name, see [Reskinning](reskinning.md).
