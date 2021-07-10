# Accentor android app

Android app for Accentor, a modern music server focusing on metadata.

## Why use Accentor?

Accentor gives you complete control over your music. You can build
your own collection (with good old CD's, bandcamp downloads, ...) in
the sound quality that you want and stream it either through the [web
frontend](https://github.com/accentor/web) or this android app.

Accentor is focused on metadata. We allow you to add detailed metadata
to your music collection, beyond what the tags inside an audio file
are capable of. Album and tracks can have multiple artists with a
different name on different albums/tracks, albums can have multiple
labels and tracks can have multiple genres.

The metadata is completely in your control: you can edit it however
you want.

## Installing

The [GitHub releases](https://github.com/accentor/android/releases)
include an APK. The app is not available on any app stores (yet).

## Local development

This repository uses a typical layout for an android app. Android
Studio should be able to work with it out-of-the-box. If not, you can
use `gradle` to compile and/or install a debug version with `./gradlew
assembleDebug` and `./gradlew installDebug` respectively. Make sure
you have setup `adb` before trying this.

## Help

Have a question? You can ask it through [GitHub
discussions](https://github.com/accentor/android/discussions) or in the
[Matrix channel](https://matrix.to/#/!PCYHOaWItkVRNacTSv:vanpetegem.me?via=vanpetegem.me&via=matrix.org).

Think you have noticed a bug or thought of a great feature we can add?
[Create an issue](https://github.com/accentor/android/issues/new/choose).
