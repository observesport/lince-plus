---
layout: ../layouts/Prose.astro
title: LINCE PLUS Desktop Privacy Policy
description: Privacy policy for the LINCE PLUS desktop application for Windows, macOS and Linux. Projects and videos stay on your computer; the app contacts the network for update checks, the optional my.lince-plus.com account, sharing you start yourself, and the analytics tag of its browser dashboard.
updated: '2026-11-06'
---

This privacy policy applies to the **LINCE PLUS** desktop application for Windows, macOS and Linux ("the App"), including the version distributed through the Microsoft Store, developed by Alberto Soto Fernández / ObserveSport ("we", "us"). It explains what data the App accesses, what leaves your computer, and how to control it.

If you have questions, contact **alberto.soto@gmail.com**.

---

## TL;DR

- Your projects, videos, observation data and exports are stored **on your computer**, in the files and folders you choose.
- The App works without an account. If you choose to sign in to **my.lince-plus.com** to unlock optional features, we store your account details as described below. Your projects and videos still stay on your computer.
- Apart from that optional account, the App contacts the network for three things only: a periodic **update check** against our GitHub repository, **sharing features you start yourself**, and a **Google Analytics tag** in the browser dashboard that you can block.

---

## What the App stores locally

- **Project files**: LINCE PLUS projects are XML files (`.xml`) saved where you choose. The App can also open the legacy Lince 1 formats (`.ilince`, `.rlince`) and export to CSV, Excel, SAS, SDIS-GSEQ, Theme and HOISAN formats, again to the location you pick. An autosave copy is written every two minutes while a project is open.
- **Video files**: the App plays the videos you select in place and never uploads them. If you use the built-in video conversion tool, the App converts the selected video to MP4 with the bundled FFmpeg and writes the result to the folder you choose on your computer. Nothing else is done with the video.
- **Settings and logs**: kept in the App's folder in your user profile. Logs contain file paths and technical messages, nothing is sent anywhere.

The App does not access your contacts, calendar, location, camera or microphone.

---

## Optional account (my.lince-plus.com)

Signing in is optional and only needed for features that require a LINCE PLUS account, such as premium or experimental features enabled per user.

When you sign in, the App sends your credentials to my.lince-plus.com over HTTPS and keeps a session token on your computer so you do not have to sign in every time. While signed in, the App periodically asks the server which features your account enables. These requests carry your session token and the App version, nothing else: no project content, file names, videos or observation data.

On the server we store:

- your email address and a password hash, or the identifier from the sign-in provider you used
- the plan and feature flags assigned to your account
- the date of account creation and of your last sign-in

We use this data only to operate your account and the features it enables. We do not sell it or use it for advertising. You can sign out in the App at any time, which deletes the session token from your computer. To delete your account and its data, email **alberto.soto@gmail.com** and we will remove it within 30 days.

---

## What leaves your computer

| Connection | When | What is sent | Who receives it |
|---|---|---|---|
| **Update check** | at start and every 20 minutes | a plain request for `lince-version.json` and `lince-information.json`, no identifiers | GitHub (raw.githubusercontent.com), under [GitHub's privacy statement](https://docs.github.com/site-policy/privacy-policies/github-general-privacy-statement) |
| **Account sign-in (optional)** | only when you sign in and while signed in | credentials at sign-in, then your session token and App version | my.lince-plus.com, operated by us |
| **Local dashboard** | when you open the browser dashboard | the App runs a web server on your computer that serves the dashboard to your own browser, and to other devices **on your local network** if you share the QR code | only devices you share the link with |
| **Remote sharing (optional)** | only when you enter your own ngrok token and start a tunnel | the dashboard traffic is relayed through ngrok | ngrok, under [ngrok's privacy policy](https://ngrok.com/privacy-policy). Not used unless you configure it |
| **Google Analytics** | when the browser dashboard loads with an internet connection | standard GA4 page view data (anonymised IP, browser and screen details, pages visited) | Google, under [Google's privacy policy](https://policies.google.com/privacy). No project data, file names or video content is included |

Like any program that accesses a web address, each connection reveals your IP address to the server. We do not store it.

---

## What we do NOT do

- We do **not** upload your projects, videos, observation data or exports.
- We do **not** create profiles or device identifiers beyond the optional account described above.
- We do **not** display advertisements.
- We do **not** sell, rent or share data with third parties.
- We do **not** use crash reporting or telemetry beyond the analytics tag described above.

---

## Your control

- **Update check**: works offline; without a connection the App simply skips it.
- **Account**: sign out in the App to remove the session token; email us to delete the account.
- **Local network sharing**: nothing is shared unless you open the dashboard link on another device. Close the App to stop the local server.
- **Remote sharing**: never active unless you configure an ngrok token. Remove the token to disable it.
- **Google Analytics**: use a content blocker in your browser, or work offline. The App itself keeps working.
- **Delete your local data**: delete your project files, converted videos and uninstall the App. We hold no copy of them and cannot recover or delete anything on your behalf because we never receive them.

---

## Third-party components

The App bundles a Java runtime, JavaFX, FFmpeg for video playback and conversion, and the Renjin R engine for statistics. All of them run locally and do not transmit data. The full list of dependencies is in the [source repository](https://github.com/observesport/lince-plus).

---

## Children's privacy

The App is a research tool, is not directed at children under 13 and does not knowingly collect any data from children.

---

## Changes to this policy

If a future version changes how the App handles data, we will update this page and its "Last updated" date, and note the change in the release notes.

---

## Contact

Alberto Soto Fernández
Email: **alberto.soto@gmail.com**
Project: [https://observesport.github.io/lince-plus/](https://observesport.github.io/lince-plus/)
Source: [https://github.com/observesport/lince-plus](https://github.com/observesport/lince-plus)
