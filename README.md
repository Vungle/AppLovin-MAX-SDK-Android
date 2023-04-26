<h2>Setting up the Project:</h2>

1. Clone repo:
   git@github.com:Vungle/Vungle-TestApp-AppLovin-MAX-SDK-Android.git

2. Add run permissions to these scripts:
   `chmod +x scripts/cleanForTesting.sh` This script adds the necessary gradle files to build and compile the adapter

`chmod +x scripts/cleanForPublish.sh` This script removes gradle files added for local dev

3. Run `git submodule update --init --recursive` This will automatically set it to master

4. Run `scripts/cleanForTesting.sh`

5. Check whether `app`、`AppLovin-MAX-SDK-Android` on the correct branch

6. Open `app` project on AndroidStudio

7. Checkout develop for `AppLovin-MAX-SDK-Android` and `Vungle-TestApp-AppLovin-MAX-SDK-Android`

<h2>Keeping Develop and Master Branch Up to Date:</h2>

1.`master` branch is to track upstream : `https://github.com/AppLovin/AppLovin-MAX-SDK-Android` and should not have any code merged into it.

2. Add `https://github.com/AppLovin/AppLovin-MAX-SDK-Android` and pull regularly upstream master into origin master to keep it up to date.
3. Then pull master into develop

<h2>How to build adapter library</h2>

1. cd to `Vungle-TestApp-AppLovin-MAX-SDK-Android/AppLovin-MAX-SDK-Android/Vungle`
2. run `gradle build`
3. cd to `AppLovin-MAX-SDK-Android/Vungle/build/outputs/aar`
4. Inside there should be a Vungle-debug.aar and Vungle-release.aar

<h2>How to build test app for QA</h2>

1. Go to bitrise: https://app.bitrise.io/app/7f6bf8f16d761e1e
2. Run the workflow `AppCenter-QA-Release`
   Releases can be found: https://appcenter.ms/orgs/Linda-Pham-Organization/apps/Android-Max-Mediation-Test-App/

<h2>Adapter release check list</h2>

1. After passing QA we'll need to run `scripts/cleanForPublish.sh` and update the PR to revert the gradle files in the Vungle directory.
2. Commit and push changes to branch.
3. Pull in latest changes from upstream master
4. Submit an upstream PR to: https://github.com/AppLovin/AppLovin-MAX-SDK-Android master branch

<h4>Adapter</h4>

When sending out a PR upstream to: https://github.com/AppLovin/AppLovin-MAX-SDK-Android
be sure first to run /scripts/cleanForPublish.sh before PR is sent to delete files used for develop

When developing run cleanForTesting.sh
When sending out a PR be sure first to run /scripts/cleanForPublish.sh

